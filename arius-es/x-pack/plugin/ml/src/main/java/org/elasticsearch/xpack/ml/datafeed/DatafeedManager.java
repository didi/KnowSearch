/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.datafeed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.AbstractRunnable;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.persistent.PersistentTasksCustomMetaData;
import org.elasticsearch.persistent.PersistentTasksCustomMetaData.PersistentTask;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.threadpool.Scheduler;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.xpack.core.ml.MlTasks;
import org.elasticsearch.xpack.core.ml.action.CloseJobAction;
import org.elasticsearch.xpack.core.ml.action.StartDatafeedAction;
import org.elasticsearch.xpack.core.ml.datafeed.DatafeedState;
import org.elasticsearch.xpack.core.ml.job.config.JobState;
import org.elasticsearch.xpack.core.ml.job.config.JobTaskState;
import org.elasticsearch.xpack.core.ml.job.messages.Messages;
import org.elasticsearch.xpack.core.ml.utils.ExceptionsHelper;
import org.elasticsearch.xpack.ml.MachineLearning;
import org.elasticsearch.xpack.ml.action.TransportStartDatafeedAction;
import org.elasticsearch.xpack.ml.job.process.autodetect.AutodetectProcessManager;
import org.elasticsearch.xpack.ml.notifications.AnomalyDetectionAuditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.elasticsearch.persistent.PersistentTasksService.WaitForPersistentTaskListener;
import static org.elasticsearch.xpack.core.ClientHelper.ML_ORIGIN;
import static org.elasticsearch.xpack.core.ClientHelper.executeAsyncWithOrigin;

public class DatafeedManager {

    private static final Logger logger = LogManager.getLogger(DatafeedManager.class);

    private final Client client;
    private final ClusterService clusterService;
    private final ThreadPool threadPool;
    private final Supplier<Long> currentTimeSupplier;
    private final AnomalyDetectionAuditor auditor;
    // Use allocationId as key instead of datafeed id
    private final ConcurrentMap<Long, Holder> runningDatafeedsOnThisNode = new ConcurrentHashMap<>();
    private final DatafeedJobBuilder datafeedJobBuilder;
    private final TaskRunner taskRunner = new TaskRunner();
    private final AutodetectProcessManager autodetectProcessManager;

    public DatafeedManager(ThreadPool threadPool, Client client, ClusterService clusterService, DatafeedJobBuilder datafeedJobBuilder,
                           Supplier<Long> currentTimeSupplier, AnomalyDetectionAuditor auditor,
                           AutodetectProcessManager autodetectProcessManager) {
        this.client = Objects.requireNonNull(client);
        this.clusterService = Objects.requireNonNull(clusterService);
        this.threadPool = threadPool;
        this.currentTimeSupplier = Objects.requireNonNull(currentTimeSupplier);
        this.auditor = Objects.requireNonNull(auditor);
        this.datafeedJobBuilder = Objects.requireNonNull(datafeedJobBuilder);
        this.autodetectProcessManager = autodetectProcessManager;
        clusterService.addListener(taskRunner);
    }

    public void run(TransportStartDatafeedAction.DatafeedTask task, Consumer<Exception> finishHandler) {
        String datafeedId = task.getDatafeedId();

        ActionListener<DatafeedJob> datafeedJobHandler = ActionListener.wrap(
                datafeedJob -> {
                    Holder holder = new Holder(task, datafeedId, datafeedJob,
                            new ProblemTracker(auditor, datafeedJob.getJobId()), finishHandler);
                    runningDatafeedsOnThisNode.put(task.getAllocationId(), holder);
                    task.updatePersistentTaskState(DatafeedState.STARTED, new ActionListener<PersistentTask<?>>() {
                        @Override
                        public void onResponse(PersistentTask<?> persistentTask) {
                            taskRunner.runWhenJobIsOpened(task);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (ExceptionsHelper.unwrapCause(e) instanceof ResourceNotFoundException) {
                                // The task was stopped in the meantime, no need to do anything
                                logger.info("[{}] Aborting as datafeed has been stopped", datafeedId);
                            } else {
                                finishHandler.accept(e);
                            }
                        }
                    });
                }, finishHandler::accept
        );

        datafeedJobBuilder.build(datafeedId, datafeedJobHandler);
    }

    public void stopDatafeed(TransportStartDatafeedAction.DatafeedTask task, String reason, TimeValue timeout) {
        logger.info("[{}] attempt to stop datafeed [{}] [{}]", reason, task.getDatafeedId(), task.getAllocationId());
        Holder holder = runningDatafeedsOnThisNode.remove(task.getAllocationId());
        if (holder != null) {
            holder.stop(reason, timeout, null);
        }
    }

    /**
     * This is used when the license expires.
     */
    public void stopAllDatafeedsOnThisNode(String reason) {
        int numDatafeeds = runningDatafeedsOnThisNode.size();
        if (numDatafeeds != 0) {
            logger.info("Closing [{}] datafeeds, because [{}]", numDatafeeds, reason);

            for (Holder holder : runningDatafeedsOnThisNode.values()) {
                holder.stop(reason, TimeValue.timeValueSeconds(20), null);
            }
        }
    }

    /**
     * This is used before the JVM is killed.  It differs from stopAllDatafeedsOnThisNode in that it leaves
     * the datafeed tasks in the "started" state, so that they get restarted on a different node.
     */
    public void isolateAllDatafeedsOnThisNodeBeforeShutdown() {
        Iterator<Holder> iter = runningDatafeedsOnThisNode.values().iterator();
        while (iter.hasNext()) {
            Holder next = iter.next();
            next.isolateDatafeed();
            // TODO: it's not ideal that this "isolate" method does something a bit different to the one below
            next.setNodeIsShuttingDown();
            iter.remove();
        }
    }

    public void isolateDatafeed(long allocationId) {
        // This calls get() rather than remove() because we expect that the persistent task will
        // be removed shortly afterwards and that operation needs to be able to find the holder
        Holder holder = runningDatafeedsOnThisNode.get(allocationId);
        if (holder != null) {
            holder.isolateDatafeed();
        }
    }

    // Important: Holder must be created and assigned to DatafeedTask before setting state to started,
    // otherwise if a stop datafeed call is made immediately after the start datafeed call we could cancel
    // the DatafeedTask without stopping datafeed, which causes the datafeed to keep on running.
    private void innerRun(Holder holder, long startTime, Long endTime) {
        holder.cancellable =
            Scheduler.wrapAsCancellable(threadPool.executor(MachineLearning.DATAFEED_THREAD_POOL_NAME).submit(new AbstractRunnable() {

            @Override
            public void onFailure(Exception e) {
                logger.error("Failed lookback import for job [" + holder.datafeedJob.getJobId() + "]", e);
                holder.stop("general_lookback_failure", TimeValue.timeValueSeconds(20), e);
            }

            @Override
            protected void doRun() {
                Long next = null;
                try {
                    next = holder.executeLookBack(startTime, endTime);
                } catch (DatafeedJob.ExtractionProblemException e) {
                    if (endTime == null) {
                        next = e.nextDelayInMsSinceEpoch;
                    }
                    holder.problemTracker.reportExtractionProblem(e.getCause().getMessage());
                } catch (DatafeedJob.AnalysisProblemException e) {
                    if (endTime == null) {
                        next = e.nextDelayInMsSinceEpoch;
                    }
                    holder.problemTracker.reportAnalysisProblem(e.getCause().getMessage());
                    if (e.shouldStop) {
                        holder.stop("lookback_analysis_error", TimeValue.timeValueSeconds(20), e);
                        return;
                    }
                } catch (DatafeedJob.EmptyDataCountException e) {
                    if (endTime == null) {
                        holder.problemTracker.reportEmptyDataCount();
                        next = e.nextDelayInMsSinceEpoch;
                    } else {
                        // Notify that a lookback-only run found no data
                        String lookbackNoDataMsg = Messages.getMessage(Messages.JOB_AUDIT_DATAFEED_LOOKBACK_NO_DATA);
                        logger.warn("[{}] {}", holder.datafeedJob.getJobId(), lookbackNoDataMsg);
                        auditor.warning(holder.datafeedJob.getJobId(), lookbackNoDataMsg);
                    }
                } catch (Exception e) {
                    logger.error("Failed lookback import for job [" + holder.datafeedJob.getJobId() + "]", e);
                    holder.stop("general_lookback_failure", TimeValue.timeValueSeconds(20), e);
                    return;
                }
                if (holder.isIsolated() == false) {
                    if (next != null) {
                        doDatafeedRealtime(next, holder.datafeedJob.getJobId(), holder);
                    } else {
                        holder.stop("no_realtime", TimeValue.timeValueSeconds(20), null);
                        holder.problemTracker.finishReport();
                    }
                }
            }
        }));
    }

    void doDatafeedRealtime(long delayInMsSinceEpoch, String jobId, Holder holder) {
        if (holder.isRunning() && !holder.isIsolated()) {
            TimeValue delay = computeNextDelay(delayInMsSinceEpoch);
            logger.debug("Waiting [{}] before executing next realtime import for job [{}]", delay, jobId);
            holder.cancellable = threadPool.schedule(new AbstractRunnable() {

                @Override
                public void onFailure(Exception e) {
                    logger.error("Unexpected datafeed failure for job [" + jobId + "] stopping...", e);
                    holder.stop("general_realtime_error", TimeValue.timeValueSeconds(20), e);
                }

                @Override
                protected void doRun() {
                    long nextDelayInMsSinceEpoch;
                    try {
                        nextDelayInMsSinceEpoch = holder.executeRealTime();
                        holder.problemTracker.reportNonEmptyDataCount();
                    } catch (DatafeedJob.ExtractionProblemException e) {
                        nextDelayInMsSinceEpoch = e.nextDelayInMsSinceEpoch;
                        holder.problemTracker.reportExtractionProblem(e.getCause().getMessage());
                    } catch (DatafeedJob.AnalysisProblemException e) {
                        nextDelayInMsSinceEpoch = e.nextDelayInMsSinceEpoch;
                        holder.problemTracker.reportAnalysisProblem(e.getCause().getMessage());
                        if (e.shouldStop) {
                            holder.stop("realtime_analysis_error", TimeValue.timeValueSeconds(20), e);
                            return;
                        }
                    } catch (DatafeedJob.EmptyDataCountException e) {
                        int emptyDataCount = holder.problemTracker.reportEmptyDataCount();
                        if (e.haveEverSeenData == false && holder.shouldStopAfterEmptyData(emptyDataCount)) {
                            logger.warn("Datafeed for [" + jobId + "] has seen no data in [" + emptyDataCount
                                + "] attempts, and never seen any data previously, so stopping...");
                            // In this case we auto-close the job, as though a lookback-only datafeed stopped
                            holder.stop("no_data", TimeValue.timeValueSeconds(20), e, true);
                            return;
                        }
                        nextDelayInMsSinceEpoch = e.nextDelayInMsSinceEpoch;
                    } catch (Exception e) {
                        logger.error("Unexpected datafeed failure for job [" + jobId + "] stopping...", e);
                        holder.stop("general_realtime_error", TimeValue.timeValueSeconds(20), e);
                        return;
                    }
                    holder.problemTracker.finishReport();
                    if (nextDelayInMsSinceEpoch >= 0) {
                        doDatafeedRealtime(nextDelayInMsSinceEpoch, jobId, holder);
                    }
                }
            }, delay, MachineLearning.DATAFEED_THREAD_POOL_NAME);
        }
    }

    private String getJobId(TransportStartDatafeedAction.DatafeedTask task) {
        return runningDatafeedsOnThisNode.get(task.getAllocationId()).getJobId();
    }

    private JobState getJobState(PersistentTasksCustomMetaData tasks, TransportStartDatafeedAction.DatafeedTask datafeedTask) {
        return MlTasks.getJobStateModifiedForReassignments(getJobId(datafeedTask), tasks);
    }

    private boolean jobHasOpenAutodetectCommunicator(PersistentTasksCustomMetaData tasks,
                                                     TransportStartDatafeedAction.DatafeedTask datafeedTask) {
        PersistentTasksCustomMetaData.PersistentTask<?> jobTask = MlTasks.getJobTask(getJobId(datafeedTask), tasks);
        if (jobTask == null) {
            return false;
        }

        JobTaskState state = (JobTaskState) jobTask.getState();
        if (state == null || state.isStatusStale(jobTask)) {
            return false;
        }

        return autodetectProcessManager.hasOpenAutodetectCommunicator(jobTask.getAllocationId());
    }

    private TimeValue computeNextDelay(long next) {
        return new TimeValue(Math.max(1, next - currentTimeSupplier.get()));
    }

    /**
     * Visible for testing
     */
    boolean isRunning(long allocationId) {
        return runningDatafeedsOnThisNode.containsKey(allocationId);
    }

    public class Holder {

        private final TransportStartDatafeedAction.DatafeedTask task;
        private final long allocationId;
        private final String datafeedId;
        // To ensure that we wait until lookback / realtime search has completed before we stop the datafeed
        private final ReentrantLock datafeedJobLock = new ReentrantLock(true);
        private final DatafeedJob datafeedJob;
        private final boolean defaultAutoCloseJob;
        private final ProblemTracker problemTracker;
        private final Consumer<Exception> finishHandler;
        volatile Scheduler.Cancellable cancellable;
        private volatile boolean isNodeShuttingDown;

        Holder(TransportStartDatafeedAction.DatafeedTask task, String datafeedId, DatafeedJob datafeedJob,
               ProblemTracker problemTracker, Consumer<Exception> finishHandler) {
            this.task = task;
            this.allocationId = task.getAllocationId();
            this.datafeedId = datafeedId;
            this.datafeedJob = datafeedJob;
            this.defaultAutoCloseJob = task.isLookbackOnly();
            this.problemTracker = problemTracker;
            this.finishHandler = finishHandler;
        }

        boolean shouldStopAfterEmptyData(int emptyDataCount) {
            Integer emptyDataCountToStopAt = datafeedJob.getMaxEmptySearches();
            return emptyDataCountToStopAt != null && emptyDataCount >= emptyDataCountToStopAt;
        }

        String getJobId() {
            return datafeedJob.getJobId();
        }

        boolean isRunning() {
            return datafeedJob.isRunning();
        }

        boolean isIsolated() {
            return datafeedJob.isIsolated();
        }

        public void stop(String source, TimeValue timeout, Exception e) {
            stop(source, timeout, e, defaultAutoCloseJob);
        }

        public void stop(String source, TimeValue timeout, Exception e, boolean autoCloseJob) {
            if (isNodeShuttingDown) {
                return;
            }

            logger.info("[{}] attempt to stop datafeed [{}] for job [{}]", source, datafeedId, datafeedJob.getJobId());
            if (datafeedJob.stop()) {
                boolean acquired = false;
                try {
                    logger.info("[{}] try lock [{}] to stop datafeed [{}] for job [{}]...", source, timeout, datafeedId,
                            datafeedJob.getJobId());
                    acquired = datafeedJobLock.tryLock(timeout.millis(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                } finally {  // It is crucial that none of the calls this "finally" block makes throws an exception for minor problems.
                    logger.info("[{}] stopping datafeed [{}] for job [{}], acquired [{}]...", source, datafeedId,
                            datafeedJob.getJobId(), acquired);
                    runningDatafeedsOnThisNode.remove(allocationId);
                    if (cancellable != null) {
                        cancellable.cancel();
                    }
                    auditor.info(datafeedJob.getJobId(),
                            Messages.getMessage(isIsolated() ? Messages.JOB_AUDIT_DATAFEED_ISOLATED : Messages.JOB_AUDIT_DATAFEED_STOPPED));
                    datafeedJob.finishReportingTimingStats();
                    finishHandler.accept(e);
                    logger.info("[{}] datafeed [{}] for job [{}] has been stopped{}", source, datafeedId, datafeedJob.getJobId(),
                            acquired ? "" : ", but there may be pending tasks as the timeout [" + timeout.getStringRep() + "] expired");
                    if (autoCloseJob && isIsolated() == false) {
                        closeJob();
                    }
                    if (acquired) {
                        datafeedJobLock.unlock();
                    }
                }
            } else {
                logger.info("[{}] datafeed [{}] for job [{}] was already stopped", source, datafeedId, datafeedJob.getJobId());
            }
        }

        /**
         * This stops a datafeed WITHOUT updating the corresponding persistent task.  When called it
         * will stop the datafeed from sending data to its job as quickly as possible.  The caller
         * must do something sensible with the corresponding persistent task.  If the node is shutting
         * down the task will automatically get reassigned.  Otherwise the caller must take action to
         * remove or reassign the persistent task, or the datafeed will be left in limbo.
         */
        public void isolateDatafeed() {
            datafeedJob.isolate();
        }

        public void setNodeIsShuttingDown() {
            isNodeShuttingDown = true;
        }

        private Long executeLookBack(long startTime, Long endTime) throws Exception {
            datafeedJobLock.lock();
            try {
                if (isRunning() && !isIsolated()) {
                    return datafeedJob.runLookBack(startTime, endTime);
                } else {
                    return null;
                }
            } finally {
                datafeedJobLock.unlock();
            }
        }

        private long executeRealTime() throws Exception {
            datafeedJobLock.lock();
            try {
                if (isRunning() && !isIsolated()) {
                    return datafeedJob.runRealtime();
                } else {
                    return -1L;
                }
            } finally {
                datafeedJobLock.unlock();
            }
        }

        private void closeJob() {
            ClusterState clusterState = clusterService.state();
            PersistentTasksCustomMetaData tasks = clusterState.getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
            JobState jobState = MlTasks.getJobState(getJobId(), tasks);
            if (jobState != JobState.OPENED) {
                logger.debug("[{}] No need to auto-close job as job state is [{}]", getJobId(), jobState);
                return;
            }

            task.waitForPersistentTask(Objects::isNull, TimeValue.timeValueSeconds(20),
                            new WaitForPersistentTaskListener<StartDatafeedAction.DatafeedParams>() {
                @Override
                public void onResponse(PersistentTask<StartDatafeedAction.DatafeedParams> persistentTask) {
                    CloseJobAction.Request closeJobRequest = new CloseJobAction.Request(getJobId());
                    /*
                        Enforces that for the close job api call the current node is the coordinating node.
                        If we are in this callback then the local node's cluster state doesn't contain a persistent task
                        for the datafeed and therefor the datafeed is stopped, so there is no need for the master node to
                        be to coordinating node.

                        Normally close job and stop datafeed are both executed via master node and both apis use master
                        node's local cluster state for validation purposes. In case of auto close this isn't the case and
                        if the job runs on a regular node then it may see the update before the close job api does in
                        the master node's local cluster state. This can cause the close job api the fail with a validation
                        error that the datafeed isn't stopped. To avoid this we use the current node as coordinating node
                        for the close job api call.
                    */
                    closeJobRequest.setLocal(true);
                    executeAsyncWithOrigin(client, ML_ORIGIN, CloseJobAction.INSTANCE, closeJobRequest,
                            new ActionListener<CloseJobAction.Response>() {

                                @Override
                                public void onResponse(CloseJobAction.Response response) {
                                    if (!response.isClosed()) {
                                        logger.error("[{}] job close action was not acknowledged", getJobId());
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // Given that the UI force-deletes the datafeed and then force-deletes the job, it's
                                    // quite likely that the auto-close here will get interrupted by a process kill request,
                                    // and it's misleading/worrying to log an error in this case.
                                    if (e instanceof ElasticsearchStatusException &&
                                            ((ElasticsearchStatusException) e).status() == RestStatus.CONFLICT) {
                                        logger.debug("[{}] {}", getJobId(), e.getMessage());
                                    } else {
                                        logger.error("[" + getJobId() + "] failed to auto-close job", e);
                                    }
                                }
                            });
                }

                @Override
                public void onFailure(Exception e) {
                    logger.error("Failed to remove datafeed persistent task - will not auto close job [" + getJobId() + "]", e);
                }
            });
        }
    }

    private class TaskRunner implements ClusterStateListener {

        private final List<TransportStartDatafeedAction.DatafeedTask> tasksToRun = new CopyOnWriteArrayList<>();

        private void runWhenJobIsOpened(TransportStartDatafeedAction.DatafeedTask datafeedTask) {
            ClusterState clusterState = clusterService.state();
            PersistentTasksCustomMetaData tasks = clusterState.getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
            if (getJobState(tasks, datafeedTask) == JobState.OPENED && jobHasOpenAutodetectCommunicator(tasks, datafeedTask)) {
                runTask(datafeedTask);
            } else {
                logger.info("Datafeed [{}] is waiting for job [{}] to be opened",
                        datafeedTask.getDatafeedId(), getJobId(datafeedTask));
                tasksToRun.add(datafeedTask);
            }
        }

        private void runTask(TransportStartDatafeedAction.DatafeedTask task) {
            // This clearing of the thread context is not strictly necessary.  Every action performed by the
            // datafeed _should_ be done using the MlClientHelper, which will set the appropriate thread
            // context.  However, by clearing the thread context here if anyone forgets to use MlClientHelper
            // somewhere else in the datafeed code then it should cause a failure in the same way in single
            // and multi node clusters.  If we didn't clear the thread context here then there's a risk that
            // a context with sufficient permissions would coincidentally be in force in some single node
            // tests, leading to bugs not caught in CI due to many tests running in single node test clusters.
            try (ThreadContext.StoredContext ignore = threadPool.getThreadContext().stashContext()) {
                innerRun(runningDatafeedsOnThisNode.get(task.getAllocationId()), task.getDatafeedStartTime(), task.getEndTime());
            }
        }

        @Override
        public void clusterChanged(ClusterChangedEvent event) {
            if (tasksToRun.isEmpty() || event.metaDataChanged() == false) {
                return;
            }
            PersistentTasksCustomMetaData previousTasks = event.previousState().getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
            PersistentTasksCustomMetaData currentTasks = event.state().getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
            if (Objects.equals(previousTasks, currentTasks)) {
                return;
            }

            List<TransportStartDatafeedAction.DatafeedTask> remainingTasks = new ArrayList<>();
            for (TransportStartDatafeedAction.DatafeedTask datafeedTask : tasksToRun) {
                if (runningDatafeedsOnThisNode.containsKey(datafeedTask.getAllocationId()) == false) {
                    continue;
                }
                JobState jobState = getJobState(currentTasks, datafeedTask);
                if (jobState == JobState.OPENING || jobHasOpenAutodetectCommunicator(currentTasks, datafeedTask) == false) {
                    remainingTasks.add(datafeedTask);
                } else if (jobState == JobState.OPENED) {
                    runTask(datafeedTask);
                } else {
                    logger.warn("Datafeed [{}] is stopping because job [{}] state is [{}]",
                            datafeedTask.getDatafeedId(), getJobId(datafeedTask), jobState);
                    datafeedTask.stop("job_never_opened", TimeValue.timeValueSeconds(20));
                }
            }
            tasksToRun.retainAll(remainingTasks);
        }
    }
}

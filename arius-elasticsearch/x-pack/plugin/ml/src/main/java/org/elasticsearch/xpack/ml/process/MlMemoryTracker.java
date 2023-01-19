/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.cluster.LocalNodeMasterListener;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.EsRejectedExecutionException;
import org.elasticsearch.persistent.PersistentTasksClusterService;
import org.elasticsearch.persistent.PersistentTasksCustomMetaData;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.xpack.core.ml.MlTasks;
import org.elasticsearch.xpack.core.ml.action.OpenJobAction;
import org.elasticsearch.xpack.core.ml.action.StartDataFrameAnalyticsAction;
import org.elasticsearch.xpack.core.ml.dataframe.DataFrameAnalyticsConfig;
import org.elasticsearch.xpack.core.ml.job.config.AnalysisLimits;
import org.elasticsearch.xpack.core.ml.job.config.Job;
import org.elasticsearch.xpack.ml.MachineLearning;
import org.elasticsearch.xpack.ml.dataframe.persistence.DataFrameAnalyticsConfigProvider;
import org.elasticsearch.xpack.ml.job.JobManager;
import org.elasticsearch.xpack.ml.job.persistence.JobResultsProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;
import java.util.stream.Collectors;

/**
 * This class keeps track of the memory requirement of ML jobs.
 * It only functions on the master node - for this reason it should only be used by master node actions.
 * The memory requirement for ML jobs can be updated in 4 ways:
 * 1. For all open ML data frame analytics jobs and anomaly detector jobs (via {@link #asyncRefresh})
 * 2. For all open/started ML jobs, plus one named ML anomaly detector job that is not open
 *    (via {@link #refreshAnomalyDetectorJobMemoryAndAllOthers})
 * 3. For all open/started ML jobs, plus one named ML data frame analytics job that is not started
 *    (via {@link #addDataFrameAnalyticsJobMemoryAndRefreshAllOthers})
 * 4. For one named ML anomaly detector job (via {@link #refreshAnomalyDetectorJobMemory})
 * In cases 2, 3 and 4 a listener informs the caller when the requested updates are complete.
 */
public class MlMemoryTracker implements LocalNodeMasterListener {

    private static final Duration RECENT_UPDATE_THRESHOLD = Duration.ofMinutes(1);

    private final Logger logger = LogManager.getLogger(MlMemoryTracker.class);
    private final Map<String, Long> memoryRequirementByAnomalyDetectorJob = new ConcurrentHashMap<>();
    private final Map<String, Long> memoryRequirementByDataFrameAnalyticsJob = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Long>> memoryRequirementByTaskName;
    private final List<ActionListener<Void>> fullRefreshCompletionListeners = new ArrayList<>();

    private final ThreadPool threadPool;
    private final ClusterService clusterService;
    private final JobManager jobManager;
    private final JobResultsProvider jobResultsProvider;
    private final DataFrameAnalyticsConfigProvider configProvider;
    private final Phaser stopPhaser;
    private volatile boolean isMaster;
    private volatile Instant lastUpdateTime;
    private volatile Duration reassignmentRecheckInterval;

    public MlMemoryTracker(Settings settings, ClusterService clusterService, ThreadPool threadPool, JobManager jobManager,
                           JobResultsProvider jobResultsProvider, DataFrameAnalyticsConfigProvider configProvider) {
        this.threadPool = threadPool;
        this.clusterService = clusterService;
        this.jobManager = jobManager;
        this.jobResultsProvider = jobResultsProvider;
        this.configProvider = configProvider;
        this.stopPhaser = new Phaser(1);

        Map<String, Map<String, Long>> memoryRequirementByTaskName = new TreeMap<>();
        memoryRequirementByTaskName.put(MlTasks.JOB_TASK_NAME, memoryRequirementByAnomalyDetectorJob);
        memoryRequirementByTaskName.put(MlTasks.DATA_FRAME_ANALYTICS_TASK_NAME, memoryRequirementByDataFrameAnalyticsJob);
        this.memoryRequirementByTaskName = Collections.unmodifiableMap(memoryRequirementByTaskName);

        setReassignmentRecheckInterval(PersistentTasksClusterService.CLUSTER_TASKS_ALLOCATION_RECHECK_INTERVAL_SETTING.get(settings));
        clusterService.addLocalNodeMasterListener(this);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(
            PersistentTasksClusterService.CLUSTER_TASKS_ALLOCATION_RECHECK_INTERVAL_SETTING, this::setReassignmentRecheckInterval);
    }

    private void setReassignmentRecheckInterval(TimeValue recheckInterval) {
        reassignmentRecheckInterval = Duration.ofNanos(recheckInterval.getNanos());
    }

    @Override
    public void onMaster() {
        isMaster = true;
        logger.trace("ML memory tracker on master");
    }

    @Override
    public void offMaster() {
        isMaster = false;
        logger.trace("ML memory tracker off master");
        for (Map<String, Long> memoryRequirementByJob : memoryRequirementByTaskName.values()) {
            memoryRequirementByJob.clear();
        }
        lastUpdateTime = null;
    }

    /**
     * Wait for all outstanding searches to complete.
     * After returning, no new searches can be started.
     */
    public void stop() {
        logger.trace("ML memory tracker stop called");
        // We never terminate the phaser
        assert stopPhaser.isTerminated() == false;
        // If there are no registered parties or no unarrived parties then there is a flaw
        // in the register/arrive/unregister logic in another method that uses the phaser
        assert stopPhaser.getRegisteredParties() > 0;
        assert stopPhaser.getUnarrivedParties() > 0;
        stopPhaser.arriveAndAwaitAdvance();
        assert stopPhaser.getPhase() > 0;
        logger.debug("ML memory tracker stopped");
    }

    @Override
    public String executorName() {
        return MachineLearning.UTILITY_THREAD_POOL_NAME;
    }

    /**
     * Is the information in this object sufficiently up to date
     * for valid task assignment decisions to be made using it?
     */
    public boolean isRecentlyRefreshed() {
        Instant localLastUpdateTime = lastUpdateTime;
        return localLastUpdateTime != null &&
            localLastUpdateTime.plus(RECENT_UPDATE_THRESHOLD).plus(reassignmentRecheckInterval).isAfter(Instant.now());
    }

    /**
     * Get the memory requirement for an anomaly detector job.
     * This method only works on the master node.
     * @param jobId The job ID.
     * @return The memory requirement of the job specified by {@code jobId},
     *         or <code>null</code> if it cannot be calculated.
     */
    public Long getAnomalyDetectorJobMemoryRequirement(String jobId) {
        return getJobMemoryRequirement(MlTasks.JOB_TASK_NAME, jobId);
    }

    /**
     * Get the memory requirement for a data frame analytics job.
     * This method only works on the master node.
     * @param id The job ID.
     * @return The memory requirement of the job specified by {@code id},
     *         or <code>null</code> if it cannot be found.
     */
    public Long getDataFrameAnalyticsJobMemoryRequirement(String id) {
        return getJobMemoryRequirement(MlTasks.DATA_FRAME_ANALYTICS_TASK_NAME, id);
    }

    /**
     * Get the memory requirement for the type of job corresponding to a specified persistent task name.
     * This method only works on the master node.
     * @param taskName The persistent task name.
     * @param id The job ID.
     * @return The memory requirement of the job specified by {@code id},
     *         or <code>null</code> if it cannot be found.
     */
    public Long getJobMemoryRequirement(String taskName, String id) {

        if (isMaster == false) {
            return null;
        }

        Map<String, Long> memoryRequirementByJob = memoryRequirementByTaskName.get(taskName);
        if (memoryRequirementByJob == null) {
            return null;
        }

        return memoryRequirementByJob.get(id);
    }

    /**
     * Remove any memory requirement that is stored for the specified anomaly detector job.
     * It doesn't matter if this method is called for a job that doesn't have a
     * stored memory requirement.
     */
    public void removeAnomalyDetectorJob(String jobId) {
        memoryRequirementByAnomalyDetectorJob.remove(jobId);
    }

    /**
     * Remove any memory requirement that is stored for the specified data frame analytics
     * job.  It doesn't matter if this method is called for a job that doesn't have a
     * stored memory requirement.
     */
    public void removeDataFrameAnalyticsJob(String id) {
        memoryRequirementByDataFrameAnalyticsJob.remove(id);
    }

    /**
     * Uses a separate thread to refresh the memory requirement for every ML anomaly detector job that has
     * a corresponding persistent task.  This method only works on the master node.
     * @return <code>true</code> if the async refresh is scheduled, and <code>false</code>
     *         if this is not possible for some reason.
     */
    public boolean asyncRefresh() {

        if (isMaster) {
            try {
                ActionListener<Void> listener = ActionListener.wrap(
                    aVoid -> logger.trace("Job memory requirement refresh request completed successfully"),
                    e -> logger.warn("Failed to refresh job memory requirements", e)
                );
                threadPool.executor(executorName()).execute(
                    () -> refresh(clusterService.state().getMetaData().custom(PersistentTasksCustomMetaData.TYPE), listener));
                return true;
            } catch (EsRejectedExecutionException e) {
                logger.warn("Couldn't schedule ML memory update - node might be shutting down", e);
            }
        }

        return false;
    }

    /**
     * This refreshes the memory requirement for every ML job that has a corresponding
     * persistent task and, in addition, one job that doesn't have a persistent task.
     * This method only works on the master node.
     * @param jobId The job ID of the job whose memory requirement is to be refreshed
     *              despite not having a corresponding persistent task.
     * @param listener Receives the memory requirement of the job specified by {@code jobId},
     *                 or <code>null</code> if it cannot be calculated.
     */
    public void refreshAnomalyDetectorJobMemoryAndAllOthers(String jobId, ActionListener<Long> listener) {

        if (isMaster == false) {
            listener.onResponse(null);
            return;
        }

        PersistentTasksCustomMetaData persistentTasks = clusterService.state().getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
        refresh(persistentTasks,
            ActionListener.wrap(aVoid -> refreshAnomalyDetectorJobMemory(jobId, listener), listener::onFailure));
    }

    /**
     * This refreshes the memory requirement for every ML job that has a corresponding
     * persistent task and, in addition, adds the memory requirement of one data frame analytics
     * job that doesn't have a persistent task.  This method only works on the master node.
     * @param id The job ID of the job whose memory requirement is to be added.
     * @param mem The memory requirement (in bytes) of the job specified by {@code id}.
     * @param listener Called when the refresh is complete or fails.
     */
    public void addDataFrameAnalyticsJobMemoryAndRefreshAllOthers(String id, long mem, ActionListener<Void> listener) {

        if (isMaster == false) {
            listener.onResponse(null);
            return;
        }

        memoryRequirementByDataFrameAnalyticsJob.put(id, mem + DataFrameAnalyticsConfig.PROCESS_MEMORY_OVERHEAD.getBytes());

        PersistentTasksCustomMetaData persistentTasks = clusterService.state().getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
        refresh(persistentTasks, listener);
    }

    /**
     * This refreshes the memory requirement for every ML job that has a corresponding persistent task.
     * It does NOT remove entries for jobs that no longer have a persistent task, because that would lead
     * to a race where a job was opened part way through the refresh.  (Instead, entries are removed when
     * jobs are deleted.)
     */
    void refresh(PersistentTasksCustomMetaData persistentTasks, ActionListener<Void> onCompletion) {

        synchronized (fullRefreshCompletionListeners) {
            fullRefreshCompletionListeners.add(onCompletion);
            if (fullRefreshCompletionListeners.size() > 1) {
                // A refresh is already in progress, so don't do another
                return;
            }
        }

        ActionListener<Void> refreshComplete = ActionListener.wrap(aVoid -> {
            lastUpdateTime = Instant.now();
            synchronized (fullRefreshCompletionListeners) {
                assert fullRefreshCompletionListeners.isEmpty() == false;
                for (ActionListener<Void> listener : fullRefreshCompletionListeners) {
                    listener.onResponse(null);
                }
                fullRefreshCompletionListeners.clear();
            }
        },
        e -> {
            synchronized (fullRefreshCompletionListeners) {
                assert fullRefreshCompletionListeners.isEmpty() == false;
                for (ActionListener<Void> listener : fullRefreshCompletionListeners) {
                    listener.onFailure(e);
                }
                // It's critical that we empty out the current listener list on
                // error otherwise subsequent retries to refresh will be ignored
                fullRefreshCompletionListeners.clear();
            }
        });

        // persistentTasks will be null if there's never been a persistent task created in this cluster
        if (persistentTasks == null) {
            refreshComplete.onResponse(null);
        } else {
            List<PersistentTasksCustomMetaData.PersistentTask<?>> mlDataFrameAnalyticsJobTasks = persistentTasks.tasks().stream()
                .filter(task -> MlTasks.DATA_FRAME_ANALYTICS_TASK_NAME.equals(task.getTaskName())).collect(Collectors.toList());
            ActionListener<Void> refreshDataFrameAnalyticsJobs =
                ActionListener.wrap(aVoid -> refreshAllDataFrameAnalyticsJobTasks(mlDataFrameAnalyticsJobTasks, refreshComplete),
                    refreshComplete::onFailure);

            List<PersistentTasksCustomMetaData.PersistentTask<?>> mlAnomalyDetectorJobTasks = persistentTasks.tasks().stream()
                .filter(task -> MlTasks.JOB_TASK_NAME.equals(task.getTaskName())).collect(Collectors.toList());
            iterateAnomalyDetectorJobTasks(mlAnomalyDetectorJobTasks.iterator(), refreshDataFrameAnalyticsJobs);
        }
    }

    private void iterateAnomalyDetectorJobTasks(Iterator<PersistentTasksCustomMetaData.PersistentTask<?>> iterator,
                                                ActionListener<Void> refreshComplete) {
        if (iterator.hasNext()) {
            OpenJobAction.JobParams jobParams = (OpenJobAction.JobParams) iterator.next().getParams();
            refreshAnomalyDetectorJobMemory(jobParams.getJobId(),
                ActionListener.wrap(
                    // Do the next iteration in a different thread, otherwise stack overflow
                    // can occur if the searches happen to be on the local node, as the huge
                    // chain of listeners are all called in the same thread if only one node
                    // is involved
                    mem -> threadPool.executor(executorName()).execute(() -> iterateAnomalyDetectorJobTasks(iterator, refreshComplete)),
                    refreshComplete::onFailure));
        } else {
            refreshComplete.onResponse(null);
        }
    }

    private void refreshAllDataFrameAnalyticsJobTasks(List<PersistentTasksCustomMetaData.PersistentTask<?>> mlDataFrameAnalyticsJobTasks,
                                                      ActionListener<Void> listener) {
        if (mlDataFrameAnalyticsJobTasks.isEmpty()) {
            listener.onResponse(null);
            return;
        }

        Set<String> jobsWithTasks = mlDataFrameAnalyticsJobTasks.stream().map(
            task -> ((StartDataFrameAnalyticsAction.TaskParams) task.getParams()).getId()).collect(Collectors.toSet());

        configProvider.getConfigsForJobsWithTasksLeniently(jobsWithTasks, ActionListener.wrap(
            analyticsConfigs -> {
                for (DataFrameAnalyticsConfig analyticsConfig : analyticsConfigs) {
                    memoryRequirementByDataFrameAnalyticsJob.put(analyticsConfig.getId(),
                        analyticsConfig.getModelMemoryLimit().getBytes() + DataFrameAnalyticsConfig.PROCESS_MEMORY_OVERHEAD.getBytes());
                }
                listener.onResponse(null);
            },
            listener::onFailure
        ));
    }

    /**
     * Refresh the memory requirement for a single anomaly detector job.
     * This method only works on the master node.
     * @param jobId    The ID of the job to refresh the memory requirement for.
     * @param listener Receives the job's memory requirement, or <code>null</code>
     *                 if it cannot be calculated.
     */
    public void refreshAnomalyDetectorJobMemory(String jobId, ActionListener<Long> listener) {
        if (isMaster == false) {
            listener.onResponse(null);
            return;
        }

        // The phaser prevents searches being started after the memory tracker's stop() method has returned
        if (stopPhaser.register() != 0) {
            // Phases above 0 mean we've been stopped, so don't do any operations that involve external interaction
            stopPhaser.arriveAndDeregister();
            listener.onFailure(new EsRejectedExecutionException("Couldn't run ML memory update - node is shutting down"));
            return;
        }
        ActionListener<Long> phaserListener = ActionListener.wrap(
            r -> {
                stopPhaser.arriveAndDeregister();
                listener.onResponse(r);
            },
            e -> {
                stopPhaser.arriveAndDeregister();
                listener.onFailure(e);
            }
        );

        try {
            jobResultsProvider.getEstablishedMemoryUsage(jobId, null, null,
                establishedModelMemoryBytes -> {
                    if (establishedModelMemoryBytes <= 0L) {
                        setAnomalyDetectorJobMemoryToLimit(jobId, phaserListener);
                    } else {
                        Long memoryRequirementBytes = establishedModelMemoryBytes + Job.PROCESS_MEMORY_OVERHEAD.getBytes();
                        memoryRequirementByAnomalyDetectorJob.put(jobId, memoryRequirementBytes);
                        phaserListener.onResponse(memoryRequirementBytes);
                    }
                },
                e -> {
                    logger.error("[" + jobId + "] failed to calculate anomaly detector job established model memory requirement", e);
                    setAnomalyDetectorJobMemoryToLimit(jobId, phaserListener);
                }
            );
        } catch (Exception e) {
            logger.error("[" + jobId + "] failed to calculate anomaly detector job established model memory requirement", e);
            setAnomalyDetectorJobMemoryToLimit(jobId, phaserListener);
        }
    }

    private void setAnomalyDetectorJobMemoryToLimit(String jobId, ActionListener<Long> listener) {
        jobManager.getJob(jobId, ActionListener.wrap(job -> {
            Long memoryLimitMb = (job.getAnalysisLimits() != null) ? job.getAnalysisLimits().getModelMemoryLimit() : null;
            // Although recent versions of the code enforce a non-null model_memory_limit
            // when parsing, the job could have been streamed from an older version node in
            // a mixed version cluster
            if (memoryLimitMb == null) {
                memoryLimitMb = AnalysisLimits.PRE_6_1_DEFAULT_MODEL_MEMORY_LIMIT_MB;
            }
            Long memoryRequirementBytes = ByteSizeUnit.MB.toBytes(memoryLimitMb) + Job.PROCESS_MEMORY_OVERHEAD.getBytes();
            memoryRequirementByAnomalyDetectorJob.put(jobId, memoryRequirementBytes);
            listener.onResponse(memoryRequirementBytes);
        }, e -> {
            if (e instanceof ResourceNotFoundException) {
                // TODO: does this also happen if the .ml-config index exists but is unavailable?
                // However, note that we wait for the .ml-config index to be available earlier on in the
                // job assignment process, so that scenario should be very rare, i.e. somebody has closed
                // the .ml-config index (which would be unexpected and unsupported for an internal index)
                // during the memory refresh.
                logger.trace("[{}] anomaly detector job deleted during ML memory update", jobId);
            } else {
                logger.error("[" + jobId + "] failed to get anomaly detector job during ML memory update", e);
            }
            memoryRequirementByAnomalyDetectorJob.remove(jobId);
            listener.onResponse(null);
        }));
    }
}

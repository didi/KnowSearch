/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.dataframe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.node.tasks.cancel.CancelTasksRequest;
import org.elasticsearch.action.admin.cluster.node.tasks.cancel.CancelTasksResponse;
import org.elasticsearch.action.admin.cluster.node.tasks.get.GetTaskRequest;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.reindex.BulkByScrollTask;
import org.elasticsearch.persistent.AllocatedPersistentTask;
import org.elasticsearch.tasks.TaskId;
import org.elasticsearch.tasks.TaskResult;
import org.elasticsearch.xpack.core.ml.MlTasks;
import org.elasticsearch.xpack.core.ml.action.GetDataFrameAnalyticsStatsAction;
import org.elasticsearch.xpack.core.ml.action.StartDataFrameAnalyticsAction;
import org.elasticsearch.xpack.core.ml.dataframe.DataFrameAnalyticsState;
import org.elasticsearch.xpack.core.ml.dataframe.DataFrameAnalyticsTaskState;
import org.elasticsearch.xpack.core.ml.job.messages.Messages;
import org.elasticsearch.xpack.core.ml.job.persistence.AnomalyDetectorsIndex;
import org.elasticsearch.xpack.core.ml.utils.ExceptionsHelper;
import org.elasticsearch.xpack.core.ml.utils.PhaseProgress;
import org.elasticsearch.xpack.core.watcher.watch.Payload;
import org.elasticsearch.xpack.ml.notifications.DataFrameAnalyticsAuditor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.elasticsearch.xpack.core.ClientHelper.ML_ORIGIN;
import static org.elasticsearch.xpack.core.ClientHelper.executeAsyncWithOrigin;

public class DataFrameAnalyticsTask extends AllocatedPersistentTask implements StartDataFrameAnalyticsAction.TaskMatcher {

    private static final Logger LOGGER = LogManager.getLogger(DataFrameAnalyticsTask.class);

    private final Client client;
    private final ClusterService clusterService;
    private final DataFrameAnalyticsManager analyticsManager;
    private final DataFrameAnalyticsAuditor auditor;
    private final StartDataFrameAnalyticsAction.TaskParams taskParams;
    @Nullable
    private volatile Long reindexingTaskId;
    private volatile boolean isReindexingFinished;
    private volatile boolean isStopping;
    private volatile boolean isMarkAsCompletedCalled;
    private final ProgressTracker progressTracker = new ProgressTracker();

    public DataFrameAnalyticsTask(long id, String type, String action, TaskId parentTask, Map<String, String> headers,
                                  Client client, ClusterService clusterService, DataFrameAnalyticsManager analyticsManager,
                                  DataFrameAnalyticsAuditor auditor, StartDataFrameAnalyticsAction.TaskParams taskParams) {
        super(id, type, action, MlTasks.DATA_FRAME_ANALYTICS_TASK_ID_PREFIX + taskParams.getId(), parentTask, headers);
        this.client = Objects.requireNonNull(client);
        this.clusterService = Objects.requireNonNull(clusterService);
        this.analyticsManager = Objects.requireNonNull(analyticsManager);
        this.auditor = Objects.requireNonNull(auditor);
        this.taskParams = Objects.requireNonNull(taskParams);
    }

    public StartDataFrameAnalyticsAction.TaskParams getParams() {
        return taskParams;
    }

    public void setReindexingTaskId(Long reindexingTaskId) {
        LOGGER.debug("[{}] Setting reindexing task id to [{}] from [{}]", taskParams.getId(), reindexingTaskId, this.reindexingTaskId);
        this.reindexingTaskId = reindexingTaskId;
    }

    public void setReindexingFinished() {
        isReindexingFinished = true;
    }

    public boolean isStopping() {
        return isStopping;
    }

    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Override
    protected void onCancelled() {
        stop(getReasonCancelled(), TimeValue.ZERO);
    }

    @Override
    public void markAsCompleted() {
        // It is possible that the stop API has been called in the meantime and that
        // may also cause this method to be called. We check whether we have already
        // been marked completed to avoid doing it twice. We need to capture that
        // locally instead of relying to isCompleted() because of the asynchronous
        // persistence of progress.
        synchronized (this) {
            if (isMarkAsCompletedCalled) {
                return;
            }
            isMarkAsCompletedCalled = true;
        }

        persistProgress(() -> super.markAsCompleted());
    }

    @Override
    public void markAsFailed(Exception e) {
        persistProgress(() -> super.markAsFailed(e));
    }

    public void stop(String reason, TimeValue timeout) {
        isStopping = true;

        ActionListener<Void> reindexProgressListener = ActionListener.wrap(
            aVoid -> doStop(reason, timeout),
            e -> {
                LOGGER.error(new ParameterizedMessage("[{}] Error updating reindexing progress", taskParams.getId()), e);
                // We should log the error but it shouldn't stop us from stopping the task
                doStop(reason, timeout);
            }
        );

        // We need to update reindexing progress before we cancel the task
        updateReindexTaskProgress(reindexProgressListener);
    }

    private void doStop(String reason, TimeValue timeout) {
        if (reindexingTaskId != null) {
            cancelReindexingTask(reason, timeout);
        }
        analyticsManager.stop(this);
    }

    private void cancelReindexingTask(String reason, TimeValue timeout) {
        TaskId reindexTaskId = new TaskId(clusterService.localNode().getId(), reindexingTaskId);
        LOGGER.debug("[{}] Cancelling reindex task [{}]", taskParams.getId(), reindexTaskId);

        CancelTasksRequest cancelReindex = new CancelTasksRequest();
        cancelReindex.setTaskId(reindexTaskId);
        cancelReindex.setReason(reason);
        cancelReindex.setTimeout(timeout);
        CancelTasksResponse cancelReindexResponse = client.admin().cluster().cancelTasks(cancelReindex).actionGet();
        Throwable firstError = null;
        if (cancelReindexResponse.getNodeFailures().isEmpty() == false) {
            firstError = cancelReindexResponse.getNodeFailures().get(0).getRootCause();
        }
        if (cancelReindexResponse.getTaskFailures().isEmpty() == false) {
            firstError = cancelReindexResponse.getTaskFailures().get(0).getCause();
        }
        // There is a chance that the task is finished by the time we cancel it in which case we'll get
        // a ResourceNotFoundException which we can ignore.
        if (firstError != null && ExceptionsHelper.unwrapCause(firstError) instanceof ResourceNotFoundException == false) {
            throw ExceptionsHelper.serverError("[" + taskParams.getId() + "] Error cancelling reindex task", firstError);
        } else {
            LOGGER.debug("[{}] Reindex task was successfully cancelled", taskParams.getId());
        }
    }

    public void updateState(DataFrameAnalyticsState state, @Nullable String reason) {
        DataFrameAnalyticsTaskState newTaskState = new DataFrameAnalyticsTaskState(state, getAllocationId(), reason);
        updatePersistentTaskState(
            newTaskState,
            ActionListener.wrap(
                updatedTask -> {
                    auditor.info(getParams().getId(), Messages.getMessage(Messages.DATA_FRAME_ANALYTICS_AUDIT_UPDATED_STATE, state));
                    LOGGER.info("[{}] Successfully update task state to [{}]", getParams().getId(), state);
                },
                e -> LOGGER.error(new ParameterizedMessage("[{}] Could not update task state to [{}] with reason [{}]",
                    getParams().getId(), state, reason), e)
            )
        );
    }

    public void updateReindexTaskProgress(ActionListener<Void> listener) {
        getReindexTaskProgress(ActionListener.wrap(
            // We set reindexing progress at least to 1 for a running process to be able to
            // distinguish a job that is running for the first time against a job that is restarting.
            reindexTaskProgress -> {
                progressTracker.reindexingPercent.set(Math.max(1, reindexTaskProgress));
                listener.onResponse(null);
            },
            listener::onFailure
        ));
    }

    private void getReindexTaskProgress(ActionListener<Integer> listener) {
        TaskId reindexTaskId = getReindexTaskId();
        if (reindexTaskId == null) {
            // The task is not present which means either it has not started yet or it finished.
            // We keep track of whether the task has finished so we can use that to tell whether the progress 100.
            listener.onResponse(isReindexingFinished ? 100 : 0);
            return;
        }

        GetTaskRequest getTaskRequest = new GetTaskRequest();
        getTaskRequest.setTaskId(reindexTaskId);
        client.admin().cluster().getTask(getTaskRequest, ActionListener.wrap(
            taskResponse -> {
                TaskResult taskResult = taskResponse.getTask();
                BulkByScrollTask.Status taskStatus = (BulkByScrollTask.Status) taskResult.getTask().getStatus();
                int progress = (int) (taskStatus.getCreated() * 100.0 / taskStatus.getTotal());
                listener.onResponse(progress);
            },
            error -> {
                if (ExceptionsHelper.unwrapCause(error) instanceof ResourceNotFoundException) {
                    // The task is not present which means either it has not started yet or it finished.
                    // We keep track of whether the task has finished so we can use that to tell whether the progress 100.
                    listener.onResponse(isReindexingFinished ? 100 : 0);
                } else {
                    listener.onFailure(error);
                }
            }
        ));
    }

    @Nullable
    private TaskId getReindexTaskId() {
        try {
            return new TaskId(clusterService.localNode().getId(), reindexingTaskId);
        } catch (NullPointerException e) {
            // This may happen if there is no reindexing task id set which means we either never started the task yet or we're finished
            return null;
        }
    }

    private void persistProgress(Runnable runnable) {
        LOGGER.debug("[{}] Persisting progress", taskParams.getId());
        GetDataFrameAnalyticsStatsAction.Request getStatsRequest = new GetDataFrameAnalyticsStatsAction.Request(taskParams.getId());
        executeAsyncWithOrigin(client, ML_ORIGIN, GetDataFrameAnalyticsStatsAction.INSTANCE, getStatsRequest, ActionListener.wrap(
            statsResponse -> {
                GetDataFrameAnalyticsStatsAction.Response.Stats stats = statsResponse.getResponse().results().get(0);
                IndexRequest indexRequest = new IndexRequest(AnomalyDetectorsIndex.jobStateIndexWriteAlias());
                indexRequest.id(StoredProgress.documentId(taskParams.getId()));
                indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                try (XContentBuilder jsonBuilder = JsonXContent.contentBuilder()) {
                    new StoredProgress(stats.getProgress()).toXContent(jsonBuilder, Payload.XContent.EMPTY_PARAMS);
                    indexRequest.source(jsonBuilder);
                }
                executeAsyncWithOrigin(client, ML_ORIGIN, IndexAction.INSTANCE, indexRequest, ActionListener.wrap(
                    indexResponse -> {
                        LOGGER.debug("[{}] Successfully indexed progress document", taskParams.getId());
                        runnable.run();
                    },
                    indexError -> {
                        LOGGER.error(new ParameterizedMessage(
                        "[{}] cannot persist progress as an error occurred while indexing", taskParams.getId()), indexError);
                        runnable.run();
                    }
                ));
            },
            e -> {
                LOGGER.error(new ParameterizedMessage(
                "[{}] cannot persist progress as an error occurred while retrieving stats", taskParams.getId()), e);
                runnable.run();
            }
        ));
    }

    /**
     * This captures the possible states a job can be when it starts.
     * {@code FIRST_TIME} means the job has never been started before.
     * {@code RESUMING_REINDEXING} means the job was stopped while it was reindexing.
     * {@code RESUMING_ANALYZING} means the job was stopped while it was analyzing.
     * {@code FINISHED} means the job had finished.
     */
    public enum StartingState {
        FIRST_TIME, RESUMING_REINDEXING, RESUMING_ANALYZING, FINISHED
    }

    public static StartingState determineStartingState(String jobId, List<PhaseProgress> progressOnStart) {
        PhaseProgress lastIncompletePhase = null;
        for (PhaseProgress phaseProgress : progressOnStart) {
            if (phaseProgress.getProgressPercent() < 100) {
                lastIncompletePhase = phaseProgress;
                break;
            }
        }

        if (lastIncompletePhase == null) {
            return StartingState.FINISHED;
        }

        LOGGER.debug("[{}] Last incomplete progress [{}, {}]", jobId, lastIncompletePhase.getPhase(),
            lastIncompletePhase.getProgressPercent());

        switch (lastIncompletePhase.getPhase()) {
            case ProgressTracker.REINDEXING:
                return lastIncompletePhase.getProgressPercent() == 0 ? StartingState.FIRST_TIME : StartingState.RESUMING_REINDEXING;
            case ProgressTracker.LOADING_DATA:
            case ProgressTracker.ANALYZING:
            case ProgressTracker.WRITING_RESULTS:
                return StartingState.RESUMING_ANALYZING;
            default:
                LOGGER.warn("[{}] Unexpected progress phase [{}]", jobId, lastIncompletePhase.getPhase());
                return StartingState.FIRST_TIME;
        }
    }

    public static class ProgressTracker {

        public static final String REINDEXING = "reindexing";
        public static final String LOADING_DATA = "loading_data";
        public static final String ANALYZING = "analyzing";
        public static final String WRITING_RESULTS = "writing_results";

        public final AtomicInteger reindexingPercent = new AtomicInteger(0);
        public final AtomicInteger loadingDataPercent = new AtomicInteger(0);
        public final AtomicInteger analyzingPercent = new AtomicInteger(0);
        public final AtomicInteger writingResultsPercent = new AtomicInteger(0);

        public List<PhaseProgress> report() {
            return Arrays.asList(
                new PhaseProgress(REINDEXING, reindexingPercent.get()),
                new PhaseProgress(LOADING_DATA, loadingDataPercent.get()),
                new PhaseProgress(ANALYZING, analyzingPercent.get()),
                new PhaseProgress(WRITING_RESULTS, writingResultsPercent.get())
            );
        }
    }
}

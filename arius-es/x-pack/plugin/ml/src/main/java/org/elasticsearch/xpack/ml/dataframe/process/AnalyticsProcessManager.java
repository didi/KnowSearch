/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.dataframe.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.lucene.util.SetOnce;
import org.elasticsearch.action.admin.indices.refresh.RefreshAction;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.xpack.core.ClientHelper;
import org.elasticsearch.xpack.core.ml.dataframe.DataFrameAnalyticsConfig;
import org.elasticsearch.xpack.core.ml.dataframe.DataFrameAnalyticsState;
import org.elasticsearch.xpack.core.ml.dataframe.analyses.DataFrameAnalysis;
import org.elasticsearch.xpack.core.ml.job.messages.Messages;
import org.elasticsearch.xpack.core.ml.job.persistence.AnomalyDetectorsIndex;
import org.elasticsearch.xpack.core.ml.utils.ExceptionsHelper;
import org.elasticsearch.xpack.ml.MachineLearning;
import org.elasticsearch.xpack.ml.dataframe.DataFrameAnalyticsTask;
import org.elasticsearch.xpack.ml.dataframe.extractor.DataFrameDataExtractor;
import org.elasticsearch.xpack.ml.dataframe.extractor.DataFrameDataExtractorFactory;
import org.elasticsearch.xpack.ml.dataframe.process.customprocessing.CustomProcessor;
import org.elasticsearch.xpack.ml.dataframe.process.customprocessing.CustomProcessorFactory;
import org.elasticsearch.xpack.ml.dataframe.process.results.AnalyticsResult;
import org.elasticsearch.xpack.ml.extractor.ExtractedFields;
import org.elasticsearch.xpack.ml.inference.persistence.TrainedModelProvider;
import org.elasticsearch.xpack.ml.notifications.DataFrameAnalyticsAuditor;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static org.elasticsearch.xpack.core.ClientHelper.ML_ORIGIN;

public class AnalyticsProcessManager {

    private static final Logger LOGGER = LogManager.getLogger(AnalyticsProcessManager.class);

    private final Client client;
    private final ExecutorService executorServiceForJob;
    private final ExecutorService executorServiceForProcess;
    private final AnalyticsProcessFactory<AnalyticsResult> processFactory;
    private final ConcurrentMap<Long, ProcessContext> processContextByAllocation = new ConcurrentHashMap<>();
    private final DataFrameAnalyticsAuditor auditor;
    private final TrainedModelProvider trainedModelProvider;

    public AnalyticsProcessManager(Client client,
                                   ThreadPool threadPool,
                                   AnalyticsProcessFactory<AnalyticsResult> analyticsProcessFactory,
                                   DataFrameAnalyticsAuditor auditor,
                                   TrainedModelProvider trainedModelProvider) {
        this(
            client,
            threadPool.generic(),
            threadPool.executor(MachineLearning.JOB_COMMS_THREAD_POOL_NAME),
            analyticsProcessFactory,
            auditor,
            trainedModelProvider);
    }

    // Visible for testing
    public AnalyticsProcessManager(Client client,
                                   ExecutorService executorServiceForJob,
                                   ExecutorService executorServiceForProcess,
                                   AnalyticsProcessFactory<AnalyticsResult> analyticsProcessFactory,
                                   DataFrameAnalyticsAuditor auditor,
                                   TrainedModelProvider trainedModelProvider) {
        this.client = Objects.requireNonNull(client);
        this.executorServiceForJob = Objects.requireNonNull(executorServiceForJob);
        this.executorServiceForProcess = Objects.requireNonNull(executorServiceForProcess);
        this.processFactory = Objects.requireNonNull(analyticsProcessFactory);
        this.auditor = Objects.requireNonNull(auditor);
        this.trainedModelProvider = Objects.requireNonNull(trainedModelProvider);
    }

    public void runJob(DataFrameAnalyticsTask task, DataFrameAnalyticsConfig config, DataFrameDataExtractorFactory dataExtractorFactory) {
        executorServiceForJob.execute(() -> {
            ProcessContext processContext = new ProcessContext(config);
            synchronized (processContextByAllocation) {
                if (task.isStopping()) {
                    // The task was requested to stop before we created the process context
                    auditor.info(config.getId(), Messages.DATA_FRAME_ANALYTICS_AUDIT_FINISHED_ANALYSIS);
                    task.markAsCompleted();
                    return;
                }
                if (processContextByAllocation.putIfAbsent(task.getAllocationId(), processContext) != null) {
                    task.updateState(
                        DataFrameAnalyticsState.FAILED, "[" + config.getId() + "] Could not create process as one already exists");
                    return;
                }
            }

            // Fetch existing model state (if any)
            BytesReference state = getModelState(config);

            if (processContext.startProcess(dataExtractorFactory, task, state)) {
                executorServiceForProcess.execute(() -> processContext.resultProcessor.get().process(processContext.process.get()));
                executorServiceForProcess.execute(() -> processData(task, processContext, state));
            } else {
                processContextByAllocation.remove(task.getAllocationId());
                auditor.info(config.getId(), Messages.DATA_FRAME_ANALYTICS_AUDIT_FINISHED_ANALYSIS);
                task.markAsCompleted();
            }
        });
    }

    @Nullable
    private BytesReference getModelState(DataFrameAnalyticsConfig config) {
        if (config.getAnalysis().persistsState() == false) {
            return null;
        }

        try (ThreadContext.StoredContext ignore = client.threadPool().getThreadContext().stashWithOrigin(ML_ORIGIN)) {
            SearchResponse searchResponse = client.prepareSearch(AnomalyDetectorsIndex.jobStateIndexPattern())
                .setSize(1)
                .setQuery(QueryBuilders.idsQuery().addIds(config.getAnalysis().getStateDocId(config.getId())))
                .get();
            SearchHit[] hits = searchResponse.getHits().getHits();
            return hits.length == 0 ? null : hits[0].getSourceRef();
        }
    }

    private void processData(DataFrameAnalyticsTask task, ProcessContext processContext, BytesReference state) {
        DataFrameAnalyticsConfig config = processContext.config;
        DataFrameDataExtractor dataExtractor = processContext.dataExtractor.get();
        AnalyticsProcess<AnalyticsResult> process = processContext.process.get();
        AnalyticsResultProcessor resultProcessor = processContext.resultProcessor.get();
        try {
            writeHeaderRecord(dataExtractor, process);
            writeDataRows(dataExtractor, process, config.getAnalysis(), task.getProgressTracker());
            process.writeEndOfDataMessage();
            process.flushStream();

            restoreState(task, config, state, process);

            LOGGER.info("[{}] Waiting for result processor to complete", config.getId());
            resultProcessor.awaitForCompletion();
            processContext.setFailureReason(resultProcessor.getFailure());

            refreshDest(config);
            refreshStateIndex(config.getId());
            LOGGER.info("[{}] Result processor has completed", config.getId());
        } catch (Exception e) {
            if (task.isStopping()) {
                // Errors during task stopping are expected but we still want to log them just in case.
                String errorMsg =
                    new ParameterizedMessage(
                        "[{}] Error while processing data [{}]; task is stopping", config.getId(), e.getMessage()).getFormattedMessage();
                LOGGER.debug(errorMsg, e);
            } else {
                String errorMsg =
                    new ParameterizedMessage("[{}] Error while processing data [{}]", config.getId(), e.getMessage()).getFormattedMessage();
                LOGGER.error(errorMsg, e);
                processContext.setFailureReason(errorMsg);
            }
        } finally {
            closeProcess(task);

            processContextByAllocation.remove(task.getAllocationId());
            LOGGER.debug("Removed process context for task [{}]; [{}] processes still running", config.getId(),
                processContextByAllocation.size());

            if (processContext.getFailureReason() == null) {
                // This results in marking the persistent task as complete
                LOGGER.info("[{}] Marking task completed", config.getId());
                auditor.info(config.getId(), Messages.DATA_FRAME_ANALYTICS_AUDIT_FINISHED_ANALYSIS);
                task.markAsCompleted();
            } else {
                LOGGER.error("[{}] Marking task failed; {}", config.getId(), processContext.getFailureReason());
                task.updateState(DataFrameAnalyticsState.FAILED, processContext.getFailureReason());
                // Note: We are not marking the task as failed here as we want the user to be able to inspect the failure reason.
            }
        }
    }

    private void writeDataRows(DataFrameDataExtractor dataExtractor, AnalyticsProcess<AnalyticsResult> process,
                               DataFrameAnalysis analysis, DataFrameAnalyticsTask.ProgressTracker progressTracker) throws IOException {

        CustomProcessor customProcessor = new CustomProcessorFactory(dataExtractor.getFieldNames()).create(analysis);

        // The extra fields are for the doc hash and the control field (should be an empty string)
        String[] record = new String[dataExtractor.getFieldNames().size() + 2];
        // The value of the control field should be an empty string for data frame rows
        record[record.length - 1] = "";

        long totalRows = process.getConfig().rows();
        long rowsProcessed = 0;

        while (dataExtractor.hasNext()) {
            Optional<List<DataFrameDataExtractor.Row>> rows = dataExtractor.next();
            if (rows.isPresent()) {
                for (DataFrameDataExtractor.Row row : rows.get()) {
                    if (row.shouldSkip() == false) {
                        String[] rowValues = row.getValues();
                        System.arraycopy(rowValues, 0, record, 0, rowValues.length);
                        record[record.length - 2] = String.valueOf(row.getChecksum());
                        customProcessor.process(record);
                        process.writeRecord(record);
                    }
                }
                rowsProcessed += rows.get().size();
                progressTracker.loadingDataPercent.set(rowsProcessed >= totalRows ? 100 : (int) (rowsProcessed * 100.0 / totalRows));
            }
        }
    }

    private void writeHeaderRecord(DataFrameDataExtractor dataExtractor, AnalyticsProcess<AnalyticsResult> process) throws IOException {
        List<String> fieldNames = dataExtractor.getFieldNames();

        // We add 2 extra fields, both named dot:
        //   - the document hash
        //   - the control message
        String[] headerRecord = new String[fieldNames.size() + 2];
        for (int i = 0; i < fieldNames.size(); i++) {
            headerRecord[i] = fieldNames.get(i);
        }

        headerRecord[headerRecord.length - 2] = ".";
        headerRecord[headerRecord.length - 1] = ".";
        process.writeRecord(headerRecord);
    }

    private void restoreState(DataFrameAnalyticsTask task, DataFrameAnalyticsConfig config, @Nullable BytesReference state,
                              AnalyticsProcess<AnalyticsResult> process) {
        if (config.getAnalysis().persistsState() == false) {
            LOGGER.debug("[{}] Analysis does not support state", config.getId());
            return;
        }

        if (state == null) {
            LOGGER.debug("[{}] No model state available to restore", config.getId());
            return;
        }

        LOGGER.debug("[{}] Restoring from previous model state", config.getId());
        auditor.info(config.getId(), Messages.DATA_FRAME_ANALYTICS_AUDIT_RESTORING_STATE);

        try (ThreadContext.StoredContext ignore = client.threadPool().getThreadContext().stashWithOrigin(ML_ORIGIN)) {
            process.restoreState(state);
        } catch (Exception e) {
            LOGGER.error(new ParameterizedMessage("[{}] Failed to restore state", process.getConfig().jobId()), e);
            task.updateState(DataFrameAnalyticsState.FAILED, "Failed to restore state: " + e.getMessage());
        }
    }

    private AnalyticsProcess<AnalyticsResult> createProcess(DataFrameAnalyticsTask task, DataFrameAnalyticsConfig config,
                                                            AnalyticsProcessConfig analyticsProcessConfig, @Nullable BytesReference state) {
        AnalyticsProcess<AnalyticsResult> process =
            processFactory.createAnalyticsProcess(config, analyticsProcessConfig, state, executorServiceForProcess, onProcessCrash(task));
        if (process.isProcessAlive() == false) {
            throw ExceptionsHelper.serverError("Failed to start data frame analytics process");
        }
        return process;
    }

    private Consumer<String> onProcessCrash(DataFrameAnalyticsTask task) {
        return reason -> {
            ProcessContext processContext = processContextByAllocation.get(task.getAllocationId());
            if (processContext != null) {
                processContext.setFailureReason(reason);
                processContext.stop();
            }
        };
    }

    private void refreshDest(DataFrameAnalyticsConfig config) {
        ClientHelper.executeWithHeaders(config.getHeaders(), ClientHelper.ML_ORIGIN, client,
            () -> client.execute(RefreshAction.INSTANCE, new RefreshRequest(config.getDest().getIndex())).actionGet());
    }

    private void refreshStateIndex(String jobId) {
        String indexName = AnomalyDetectorsIndex.jobStateIndexPattern();
        LOGGER.debug("[{}] Refresh index {}", jobId, indexName);

        RefreshRequest refreshRequest = new RefreshRequest(indexName);
        refreshRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        try (ThreadContext.StoredContext ignore = client.threadPool().getThreadContext().stashWithOrigin(ML_ORIGIN)) {
            client.admin().indices().refresh(refreshRequest).actionGet();
        }
    }

    private void closeProcess(DataFrameAnalyticsTask task) {
        String configId = task.getParams().getId();
        LOGGER.info("[{}] Closing process", configId);

        ProcessContext processContext = processContextByAllocation.get(task.getAllocationId());
        try {
            processContext.process.get().close();
            LOGGER.info("[{}] Closed process", configId);
        } catch (Exception e) {
            LOGGER.error("[" + configId + "] Error closing data frame analyzer process", e);
            String errorMsg = new ParameterizedMessage(
                "[{}] Error closing data frame analyzer process [{}]", configId, e.getMessage()).getFormattedMessage();
            processContext.setFailureReason(errorMsg);
        }
    }

    public void stop(DataFrameAnalyticsTask task) {
        ProcessContext processContext;
        synchronized (processContextByAllocation) {
            processContext = processContextByAllocation.get(task.getAllocationId());
        }
        if (processContext != null) {
            LOGGER.debug("[{}] Stopping process", task.getParams().getId());
            processContext.stop();
        } else {
            LOGGER.debug("[{}] No process context to stop", task.getParams().getId());
            task.markAsCompleted();
        }
    }

    // Visible for testing
    int getProcessContextCount() {
        return processContextByAllocation.size();
    }

    class ProcessContext {

        private final DataFrameAnalyticsConfig config;
        private final SetOnce<AnalyticsProcess<AnalyticsResult>> process = new SetOnce<>();
        private final SetOnce<DataFrameDataExtractor> dataExtractor = new SetOnce<>();
        private final SetOnce<AnalyticsResultProcessor> resultProcessor = new SetOnce<>();
        private final SetOnce<String> failureReason = new SetOnce<>();

        ProcessContext(DataFrameAnalyticsConfig config) {
            this.config = Objects.requireNonNull(config);
        }

        String getFailureReason() {
            return failureReason.get();
        }

        void setFailureReason(String failureReason) {
            if (failureReason == null) {
                return;
            }
            // Only set the new reason if there isn't one already as we want to keep the first reason (most likely the root cause).
            this.failureReason.trySet(failureReason);
        }

        synchronized void stop() {
            LOGGER.debug("[{}] Stopping process", config.getId());
            if (dataExtractor.get() != null) {
                dataExtractor.get().cancel();
            }
            if (resultProcessor.get() != null) {
                resultProcessor.get().cancel();
            }
            if (process.get() != null) {
                try {
                    process.get().kill();
                } catch (IOException e) {
                    LOGGER.error(new ParameterizedMessage("[{}] Failed to kill process", config.getId()), e);
                }
            }
        }

        /**
         * @return {@code true} if the process was started or {@code false} if it was not because it was stopped in the meantime
         */
        synchronized boolean startProcess(DataFrameDataExtractorFactory dataExtractorFactory,
                                          DataFrameAnalyticsTask task,
                                          @Nullable BytesReference state) {
            if (task.isStopping()) {
                // The job was stopped before we started the process so no need to start it
                return false;
            }

            dataExtractor.set(dataExtractorFactory.newExtractor(false));
            AnalyticsProcessConfig analyticsProcessConfig =
                createProcessConfig(dataExtractor.get(), dataExtractorFactory.getExtractedFields());
            LOGGER.trace("[{}] creating analytics process with config [{}]", config.getId(), Strings.toString(analyticsProcessConfig));
            // If we have no rows, that means there is no data so no point in starting the native process
            // just finish the task
            if (analyticsProcessConfig.rows() == 0) {
                LOGGER.info("[{}] no data found to analyze. Will not start analytics native process.", config.getId());
                return false;
            }
            process.set(createProcess(task, config, analyticsProcessConfig, state));
            resultProcessor.set(createResultProcessor(task, dataExtractorFactory));
            return true;
        }

        private AnalyticsProcessConfig createProcessConfig(DataFrameDataExtractor dataExtractor,
                                                           ExtractedFields extractedFields) {
            DataFrameDataExtractor.DataSummary dataSummary = dataExtractor.collectDataSummary();
            Set<String> categoricalFields = dataExtractor.getCategoricalFields(config.getAnalysis());
            return new AnalyticsProcessConfig(
                config.getId(),
                dataSummary.rows,
                dataSummary.cols,
                config.getModelMemoryLimit(),
                1,
                config.getDest().getResultsField(),
                categoricalFields,
                config.getAnalysis(),
                extractedFields);
        }

        private AnalyticsResultProcessor createResultProcessor(DataFrameAnalyticsTask task,
                                                               DataFrameDataExtractorFactory dataExtractorFactory) {
            DataFrameRowsJoiner dataFrameRowsJoiner =
                new DataFrameRowsJoiner(config.getId(), client, dataExtractorFactory.newExtractor(true));
            return new AnalyticsResultProcessor(
                config, dataFrameRowsJoiner, task.getProgressTracker(), trainedModelProvider, auditor, dataExtractor.get().getFieldNames());
        }
    }
}

/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.job.process.normalizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.EsRejectedExecutionException;
import org.elasticsearch.core.internal.io.IOUtils;
import org.elasticsearch.env.Environment;
import org.elasticsearch.xpack.core.ml.utils.ExceptionsHelper;
import org.elasticsearch.xpack.ml.MachineLearning;
import org.elasticsearch.xpack.ml.process.NativeController;
import org.elasticsearch.xpack.ml.process.ProcessPipes;
import org.elasticsearch.xpack.ml.utils.NamedPipeHelper;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class NativeNormalizerProcessFactory implements NormalizerProcessFactory {

    private static final Logger LOGGER = LogManager.getLogger(NativeNormalizerProcessFactory.class);
    private static final NamedPipeHelper NAMED_PIPE_HELPER = new NamedPipeHelper();

    private final Environment env;
    private final NativeController nativeController;
    private volatile Duration processConnectTimeout;

    public NativeNormalizerProcessFactory(Environment env, NativeController nativeController, ClusterService clusterService) {
        this.env = Objects.requireNonNull(env);
        this.nativeController = Objects.requireNonNull(nativeController);
        setProcessConnectTimeout(MachineLearning.PROCESS_CONNECT_TIMEOUT.get(env.settings()));
        clusterService.getClusterSettings().addSettingsUpdateConsumer(MachineLearning.PROCESS_CONNECT_TIMEOUT,
            this::setProcessConnectTimeout);
    }

    void setProcessConnectTimeout(TimeValue processConnectTimeout) {
        this.processConnectTimeout = Duration.ofMillis(processConnectTimeout.getMillis());
    }

    @Override
    public NormalizerProcess createNormalizerProcess(String jobId, String quantilesState, Integer bucketSpan,
                                                     ExecutorService executorService) {
        ProcessPipes processPipes = new ProcessPipes(env, NAMED_PIPE_HELPER, NormalizerBuilder.NORMALIZE, jobId,
                true, false, true, true, false, false);
        createNativeProcess(jobId, quantilesState, processPipes, bucketSpan);

        NativeNormalizerProcess normalizerProcess = new NativeNormalizerProcess(jobId, processPipes.getLogStream().get(),
                processPipes.getProcessInStream().get(), processPipes.getProcessOutStream().get(), processConnectTimeout);

        try {
            normalizerProcess.start(executorService);
            return normalizerProcess;
        } catch (EsRejectedExecutionException e) {
            try {
                IOUtils.close(normalizerProcess);
            } catch (IOException ioe) {
                LOGGER.error("Can't close normalizer", ioe);
            }
            throw e;
        }
    }

    private void createNativeProcess(String jobId, String quantilesState, ProcessPipes processPipes, Integer bucketSpan) {

        try {
            List<String> command = new NormalizerBuilder(env, jobId, quantilesState, bucketSpan).build();
            processPipes.addArgs(command);
            nativeController.startProcess(command);
            processPipes.connectStreams(processConnectTimeout);
        } catch (IOException e) {
            String msg = "Failed to launch normalizer for job " + jobId;
            LOGGER.error(msg);
            throw ExceptionsHelper.serverError(msg, e);
        }
    }
}


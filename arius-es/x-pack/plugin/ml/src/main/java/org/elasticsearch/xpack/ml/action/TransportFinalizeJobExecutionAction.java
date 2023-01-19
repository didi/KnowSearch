/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.action.update.UpdateAction;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.xpack.core.ml.action.FinalizeJobExecutionAction;
import org.elasticsearch.xpack.core.ml.job.config.Job;
import org.elasticsearch.xpack.core.ml.job.persistence.AnomalyDetectorsIndex;
import org.elasticsearch.xpack.ml.MachineLearning;
import org.elasticsearch.xpack.ml.utils.VoidChainTaskExecutor;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.elasticsearch.xpack.core.ClientHelper.ML_ORIGIN;
import static org.elasticsearch.xpack.core.ClientHelper.executeAsyncWithOrigin;

public class TransportFinalizeJobExecutionAction extends TransportMasterNodeAction<FinalizeJobExecutionAction.Request,
    AcknowledgedResponse> {

    private static final Logger logger = LogManager.getLogger(TransportFinalizeJobExecutionAction.class);

    private final Client client;

    @Inject
    public TransportFinalizeJobExecutionAction(TransportService transportService, ClusterService clusterService, ThreadPool threadPool,
                                               ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver,
                                               Client client) {
        super(FinalizeJobExecutionAction.NAME, transportService, clusterService, threadPool, actionFilters,
                FinalizeJobExecutionAction.Request::new, indexNameExpressionResolver);
        this.client = client;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.SAME;
    }

    @Override
    protected AcknowledgedResponse read(StreamInput in) throws IOException {
        return new AcknowledgedResponse(in);
    }

    @Override
    protected void masterOperation(FinalizeJobExecutionAction.Request request, ClusterState state,
                                   ActionListener<AcknowledgedResponse> listener) {
        String jobIdString = String.join(",", request.getJobIds());
        logger.debug("finalizing jobs [{}]", jobIdString);

        VoidChainTaskExecutor voidChainTaskExecutor = new VoidChainTaskExecutor(threadPool.executor(
                MachineLearning.UTILITY_THREAD_POOL_NAME), true);

        Map<String, Object> update = Collections.singletonMap(Job.FINISHED_TIME.getPreferredName(), new Date());

        for (String jobId: request.getJobIds()) {
            UpdateRequest updateRequest = new UpdateRequest(AnomalyDetectorsIndex.configIndexName(), Job.documentId(jobId));
            updateRequest.retryOnConflict(3);
            updateRequest.doc(update);
            updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

            voidChainTaskExecutor.add(chainedListener -> {
                executeAsyncWithOrigin(client, ML_ORIGIN, UpdateAction.INSTANCE, updateRequest, ActionListener.wrap(
                        updateResponse -> chainedListener.onResponse(null),
                        chainedListener::onFailure
                ));
            });
        }

        voidChainTaskExecutor.execute(ActionListener.wrap(
                aVoids ->  {
                    logger.debug("finalized job [{}]", jobIdString);
                    listener.onResponse(new AcknowledgedResponse(true));
                },
                listener::onFailure
        ));
    }

    @Override
    protected ClusterBlockException checkBlock(FinalizeJobExecutionAction.Request request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_WRITE);
    }
}

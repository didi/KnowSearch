/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.ilm.action;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.cluster.AckedClusterStateUpdateTask;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.xpack.core.ilm.OperationMode;
import org.elasticsearch.xpack.core.ilm.StopILMRequest;
import org.elasticsearch.xpack.core.ilm.action.StopILMAction;
import org.elasticsearch.xpack.ilm.OperationModeUpdateTask;

import java.io.IOException;

public class TransportStopILMAction extends TransportMasterNodeAction<StopILMRequest, AcknowledgedResponse> {

    @Inject
    public TransportStopILMAction(TransportService transportService, ClusterService clusterService, ThreadPool threadPool,
                                  ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver) {
        super(StopILMAction.NAME, transportService, clusterService, threadPool, actionFilters, StopILMRequest::new,
            indexNameExpressionResolver);
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
    protected void masterOperation(StopILMRequest request, ClusterState state, ActionListener<AcknowledgedResponse> listener) {
        clusterService.submitStateUpdateTask("ilm_operation_mode_update",
                new AckedClusterStateUpdateTask<AcknowledgedResponse>(request, listener) {
                    @Override
                    public String taskType() {
                        return "ilm_operation_mode_update";
                    }

                    @Override
                public ClusterState execute(ClusterState currentState) {
                        return (OperationModeUpdateTask.ilmMode(OperationMode.STOPPING)).execute(currentState);
                }

                @Override
                    protected AcknowledgedResponse newResponse(boolean acknowledged) {
                        return new AcknowledgedResponse(acknowledged);
                }
            });
    }

    @Override
    protected ClusterBlockException checkBlock(StopILMRequest request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_WRITE);
    }
}

package org.elasticsearch.dcdr.action;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.cluster.AckedClusterStateUpdateTask;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.translog.primary.DCDRIndexMetadata;
import org.elasticsearch.dcdr.translog.primary.DCDRMetadata;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * author weizijun
 * date：2019-08-27
 */
public class TransportDeleteReplicationAction extends TransportMasterNodeAction<DeleteReplicationAction.Request, AcknowledgedResponse> {
    @Inject
    public TransportDeleteReplicationAction(
        Settings settings, TransportService transportService, ClusterService clusterService,
        ThreadPool threadPool, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver
    ) {
        super(
            DeleteReplicationAction.NAME,
            transportService,
            clusterService,
            threadPool,
            actionFilters,
            DeleteReplicationAction.Request::new,
            indexNameExpressionResolver
        );
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.SAME;
    }

    @Override
    protected AcknowledgedResponse read(StreamInput in) throws IOException {
        return new AcknowledgedResponse(in.readBoolean());
    }

    @Override
    protected void masterOperation(
        DeleteReplicationAction.Request request,
        ClusterState state,
        ActionListener<AcknowledgedResponse> listener
    ) throws Exception {
        clusterService.submitStateUpdateTask(
            "delete-replication-" + request.getPrimaryIndex(),
            new AckedClusterStateUpdateTask<AcknowledgedResponse>(Priority.URGENT, request, listener) {
                @Override
                public String taskType() {
                    return "delete-replication";
                }

                @Override
                public ClusterState execute(ClusterState currentState) throws Exception {
                    ClusterState.Builder newState = ClusterState.builder(currentState);
                    DCDRMetadata dcdrMetadata = currentState.metaData().custom(DCDRMetadata.TYPE);
                    if (dcdrMetadata == null) {
                        dcdrMetadata = DCDRMetadata.EMPTY;
                    }

                    SortedMap<String, DCDRIndexMetadata> replicaIndices = new TreeMap<>(dcdrMetadata.getReplicaIndices());
                    replicaIndices.remove(
                        DCDRIndexMetadata.name(
                            request.getPrimaryIndex(),
                            request.getReplicaIndex(),
                            request.getReplicaCluster()
                        )
                    );

                    DCDRMetadata newMetadata = new DCDRMetadata(replicaIndices, dcdrMetadata.getReplicaTemplates());
                    newState.metaData(
                        MetaData.builder(currentState.getMetaData())
                            .putCustom(DCDRMetadata.TYPE, newMetadata)
                            .build()
                    );
                    return newState.build();
                }

                @Override
                protected AcknowledgedResponse newResponse(boolean acknowledged) {
                    return new AcknowledgedResponse(acknowledged);
                }
            }
        );
    }

    @Override
    protected ClusterBlockException checkBlock(DeleteReplicationAction.Request request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_WRITE);
    }
}

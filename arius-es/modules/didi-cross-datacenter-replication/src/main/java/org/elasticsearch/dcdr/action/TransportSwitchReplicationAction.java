package org.elasticsearch.dcdr.action;

import java.io.IOException;
import java.util.Map;
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
 * author zhz
 * date：2019-08-27
 */
public class TransportSwitchReplicationAction extends TransportMasterNodeAction<SwitchReplicationAction.Request, AcknowledgedResponse> {
    @Inject
    public TransportSwitchReplicationAction(
        Settings settings, TransportService transportService, ClusterService clusterService,
        ThreadPool threadPool, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver
    ) {
        super(
            SwitchReplicationAction.NAME,
            transportService,
            clusterService,
            threadPool,
            actionFilters,
            SwitchReplicationAction.Request::new,
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
        SwitchReplicationAction.Request request,
        ClusterState state,
        ActionListener<AcknowledgedResponse> listener
    ) throws Exception {

        // 校验索引是否配置了DCDR链路
        DCDRMetadata dcdrMetadata = state.metaData().custom(DCDRMetadata.TYPE);
        if (dcdrMetadata == null) {
            listener.onFailure(new IllegalArgumentException("no replication config for [" + request.getPrimaryIndex() + "]"));
            return;
        }

        Map<String, DCDRIndexMetadata> dcdrIndexMetadataMap = dcdrMetadata.getReplicaIndices();
        if (dcdrIndexMetadataMap == null) {
            listener.onFailure(new IllegalArgumentException("no replication config for [" + request.getPrimaryIndex() + "]"));
            return;
        }

        String name = DCDRIndexMetadata.name(request.getPrimaryIndex(), request.getReplicaIndex(), request.getReplicaCluster());
        if (!dcdrIndexMetadataMap.containsKey(name)) {
            listener.onFailure(new IllegalArgumentException("no replication config for [" + name + "]"));
            return;
        }

        clusterService.submitStateUpdateTask(
            "switch-replication-" + request.getPrimaryIndex(),
            new AckedClusterStateUpdateTask<AcknowledgedResponse>(Priority.URGENT, request, listener) {
                @Override
                public String taskType() {
                    return "switch-replication";
                }

                @Override
                public ClusterState execute(ClusterState currentState) throws Exception {
                    ClusterState.Builder newState = ClusterState.builder(currentState);
                    DCDRMetadata dcdrMetadata = currentState.metaData().custom(DCDRMetadata.TYPE);
                    if (dcdrMetadata == null) {
                        dcdrMetadata = DCDRMetadata.EMPTY;
                    }

                    Map<String, DCDRIndexMetadata> replicaIndices = new TreeMap<>(dcdrMetadata.getReplicaIndices());

                    // 修改链路state
                    DCDRIndexMetadata indexMetadataNew = new DCDRIndexMetadata(
                        replicaIndices.get(name).getPrimaryIndex(),
                        replicaIndices.get(name).getReplicaIndex(),
                        replicaIndices.get(name).getReplicaCluster(),
                        request.getReplicationState()
                    );

                    replicaIndices.put(name, indexMetadataNew);

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
    protected ClusterBlockException checkBlock(SwitchReplicationAction.Request request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_WRITE);
    }
}

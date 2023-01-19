package org.elasticsearch.dcdr.action;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.AckedClusterStateUpdateTask;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.translog.primary.DCDRMetadata;
import org.elasticsearch.dcdr.translog.primary.DCDRTemplateMetadata;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import com.carrotsearch.hppc.ObjectHashSet;
import com.carrotsearch.hppc.ObjectLookupContainer;

/**
 * author weizijun
 * date：2019-08-12
 */
public class TransportCreateAutoReplicationAction extends
    TransportMasterNodeAction<CreateAutoReplicationAction.Request, AcknowledgedResponse> {

    @Inject
    public TransportCreateAutoReplicationAction(
        Client client, IndicesService indicesService, Settings settings, TransportService transportService, ClusterService clusterService,
        ThreadPool threadPool, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver
    ) {
        super(
            CreateAutoReplicationAction.NAME,
            transportService,
            clusterService,
            threadPool,
            actionFilters,
            CreateAutoReplicationAction.Request::new,
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
        CreateAutoReplicationAction.Request request,
        ClusterState state,
        ActionListener<AcknowledgedResponse> listener
    ) throws Exception {

        // 校验索引是否配置了DCDR链路
        ImmutableOpenMap<String, IndexTemplateMetaData> templates = state.metaData().templates();
        ObjectLookupContainer<String> templateNames = templates != null ? templates.keys() : new ObjectHashSet<>();
        if (!templateNames.contains(request.getTemplate())) {
            listener.onFailure(new IllegalArgumentException("template not exist [" + request.getTemplate() + "]"));
            return;
        }

        clusterService.submitStateUpdateTask(
            "create-auto-replication-" + request.getName(),
            new AckedClusterStateUpdateTask<AcknowledgedResponse>(Priority.URGENT, request, listener) {
                @Override
                public String taskType() {
                    return "create-auto-replication";
                }

                @Override
                public ClusterState execute(ClusterState currentState) throws Exception {
                    ClusterState.Builder newState = ClusterState.builder(currentState);
                    DCDRMetadata dcdrMetadata = currentState.metaData().custom(DCDRMetadata.TYPE);
                    if (dcdrMetadata == null) {
                        dcdrMetadata = DCDRMetadata.EMPTY;
                    }

                    SortedMap<String, DCDRTemplateMetadata> replicaTemplates = new TreeMap<>(dcdrMetadata.getReplicaTemplates());

                    DCDRTemplateMetadata dcdrTemplateMetadata = new DCDRTemplateMetadata(
                        request.getName(),
                        request.getTemplate(),
                        request.getReplicaCluster()
                    );
                    replicaTemplates.put(request.getName(), dcdrTemplateMetadata);

                    DCDRMetadata newMetadata = new DCDRMetadata(dcdrMetadata.getReplicaIndices(), replicaTemplates);
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
    protected ClusterBlockException checkBlock(CreateAutoReplicationAction.Request request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_WRITE);
    }
}

package org.elasticsearch.dcdr.action;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.translog.primary.DCDRMetadata;
import org.elasticsearch.dcdr.translog.primary.DCDRTemplateMetadata;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * author weizijun
 * date：2019-08-27
 */
public class TransportGetAutoReplicationAction extends
    TransportMasterNodeAction<GetAutoReplicationAction.Request, GetAutoReplicationAction.Response> {
    @Inject
    public TransportGetAutoReplicationAction(
        Settings settings, TransportService transportService, ClusterService clusterService,
        ThreadPool threadPool, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver
    ) {
        super(
            GetAutoReplicationAction.NAME,
            transportService,
            clusterService,
            threadPool,
            actionFilters,
            GetAutoReplicationAction.Request::new,
            indexNameExpressionResolver
        );
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.SAME;
    }

    @Override
    protected GetAutoReplicationAction.Response read(StreamInput in) throws IOException {
        return new GetAutoReplicationAction.Response(in);
    }

    @Override
    protected void masterOperation(
        GetAutoReplicationAction.Request request,
        ClusterState state,
        ActionListener<GetAutoReplicationAction.Response> listener
    ) throws Exception {
        Map<String, DCDRTemplateMetadata> dcdrTemplateMetadatas = getDCDRTemplateMetadata(state.metaData(), request.getName());
        listener.onResponse(new GetAutoReplicationAction.Response(dcdrTemplateMetadatas));

    }

    @Override
    protected ClusterBlockException checkBlock(GetAutoReplicationAction.Request request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_READ);
    }

    private Map<String, DCDRTemplateMetadata> getDCDRTemplateMetadata(MetaData metaData, String name) {
        DCDRMetadata dcdrMetadata = metaData.custom(DCDRMetadata.TYPE);
        if (dcdrMetadata == null) {
            return Collections.emptyMap();
        }

        Map<String, DCDRTemplateMetadata> dcdrTemplateMetadatas = dcdrMetadata.getReplicaTemplates();

        if (dcdrTemplateMetadatas == null) {
            return Collections.emptyMap();
        }

        if (name == null) {
            return dcdrTemplateMetadatas;
        }

        DCDRTemplateMetadata dcdrTemplateMetadata = dcdrTemplateMetadatas.get(name);
        if (dcdrTemplateMetadata == null) {
            return Collections.emptyMap();
        }

        return Collections.singletonMap(name, dcdrTemplateMetadata);
    }
}

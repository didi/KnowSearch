package org.elasticsearch.dcdr.action;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.elasticsearch.ResourceNotFoundException;
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
import org.elasticsearch.dcdr.translog.primary.DCDRIndexMetadata;
import org.elasticsearch.dcdr.translog.primary.DCDRMetadata;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * author weizijun
 * date：2019-08-27
 */
public class TransportGetReplicationAction extends
    TransportMasterNodeAction<GetReplicationAction.Request, GetReplicationAction.Response> {
    @Inject
    public TransportGetReplicationAction(
        Settings settings, TransportService transportService, ClusterService clusterService,
        ThreadPool threadPool, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver
    ) {
        super(
            GetReplicationAction.NAME,
            transportService,
            clusterService,
            threadPool,
            actionFilters,
            GetReplicationAction.Request::new,
            indexNameExpressionResolver
        );
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.SAME;
    }

    @Override
    protected GetReplicationAction.Response read(StreamInput in) throws IOException {
        return new GetReplicationAction.Response(in);
    }

    @Override
    protected void masterOperation(
        GetReplicationAction.Request request,
        ClusterState state,
        ActionListener<GetReplicationAction.Response> listener
    ) throws Exception {
        Map<String, DCDRIndexMetadata> dcdrIndexMetadatas = getDCDRIndexMetadata(state.metaData(), request.getPrimaryIndex());
        listener.onResponse(new GetReplicationAction.Response(dcdrIndexMetadatas));

    }

    @Override
    protected ClusterBlockException checkBlock(GetReplicationAction.Request request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_READ);
    }

    private Map<String, DCDRIndexMetadata> getDCDRIndexMetadata(MetaData metaData, String primaryIndex) {
        DCDRMetadata dcdrMetadata = metaData.custom(DCDRMetadata.TYPE);
        if (dcdrMetadata == null) {
            return Collections.emptyMap();
        }

        Map<String, DCDRIndexMetadata> dcdrIndexMetadatas = dcdrMetadata.getReplicaIndices();

        if (dcdrIndexMetadatas == null) {
            if (primaryIndex == null) {
                return Collections.emptyMap();
            }
        }

        if (primaryIndex == null) {
            return dcdrIndexMetadatas;
        }

        Map<String, DCDRIndexMetadata> result = new TreeMap<>();
        for (Map.Entry<String, DCDRIndexMetadata> entry : dcdrIndexMetadatas.entrySet()) {
            if (entry.getValue().getPrimaryIndex().equals(primaryIndex)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}

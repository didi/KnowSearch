package org.elasticsearch.dcdr.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.node.TransportBroadcastByNodeAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.routing.PlainShardsIterator;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardsIterator;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.dcdr.translog.primary.*;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * author weizijun
 * date：2019-09-25
 */
public class TransportReplicationStatsAction extends
    TransportBroadcastByNodeAction<ReplicationStatsAction.Request, ReplicationStatsAction.Response, CompositeDCDRStats> {

    private final ClusterService clusterService;

    private ReplicationService replicationService;

    @Inject
    public TransportReplicationStatsAction(
        ClusterService clusterService,
        TransportService transportService, IndicesService indicesService,
        ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver, ReplicationService replicationService
    ) {
        super(
            ReplicationStatsAction.NAME,
            clusterService,
            transportService,
            actionFilters,
            indexNameExpressionResolver,
            ReplicationStatsAction.Request::new,
            ThreadPool.Names.MANAGEMENT
        );
        this.clusterService = clusterService;
        this.replicationService = replicationService;
    }

    /**
     * Status goes across *all* shards.
     */
    @Override
    protected ShardsIterator shards(ClusterState clusterState, ReplicationStatsAction.Request request, String[] concreteIndices) {
        List<ShardRouting> allShards = new ArrayList<>();

        // 找到索引配置了dcdr链路的索引
        DCDRMetadata dcdrMetadata = clusterState.metaData().custom(DCDRMetadata.TYPE);
        if (dcdrMetadata != null) {
            Map<String, DCDRIndexMetadata> replicaIndices = dcdrMetadata.getReplicaIndices();
            if (replicaIndices != null && !replicaIndices.isEmpty()) {
                Set<String> dcdrIndices = replicaIndices.values()
                    .stream()
                    .map(DCDRIndexMetadata::getPrimaryIndex)
                    .collect(Collectors.toSet());
                for (String index : concreteIndices) {
                    if (dcdrIndices.contains(index)) {
                        allShards.addAll(clusterState.routingTable().allShards(index));
                    }
                }
            }
        }

        return new PlainShardsIterator(allShards.stream().filter(ShardRouting::primary).collect(Collectors.toList()));
    }

    @Override
    protected ClusterBlockException checkGlobalBlock(ClusterState state, ReplicationStatsAction.Request request) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_READ);
    }

    @Override
    protected ClusterBlockException checkRequestBlock(
        ClusterState state,
        ReplicationStatsAction.Request request,
        String[] concreteIndices
    ) {
        return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA_READ, concreteIndices);
    }

    @Override
    protected CompositeDCDRStats readShardResult(StreamInput in) throws IOException {
        CompositeDCDRStats compositeDCDRStats = new CompositeDCDRStats();
        compositeDCDRStats.readFrom(in);
        return compositeDCDRStats;
    }

    @Override
    protected ReplicationStatsAction.Response newResponse(
        ReplicationStatsAction.Request request,
        int totalShards,
        int successfulShards,
        int failedShards,
        List<CompositeDCDRStats> responses,
        List<DefaultShardOperationFailedException> shardFailures,
        ClusterState clusterState
    ) {
        return new ReplicationStatsAction.Response(responses);
    }

    @Override
    protected ReplicationStatsAction.Request readRequestFrom(StreamInput in) throws IOException {
        ReplicationStatsAction.Request request = new ReplicationStatsAction.Request(in);
        return request;
    }

    @Override
    protected CompositeDCDRStats shardOperation(ReplicationStatsAction.Request request, ShardRouting shardRouting) {
        final ClusterState state = clusterService.state();

        CompositeDCDRStats compositeDCDRStats = new CompositeDCDRStats();
        compositeDCDRStats.setPrimaryIndex(shardRouting.getIndexName());
        compositeDCDRStats.setShardId(shardRouting.getId());

        DCDRMetadata dcdrMetadata = state.metaData().custom(DCDRMetadata.TYPE);
        if (dcdrMetadata == null) {
            return compositeDCDRStats;
        }

        Map<String, DCDRIndexMetadata> dcdrIndexMetadataMap = dcdrMetadata.getReplicaIndices();
        if (dcdrIndexMetadataMap == null) {
            return compositeDCDRStats;
        }

        List<DCDRIndexMetadata> matchedDCDRIndexMetadata = new ArrayList<>();
        for (DCDRIndexMetadata dcdrIndexMetadata : dcdrIndexMetadataMap.values()) {
            if (shardRouting.getIndexName().equals(dcdrIndexMetadata.getPrimaryIndex())) {
                matchedDCDRIndexMetadata.add(dcdrIndexMetadata);
            }
        }

        if (matchedDCDRIndexMetadata.isEmpty()) {
            return compositeDCDRStats;
        }

        List<DCDRStats> dcdrStatsList = new ArrayList<>();

        for (DCDRIndexMetadata dcdrIndexMetadata : matchedDCDRIndexMetadata) {
            ReplicationIndexService replicationIndexService = replicationService.getReplicationIndexService(dcdrIndexMetadata);
            if (replicationIndexService == null) {
                continue;
            }

            ReplicationShardService replicationShardService = replicationIndexService.getReplicationShardService(shardRouting.shardId());
            if (replicationShardService == null) {
                continue;
            }

            dcdrStatsList.add(replicationShardService.getDCDRStats());
        }

        compositeDCDRStats.setDcdrStatsList(dcdrStatsList);

        return compositeDCDRStats;

    }
}

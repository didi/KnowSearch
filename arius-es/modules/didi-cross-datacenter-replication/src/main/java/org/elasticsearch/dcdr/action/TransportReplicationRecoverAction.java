package org.elasticsearch.dcdr.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastResponse;
import org.elasticsearch.action.support.broadcast.node.TransportBroadcastByNodeAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.routing.*;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.indices.recovery.RecoverCase;
import org.elasticsearch.dcdr.translog.primary.*;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * author weizijun
 * date：2019-10-24
 */
public class TransportReplicationRecoverAction extends
    TransportBroadcastByNodeAction<ReplicationRecoverAction.Request, BroadcastResponse, TransportBroadcastByNodeAction.EmptyResult> {

    private final ClusterService clusterService;

    private ReplicationService replicationService;

    @Inject
    public TransportReplicationRecoverAction(
        Settings settings, ThreadPool threadPool, ClusterService clusterService,
        TransportService transportService,
        ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver, ReplicationService replicationService
    ) {
        super(
            ReplicationRecoverAction.NAME,
            clusterService,
            transportService,
            actionFilters,
            indexNameExpressionResolver,
            ReplicationRecoverAction.Request::new,
            ThreadPool.Names.MANAGEMENT
        );
        this.clusterService = clusterService;
        this.replicationService = replicationService;
    }

    /**
     * Status goes across *all* shards.
     */
    @Override
    protected ShardsIterator shards(ClusterState clusterState, ReplicationRecoverAction.Request request, String[] concreteIndices) {
        List<ShardRouting> allShards = new ArrayList<>();
        for (String index : concreteIndices) {
            if (request.getShardNum() >= 0) {
                IndexRoutingTable indexRoutingTable = clusterState.routingTable().index(index);
                if (indexRoutingTable == null) {
                    continue;
                }

                IndexShardRoutingTable indexShardRoutingTable = indexRoutingTable.shard(request.getShardNum());
                if (indexShardRoutingTable == null) {
                    continue;
                }

                allShards.add(indexShardRoutingTable.primaryShard());
            } else {
                allShards.addAll(clusterState.routingTable().allShards(index));
            }
        }
        return new PlainShardsIterator(allShards.stream().filter(ShardRouting::primary).collect(Collectors.toList()));
    }

    @Override
    protected ClusterBlockException checkGlobalBlock(ClusterState state, ReplicationRecoverAction.Request request) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_WRITE);
    }

    @Override
    protected ClusterBlockException checkRequestBlock(
        ClusterState state,
        ReplicationRecoverAction.Request request,
        String[] concreteIndices
    ) {
        return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA_WRITE, concreteIndices);
    }

    @Override
    protected TransportBroadcastByNodeAction.EmptyResult readShardResult(StreamInput in) throws IOException {
        return EmptyResult.readEmptyResultFrom(in);
    }

    @Override
    protected BroadcastResponse newResponse(
        ReplicationRecoverAction.Request request,
        int totalShards,
        int successfulShards,
        int failedShards,
        List<TransportBroadcastByNodeAction.EmptyResult> responses,
        List<DefaultShardOperationFailedException> shardFailures,
        ClusterState clusterState
    ) {
        return new BroadcastResponse(totalShards, successfulShards, failedShards, shardFailures);
    }

    @Override
    protected ReplicationRecoverAction.Request readRequestFrom(StreamInput in) throws IOException {
        ReplicationRecoverAction.Request request = new ReplicationRecoverAction.Request(in);
        return request;
    }

    @Override
    protected TransportBroadcastByNodeAction.EmptyResult shardOperation(
        ReplicationRecoverAction.Request request,
        ShardRouting shardRouting
    ) {
        final ClusterState state = clusterService.state();

        DCDRMetadata dcdrMetadata = state.metaData().custom(DCDRMetadata.TYPE);
        if (dcdrMetadata == null) {
            return EmptyResult.INSTANCE;
        }

        Map<String, DCDRIndexMetadata> dcdrIndexMetadataMap = dcdrMetadata.getReplicaIndices();
        if (dcdrIndexMetadataMap == null) {
            return EmptyResult.INSTANCE;
        }

        List<DCDRIndexMetadata> matchedDCDRIndexMetadata = new ArrayList<>();
        for (DCDRIndexMetadata dcdrIndexMetadata : dcdrIndexMetadataMap.values()) {
            if (shardRouting.getIndexName().equals(dcdrIndexMetadata.getPrimaryIndex())) {
                matchedDCDRIndexMetadata.add(dcdrIndexMetadata);
            }
        }

        if (matchedDCDRIndexMetadata.isEmpty()) {
            return EmptyResult.INSTANCE;
        }

        for (DCDRIndexMetadata dcdrIndexMetadata : matchedDCDRIndexMetadata) {
            ReplicationIndexService replicationIndexService = replicationService.getReplicationIndexService(dcdrIndexMetadata);
            if (replicationIndexService == null) {
                continue;
            }

            ReplicationShardService replicationShardService = replicationIndexService.getReplicationShardService(shardRouting.shardId());
            if (replicationShardService == null) {
                continue;
            }

            replicationShardService.doRecover(RecoverCase.Outter);
        }

        return EmptyResult.INSTANCE;
    }
}

package org.elasticsearch.dcdr.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.node.TransportBroadcastByNodeAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.PlainShardsIterator;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardsIterator;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.DCDRShardInfo;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * author weizijun
 * date：2019-12-09
 */
public class TransportFetchShardInfoAction extends
    TransportBroadcastByNodeAction<FetchShardInfoAction.Request, FetchShardInfoAction.Response, DCDRShardInfo> {

    private final ClusterService clusterService;
    private final IndicesService indicesService;

    @Inject
    public TransportFetchShardInfoAction(
        Settings settings, ThreadPool threadPool, ClusterService clusterService,
        TransportService transportService, IndicesService indicesService,
        ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver
    ) {
        super(
            FetchShardInfoAction.NAME,
            clusterService,
            transportService,
            actionFilters,
            indexNameExpressionResolver,
            FetchShardInfoAction.Request::new,
            ThreadPool.Names.MANAGEMENT
        );
        this.clusterService = clusterService;
        this.indicesService = indicesService;
    }

    @Override
    protected DCDRShardInfo readShardResult(StreamInput in) throws IOException {
        return new DCDRShardInfo(in);
    }

    @Override
    protected FetchShardInfoAction.Response newResponse(
        FetchShardInfoAction.Request request,
        int totalShards,
        int successfulShards,
        int failedShards,
        List<DCDRShardInfo> dcdrShardInfos,
        List<DefaultShardOperationFailedException> shardFailures,
        ClusterState clusterState
    ) {

        if (dcdrShardInfos.size() != 1) {
            throw new ElasticsearchException("fetch shard response number exception,size=" + dcdrShardInfos.size());
        }

        return new FetchShardInfoAction.Response(dcdrShardInfos.get(0));
    }

    @Override
    protected FetchShardInfoAction.Request readRequestFrom(StreamInput in) throws IOException {
        FetchShardInfoAction.Request request = new FetchShardInfoAction.Request(in);
        return request;
    }

    @Override
    protected DCDRShardInfo shardOperation(FetchShardInfoAction.Request request, ShardRouting shardRouting) throws IOException {
        IndexService indexService = indicesService.indexServiceSafe(shardRouting.shardId().getIndex());
        IndexShard indexShard = indexService.getShard(shardRouting.shardId().id());

        DiscoveryNode discoveryNode = clusterService.state().nodes().get(shardRouting.currentNodeId());

        String historyUUID = "";
        long checkPoint = -1;

        if (indexShard.getEngineOrNull() == null) {
            return new DCDRShardInfo(discoveryNode, historyUUID, checkPoint, shardRouting.shardId(), null, null);
        }

        try {
            if (indexShard.commitStats() != null && indexShard.commitStats().getUserData() != null) {
                historyUUID = indexShard.commitStats().getUserData().get("history_uuid");
            }

            if (indexShard.seqNoStats() != null) {
                checkPoint = indexShard.seqNoStats().getLocalCheckpoint();
            }

            return new DCDRShardInfo(
                discoveryNode,
                historyUUID,
                checkPoint,
                shardRouting.shardId(),
                indexShard.commitStats(),
                indexShard.seqNoStats()
            );
        } catch (ElasticsearchException e) {
            logger.warn("fetch exception, shard=" + shardRouting.shardId(), e);
            return new DCDRShardInfo(discoveryNode, historyUUID, checkPoint, shardRouting.shardId(), null, null);
        }
    }

    @Override
    protected ShardsIterator shards(ClusterState clusterState, FetchShardInfoAction.Request request, String[] concreteIndices) {
        ShardsIterator shardsIterator = clusterState.routingTable().allShards(concreteIndices);
        List<ShardRouting> shards = new ArrayList<>();
        for (ShardRouting shardRouting : shardsIterator) {
            if (request.getShardNum() >= 0 && shardRouting.shardId().id() != request.getShardNum()) {
                continue;
            }

            if (shardRouting.primary()) {
                shards.add(shardRouting);
            }
        }

        if (shards.size() != 1) {
            throw new ElasticsearchException("fetch shard number exception,size=" + shards.size());
        }

        return new PlainShardsIterator(shards);
    }

    @Override
    protected ClusterBlockException checkGlobalBlock(ClusterState state, FetchShardInfoAction.Request request) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_READ);
    }

    @Override
    protected ClusterBlockException checkRequestBlock(ClusterState state, FetchShardInfoAction.Request request, String[] concreteIndices) {
        return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA_READ, concreteIndices);
    }
}

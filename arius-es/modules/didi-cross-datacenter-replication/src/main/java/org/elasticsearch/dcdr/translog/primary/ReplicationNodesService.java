package org.elasticsearch.dcdr.translog.primary;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.dcdr.indices.recovery.PeerRecoverySourceService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.translog.TranslogReaderProxy;
import org.elasticsearch.threadpool.ThreadPool;

/**
 * author weizijun
 * date：2019-10-21
 */
public class ReplicationNodesService {
    private Map<ShardId, ShardService> linkService = new HashMap<>();

    private final Client client;
    private final ThreadPool threadPool;
    private final PeerRecoverySourceService peerRecoverySourceService;

    public ReplicationNodesService(
        Client client, ThreadPool threadPool,
        PeerRecoverySourceService peerRecoverySourceService
    ) {
        this.client = client;
        this.threadPool = threadPool;
        this.peerRecoverySourceService = peerRecoverySourceService;
    }

    public ReplicationShardService addLink(
        ShardId shardId,
        DCDRIndexMetadata dcdrIndexMetadata,
        TranslogReaderProxy translogReaderProxy,
        IndexShard indexShard
    ) {
        ShardService shardService = linkService.get(shardId);
        if (shardService == null) {
            shardService = new ShardService(client, shardId, threadPool, translogReaderProxy, peerRecoverySourceService, indexShard);
            linkService.put(shardId, shardService);
        }

        return shardService.addLink(dcdrIndexMetadata);
    }

    public void deleteLink(ReplicationShardService link) {
        ShardService shardService = linkService.get(link.getShardId());
        if (shardService != null) {
            shardService.removeLink(link);
            if (shardService.isEmpty()) {
                linkService.remove(link.getShardId());
            }
        }
    }
}

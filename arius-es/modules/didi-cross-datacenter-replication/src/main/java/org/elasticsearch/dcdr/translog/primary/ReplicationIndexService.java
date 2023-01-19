package org.elasticsearch.dcdr.translog.primary;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.engine.InternalEngine;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.translog.TranslogReaderProxy;
import org.elasticsearch.indices.IndicesService;

/**
 * author weizijun
 * date：2019-10-14
 */
public class ReplicationIndexService implements Closeable {
    private static final Logger logger = LogManager.getLogger(ReplicationIndexService.class);

    private final DCDRIndexMetadata dcdrIndexMetadata;
    private final IndicesService indicesService;
    private final ReplicationNodesService replicationNodesService;

    private final Map<ShardId, ReplicationShardService> replicationShardServices = new HashMap<>();

    public ReplicationIndexService(
        DCDRIndexMetadata dcdrIndexMetadata, IndicesService indicesService,
        ReplicationNodesService replicationNodesService
    ) {
        this.dcdrIndexMetadata = dcdrIndexMetadata;
        this.indicesService = indicesService;
        this.replicationNodesService = replicationNodesService;
    }

    public void resetReplicationState(boolean state) {
        this.dcdrIndexMetadata.setReplicationState(state);
        replicationShardServices.forEach(
            (shardId, service) -> service.resetReplicationState(state)
        );
    }

    public ReplicationShardService getReplicationShardService(ShardId shardId) {
        if (replicationShardServices.containsKey(shardId)) {
            return replicationShardServices.get(shardId);
        }
        return null;
    }

    public void resetShards(Set<ShardId> shards) {
        List<ShardId> removed = replicationShardServices.entrySet()
            .stream()
            .filter(e -> !shards.contains(e.getKey()))
            .map(e -> e.getKey())
            .collect(Collectors.toList());

        for (ShardId shardId : removed) {
            ReplicationShardService service = replicationShardServices.remove(shardId);
            replicationNodesService.deleteLink(service);
        }

        shards.forEach((shardId -> {
            if (!replicationShardServices.containsKey(shardId)) {
                IndexShard indexShard = indicesService.getShardOrNull(shardId);
                if (indexShard == null) {
                    logger.debug("{}[{}] not ready, indexShard is null", dcdrIndexMetadata, shardId.getId());
                    return;
                }

                InternalEngine engine = (InternalEngine) indexShard.getEngineOrNull();
                if (engine == null) {
                    logger.debug("{}[{}] not ready, engine is null", dcdrIndexMetadata, shardId.getId());
                    return;
                }

                TranslogReaderProxy translogReaderProxy = engine.getTranslog().getTranslogReaderProxy();
                ReplicationShardService link = replicationNodesService.addLink(
                    shardId,
                    dcdrIndexMetadata,
                    translogReaderProxy,
                    indexShard
                );
                replicationShardServices.put(shardId, link);
            }
        }));
    }

    public DCDRIndexMetadata getDcdrIndexMetadata() {
        return dcdrIndexMetadata;
    }

    @Override
    public void close() throws IOException {
        replicationShardServices.forEach(
            (shardId, service) -> {
                replicationNodesService.deleteLink(service);
            }
        );

        replicationShardServices.clear();
    }
}

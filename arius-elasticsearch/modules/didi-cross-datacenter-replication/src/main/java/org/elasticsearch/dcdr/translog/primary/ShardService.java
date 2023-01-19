package org.elasticsearch.dcdr.translog.primary;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.SuppressLoggerChecks;
import org.elasticsearch.dcdr.indices.recovery.PeerRecoverySourceService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.translog.Translog;
import org.elasticsearch.index.translog.TranslogReaderProxy;
import org.elasticsearch.threadpool.ThreadPool;

/**
 * author weizijun
 * date：2019-10-22
 */
@SuppressLoggerChecks(reason = "safely delegates to logger")
public class ShardService {
    private static final Logger logger = LogManager.getLogger(ShardService.class);

    private final ShardId shardId;
    private final Client client;
    private final ThreadPool threadPool;
    private final TranslogReaderProxy translogReaderProxy;
    private final IndexShard indexShard;
    private final PeerRecoverySourceService peerRecoverySourceService;
    private Map<String, ReplicationShardService> linkMap = new HashMap<>();
    private Map<String, Translog.Location> commitOffsets = new HashMap<>();

    public ShardService(
        Client client, ShardId shardId, ThreadPool threadPool, TranslogReaderProxy translogReaderProxy,
        PeerRecoverySourceService peerRecoverySourceService, IndexShard indexShard
    ) {
        this.shardId = shardId;
        this.client = client;
        this.threadPool = threadPool;
        this.translogReaderProxy = translogReaderProxy;
        this.peerRecoverySourceService = peerRecoverySourceService;
        this.indexShard = indexShard;
    }

    public ReplicationShardService addLink(DCDRIndexMetadata dcdrIndexMetadata) {
        ReplicationShardService link = new ReplicationShardService(
            client,
            shardId,
            dcdrIndexMetadata,
            threadPool,
            translogReaderProxy,
            peerRecoverySourceService,
            this,
            indexShard
        );
        linkMap.put(ShardService.name(link.getDcdrIndexMetadata(), link.getShardId()), link);
        translogReaderProxy.startCheckCommitGen();
        return link;
    }

    public void removeLink(ReplicationShardService link) {
        linkMap.remove(ShardService.name(link.getDcdrIndexMetadata(), link.getShardId()));
        if (link != null) {
            try {
                link.close();
            } catch (IOException e) {
                logger.warn("{}[{}] close error", link.getDcdrIndexMetadata(), shardId.getId(), e);
            }
        }

        if (isEmpty()) {
            translogReaderProxy.stopCheckCommitGen();
        }
    }

    public boolean isEmpty() {
        return linkMap.size() == 0;
    }

    public void updateCommitOffset(DCDRIndexMetadata dcdrIndexMetadata, Translog.Location location) {
        commitOffsets.put(ShardService.name(dcdrIndexMetadata, shardId), location);
        Translog.Location min = commitOffsets.entrySet().stream().min(Map.Entry.comparingByValue()).get().getValue();
        if (min.equals(location)) {
            translogReaderProxy.updateCommitOffset(location);
        }
    }

    public static String name(DCDRIndexMetadata dcdrIndexMetadata, ShardId shardId) {
        return String.format(Locale.US, "%s[%d]", dcdrIndexMetadata.name(), shardId.getId());
    }
}

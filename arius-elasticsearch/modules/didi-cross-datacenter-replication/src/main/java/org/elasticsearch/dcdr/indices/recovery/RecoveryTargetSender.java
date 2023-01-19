package org.elasticsearch.dcdr.indices.recovery;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.apache.lucene.store.RateLimiter;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.store.Store;
import org.elasticsearch.index.store.StoreFileMetaData;
import org.elasticsearch.indices.recovery.*;
import org.elasticsearch.transport.EmptyTransportResponseHandler;
import org.elasticsearch.transport.TransportRequestOptions;
import org.elasticsearch.transport.TransportService;

/**
 * author weizijun
 * date：2019-09-06
 */
public class RecoveryTargetSender {
    private final TransportService transportService;
    private long recoveryId;
    private final ShardId shardId;
    private final DiscoveryNode targetNode;
    private final RecoverySettings recoverySettings;

    private final TransportRequestOptions fileChunkRequestOptions;

    private final AtomicLong bytesSinceLastPause = new AtomicLong();

    private final Consumer<Long> onSourceThrottle;

    private final IndexShard indexShard;

    public RecoveryTargetSender(
        ShardId shardId, TransportService transportService,
        DiscoveryNode targetNode, RecoverySettings recoverySettings, Consumer<Long> onSourceThrottle,
        IndexShard indexShard
    ) {
        this.transportService = transportService;
        this.shardId = shardId;
        this.targetNode = targetNode;
        this.recoverySettings = recoverySettings;
        this.onSourceThrottle = onSourceThrottle;
        this.fileChunkRequestOptions = TransportRequestOptions.builder()
            // we are saving the cpu for other things
            .withType(TransportRequestOptions.Type.RECOVERY)
            .withTimeout(recoverySettings.internalActionTimeout())
            .build();

        transportService.connectToNode(targetNode);

        this.indexShard = indexShard;
    }

    public StartRecoveryResponse startRecovery() {
        final StartRecoveryResponse response = transportService.submitRequest(
            targetNode,
            PeerRecoveryTargetService.Actions.START_RECOVERY,
            new StartRecoveryRequest(shardId),
            TransportRequestOptions.builder().withTimeout(recoverySettings.internalActionLongTimeout()).build(),
            StartRecoveryResponse.HANDLER
        ).txGet();
        recoveryId = response.recoveryId();
        return response;
    }

    public void finalizeRecovery(final long globalCheckpoint) {
        transportService.submitRequest(
            targetNode,
            PeerRecoveryTargetService.Actions.FINALIZE,
            // TODO
            new RecoveryFinalizeRecoveryRequest(recoveryId, shardId, globalCheckpoint, -1),
            TransportRequestOptions.builder().withTimeout(recoverySettings.internalActionLongTimeout()).build(),
            EmptyTransportResponseHandler.INSTANCE_SAME
        ).txGet();
    }

    public void receiveFileInfo(
        List<String> phase1FileNames,
        List<Long> phase1FileSizes,
        List<String> phase1ExistingFileNames,
        List<Long> phase1ExistingFileSizes,
        int totalTranslogOps
    ) {

        RecoveryFilesInfoRequest recoveryInfoFilesRequest = new RecoveryFilesInfoRequest(
            recoveryId,
            shardId,
            phase1FileNames,
            phase1FileSizes,
            phase1ExistingFileNames,
            phase1ExistingFileSizes,
            totalTranslogOps
        );
        transportService.submitRequest(
            targetNode,
            PeerRecoveryTargetService.Actions.FILES_INFO,
            recoveryInfoFilesRequest,
            TransportRequestOptions.builder().withTimeout(recoverySettings.internalActionTimeout()).build(),
            EmptyTransportResponseHandler.INSTANCE_SAME
        ).txGet();
    }

    public void cleanFiles(int totalTranslogOps, Store.MetadataSnapshot sourceMetaData) throws IOException {
        transportService.submitRequest(
            targetNode,
            PeerRecoveryTargetService.Actions.CLEAN_FILES,
            // TODO
            new RecoveryCleanFilesRequest(recoveryId, shardId, sourceMetaData, totalTranslogOps, -1),
            TransportRequestOptions.builder().withTimeout(recoverySettings.internalActionTimeout()).build(),
            EmptyTransportResponseHandler.INSTANCE_SAME
        ).txGet();
    }

    public void writeFileChunk(
        StoreFileMetaData fileMetaData,
        long position,
        BytesReference content,
        boolean lastChunk,
        int totalTranslogOps
    ) throws IOException {
        // Pause using the rate limiter, if desired, to throttle the recovery
        final long throttleTimeInNanos;
        // always fetch the ratelimiter - it might be updated in real-time on the recovery settings
        final MulitDiskLImiter rl = recoverySettings.rateLimiter();
        if (rl != null) {
            long bytes = bytesSinceLastPause.addAndGet(content.length());
            if (bytes > rl.getMinPauseCheckBytes()) {
                // Time to pause
                bytesSinceLastPause.addAndGet(-bytes);
                try {
                    throttleTimeInNanos = rl.pause(bytes, indexShard);
                    onSourceThrottle.accept(throttleTimeInNanos);
                } catch (IOException e) {
                    throw new ElasticsearchException("failed to pause recovery", e);
                }
            } else {
                throttleTimeInNanos = 0;
            }
        } else {
            throttleTimeInNanos = 0;
        }

        transportService.submitRequest(
            targetNode,
            PeerRecoveryTargetService.Actions.FILE_CHUNK,
            new RecoveryFileChunkRequest(
                recoveryId,
                shardId,
                fileMetaData,
                position,
                content,
                lastChunk,
                totalTranslogOps,
                /* we send estimateTotalOperations with every request since we collect stats on the target and that way we can
                 * see how many translog ops we accumulate while copying files across the network. A future optimization
                 * would be in to restart file copy again (new deltas) if we have too many translog ops are piling up.
                 */
                throttleTimeInNanos
            ),
            fileChunkRequestOptions,
            EmptyTransportResponseHandler.INSTANCE_SAME
        ).txGet();
    }
}

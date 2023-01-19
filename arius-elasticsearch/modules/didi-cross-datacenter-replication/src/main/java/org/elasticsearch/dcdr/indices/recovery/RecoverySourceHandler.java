package org.elasticsearch.dcdr.indices.recovery;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexFormatTooNewException;
import org.apache.lucene.index.IndexFormatTooOldException;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.ArrayUtil;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresAction;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresRequest;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresResponse;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.StopWatch;
import org.elasticsearch.common.SuppressLoggerChecks;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.collect.ImmutableOpenIntMap;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.lucene.store.InputStreamIndexInput;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.util.CancellableThreads;
import org.elasticsearch.core.internal.io.IOUtils;
import org.elasticsearch.core.internal.io.Streams;
import org.elasticsearch.dcdr.DCDRShardInfo;
import org.elasticsearch.dcdr.action.FetchShardInfoAction;
import org.elasticsearch.dcdr.translog.primary.ReplicationShardService;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.engine.RecoveryEngineException;
import org.elasticsearch.index.seqno.SequenceNumbers;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.IndexShardClosedException;
import org.elasticsearch.index.shard.IndexShardState;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.store.Store;
import org.elasticsearch.index.store.StoreFileMetaData;
import org.elasticsearch.indices.recovery.DelayRecoveryException;
import org.elasticsearch.indices.recovery.RecoverFilesRecoveryException;
import org.elasticsearch.indices.recovery.RecoverySettings;
import org.elasticsearch.transport.RemoteTransportException;
import org.elasticsearch.transport.TransportService;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;

/**
 * author weizijun
 * date：2019-09-05
 */
@SuppressLoggerChecks(reason = "safely delegates to logger")
public class RecoverySourceHandler {

    protected final Logger logger;
    private final IndexShard shard;

    private final int chunkSizeInBytes;
    private RecoveryTargetSender recoveryTarget;
    private final ReplicationShardService replicationShardService;
    private DCDRShardInfo replicaShardInfo;
    private TransportService transportService;
    private RecoverySettings recoverySettings;

    private final CancellableThreads cancellableThreads = new CancellableThreads();

    public RecoverySourceHandler(
        final IndexShard shard, TransportService transportService, RecoverySettings recoverySettings,
        ReplicationShardService replicationShardService
    ) {
        this.shard = shard;

        this.replicationShardService = replicationShardService;
        this.logger = Loggers.getLogger(
            getClass(),
            replicationShardService.getShardId(),
            "recover " + replicationShardService.getShardId()
        );
        this.chunkSizeInBytes = recoverySettings.getChunkSize().bytesAsInt();
        this.transportService = transportService;
        this.recoverySettings = recoverySettings;
    }

    public void init() {
        replicaShardInfo = getShardInfo();
        if (replicaShardInfo == null || replicaShardInfo.getDiscoveryNode() == null) {
            throw new DelayRecoveryException("replica shard [" + replicationShardService.getShardId() + "] targetNode not found");
        }
        logger.debug(
            "[{}][{}] replica info={}",
            replicationShardService.getDcdrIndexMetadata(),
            replicationShardService.getShardId().id(),
            replicaShardInfo
        );

        this.recoveryTarget = new RecoveryTargetSender(
            replicaShardInfo.getShardId(),
            transportService,
            replicaShardInfo.getDiscoveryNode(),
            recoverySettings,
            throttleTime -> shard.recoveryStats().addThrottleTime(throttleTime),
            shard
        );
    }

    public void recoverToTarget() throws IOException {
        try {
            if (replicaShardInfo == null
                || !isTargetSameHistory(replicaShardInfo.getShardId(), replicaShardInfo.getHistoryUUID())
                || replicaShardInfo.getCheckPoint() < 0
                || !replicationShardService.checkPointLocationExists(replicaShardInfo.getCheckPoint())) {
                // recover from lucene segment
                luceneSegmentRecovery();
                replicaShardInfo = getShardInfo();
            }

            // reset replicationShardService
            replicationShardService.resetReplica(
                replicaShardInfo.getShardId(),
                replicaShardInfo.getHistoryUUID(),
                replicaShardInfo.getCheckPoint()
            );

            logger.info(
                "[{}][{}] recovering done",
                replicationShardService.getDcdrIndexMetadata(),
                replicationShardService.getShardId().id()
            );
        } catch (Exception e) {
            logger.warn(
                "[{}][{}] recovering exception",
                replicationShardService.getDcdrIndexMetadata(),
                replicationShardService.getShardId().id(),
                e
            );
            throw e;
        }
    }

    private void luceneSegmentRecovery() throws IOException {
        logger.debug(
            "[{}][{}] start recover from lucene segment",
            replicationShardService.getDcdrIndexMetadata(),
            replicationShardService.getShardId().id()
        );

        StopWatch stopWatch = new StopWatch().start();
        // send start recovery to peer
        StartRecoveryResponse response = recoveryTarget.startRecovery();

        final long startingSeqNo;
        final long requiredSeqNoRangeStart;
        final Engine.IndexCommitRef snapshot;
        try {
            snapshot = shard.acquireSafeIndexCommit();
        } catch (final Exception e) {
            throw new RecoveryEngineException(shard.shardId(), 1, "snapshot failed", e);
        }

        long snapshotCheckpoint = Long.parseLong(snapshot.getIndexCommit().getUserData().get(SequenceNumbers.LOCAL_CHECKPOINT_KEY));

        // We must have everything above the local checkpoint in the commit
        requiredSeqNoRangeStart = snapshotCheckpoint + 1;
        // If soft-deletes enabled, we need to transfer only operations after the local_checkpoint of the commit to have
        // the same history on the target. However, with translog, we need to set this to 0 to create a translog roughly
        // according to the retention policy on the target. Note that it will still filter out legacy operations without seqNo.
        startingSeqNo = shard.indexSettings().isSoftDeleteEnabled() ? requiredSeqNoRangeStart : 0;
        try {
            final int estimateNumOps = shard.estimateNumberOfHistoryOperations("peer-recovery", Engine.HistorySource.TRANSLOG, startingSeqNo);
            sendLuceneSegments(snapshot.getIndexCommit(), () -> estimateNumOps, response);
        } catch (final Exception e) {
            throw new RecoveryEngineException(shard.shardId(), 1, "sendLuceneSegments failed", e);
        } finally {
            try {
                IOUtils.close(snapshot);
            } catch (final IOException ex) {
                logger.warn("releasing snapshot caused exception", ex);
            }
        }

        // final recover
        recoveryTarget.finalizeRecovery(snapshotCheckpoint);

        stopWatch.stop();
        logger.info(
            "[{}][{}] recover from lucene segment done, cost={}",
            replicationShardService.getDcdrIndexMetadata(),
            replicationShardService.getShardId().id(),
            stopWatch.totalTime().millis()
        );
    }

    private boolean isTargetSameHistory(ShardId replicaShardId, String currentHistoryUuid) {
        final ShardId targetReplicaShardId = replicationShardService.getReplicaShardId();
        final String targetHistoryUUID = replicationShardService.getReplicaHistoryUuid();

        return (targetHistoryUUID != null && replicationShardService.getReplicaHistoryUuid().equals(currentHistoryUuid)) &&
            (targetReplicaShardId != null && targetReplicaShardId.equals(replicaShardId));
    }

    public void sendLuceneSegments(final IndexCommit snapshot, final Supplier<Integer> translogOps, final StartRecoveryResponse response) {
        cancellableThreads.checkForCancel();
        // Total size of segment files that are recovered
        long totalSize = 0;
        // Total size of segment files that were able to be re-used
        long existingTotalSize = 0;
        final Store store = shard.store();
        store.incRef();

        List<String> fileNames = new ArrayList<>();
        List<Long> fileSizes = new ArrayList<>();
        List<String> existFileNames = new ArrayList<>();
        List<Long> existFileSizes = new ArrayList<>();

        try {
            StopWatch stopWatch = new StopWatch().start();
            final Store.MetadataSnapshot recoverySourceMetadata;
            try {
                recoverySourceMetadata = store.getMetadata(snapshot);
            } catch (CorruptIndexException | IndexFormatTooOldException | IndexFormatTooNewException ex) {
                shard.failShard("recovery", ex);
                throw ex;
            }
            for (String name : snapshot.getFileNames()) {
                final StoreFileMetaData md = recoverySourceMetadata.get(name);
                if (md == null) {
                    logger.info("Snapshot differs from actual index for file: {} meta: {}", name, recoverySourceMetadata.asMap());
                    throw new CorruptIndexException(
                        "Snapshot differs from actual index - maybe index was removed metadata has " +
                            recoverySourceMetadata.asMap().size() + " files",
                        name
                    );
                }
            }

            String recoverySourceSyncId = recoverySourceMetadata.getSyncId();
            String recoveryTargetSyncId = response.metadataSnapshot().getSyncId();
            final boolean recoverWithSyncId = recoverySourceSyncId != null &&
                recoverySourceSyncId.equals(recoveryTargetSyncId);
            if (recoverWithSyncId) {
                final long numDocsTarget = response.metadataSnapshot().getNumDocs();
                final long numDocsSource = recoverySourceMetadata.getNumDocs();
                if (numDocsTarget != numDocsSource) {
                    throw new IllegalStateException(
                        "try to recover " + response.shardId() + " from primary shard with sync id but number " +
                            "of docs differ: " + numDocsSource + " vs " + numDocsTarget
                            + "(" + replicaShardInfo.getDiscoveryNode().getName() + ")"
                    );
                }

                logger.trace("skipping [sendLuceneSegments]- identical sync id [{}] found on both source and target", recoverySourceSyncId);
            } else {
                final Store.RecoveryDiff diff = recoverySourceMetadata.recoveryDiff(response.metadataSnapshot());
                for (StoreFileMetaData md : diff.identical) {
                    existFileNames.add(md.name());
                    existFileSizes.add(md.length());
                    existingTotalSize += md.length();
                    if (logger.isTraceEnabled()) {
                        logger.trace(
                            "recovery [sendLuceneSegments]: not recovering [{}], exist in local store and has checksum [{}]," +
                                " size [{}]",
                            md.name(),
                            md.checksum(),
                            md.length()
                        );
                    }
                    totalSize += md.length();
                }
                List<StoreFileMetaData> phase1Files = new ArrayList<>(diff.different.size() + diff.missing.size());
                phase1Files.addAll(diff.different);
                phase1Files.addAll(diff.missing);
                for (StoreFileMetaData md : phase1Files) {
                    if (response.metadataSnapshot().asMap().containsKey(md.name())) {
                        logger.trace(
                            "recovery [sendLuceneSegments]: recovering [{}], exists in local store, but is different: remote [{}], local [{}]",
                            md.name(),
                            response.metadataSnapshot().asMap().get(md.name()),
                            md
                        );
                    } else {
                        logger.trace("recovery [sendLuceneSegments]: recovering [{}], does not exist in remote", md.name());
                    }
                    fileNames.add(md.name());
                    fileSizes.add(md.length());
                    totalSize += md.length();
                }

                logger.trace(
                    "recovery [sendLuceneSegments]: recovering_files [{}] with total_size [{}], reusing_files [{}] with total_size [{}]",
                    fileNames.size(),
                    new ByteSizeValue(totalSize),
                    existFileNames.size(),
                    new ByteSizeValue(existingTotalSize)
                );
                cancellableThreads.execute(
                    () -> recoveryTarget.receiveFileInfo(
                        fileNames,
                        fileSizes,
                        existFileNames,
                        existFileSizes,
                        translogOps.get()
                    )
                );

                // How many bytes we've copied since we last called RateLimiter.pause
                final Function<StoreFileMetaData, OutputStream> outputStreamFactories =
                    md -> new BufferedOutputStream(new RecoveryOutputStream(md, translogOps), chunkSizeInBytes);
                sendFiles(store, phase1Files.toArray(new StoreFileMetaData[phase1Files.size()]), outputStreamFactories);

                // Send the CLEAN_FILES request, which takes all of the files that
                // were transferred and renames them from their temporary file
                // names to the actual file names. It also writes checksums for
                // the files after they have been renamed.
                //
                // Once the files have been renamed, any other files that are not
                // related to this recovery (out of date segments, for example)
                // are deleted
                try {
                    cancellableThreads.executeIO(() -> recoveryTarget.cleanFiles(translogOps.get(), recoverySourceMetadata));
                } catch (RemoteTransportException | IOException targetException) {
                    final IOException corruptIndexException;
                    // we realized that after the index was copied and we wanted to finalize the recovery
                    // the index was corrupted:
                    // - maybe due to a broken segments file on an empty index (transferred with no checksum)
                    // - maybe due to old segments without checksums or length only checks
                    if ((corruptIndexException = ExceptionsHelper.unwrapCorruption(targetException)) != null) {
                        try {
                            final Store.MetadataSnapshot recoverySourceMetadata1 = store.getMetadata(snapshot);
                            StoreFileMetaData[] metadata =
                                StreamSupport.stream(recoverySourceMetadata1.spliterator(), false).toArray(StoreFileMetaData[]::new);
                            ArrayUtil.timSort(metadata, Comparator.comparingLong(StoreFileMetaData::length)); // check small files first
                            for (StoreFileMetaData md : metadata) {
                                cancellableThreads.checkForCancel();
                                logger.debug("checking integrity for file {} after remove corruption exception", md);
                                if (store.checkIntegrityNoException(md) == false) { // we are corrupted on the primary -- fail!
                                    shard.failShard("recovery", corruptIndexException);
                                    logger.warn("Corrupted file detected {} checksum mismatch", md);
                                    throw corruptIndexException;
                                }
                            }
                        } catch (IOException ex) {
                            targetException.addSuppressed(ex);
                            throw targetException;
                        }
                        // corruption has happened on the way to replica
                        RemoteTransportException exception = new RemoteTransportException(
                            "File corruption occurred on recovery but " +
                                "checksums are ok",
                            null
                        );
                        exception.addSuppressed(targetException);
                        logger.warn(
                            () -> new ParameterizedMessage(
                                "{} Remote file corruption during finalization of recovery on node {}. local checksum OK",
                                shard.shardId(),
                                replicaShardInfo.getDiscoveryNode()
                            ),
                            corruptIndexException
                        );
                        throw exception;
                    } else {
                        throw targetException;
                    }
                }
            }
            stopWatch.stop();
            logger.trace("recovery [sendLuceneSegments]: took [{}]", stopWatch.totalTime());
        } catch (Exception e) {
            throw new RecoverFilesRecoveryException(response.shardId(), /* recover size */0, new ByteSizeValue(totalSize), e);
        } finally {
            store.decRef();
        }
    }

    /**
     * Cancels the recovery and interrupts all eligible threads.
     */
    public void cancel(String reason) {
        cancellableThreads.cancel(reason);
    }

    @Override
    public String toString() {
        return "ShardRecoveryHandler{" +
            "shardId=" + replicationShardService.getShardId() +
            '}';
    }

    final class RecoveryOutputStream extends OutputStream {
        private final StoreFileMetaData md;
        private final Supplier<Integer> translogOps;
        private long position = 0;

        RecoveryOutputStream(StoreFileMetaData md, Supplier<Integer> translogOps) {
            this.md = md;
            this.translogOps = translogOps;
        }

        @Override
        public void write(int b) throws IOException {
            throw new UnsupportedOperationException("we can't send single bytes over the wire");
        }

        @Override
        public void write(byte[] b, int offset, int length) throws IOException {
            sendNextChunk(position, new BytesArray(b, offset, length), md.length() == position + length);
            position += length;
            assert md.length() >= position : "length: " + md.length() + " but positions was: " + position;
        }

        private void sendNextChunk(long position, BytesArray content, boolean lastChunk) throws IOException {
            // Actually send the file chunk to the target node, waiting for it to complete
            cancellableThreads.executeIO(
                () -> recoveryTarget.writeFileChunk(md, position, content, lastChunk, translogOps.get())
            );
            if (shard.state() == IndexShardState.CLOSED) { // check if the shard got closed on us
                throw new IndexShardClosedException(replicationShardService.getShardId());
            }
        }
    }

    void sendFiles(Store store, StoreFileMetaData[] files, Function<StoreFileMetaData, OutputStream> outputStreamFactory) throws Exception {
        store.incRef();
        try {
            ArrayUtil.timSort(files, Comparator.comparingLong(StoreFileMetaData::length)); // send smallest first
            for (int i = 0; i < files.length; i++) {
                final StoreFileMetaData md = files[i];
                try (IndexInput indexInput = store.directory().openInput(md.name(), IOContext.READONCE)) {
                    // it's fine that we are only having the indexInput in the try/with block. The copy methods handles
                    // exceptions during close correctly and doesn't hide the original exception.
                    Streams.copy(new InputStreamIndexInput(indexInput, md.length()), outputStreamFactory.apply(md));
                } catch (Exception e) {
                    final IOException corruptIndexException;
                    if ((corruptIndexException = ExceptionsHelper.unwrapCorruption(e)) != null) {
                        if (store.checkIntegrityNoException(md) == false) { // we are corrupted on the primary -- fail!
                            logger.warn("{} Corrupted file detected {} checksum mismatch", replicationShardService.getShardId(), md);
                            failEngine(corruptIndexException);
                            throw corruptIndexException;
                        } else { // corruption has happened on the way to replica
                            RemoteTransportException exception = new RemoteTransportException(
                                "File corruption occurred on recovery but " +
                                    "checksums are ok",
                                null
                            );
                            exception.addSuppressed(e);
                            logger.warn(
                                () -> new ParameterizedMessage(
                                    "{} Remote file corruption, recovering {}. local checksum OK",
                                    replicationShardService.getShardId(),
                                    md
                                ),
                                corruptIndexException
                            );
                            throw exception;
                        }
                    } else {
                        throw e;
                    }
                }
            }
        } finally {
            store.decRef();
        }
    }

    protected void failEngine(IOException cause) {
        shard.failShard("recovery", cause);
    }

    private DCDRShardInfo getShardInfo() {
        try {
            FetchShardInfoAction.Response response = replicationShardService.getRemoteClient()
                .execute(
                    FetchShardInfoAction.INSTANCE,
                    new FetchShardInfoAction.Request(replicationShardService.getReplicaIndex(), replicationShardService.getShardId().id())
                ).actionGet(ReplicationShardService.REMOTE_REQUEST_TIMEOUT);
            return response.getDcdrShardInfo();
        } catch (Exception e) {
            logger.info(
                "[{}][{}]  getShardInfo error",
                replicationShardService.getDcdrIndexMetadata(),
                replicationShardService.getShardId().id(),
                e
            );
            return null;
        }

    }
}

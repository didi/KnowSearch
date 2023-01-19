package org.elasticsearch.dcdr.indices.recovery;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexFormatTooNewException;
import org.apache.lucene.index.IndexFormatTooOldException;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefIterator;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.cluster.action.shard.ShardStateAction;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.UUIDs;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.util.CancellableThreads;
import org.elasticsearch.common.util.concurrent.AbstractRefCounted;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;
import org.elasticsearch.index.seqno.ReplicationTracker;
import org.elasticsearch.index.seqno.SequenceNumbers;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.store.Store;
import org.elasticsearch.index.store.StoreFileMetaData;
import org.elasticsearch.index.translog.Translog;
import org.elasticsearch.indices.recovery.RecoveryFailedException;
import org.elasticsearch.indices.recovery.RecoveryState;

/**
 * author weizijun
 * date：2019-09-11
 */
public class RecoveryTargetHandler extends AbstractRefCounted {
    private final Logger logger;

    private final ShardStateAction shardStateAction;
    private final List<ShardRouting> replicaShards;

    private static final AtomicLong idGenerator = new AtomicLong();

    private static final String RECOVERY_PREFIX = "recovery.";

    private final ShardId shardId;
    private final long recoveryId;
    private final IndexShard indexShard;
    private final String tempFilePrefix;
    private final Store store;
    private final LongConsumer ensureClusterStateVersionCallback;

    private final AtomicBoolean finished = new AtomicBoolean();

    private final ConcurrentMap<String, IndexOutput> openIndexOutputs = ConcurrentCollections.newConcurrentMap();
    private final CancellableThreads cancellableThreads;

    // last time this status was accessed
    private volatile long lastAccessTime = System.nanoTime();

    // latch that can be used to blockingly wait for RecoveryTarget to be closed
    private final CountDownLatch closedLatch = new CountDownLatch(1);

    private final Map<String, String> tempFileNames = ConcurrentCollections.newConcurrentMap();

    private final RecoveryState.Index index = new RecoveryState.Index();

    /**
     * Creates a new recovery target object that represents a recovery to the provided shard.
     *
     * @param indexShard                        local shard where we want to recover to
     * @param ensureClusterStateVersionCallback callback to ensure that the current node is at least on a cluster state with the provided
     *                                          version; necessary for primary relocation so that new primary knows about all other ongoing
     *                                          replica recoveries when replicating documents (see {@link RecoverySourceHandler})
     */
    public RecoveryTargetHandler(
        final IndexShard indexShard,
        final LongConsumer ensureClusterStateVersionCallback,
        final ShardStateAction shardStateAction,
        final List<ShardRouting> replicaShards
    ) {
        super("recovery_status");
        this.cancellableThreads = new CancellableThreads();
        this.recoveryId = idGenerator.incrementAndGet();
        this.logger = Loggers.getLogger(getClass(), indexShard.shardId());
        this.indexShard = indexShard;
        this.shardId = indexShard.shardId();
        this.tempFilePrefix = RECOVERY_PREFIX + UUIDs.randomBase64UUID() + ".";
        this.store = indexShard.store();
        this.ensureClusterStateVersionCallback = ensureClusterStateVersionCallback;
        this.shardStateAction = shardStateAction;
        this.replicaShards = replicaShards;
        // make sure the store is not released until we are done.
        store.incRef();
        indexShard.recoveryStats().incCurrentAsTarget();
    }

    public long recoveryId() {
        return recoveryId;
    }

    public ShardId shardId() {
        return shardId;
    }

    public IndexShard indexShard() {
        ensureRefCount();
        return indexShard;
    }

    public RecoveryState.Index indexState() {
        return index;
    }

    public CancellableThreads cancellableThreads() {
        return cancellableThreads;
    }

    /** return the last time this RecoveryStatus was used (based on System.nanoTime() */
    public long lastAccessTime() {
        return lastAccessTime;
    }

    /** sets the lasAccessTime flag to now */
    public void setLastAccessTime() {
        lastAccessTime = System.nanoTime();
    }

    public Store store() {
        ensureRefCount();
        return store;
    }

    /** renames all temporary files to their true name, potentially overriding existing files */
    public void renameAllTempFiles() throws IOException {
        ensureRefCount();
        store.renameTempFilesSafe(tempFileNames);
    }

    /**
     * cancel the recovery. calling this method will clean temporary files and release the store
     * unless this object is in use (in which case it will be cleaned once all ongoing users call
     * {@link #decRef()}
     * <p>
     * if {@link #cancellableThreads()} was used, the threads will be interrupted.
     */
    public void cancel(String reason) {
        if (finished.compareAndSet(false, true)) {
            try {
                logger.debug("recovery canceled (reason: [{}])", reason);
                cancellableThreads.cancel(reason);
            } finally {
                // release the initial reference. recovery files will be cleaned as soon as ref count goes to zero, potentially now
                decRef();
            }
        }
    }

    /**
     * fail the recovery
     *
     * @param e                exception that encapsulating the failure
     * @param sendShardFailure indicates whether to notify the master of the shard failure
     */
    public void fail(RecoveryFailedException e, boolean sendShardFailure) {
        if (finished.compareAndSet(false, true)) {
            try {
                cancellableThreads.cancel("failed recovery [" + ExceptionsHelper.stackTrace(e) + "]");
            } finally {
                // release the initial reference. recovery files will be cleaned as soon as ref count goes to zero, potentially now
                decRef();
            }
        }
    }

    /** mark the current recovery as done */
    public void markAsDone() {
        if (finished.compareAndSet(false, true)) {
            assert tempFileNames.isEmpty() : "not all temporary files are renamed";
            decRef();
        }
    }

    /** Get a temporary name for the provided file name. */
    public String getTempNameForFile(String origFile) {
        return tempFilePrefix + origFile;
    }

    public IndexOutput getOpenIndexOutput(String key) {
        ensureRefCount();
        return openIndexOutputs.get(key);
    }

    /** remove and {@link org.apache.lucene.store.IndexOutput} for a given file. It is the caller's responsibility to close it */
    public IndexOutput removeOpenIndexOutputs(String name) {
        ensureRefCount();
        return openIndexOutputs.remove(name);
    }

    /**
     * Creates an {@link org.apache.lucene.store.IndexOutput} for the given file name. Note that the
     * IndexOutput actually point at a temporary file.
     * <p>
     * Note: You can use {@link #getOpenIndexOutput(String)} with the same filename to retrieve the same IndexOutput
     * at a later stage
     */
    public IndexOutput openAndPutIndexOutput(String fileName, StoreFileMetaData metaData, Store store) throws IOException {
        ensureRefCount();
        String tempFileName = getTempNameForFile(fileName);
        if (tempFileNames.containsKey(tempFileName)) {
            throw new IllegalStateException("output for file [" + fileName + "] has already been created");
        }
        // add first, before it's created
        tempFileNames.put(tempFileName, fileName);
        IndexOutput indexOutput = store.createVerifyingOutput(tempFileName, metaData, IOContext.DEFAULT);
        openIndexOutputs.put(fileName, indexOutput);
        return indexOutput;
    }

    @Override
    protected void closeInternal() {
        try {
            // clean open index outputs
            Iterator<Map.Entry<String, IndexOutput>> iterator = openIndexOutputs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, IndexOutput> entry = iterator.next();
                logger.trace("closing IndexOutput file [{}]", entry.getValue());
                try {
                    entry.getValue().close();
                } catch (Exception e) {
                    logger.debug(() -> new ParameterizedMessage("error while closing recovery output [{}]", entry.getValue()), e);
                }
                iterator.remove();
            }
            // trash temporary files
            for (String file : tempFileNames.keySet()) {
                logger.trace("cleaning temporary file [{}]", file);
                store.deleteQuiet(file);
            }
        } finally {
            // free store. increment happens in constructor
            store.decRef();
            indexShard.recoveryStats().decCurrentAsTarget();
            closedLatch.countDown();
        }
    }

    @Override
    public String toString() {
        return shardId + " [" + recoveryId + "]";
    }

    private void ensureRefCount() {
        if (refCount() <= 0) {
            throw new ElasticsearchException(
                "RecoveryStatus is used but it's refcount is 0. Probably a mismatch between incRef/decRef " +
                    "calls"
            );
        }
    }

    public StartRecoveryResponse startRecovery() throws IOException {
        logger.trace("{} preparing shard for peer recovery", shardId);
        indexShard().closeEngine();
        return getStartRecoveryResponse();
    }

    public void finalizeRecovery(final long globalCheckpoint) throws IOException {
        final IndexShard indexShard = indexShard();
        indexShard.openDirectEngineAndSkipTranslogRecovery();
        indexShard.updateLocalCheckpointForShard(indexShard.routingEntry().allocationId().getId(), globalCheckpoint);
        indexShard.sync();
        indexShard.finalizeRecoveryDone();

        for (ShardRouting shardRouting : replicaShards) {
            if (shardRouting.active()) {
                shardStateAction.remoteShardFailed(
                    shardRouting.shardId(),
                    shardRouting.allocationId().getId(),
                    indexShard.getPendingPrimaryTerm(),
                    false,
                    "recovery resync",
                    null,
                    ActionListener.wrap((r)->{}, (e) -> {
                        // TODO 发送失败处理
                        logger.warn("fail to send failed shard message,shard={}" + shardId, e);
                    })
                );
            }
        }
    }

    public void ensureClusterStateVersion(long clusterStateVersion) {
        ensureClusterStateVersionCallback.accept(clusterStateVersion);
    }

    public void handoffPrimaryContext(final ReplicationTracker.PrimaryContext primaryContext) {
        indexShard.activateWithPrimaryContext(primaryContext);
    }

    public void receiveFileInfo(
        List<String> phase1FileNames,
        List<Long> phase1FileSizes,
        List<String> phase1ExistingFileNames,
        List<Long> phase1ExistingFileSizes,
        int totalTranslogOps
    ) {
        final RecoveryState.Index index = indexState();
        for (int i = 0; i < phase1ExistingFileNames.size(); i++) {
            index.addFileDetail(phase1ExistingFileNames.get(i), phase1ExistingFileSizes.get(i), true);
        }
        for (int i = 0; i < phase1FileNames.size(); i++) {
            index.addFileDetail(phase1FileNames.get(i), phase1FileSizes.get(i), false);
        }
    }

    public void cleanFiles(int totalTranslogOps, Store.MetadataSnapshot sourceMetaData) throws IOException {
        // first, we go and move files that were created with the recovery id suffix to
        // the actual names, its ok if we have a corrupted index here, since we have replicas
        // to recover from in case of a full cluster shutdown just when this code executes...
        renameAllTempFiles();
        final Store store = store();
        store.incRef();
        try {
            store.cleanupAndVerify("recovery CleanFilesRequestHandler", sourceMetaData);
            // TODO: Assign the global checkpoint to the max_seqno of the safe commit if the index version >= 6.2
            final String translogUUID = Translog.createEmptyTranslog(
                indexShard.shardPath().resolveTranslog(),
                SequenceNumbers.UNASSIGNED_SEQ_NO,
                shardId,
                indexShard.getPendingPrimaryTerm()
            );
            store.associateIndexWithNewTranslog(translogUUID);
        } catch (CorruptIndexException | IndexFormatTooNewException | IndexFormatTooOldException ex) {
            // this is a fatal exception at this stage.
            // this means we transferred files from the remote that have not be checksummed and they are
            // broken. We have to clean up this shard entirely, remove all files and bubble it up to the
            // source shard since this index might be broken there as well? The Source can handle this and checks
            // its content on disk if possible.
            try {
                try {
                    store.removeCorruptionMarker();
                } finally {
                    Lucene.cleanLuceneIndex(store.directory()); // clean up and delete all files
                }
            } catch (Exception e) {
                logger.debug("Failed to clean lucene index", e);
                ex.addSuppressed(e);
            }
            RecoveryFailedException rfe = new RecoveryFailedException(shardId, null, null, "failed to clean after recovery", ex);
            fail(rfe, true);
            throw rfe;
        } catch (Exception ex) {
            RecoveryFailedException rfe = new RecoveryFailedException(shardId, null, null, "failed to clean after recovery", ex);
            fail(rfe, true);
            throw rfe;
        } finally {
            store.decRef();
        }
    }

    public void writeFileChunk(
        StoreFileMetaData fileMetaData,
        long position,
        BytesReference content,
        boolean lastChunk,
        int totalTranslogOps
    ) throws IOException {
        final Store store = store();
        final String name = fileMetaData.name();
        final RecoveryState.Index indexState = indexState();
        IndexOutput indexOutput;
        if (position == 0) {
            indexOutput = openAndPutIndexOutput(name, fileMetaData, store);
        } else {
            indexOutput = getOpenIndexOutput(name);
        }
        BytesRefIterator iterator = content.iterator();
        BytesRef scratch;
        while ((scratch = iterator.next()) != null) { // we iterate over all pages - this is a 0-copy for all core impls
            indexOutput.writeBytes(scratch.bytes, scratch.offset, scratch.length);
        }
        indexState.addRecoveredBytesToFile(name, content.length());
        if (indexOutput.getFilePointer() >= fileMetaData.length() || lastChunk) {
            try {
                Store.verify(indexOutput);
            } finally {
                // we are done
                indexOutput.close();
            }
            final String temporaryFileName = getTempNameForFile(name);
            assert Arrays.asList(store.directory().listAll()).contains(temporaryFileName) : "expected: [" + temporaryFileName + "] in "
                + Arrays.toString(store.directory().listAll());
            store.directory().sync(Collections.singleton(temporaryFileName));
            IndexOutput remove = removeOpenIndexOutputs(name);
            assert remove == null || remove == indexOutput; // remove maybe null if we got finished
        }
    }

    /**
     * Prepare the start recovery request.
     *
     * @return a start recovery request
     */
    private StartRecoveryResponse getStartRecoveryResponse() {
        final StartRecoveryResponse response;
        logger.trace("{} collecting local files", shardId);

        final Store.MetadataSnapshot metadataSnapshot = getStoreMetadataSnapshot();
        logger.trace("{} local file count [{}]", shardId, metadataSnapshot.size());

        response = new StartRecoveryResponse(
            shardId,
            indexShard().routingEntry().allocationId().getId(),
            metadataSnapshot,
            recoveryId
        );
        return response;
    }

    /**
     * Obtains a snapshot of the store metadata for the recovery target.
     *
     * @return a snapshot of the store metadata
     */
    private Store.MetadataSnapshot getStoreMetadataSnapshot() {
        try {
            return indexShard().snapshotStoreMetadata();
        } catch (final org.apache.lucene.index.IndexNotFoundException e) {
            // happens on an empty folder. no need to log
            logger.trace("{} shard folder empty, recovering all files", shardId);
            return Store.MetadataSnapshot.EMPTY;
        } catch (final IOException e) {
            logger.warn("error while listing local files, recovering as if there are none", e);
            return Store.MetadataSnapshot.EMPTY;
        }
    }
}

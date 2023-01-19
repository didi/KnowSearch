package org.elasticsearch.dcdr.indices.recovery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongConsumer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.elasticsearch.cluster.action.shard.ShardStateAction;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.AbstractRunnable;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.IndexShardClosedException;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.recovery.RecoveryFailedException;
import org.elasticsearch.threadpool.ThreadPool;

/**
 * author weizijun
 * date：2019-09-11
 */
public class RecoveriesCollection {

    /** This is the single source of truth for ongoing recoveries. If it's not here, it was canceled or done */
    private final ConcurrentMap<Long, RecoveryTargetHandler> onGoingRecoveries = ConcurrentCollections.newConcurrentMap();

    private final Logger logger;
    private final ThreadPool threadPool;
    private final LongConsumer ensureClusterStateVersionCallback;

    public RecoveriesCollection(Logger logger, ThreadPool threadPool, LongConsumer ensureClusterStateVersionCallback) {
        this.logger = logger;
        this.threadPool = threadPool;
        this.ensureClusterStateVersionCallback = ensureClusterStateVersionCallback;
    }

    /**
     * Starts are new recovery for the given shard, source node and state
     *
     * @return the id of the new recovery.
     */
    public long startRecovery(
        IndexShard indexShard,
        TimeValue activityTimeout,
        ShardStateAction shardStateAction,
        List<ShardRouting> replicaShards
    ) {
        RecoveryTargetHandler recoveryTarget = new RecoveryTargetHandler(
            indexShard,
            ensureClusterStateVersionCallback,
            shardStateAction,
            replicaShards
        );
        startRecoveryInternal(recoveryTarget, activityTimeout);
        return recoveryTarget.recoveryId();
    }

    private void startRecoveryInternal(RecoveryTargetHandler recoveryTarget, TimeValue activityTimeout) {
        RecoveryTargetHandler existingTarget = onGoingRecoveries.putIfAbsent(recoveryTarget.recoveryId(), recoveryTarget);
        assert existingTarget == null : "found two RecoveryStatus instances with the same id";
        logger.trace(
            "{} started recovery, id [{}]",
            recoveryTarget.shardId(),
            recoveryTarget.recoveryId()
        );
        threadPool.schedule(
            new RecoveryMonitor(recoveryTarget.recoveryId(), recoveryTarget.lastAccessTime(), activityTimeout),
            activityTimeout,
            ThreadPool.Names.GENERIC
        );
    }

    public RecoveryTargetHandler getRecoveryTarget(long id) {
        return onGoingRecoveries.get(id);
    }

    /**
     * gets the {@link RecoveryTargetHandler } for a given id. The RecoveryStatus returned has it's ref count already incremented
     * to make sure it's safe to use. However, you must call {@link RecoveryTargetHandler#decRef()} when you are done with it, typically
     * by using this method in a try-with-resources clause.
     * <p>
     * Returns null if recovery is not found
     */
    public RecoveriesCollection.RecoveryRef getRecovery(long id) {
        RecoveryTargetHandler status = onGoingRecoveries.get(id);
        if (status != null && status.tryIncRef()) {
            return new RecoveriesCollection.RecoveryRef(status);
        }
        return null;
    }

    /** Similar to {@link #getRecovery(long)} but throws an exception if no recovery is found */
    public RecoveriesCollection.RecoveryRef getRecoverySafe(long id, ShardId shardId) {
        RecoveriesCollection.RecoveryRef recoveryRef = getRecovery(id);
        if (recoveryRef == null) {
            throw new IndexShardClosedException(shardId);
        }
        assert recoveryRef.target().shardId().equals(shardId);
        return recoveryRef;
    }

    /** cancel the recovery with the given id (if found) and remove it from the recovery collection */
    public boolean cancelRecovery(long id, String reason) {
        RecoveryTargetHandler removed = onGoingRecoveries.remove(id);
        boolean cancelled = false;
        if (removed != null) {
            logger.trace(
                "{} canceled recovery, id [{}] (reason [{}])",
                removed.shardId(),
                removed.recoveryId(),
                reason
            );
            removed.cancel(reason);
            cancelled = true;
        }
        return cancelled;
    }

    /**
     * fail the recovery with the given id (if found) and remove it from the recovery collection
     *
     * @param id               id of the recovery to fail
     * @param e                exception with reason for the failure
     * @param sendShardFailure true a shard failed message should be sent to the master
     */
    public void failRecovery(long id, RecoveryFailedException e, boolean sendShardFailure) {
        RecoveryTargetHandler removed = onGoingRecoveries.remove(id);
        if (removed != null) {
            logger.trace(
                "{} failing recovery, id [{}]. Send shard failure: [{}]",
                removed.shardId(),
                removed.recoveryId(),
                sendShardFailure
            );
            removed.fail(e, sendShardFailure);
        }
    }

    /** mark the recovery with the given id as done (if found) */
    public void markRecoveryAsDone(long id) {
        RecoveryTargetHandler removed = onGoingRecoveries.remove(id);
        if (removed != null) {
            logger.trace("{} marking recovery as done, id [{}]", removed.shardId(), removed.recoveryId());
            removed.markAsDone();
        }
    }

    /** the number of ongoing recoveries */
    public int size() {
        return onGoingRecoveries.size();
    }

    /**
     * cancel all ongoing recoveries for the given shard
     *
     * @param reason       reason for cancellation
     * @param shardId      shardId for which to cancel recoveries
     * @return true if a recovery was cancelled
     */
    public boolean cancelRecoveriesForShard(ShardId shardId, String reason) {
        boolean cancelled = false;
        List<RecoveryTargetHandler> matchedRecoveries = new ArrayList<>();
        synchronized (onGoingRecoveries) {
            for (Iterator<RecoveryTargetHandler> it = onGoingRecoveries.values().iterator(); it.hasNext(); ) {
                RecoveryTargetHandler status = it.next();
                if (status.shardId().equals(shardId)) {
                    matchedRecoveries.add(status);
                    it.remove();
                }
            }
        }
        for (RecoveryTargetHandler removed : matchedRecoveries) {
            logger.trace("{} canceled recovery, id [{}] (reason [{}])",
                removed.shardId(), removed.recoveryId(), reason);
            removed.cancel(reason);
            cancelled = true;
        }
        return cancelled;
    }

    /**
     * a reference to {@link RecoveryTargetHandler}, which implements {@link AutoCloseable}. closing the reference
     * causes {@link RecoveryTargetHandler#decRef()} to be called. This makes sure that the underlying resources
     * will not be freed until {@link RecoveriesCollection.RecoveryRef#close()} is called.
     */
    public static class RecoveryRef implements AutoCloseable {

        private final RecoveryTargetHandler status;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /**
         * Important: {@link RecoveryTargetHandler#tryIncRef()} should
         * be *successfully* called on status before
         */
        public RecoveryRef(RecoveryTargetHandler status) {
            this.status = status;
            this.status.setLastAccessTime();
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                status.decRef();
            }
        }

        public RecoveryTargetHandler target() {
            return status;
        }
    }

    private class RecoveryMonitor extends AbstractRunnable {
        private final long recoveryId;
        private final TimeValue checkInterval;

        private long lastSeenAccessTime;

        private RecoveryMonitor(long recoveryId, long lastSeenAccessTime, TimeValue checkInterval) {
            this.recoveryId = recoveryId;
            this.checkInterval = checkInterval;
            this.lastSeenAccessTime = lastSeenAccessTime;
        }

        @Override
        public void onFailure(Exception e) {
            logger.error(() -> new ParameterizedMessage("unexpected error while monitoring recovery [{}]", recoveryId), e);
        }

        @Override
        protected void doRun() throws Exception {
            RecoveryTargetHandler status = onGoingRecoveries.get(recoveryId);
            if (status == null) {
                logger.trace("[monitor] no status found for [{}], shutting down", recoveryId);
                return;
            }
            long accessTime = status.lastAccessTime();
            if (accessTime == lastSeenAccessTime) {
                String message = "no activity after [" + checkInterval + "]";
                failRecovery(
                    recoveryId,
                    new RecoveryFailedException(status.shardId(), null, null, message, new ElasticsearchTimeoutException(message)),
                    true // to be safe, we don't know what go stuck
                );
                return;
            }
            lastSeenAccessTime = accessTime;
            logger.trace("[monitor] rescheduling check for [{}]. last access time is [{}]", recoveryId, lastSeenAccessTime);
            threadPool.schedule(this, checkInterval, ThreadPool.Names.GENERIC);
        }
    }
}

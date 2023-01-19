package org.elasticsearch.dcdr.translog.primary;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.store.AlreadyClosedException;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.NoShardAvailableActionException;
import org.elasticsearch.action.UnavailableShardsException;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.SuppressLoggerChecks;
import org.elasticsearch.common.transport.NetworkExceptionHelper;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.EsRejectedExecutionException;
import org.elasticsearch.dcdr.DCDR;
import org.elasticsearch.dcdr.DCDRShardInfo;
import org.elasticsearch.dcdr.action.FetchShardInfoAction;
import org.elasticsearch.dcdr.indices.recovery.PeerRecoverySourceService;
import org.elasticsearch.dcdr.indices.recovery.RecoverCase;
import org.elasticsearch.dcdr.translog.replica.bulk.TranslogSyncAction;
import org.elasticsearch.dcdr.translog.replica.bulk.TranslogSyncRequest;
import org.elasticsearch.dcdr.translog.replica.bulk.TranslogSyncResponse;
import org.elasticsearch.index.mapper.MapperException;
import org.elasticsearch.index.seqno.SeqNoStats;
import org.elasticsearch.index.shard.IllegalIndexShardStateException;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.shard.ShardNotFoundException;
import org.elasticsearch.index.translog.Translog;
import org.elasticsearch.index.translog.TranslogOffsetBehindException;
import org.elasticsearch.index.translog.TranslogOperation;
import org.elasticsearch.index.translog.TranslogReaderProxy;
import org.elasticsearch.indices.IndexClosedException;
import org.elasticsearch.node.NodeClosedException;
import org.elasticsearch.threadpool.Scheduler;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.ConnectTransportException;

/**
 * author weizijun
 * date：2019-08-05
 */
@SuppressLoggerChecks(reason = "safely delegates to logger")
public class ReplicationShardService implements Closeable {
    private static final Logger logger = LogManager.getLogger(ReplicationShardService.class);

    public final static long REMOTE_REQUEST_TIMEOUT = 10000;
    private static final int BULK_TRANSLOG_MAX_COUNT = 1000;
    private static final int TRANSLOG_MAX_BATCH_SIZE = 10;
    private static final int TRANSLOG_REPLICATION_RETRY_COUNT = 5;
    private static final int RECOVER_DELAY_MILLIS = 5000;
    private static final int BULK_DELAY_MILLIS = 100;
    private static final long REPLICATION_DELAY_MILLIS = 1000;
    private static final long CHECK_DELAY_MILLIS = 60000;
    private static final long NO_REPLICATION_MILLIS = 60000;

    private static final long REPLICATION_INITING = -1L;
    private static final long REPLICATION_IN_SYNCING = -2L;
    private static final long REPLICATION_RECOVING = -3L;
    private static final long REPLICATION_NOT_RUNNING = -4L;
    private static final long REPLICATION_BEGIN_RECOVERING = -5L;

    private TranslogOffset commitOffset;
    private TranslogOffset currentTranslogOffset;
    private Map<TranslogOffset, TranslogOffset> inSyncOffset = new TreeMap<>();

    private final AtomicReference<Boolean> syncing = new AtomicReference<>(false);
    private final AtomicReference<Boolean> recovering = new AtomicReference<>(false);
    private final AtomicReference<Boolean> closed = new AtomicReference<>(false);
    private final Semaphore semaphore = new Semaphore(TRANSLOG_MAX_BATCH_SIZE);
    private volatile boolean running = true;

    private TranslogReaderProxy translogReaderProxy;
    private ShardId shardId;
    private ShardId replicaShardId;
    private String replicaHistoryUuid;
    private DCDRIndexMetadata dcdrIndexMetadata;
    private ThreadPool threadPool;
    private Client client;
    private PeerRecoverySourceService peerRecoverySourceService;
    private ShardService shardService;
    private IndexShard indexShard;
    private Scheduler.Cancellable replicationCancellable;
    private Scheduler.Cancellable checkCancellable;

    /*************************************** stats **************************************/
    private long primaryGlobalCheckpoint = -1;
    private long primaryMaxSeqNo = -1;
    private long replicaGlobalCheckpoint = -1;
    private long replicaMaxSeqNo = -1;
    private long lastRequestedSeqNo = -1;
    private long totalSendTimeMillis = 0;
    private long successfulSendRequests = 0;
    private AtomicLong failedSendRequests = new AtomicLong(0);
    private long operationsSends = 0;
    private long bytesSend = 0;
    private long lastSendTime = -1;
    private long lastUpdateReplicaCheckPoint = -1;
    private long successRecoverCount = 0;
    private long failedRecoverCount = 0;
    private AtomicLong recoverTotalTimeMillis = new AtomicLong(0);

    /*************************************** stats **************************************/

    public ReplicationShardService(
        Client client, ShardId shardId, DCDRIndexMetadata dcdrIndexMetadata, ThreadPool threadPool, TranslogReaderProxy translogReaderProxy,
        PeerRecoverySourceService peerRecoverySourceService, ShardService shardService, IndexShard indexShard
    ) {
        this.shardId = shardId;
        this.dcdrIndexMetadata = dcdrIndexMetadata;
        this.translogReaderProxy = translogReaderProxy;
        this.client = client;
        this.peerRecoverySourceService = peerRecoverySourceService;
        this.shardService = shardService;
        this.threadPool = threadPool;
        this.indexShard = indexShard;
        this.replicationCancellable = this.threadPool.scheduleWithFixedDelay(new ReplicaCommand(), TimeValue.timeValueMillis(REPLICATION_DELAY_MILLIS), DCDR.DCDR_THREAD_POOL_NAME);
        this.checkCancellable = this.threadPool.scheduleWithFixedDelay(new CheckCommond(), TimeValue.timeValueMillis(CHECK_DELAY_MILLIS), DCDR.DCDR_THREAD_POOL_NAME);
        logger.info("{}[{}] replication start", dcdrIndexMetadata, shardId.getId());
    }

    public long replicaTranslog() {
        if (!running) {
            return REPLICATION_NOT_RUNNING;
        }

        // 检查是否在recover
        if (recovering.get()) {
            return REPLICATION_RECOVING;
        }

        try {
            // 检查是否正在replica，如果正在replica，则直接退出
            if (!syncing.compareAndSet(false, true)) {
                return REPLICATION_IN_SYNCING;
            }

            if (Strings.isEmpty(replicaHistoryUuid)) {
                initShardReplication();
            }

            logger.trace("{}[{}] start replicaTranslog", dcdrIndexMetadata, shardId.getId());

            long count = 0;
            List<TranslogOperation> translogList;
            do {
                translogList = translogReaderProxy.readTranslog(
                    currentTranslogOffset.getNext().generation,
                    currentTranslogOffset.getNext().translogLocation,
                    BULK_TRANSLOG_MAX_COUNT
                );
                if (translogList.size() > 0) {
                    if (!semaphore.tryAcquire()) {
                        return count;
                    }

                    count += translogList.size();
                    // 更新currentTranslogOffset
                    updateCurrentOffset(translogList);
                    // 发送translog
                    sendTranslog(translogList);

                    this.operationsSends += translogList.size();
                    this.bytesSend += translogList.stream().mapToLong(op -> op.getOperation().estimateSize()).sum();

                    if (translogList.size() < BULK_TRANSLOG_MAX_COUNT) {
                        // 读取的translog小于BULK_TRANSLOG_MAX_COUNT，则直接返回
                        return count;
                    }
                }
            } while (translogList.size() > 0);
            return count;
        } catch (Throwable e) {
            logger.debug("{}[{}] replicaTranslog exception", dcdrIndexMetadata, shardId.getId(), e);
            doRecover(RecoverCase.Other);
            return REPLICATION_BEGIN_RECOVERING;
        } finally {
            syncing.set(false);
        }
    }

    public void statsCheck() {
        if (!running) {
            return ;
        }

        // 检查是否在recover
        if (recovering.get()) {
            return ;
        }

        try {
            // 检查是否正在replica，如果正在replica，则直接退出
            if (!syncing.compareAndSet(false, true)) {
                return ;
            }

            // 还未初始化时，无需check状态
            if (Strings.isEmpty(replicaHistoryUuid)) {
                return ;
            }

            logger.trace("{}[{}] start statsCheck", dcdrIndexMetadata, shardId.getId());

            // 检查replica的checkPoint落后太多的情况
            if (true == checkNoUpdateCheckPoint()) {
                return ;
            }
        } catch (Throwable e) {
            logger.debug("{}[{}] statsCheck exception", dcdrIndexMetadata, shardId.getId(), e);
        } finally {
            syncing.set(false);
        }
    }

    private boolean checkNoUpdateCheckPoint() {
        long timeSinceUpdateCheckpoint = 0;
        if (lastUpdateReplicaCheckPoint != -1) {
            timeSinceUpdateCheckpoint = System.currentTimeMillis() - lastUpdateReplicaCheckPoint;
        }

        if (timeSinceUpdateCheckpoint < NO_REPLICATION_MILLIS) {
            return false;
        }

        // 主从checkpoint一致，直接退出
        fetchPrimaryShardInfo();
        if (this.replicaGlobalCheckpoint == this.primaryGlobalCheckpoint) {
            // 更新lastUpdateReplicaCheckPoint，防止数据不再写入后lastUpdateReplicaCheckPoint不更新的问题
            lastUpdateReplicaCheckPoint = System.currentTimeMillis();
            return false;
        }

        FetchShardInfoAction.Response fetchResponse = getRemoteClient().execute(
            FetchShardInfoAction.INSTANCE,
            new FetchShardInfoAction.Request(dcdrIndexMetadata.getReplicaIndex(), shardId.id())
        ).actionGet(REMOTE_REQUEST_TIMEOUT);
        DCDRShardInfo replicaShardInfo = fetchResponse.getDcdrShardInfo();
        if (replicaShardInfo == null) {
            logger.info("{}[{}] shard info not found", dcdrIndexMetadata, shardId.getId());
            return false;
        }

        long replicaGlobalCheckpoint = replicaShardInfo.getCheckPoint();
        if (replicaGlobalCheckpoint > this.replicaGlobalCheckpoint) {
            updateCheckpoint(replicaGlobalCheckpoint);
        }

        // 最新的replica checkpoint跟primary一致，则直接退出
        if (this.replicaGlobalCheckpoint >= this.primaryGlobalCheckpoint) {
            return false;
        }

        logger.info("{}[{}] checkPoint diff, primary={}, replica={}, start recover", dcdrIndexMetadata, shardId.getId(), primaryGlobalCheckpoint, replicaGlobalCheckpoint);

        // 此时已经超过NO_REPLICATION_MILLIS时间数据未同步
        doRecover(RecoverCase.CheckPointDelay);
        return true;
    }

    public void resetReplica(ShardId replicaShardId, String replicaHistoryUuid, long replicaGlobalCheckpoint) {
        this.replicaShardId = replicaShardId;
        this.replicaHistoryUuid = replicaHistoryUuid;
        Translog.Location location;
        if (replicaGlobalCheckpoint < 0) {
            if (indexShard.commitStats().getNumDocs() > 0) {
                throw new TranslogOffsetBehindException(
                    String.format(
                        Locale.US,
                        "{%s}[{%d}] initShardReplication current checkpoint is exception",
                        dcdrIndexMetadata.toString(),
                        shardId.getId()
                    )
                );
            }

            location = translogReaderProxy.getInitLocation();
        } else {
            location = translogReaderProxy.getCheckpointLocation(replicaGlobalCheckpoint);
            if (location == null) {
                throw new TranslogOffsetBehindException(
                    String.format(
                        Locale.US,
                        "{%s}[{%d}] initShardReplication current checkpoint(%d) is expire",
                        dcdrIndexMetadata.toString(),
                        shardId.getId(),
                        replicaGlobalCheckpoint
                    )
                );
            }
        }

        this.currentTranslogOffset = TranslogOffset.createOffset(location, location);
        commitOffset(currentTranslogOffset);
        updateCheckpoint(replicaGlobalCheckpoint);
        inSyncOffset = new TreeMap<>();
    }

    public boolean checkPointLocationExists(long checkpoint) {
        Translog.Location location = translogReaderProxy.getCheckpointLocation(checkpoint);
        return location != null;
    }

    public String getReplicaIndex() {
        return dcdrIndexMetadata.getReplicaIndex();
    }

    public ShardId getShardId() {
        return shardId;
    }

    public ShardId getReplicaShardId() {
        return replicaShardId;
    }

    public String getReplicaHistoryUuid() {
        return replicaHistoryUuid;
    }

    public Client getRemoteClient() {
        return client.getRemoteClusterClient(dcdrIndexMetadata.getReplicaCluster());
    }

    public DCDRIndexMetadata getDcdrIndexMetadata() {
        return dcdrIndexMetadata;
    }

    public void resetReplicationState(boolean state) {
        this.dcdrIndexMetadata.setReplicationState(state);

        boolean beforeState = running;
        this.running = state;

        if (beforeState && !running) {
            logger.info("{}[{}] change replication [{}]", dcdrIndexMetadata, shardId.getId(), state);
        }

        if (!beforeState && running) {
            logger.info("{}[{}] change replication [{}]", dcdrIndexMetadata, shardId.getId(), state);
            this.threadPool.schedule(new ReplicaCommand(), TimeValue.timeValueMillis(REPLICATION_DELAY_MILLIS), DCDR.DCDR_THREAD_POOL_NAME);
        }
    }

    private void updateCurrentOffset(List<TranslogOperation> translogList) {
        TranslogOperation lastTranslog = translogList.get(translogList.size() - 1);
        TranslogOffset newOffset = TranslogOffset.createOffset(lastTranslog.getCurrent(), lastTranslog.getNext());
        currentTranslogOffset = newOffset;
    }

    private synchronized void updateCommitOffsetAndStatis(
        TranslogOffset startOffset,
        TranslogOffset endOffset,
        TranslogSyncResponse response
    ) {
        updateCommitOffset(startOffset, endOffset);
        statisSuccResponse(response);
    }

    private void updateCommitOffset(TranslogOffset startOffset, TranslogOffset endOffset) {
        if (translogReaderProxy.isTranslogPosContinuous(commitOffset.getNext(), startOffset.getCurrent())) {
            // commitOffset跟start的offset重合
            Iterator<TranslogOffset> iter = inSyncOffset.keySet().iterator();
            if (iter.hasNext()) {
                // 如果inSyncOffset不为空，取出第一段offset记录
                TranslogOffset start = iter.next();
                if (translogReaderProxy.isTranslogPosContinuous(endOffset.getNext(), start.getCurrent())) {
                    // 如果本次提交的endOffset跟第一段的startOffset记录重合，则移除该段offset
                    // 同时设置commitOffset为第一段offset记录的endOffset
                    TranslogOffset end = inSyncOffset.get(start);
                    inSyncOffset.remove(start);
                    commitOffset(end);
                    return;
                }
            }

            // 如果提交段的offset没有跟第一段offset重合，则commitOffset设置为提交段的endOffset
            commitOffset(endOffset);
        } else {
            // commitOffset跟start的offset没有重合，则本次不更新commitOffset
            // 但是要确认提交段的offset是否跟其他段合并
            Iterator<TranslogOffset> iter = inSyncOffset.keySet().iterator();
            while (iter.hasNext()) {
                // 遍历inSyncOffset列表
                TranslogOffset start = iter.next();
                TranslogOffset end = inSyncOffset.get(start);

                assert startOffset.compareTo(end) > 0 || endOffset.compareTo(start) < 0 :
                    "startOffset="+startOffset+",endOffset="+endOffset+",start="+start+",end="+end;

                if (translogReaderProxy.isTranslogPosContinuous(endOffset.getNext(), start.getCurrent())) {
                    // 如果提交段的endOffset跟遍历当前端的startOffset重合，则移除遍历当前端，将两段合并，添加到inSyncOffset
                    inSyncOffset.remove(start);
                    inSyncOffset.put(startOffset, end);
                    return;
                }

                if (translogReaderProxy.isTranslogPosContinuous(end.getNext(), startOffset.getCurrent())) {
                    // 如果提交段的startOffset跟遍历当期端的endOffset重合
                    // 则还需要进一步判断提交段的endOffset是否跟下一段offset的startOffset重合
                    // 这样可能将3个段合并
                    if (iter.hasNext()) {
                        TranslogOffset startNext = iter.next();
                        if (translogReaderProxy.isTranslogPosContinuous(endOffset.getNext(), startNext.getCurrent())) {
                            TranslogOffset endNext = inSyncOffset.get(startNext);
                            inSyncOffset.remove(start);
                            inSyncOffset.remove(startNext);
                            inSyncOffset.put(start, endNext);
                            return;
                        }
                    }

                    inSyncOffset.remove(start);
                    inSyncOffset.put(start, endOffset);
                    return;
                }
            }

            // 如果inSyncOffset为空，则直接添加提交段
            inSyncOffset.put(startOffset, endOffset);
        }
    }

    private void statisSuccResponse(TranslogSyncResponse response) {
        if (response.getLocalCheckpoint() > this.replicaGlobalCheckpoint) {
            updateCheckpoint(response.getLocalCheckpoint());
        }

        if (response.getMaxSeqNo() > this.replicaMaxSeqNo) {
            this.replicaMaxSeqNo = response.getMaxSeqNo();
        }

        this.successfulSendRequests++;
    }

    private synchronized void commitOffset(TranslogOffset commitOffset) {
        this.commitOffset = commitOffset;
        shardService.updateCommitOffset(dcdrIndexMetadata, commitOffset.getNext());
    }

    private void sendTranslog(List<TranslogOperation> translogList) {
        List<Translog.Operation> translogs = translogList.stream().map(TranslogOperation::getOperation).collect(Collectors.toList());
        TranslogSyncRequest bulkShardOperationsRequest = new TranslogSyncRequest(
            replicaShardId,
            replicaHistoryUuid,
            translogs,
            indexShard.getMaxSeqNoOfUpdatesOrDeletes()
        );
        AtomicInteger retryCounter = new AtomicInteger(0);
        executeBulk(translogList, bulkShardOperationsRequest, retryCounter, System.nanoTime());
    }

    private void executeBulk(
        List<TranslogOperation> translogList,
        TranslogSyncRequest bulkShardOperationsRequest,
        AtomicInteger retryCounter,
        long startTime
    ) {
        this.lastSendTime = startTime;
        if (retryCounter.get() < TRANSLOG_REPLICATION_RETRY_COUNT) {
            AtomicBoolean succ = new AtomicBoolean(false);
            ActionListener<TranslogSyncResponse> listener = ActionListener.wrap((response) -> {
                this.totalSendTimeMillis += TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

                semaphore.release();
                succ.set(true);
                logger.trace(
                    "{}[{}] sendTranslog success, checkpoint={}||maxSeqNo={}",
                    dcdrIndexMetadata,
                    shardId.getId(),
                    response.getLocalCheckpoint(),
                    response.getMaxSeqNo()
                );

                if (response.getLocalCheckpoint() < 0) {
                    logger.info(
                        "{}[{}] peer checkpoint error, checkpoint={}||maxSeqNo={}",
                        dcdrIndexMetadata,
                        shardId.getId(),
                        response.getLocalCheckpoint(),
                        response.getMaxSeqNo()
                    );
                    doRecover(RecoverCase.CheckPointError);
                    return;
                }

                TranslogOperation start = translogList.get(0);
                TranslogOperation end = translogList.get(translogList.size() - 1);
                TranslogOffset startOffset = TranslogOffset.createOffset(start.getCurrent(), start.getNext());
                TranslogOffset endOffset = TranslogOffset.createOffset(end.getCurrent(), end.getNext());

                updateCommitOffsetAndStatis(startOffset, endOffset, response);
            }, (e) -> {
                this.totalSendTimeMillis += TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

                logger.debug("{}[{}] sendTranslog error, retry count={}", dcdrIndexMetadata, shardId.getId(), retryCounter.get(), e);

                this.failedSendRequests.incrementAndGet();

                Throwable exception = ExceptionsHelper.unwrapCause(e);
                if (exception instanceof MapperException) {
                    // 处理mapping动态变更
                    doRecover(RecoverCase.MappingUpdate);
                } else if (shouldRetry(exception)) {
                    int currentRetry = retryCounter.incrementAndGet();
                    long delay = computeDelay(currentRetry, BULK_DELAY_MILLIS);
                    this.threadPool.schedule(() -> {
                        executeBulk(translogList, bulkShardOperationsRequest, retryCounter, System.nanoTime());
                    }, TimeValue.timeValueMillis(delay), DCDR.DCDR_THREAD_POOL_NAME);
                    succ.set(true);
                } else {
                    // 进入recover流程
                    doRecover(RecoverCase.Other);
                }

                if (!succ.get()) {
                    // 失败也要释放，重试无需释放
                    semaphore.release();
                }
            });

            getRemoteClient().execute(TranslogSyncAction.INSTANCE, bulkShardOperationsRequest, listener);
        } else {
            semaphore.release();

            // 进入recover流程
            doRecover(RecoverCase.Other);
        }
    }

    private boolean shouldRetry(Throwable e) {
        if (NetworkExceptionHelper.isConnectException(e)) {
            return true;
        } else if (NetworkExceptionHelper.isCloseConnectionException(e)) {
            return true;
        }

        return e instanceof ShardNotFoundException ||
            e instanceof IllegalIndexShardStateException ||
            e instanceof NoShardAvailableActionException ||
            e instanceof UnavailableShardsException ||
            e instanceof AlreadyClosedException ||
            e instanceof ElasticsearchSecurityException ||
            e instanceof ClusterBlockException ||
            e instanceof IndexClosedException ||
            e instanceof ConnectTransportException ||
            e instanceof NodeClosedException ||
            e instanceof EsRejectedExecutionException ||
            (e.getMessage() != null && e.getMessage().contains("TransportService is closed"));
    }

    private long computeDelay(int currentRetry, int delay) {
        // Cap currentRetry to avoid overflow when computing n variable
        int maxCurrentRetry = Math.min(currentRetry, TRANSLOG_REPLICATION_RETRY_COUNT);
        long n = Math.round(Math.pow(2, maxCurrentRetry - 1));
        int backOffDelay = Math.toIntExact(n) * delay;
        return backOffDelay;
    }

    private void initShardReplication() {
        FetchShardInfoAction.Response fetchResponse = getRemoteClient().execute(
            FetchShardInfoAction.INSTANCE,
            new FetchShardInfoAction.Request(dcdrIndexMetadata.getReplicaIndex(), shardId.id())
        ).actionGet(REMOTE_REQUEST_TIMEOUT);
        DCDRShardInfo replicaShardInfo = fetchResponse.getDcdrShardInfo();
        if (replicaShardInfo == null) {
            logger.info("{}[{}] initShardReplication shard info not found", dcdrIndexMetadata, shardId.getId());
            throw new ShardInitException(
                String.format(
                    Locale.US,
                    "{%s}[{%d}] initShardReplication shard info not found",
                    dcdrIndexMetadata.toString(),
                    shardId.getId()
                )
            );
        }

        replicaShardId = replicaShardInfo.getShardId();
        replicaHistoryUuid = replicaShardInfo.getHistoryUUID();
        long replicaGlobalCheckpoint = replicaShardInfo.getCheckPoint();

        Translog.Location replicaLocation;
        if (replicaGlobalCheckpoint < 0) {
            // 判断replicaGlobalCheckpoint小于0的场景
            if (indexShard.commitStats().getNumDocs() > 0) {
                // 如果主shard有数据，则说明从shard有问题，需要异常恢复
                throw new ShardInitException(
                    String.format(
                        Locale.US,
                        "{%s}[{%d}] initShardReplication shard checkpoint error",
                        dcdrIndexMetadata.toString(),
                        shardId.getId()
                    )
                );
            }

            // 主shard没数据，则location设置为translog的起点
            replicaLocation = translogReaderProxy.getInitLocation();
        } else {
            replicaLocation = translogReaderProxy.getCheckpointLocation(replicaGlobalCheckpoint);

            if (replicaLocation == null) {
                throw new TranslogOffsetBehindException(
                    String.format(
                        Locale.US,
                        "{%s}[{%d}] initShardReplication current checkpoint is expire",
                        dcdrIndexMetadata.toString(),
                        shardId.getId()
                    )
                );
            }
        }

        currentTranslogOffset = TranslogOffset.createOffset(replicaLocation, replicaLocation);
        commitOffset(currentTranslogOffset);
        updateCheckpoint(replicaGlobalCheckpoint);
    }

    @Override
    public void close() throws IOException {
        logger.info("{}[{}] close replication", dcdrIndexMetadata, shardId.getId());
        closed.set(true);
        synchronized (this) {
            if (replicationCancellable != null) {
                replicationCancellable.cancel();
                replicationCancellable = null;
            }

            if (checkCancellable != null) {
                checkCancellable.cancel();
                checkCancellable = null;
            }
        }
    }

    public void doRecover(RecoverCase recoverCase) {
        if (recovering.compareAndSet(false, true)) {
            logger.info("{}[{}] start doRecover", dcdrIndexMetadata, shardId.getId());
            threadPool.executor(DCDR.DCDR_RECOVER_THREAD_POOL_NAME).execute(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    int retryCount = 0;
                    while (true) {
                        if (closed.get()) {
                            return;
                        }

                        if (!running) {
                            return;
                        }

                        try {
                            peerRecoverySourceService.recover(this, recoverCase);
                            break;
                        } catch (Throwable e) {
                            logger.info("{}[{}] doRecover error", dcdrIndexMetadata, shardId.getId(), e);
                            long delay = computeDelay(retryCount, RECOVER_DELAY_MILLIS);
                            retryCount++;

                            try {
                                Thread.sleep(delay);
                            } catch (InterruptedException e1) {
                                logger.error("{}[{}] doRecover sleep error", dcdrIndexMetadata, shardId.getId(), e1);
                            }
                        }
                    }
                    recoverTotalTimeMillis.addAndGet(System.currentTimeMillis() - startTime);
                } finally {
                    recovering.set(false);
                }
            });
        }
    }

    class ReplicaCommand implements Runnable {
        @Override
        public void run() {
            try {
                if (closed.get()) {
                    return;
                }

                long count = replicaTranslog();
                if (count != 0) {
                    logger.trace("{}[{}] send {} translogs to replication", dcdrIndexMetadata, shardId.getId(), count);
                }
            } catch (Throwable e) {
                logger.error("replicaTranslog error", e);
            }
        }
    }

    class  CheckCommond implements Runnable {

        @Override
        public void run() {
            try {
                if (closed.get()) {
                    return;
                }

                statsCheck();
            } catch (Throwable e) {
                logger.error("replicaTranslog error", e);
            }
        }
    }

    private void fetchPrimaryShardInfo() {
        final SeqNoStats seqNoStats = indexShard.seqNoStats();
        this.primaryGlobalCheckpoint = seqNoStats.getGlobalCheckpoint();
        this.primaryMaxSeqNo = seqNoStats.getMaxSeqNo();
    }

    private long updateCheckpoint(long checkPoint) {
        this.replicaGlobalCheckpoint = checkPoint;
        this.lastUpdateReplicaCheckPoint = System.currentTimeMillis();
        return replicaGlobalCheckpoint;
    }

    public synchronized DCDRStats getDCDRStats() {
        final long timeSinceLastSendMillis;
        if (lastSendTime != -1) {
            timeSinceLastSendMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastSendTime);
        } else {
            timeSinceLastSendMillis = 0;
        }

        final long timeSinceUpdateReplicaCheckPoint;
        if (lastUpdateReplicaCheckPoint != -1) {
            timeSinceUpdateReplicaCheckPoint = System.currentTimeMillis() - lastUpdateReplicaCheckPoint;
        } else {
            timeSinceUpdateReplicaCheckPoint = 0;
        }

        fetchPrimaryShardInfo();

        DCDRStats dcdrStats = new DCDRStats();
        dcdrStats.setPrimaryIndex(dcdrIndexMetadata.getPrimaryIndex());
        dcdrStats.setReplicaIndex(dcdrIndexMetadata.getReplicaIndex());
        dcdrStats.setReplicaCluster(dcdrIndexMetadata.getReplicaCluster());
        dcdrStats.setReplicationState(dcdrIndexMetadata.getReplicationState());
        dcdrStats.setShardId(shardId.getId());
        dcdrStats.setPrimaryGlobalCheckpoint(this.primaryGlobalCheckpoint);
        dcdrStats.setPrimaryMaxSeqNo(this.primaryMaxSeqNo);
        dcdrStats.setReplicaGlobalCheckpoint(this.replicaGlobalCheckpoint);
        dcdrStats.setReplicaMaxSeqNo(this.replicaMaxSeqNo);
        dcdrStats.setTimeSinceUpdateReplicaCheckPoint(timeSinceUpdateReplicaCheckPoint);
        dcdrStats.setTotalSendTimeMillis(this.totalSendTimeMillis);
        dcdrStats.setSuccessfulSendRequests(this.successfulSendRequests);
        dcdrStats.setFailedSendRequests(this.failedSendRequests.get());
        dcdrStats.setOperationsSends(this.operationsSends);
        dcdrStats.setBytesSend(this.bytesSend);
        dcdrStats.setTimeSinceLastSendMillis(timeSinceLastSendMillis);
        dcdrStats.setCommitOffsetStr(this.commitOffset.toReadableString());
        dcdrStats.setCurrentOffsetStr(this.currentTranslogOffset.toReadableString());
        dcdrStats.setSyncing(syncing.get());
        dcdrStats.setRecovering(recovering.get());
        dcdrStats.setClosed(closed.get());
        dcdrStats.setAvailableSendBulkNumber(semaphore.availablePermits());
        dcdrStats.setSuccessRecoverCount(successRecoverCount);
        dcdrStats.setFailedRecoverCount(failedRecoverCount);
        dcdrStats.setRecoverTotalTimeMillis(recoverTotalTimeMillis.get());

        List<DCDRStats.TranslogOffsetGapTuple> inSyncOffsetList = new ArrayList<>();
        for (Map.Entry<TranslogOffset, TranslogOffset> entry : inSyncOffset.entrySet()) {
            inSyncOffsetList.add(
                new DCDRStats.TranslogOffsetGapTuple(entry.getKey().toReadableString(), entry.getValue().toReadableString())
            );
        }
        dcdrStats.setInSyncOffset(inSyncOffsetList);

        return dcdrStats;

    }
}

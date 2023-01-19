package org.elasticsearch.dcdr.indices.recovery;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.lucene.store.RateLimiter;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateObserver;
import org.elasticsearch.cluster.action.shard.ShardStateAction;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.dcdr.indices.recovery.RecoveriesCollection.RecoveryRef;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.shard.IndexEventListener;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.indices.cluster.IndicesClusterStateService;
import org.elasticsearch.indices.recovery.*;
import org.elasticsearch.node.NodeClosedException;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportChannel;
import org.elasticsearch.transport.TransportRequestHandler;
import org.elasticsearch.transport.TransportResponse;
import org.elasticsearch.transport.TransportService;

/**
 * author weizijun
 * date：2019-09-06
 */
public class PeerRecoveryTargetService implements IndexEventListener {

    private static final Logger logger = LogManager.getLogger(PeerRecoveryTargetService.class);

    private volatile int maxRecoverShard;
    public static final Setting<Integer> MAX_RECOVER_TARGET_SHARD_SETTING =
        Setting.intSetting(
            "cluster.dcdr.node.recover.target.shard",
            5,
            Setting.Property.Dynamic,
            Setting.Property.NodeScope
        );


    private final ThreadPool threadPool;

    private final TransportService transportService;
    private final IndicesClusterStateService indicesClusterStateService;
    private final IndicesService indicesService;
    private final RecoverySettings recoverySettings;
    private final ClusterService clusterService;
    private final ShardStateAction shardStateAction;
    private final RecoveriesCollection onGoingRecoveries;

    public static class Actions {
        public static final String START_RECOVERY = "internal:index/shard/replica/recovery/start_recovery";
        public static final String FILES_INFO = "internal:index/shard/replica/recovery/filesInfo";
        public static final String FILE_CHUNK = "internal:index/shard/replica/recovery/file_chunk";
        public static final String CLEAN_FILES = "internal:index/shard/replica/recovery/clean_files";
        public static final String FINALIZE = "internal:index/shard/replica/recovery/finalize";
    }

    @Inject
    public PeerRecoveryTargetService(
        ThreadPool threadPool, TransportService transportService,
        IndicesClusterStateService indicesClusterStateService,
        IndicesService indicesService,
        RecoverySettings recoverySettings, ClusterService clusterService,
        ShardStateAction shardStateAction,
        Settings settings, ClusterSettings clusterSettings
    ) {
        this.threadPool = threadPool;
        this.transportService = transportService;
        this.indicesClusterStateService = indicesClusterStateService;
        this.indicesService = indicesService;
        this.recoverySettings = recoverySettings;
        this.clusterService = clusterService;
        this.shardStateAction = shardStateAction;
        this.onGoingRecoveries = new RecoveriesCollection(logger, threadPool, this::waitForClusterState);

        this.indicesClusterStateService.addListener(this);

        this.transportService.registerRequestHandler(
            Actions.START_RECOVERY,
            ThreadPool.Names.GENERIC,
            StartRecoveryRequest::new,
            new StartRecoveryTransportRequestHandler()
        );
        this.transportService.registerRequestHandler(
            Actions.FILES_INFO,
            ThreadPool.Names.GENERIC,
            RecoveryFilesInfoRequest::new,
            new FilesInfoRequestHandler()
        );
        this.transportService.registerRequestHandler(
            Actions.FILE_CHUNK,
            ThreadPool.Names.GENERIC,
            RecoveryFileChunkRequest::new,
            new FileChunkTransportRequestHandler()
        );
        this.transportService.registerRequestHandler(
            Actions.CLEAN_FILES,
            ThreadPool.Names.GENERIC,
            RecoveryCleanFilesRequest::new,
            new CleanFilesRequestHandler()
        );
        this.transportService.registerRequestHandler(
            Actions.FINALIZE,
            ThreadPool.Names.GENERIC,
            RecoveryFinalizeRecoveryRequest::new,
            new FinalizeRecoveryRequestHandler()
        );

        setMaxRecoverShard(MAX_RECOVER_TARGET_SHARD_SETTING.get(settings));
        clusterSettings.addSettingsUpdateConsumer(MAX_RECOVER_TARGET_SHARD_SETTING, this::setMaxRecoverShard);
    }

    public void setMaxRecoverShard(int maxRecoverShard) {
        this.maxRecoverShard = maxRecoverShard;
    }

    @Override
    public void beforeIndexShardClosed(ShardId shardId, @Nullable IndexShard indexShard, Settings indexSettings) {
        if (indexShard != null) {
            onGoingRecoveries.cancelRecoveriesForShard(shardId, "shard closed");
        }
    }

    private StartRecoveryResponse doRecovery(final StartRecoveryRequest request, final long recoveryId) {
        try (RecoveriesCollection.RecoveryRef recoveryRef = onGoingRecoveries.getRecovery(recoveryId)) {
            if (recoveryRef == null) {
                logger.trace("not running recovery with id [{}] - can not find it (probably finished)", recoveryId);
                return null;
            }

            try {
                return recoveryRef.target().startRecovery();
            } catch (final Exception e) {
                // this will be logged as warning later on...
                logger.trace("unexpected error while preparing shard for peer recovery, failing recovery", e);
                onGoingRecoveries.failRecovery(
                    recoveryId,
                    new RecoveryFailedException(request.getShardId(), null, null, "failed to prepare shard for recovery", e),
                    true
                );
                return null;
            }
        }
    }

    public class StartRecoveryTransportRequestHandler implements TransportRequestHandler<StartRecoveryRequest> {
        @Override
        public void messageReceived(final StartRecoveryRequest request, final TransportChannel channel, Task task) throws Exception {
            final IndexService indexService = indicesService.indexServiceSafe(request.getShardId().getIndex());
            final IndexShard indexShard = indexService.getShard(request.getShardId().id());

            // 确认当前节点是primary
            if (!indexShard.routingEntry().primary()) {
                throw new DelayRecoveryException("shard [" + indexShard.routingEntry() + "] is not primary");
            }

            if (onGoingRecoveries.size() >= maxRecoverShard) {
                throw new DelayRecoveryException("current recover target shards is more than " + maxRecoverShard + ", waiting...");
            }

            List<ShardRouting> replicaShards = clusterService.state()
                .routingTable()
                .index(request.getShardId().getIndexName())
                .shard(request.getShardId().id())
                .replicaShards();
            final long recoveryId = onGoingRecoveries.startRecovery(
                indexShard,
                recoverySettings.activityTimeout(),
                shardStateAction,
                replicaShards
            );

            StartRecoveryResponse response = doRecovery(request, recoveryId);
            channel.sendResponse(response);
        }
    }

    class FilesInfoRequestHandler implements TransportRequestHandler<RecoveryFilesInfoRequest> {

        @Override
        public void messageReceived(RecoveryFilesInfoRequest request, TransportChannel channel, Task task) throws Exception {
            try (RecoveryRef recoveryRef = onGoingRecoveries.getRecoverySafe(
                request.recoveryId(),
                request.shardId()
            )) {
                recoveryRef.target()
                    .receiveFileInfo(
                        request.phase1FileNames,
                        request.phase1FileSizes,
                        request.phase1ExistingFileNames,
                        request.phase1ExistingFileSizes,
                        request.totalTranslogOps
                    );
            } catch (final Exception e) {
                // this will be logged as warning later on...
                logger.trace("unexpected error while receiveFileInfo, failing recovery", e);
                onGoingRecoveries.failRecovery(
                    request.recoveryId(),
                    new RecoveryFailedException(request.shardId(), null, null, "failed to receiveFileInfo", e),
                    true
                );
            }
            channel.sendResponse(TransportResponse.Empty.INSTANCE);
        }
    }

    class CleanFilesRequestHandler implements TransportRequestHandler<RecoveryCleanFilesRequest> {

        @Override
        public void messageReceived(RecoveryCleanFilesRequest request, TransportChannel channel, Task task) throws Exception {
            try (RecoveryRef recoveryRef = onGoingRecoveries.getRecoverySafe(
                request.recoveryId(),
                request.shardId()
            )) {
                recoveryRef.target().cleanFiles(request.totalTranslogOps(), request.sourceMetaSnapshot());
            } catch (final Exception e) {
                // this will be logged as warning later on...
                logger.trace("unexpected error while cleanFiles, failing recovery", e);
                onGoingRecoveries.failRecovery(
                    request.recoveryId(),
                    new RecoveryFailedException(request.shardId(), null, null, "failed to cleanFiles", e),
                    true
                );
            }
            channel.sendResponse(TransportResponse.Empty.INSTANCE);
        }
    }

    class FileChunkTransportRequestHandler implements TransportRequestHandler<RecoveryFileChunkRequest> {

        // How many bytes we've copied since we last called RateLimiter.pause
        final AtomicLong bytesSinceLastPause = new AtomicLong();

        @Override
        public void messageReceived(final RecoveryFileChunkRequest request, TransportChannel channel, Task task) throws Exception {
            try (RecoveryRef recoveryRef = onGoingRecoveries.getRecoverySafe(
                request.recoveryId(),
                request.shardId()
            )) {
                final RecoveryTargetHandler recoveryTarget = recoveryRef.target();
                final RecoveryState.Index indexState = recoveryTarget.indexState();
                if (request.sourceThrottleTimeInNanos() != RecoveryState.Index.UNKNOWN) {
                    indexState.addSourceThrottling(request.sourceThrottleTimeInNanos());
                }

                MulitDiskLImiter rateLimiter = recoverySettings.rateLimiter();
                if (rateLimiter != null) {
                    long bytes = bytesSinceLastPause.addAndGet(request.content().length());
                    if (bytes > rateLimiter.getMinPauseCheckBytes()) {
                        // Time to pause
                        bytesSinceLastPause.addAndGet(-bytes);
                        long throttleTimeInNanos = rateLimiter.pause(bytes, recoveryTarget.indexShard());
                        indexState.addTargetThrottling(throttleTimeInNanos);
                        recoveryTarget.indexShard().recoveryStats().addThrottleTime(throttleTimeInNanos);
                    }
                }

                recoveryTarget.writeFileChunk(
                    request.metadata(),
                    request.position(),
                    request.content(),
                    request.lastChunk(),
                    request.totalTranslogOps()
                );
            } catch (final Exception e) {
                // this will be logged as warning later on...
                logger.trace("unexpected error while writeFileChunk, failing recovery", e);
                onGoingRecoveries.failRecovery(
                    request.recoveryId(),
                    new RecoveryFailedException(request.shardId(), null, null, "failed to writeFileChunk", e),
                    true
                );
            }
            channel.sendResponse(TransportResponse.Empty.INSTANCE);
        }
    }

    class FinalizeRecoveryRequestHandler implements TransportRequestHandler<RecoveryFinalizeRecoveryRequest> {

        @Override
        public void messageReceived(RecoveryFinalizeRecoveryRequest request, TransportChannel channel, Task task) throws Exception {
            try (RecoveryRef recoveryRef =
                onGoingRecoveries.getRecoverySafe(request.recoveryId(), request.shardId())) {
                recoveryRef.target().finalizeRecovery(request.globalCheckpoint());

                onGoingRecoveries.markRecoveryAsDone(request.recoveryId());
            } catch (final Exception e) {
                // this will be logged as warning later on...
                logger.trace("unexpected error while finalizeRecovery, failing recovery", e);
                onGoingRecoveries.failRecovery(
                    request.recoveryId(),
                    new RecoveryFailedException(request.shardId(), null, null, "failed to finalizeRecovery", e),
                    true
                );
            }

            channel.sendResponse(TransportResponse.Empty.INSTANCE);
        }
    }

    private void waitForClusterState(long clusterStateVersion) {
        final ClusterState clusterState = clusterService.state();
        ClusterStateObserver observer = new ClusterStateObserver(
            clusterState,
            clusterService,
            TimeValue.timeValueMinutes(5),
            logger,
            threadPool.getThreadContext()
        );
        if (clusterState.getVersion() >= clusterStateVersion) {
            logger.trace(
                "node has cluster state with version higher than {} (current: {})",
                clusterStateVersion,
                clusterState.getVersion()
            );
            return;
        } else {
            logger.trace("waiting for cluster state version {} (current: {})", clusterStateVersion, clusterState.getVersion());
            final PlainActionFuture<Long> future = new PlainActionFuture<>();
            observer.waitForNextChange(new ClusterStateObserver.Listener() {

                @Override
                public void onNewClusterState(ClusterState state) {
                    future.onResponse(state.getVersion());
                }

                @Override
                public void onClusterServiceClose() {
                    future.onFailure(new NodeClosedException(clusterService.localNode()));
                }

                @Override
                public void onTimeout(TimeValue timeout) {
                    future.onFailure(new IllegalStateException("cluster state never updated to version " + clusterStateVersion));
                }
            }, newState -> newState.getVersion() >= clusterStateVersion);
            try {
                long currentVersion = future.get();
                logger.trace("successfully waited for cluster state with version {} (current: {})", clusterStateVersion, currentVersion);
            } catch (Exception e) {
                logger.debug(
                    () -> new ParameterizedMessage(
                        "failed waiting for cluster state with version {} (current: {})",
                        clusterStateVersion,
                        clusterService.state().getVersion()
                    ),
                    e
                );
                throw ExceptionsHelper.convertToRuntime(e);
            }
        }
    }
}

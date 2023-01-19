package org.elasticsearch.dcdr.indices.recovery;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.StopWatch;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.dcdr.translog.primary.ReplicationShardService;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.mapper.MapperException;
import org.elasticsearch.index.shard.IndexEventListener;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.indices.cluster.IndicesClusterStateService;
import org.elasticsearch.indices.recovery.DelayRecoveryException;
import org.elasticsearch.indices.recovery.RecoverySettings;
import org.elasticsearch.transport.TransportService;

/**
 * author weizijun
 * date：2019-09-05
 */
public class PeerRecoverySourceService implements IndexEventListener {

    private static final Logger logger = LogManager.getLogger(PeerRecoverySourceService.class);

    private volatile int maxRecoverShard;
    public static final Setting<Integer> MAX_RECOVER_SOURCE_SHARD_SETTING =
        Setting.intSetting(
            "cluster.dcdr.node.recover.source.shard",
            5,
            Setting.Property.Dynamic,
            Setting.Property.NodeScope
        );

    private final TransportService transportService;
    private final IndicesClusterStateService indicesClusterStateService;
    private final IndicesService indicesService;
    private final RecoverySettings recoverySettings;
    private final ClusterService clusterService;

    final OngoingRecoveries ongoingRecoveries = new OngoingRecoveries();

    @Inject
    public PeerRecoverySourceService(
        ClusterService clusterService, TransportService transportService, IndicesService indicesService,
        IndicesClusterStateService indicesClusterStateService,
        RecoverySettings recoverySettings,
        Settings settings, ClusterSettings clusterSettings
    ) {
        this.clusterService = clusterService;
        this.indicesClusterStateService = indicesClusterStateService;
        this.transportService = transportService;
        this.indicesService = indicesService;
        this.recoverySettings = recoverySettings;
        this.indicesClusterStateService.addListener(this);
        setMaxRecoverShard(MAX_RECOVER_SOURCE_SHARD_SETTING.get(settings));
        clusterSettings.addSettingsUpdateConsumer(MAX_RECOVER_SOURCE_SHARD_SETTING, this::setMaxRecoverShard);
    }

    public void setMaxRecoverShard(int maxRecoverShard) {
        this.maxRecoverShard = maxRecoverShard;
    }

    @Override
    public void beforeIndexShardClosed(
        ShardId shardId,
        @Nullable IndexShard indexShard,
        Settings indexSettings
    ) {
        if (indexShard != null) {
            ongoingRecoveries.cancel(indexShard, "shard is closed");
        }
    }

    public void recover(ReplicationShardService replicationShardService, RecoverCase recoverCase) throws IOException {
        if (recoverCase == RecoverCase.MappingUpdate) {
            IndexMetaData indexMetaData = clusterService.state().metaData().index(replicationShardService.getShardId().getIndex());
            indexMetaData.getMappings().forEach((cursor) -> {
                AcknowledgedResponse response = replicationShardService.getRemoteClient()
                    .admin()
                    .indices()
                    .preparePutMapping(replicationShardService.getReplicaIndex())
                    .setType(cursor.key)
                    .setSource(cursor.value.sourceAsMap())
                    .get();
                if (!response.isAcknowledged()) {
                    throw new MapperException("[" + replicationShardService.getDcdrIndexMetadata() + "] update mappings exception");
                }
            });
        }

        startRecover(replicationShardService);
    }

    private void startRecover(ReplicationShardService replicationShardService) throws IOException {
        final IndexService indexService = indicesService.indexServiceSafe(replicationShardService.getShardId().getIndex());
        final IndexShard shard = indexService.getShard(replicationShardService.getShardId().id());

        final ShardRouting routingEntry = shard.routingEntry();

        if (routingEntry.primary() == false || routingEntry.active() == false) {
            throw new DelayRecoveryException("source shard [" + routingEntry + "] is not an active primary");
        }

        StopWatch stopWatch = new StopWatch().start();

        RecoverySourceHandler handler = ongoingRecoveries.addNewRecovery(replicationShardService, shard);

        logger.trace(
            "[{}][{}] starting recovery",
            replicationShardService.getDcdrIndexMetadata(),
            replicationShardService.getShardId().id()
        );
        try {
            handler.init();
            handler.recoverToTarget();
        } finally {
            ongoingRecoveries.remove(shard, handler);
        }
        stopWatch.stop();
        logger.info(
            "[{}][{}] recovery done, cost={}",
            replicationShardService.getDcdrIndexMetadata(),
            replicationShardService.getShardId().id(),
            stopWatch.totalTime().millis()
        );
    }

    final class OngoingRecoveries {
        private final Map<IndexShard, ShardRecoveryManager> ongoingRecoveries = new ConcurrentHashMap<>();

        synchronized RecoverySourceHandler addNewRecovery(ReplicationShardService replicationShardService, IndexShard shard) {
            final ShardRecoveryManager shardContext = ongoingRecoveries.computeIfAbsent(
                shard,
                s -> new OngoingRecoveries.ShardRecoveryManager()
            );
            RecoverySourceHandler handler = shardContext.addNewRecovery(replicationShardService, shard);
            shard.recoveryStats().incCurrentAsSource();
            return handler;
        }

        synchronized void remove(IndexShard shard, RecoverySourceHandler handler) {
            final ShardRecoveryManager shardRecoveryContext = ongoingRecoveries.get(shard);
            assert shardRecoveryContext != null : "Shard was not registered [" + shard + "]";
            boolean remove = shardRecoveryContext.recoveryHandlers.remove(handler);
            assert remove : "Handler was not registered [" + handler + "]";
            if (remove) {
                shard.recoveryStats().decCurrentAsSource();
            }
            if (shardRecoveryContext.recoveryHandlers.isEmpty()) {
                ongoingRecoveries.remove(shard);
            }
        }

        synchronized void cancel(IndexShard shard, String reason) {
            final ShardRecoveryManager shardRecoveryContext = ongoingRecoveries.get(shard);
            if (shardRecoveryContext != null) {
                final List<Exception> failures = new ArrayList<>();
                for (RecoverySourceHandler handlers : shardRecoveryContext.recoveryHandlers) {
                    try {
                        handlers.cancel(reason);
                    } catch (Exception ex) {
                        failures.add(ex);
                    } finally {
                        shard.recoveryStats().decCurrentAsSource();
                    }
                }
                ExceptionsHelper.maybeThrowRuntimeAndSuppress(failures);
            }
        }

        private final class ShardRecoveryManager {
            final Set<RecoverySourceHandler> recoveryHandlers = new HashSet<>();

            /**
             * Adds recovery source handler.
             */
            synchronized RecoverySourceHandler addNewRecovery(ReplicationShardService replicationShardService, IndexShard shard) {
                // TODO check exists now?
                // for (RecoverySourceHandler existingHandler : recoveryHandlers) {
                // if (existingHandler.getResponse().targetAllocationId().equals(request.targetAllocationId())) {
                // throw new DelayRecoveryException("recovery with same target already registered, waiting for " +
                // "previous recovery attempt to be cancelled or completed");
                // }
                // }

                if (recoveryHandlers.size() >= maxRecoverShard) {
                    throw new DelayRecoveryException("current recover source shards is more than " + maxRecoverShard + ", waiting...");
                }

                RecoverySourceHandler handler = createRecoverySourceHandler(replicationShardService, shard);
                recoveryHandlers.add(handler);
                return handler;
            }

            private RecoverySourceHandler createRecoverySourceHandler(ReplicationShardService replicationShardService, IndexShard shard) {
                return new RecoverySourceHandler(shard, transportService, recoverySettings, replicationShardService);
            }
        }
    }
}

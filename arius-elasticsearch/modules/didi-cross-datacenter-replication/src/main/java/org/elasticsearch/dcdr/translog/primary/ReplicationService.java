package org.elasticsearch.dcdr.translog.primary;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.routing.RoutingNode;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.SuppressLoggerChecks;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.dcdr.indices.recovery.PeerRecoverySourceService;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;

/**
 * author weizijun
 * date：2019-08-08
 */
@SuppressLoggerChecks(reason = "safely delegates to logger")
public class ReplicationService implements ClusterStateListener {
    private static final Logger logger = LogManager.getLogger(ReplicationService.class);

    private ThreadPool threadPool;
    private ClusterService clusterService;
    private IndicesService indicesService;
    private Client client;
    private PeerRecoverySourceService peerRecoverySourceService;
    private Map<String, ReplicationIndexService> replicationIndices = new HashMap<>();
    private ReplicationNodesService replicationNodesService;

    @Inject
    public ReplicationService(
        Client client, ClusterService clusterService, IndicesService indicesService, ThreadPool threadPool,
        PeerRecoverySourceService peerRecoverySourceService
    ) {
        clusterService.addListener(this);
        this.threadPool = threadPool;
        this.clusterService = clusterService;
        this.indicesService = indicesService;
        this.client = client;
        this.peerRecoverySourceService = peerRecoverySourceService;
        this.replicationNodesService = new ReplicationNodesService(client, threadPool, peerRecoverySourceService);
    }

    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        processIndexDCDRChangeEvent(event);

        if (event.localNodeMaster()) {
            processIndexDeletedEvent(event);
        }
    }

    private void processIndexDeletedEvent(ClusterChangedEvent event) {
        DCDRMetadata dcdrMetadata = event.state().metaData().custom(DCDRMetadata.TYPE);

        if (dcdrMetadata == null) {
            return;
        }

        Map<String, DCDRIndexMetadata> clusterReplicaIndices = dcdrMetadata.getReplicaIndices();
        Map<String, DCDRIndexMetadata> shouldDeleteDCDRIndexMetadataMap = new HashMap<>();
        ImmutableOpenMap<String, IndexMetaData> clusterIndices = event.state().metaData().indices();

        for (DCDRIndexMetadata dcdrIndexMetadata : clusterReplicaIndices.values()) {
            if (clusterIndices.containsKey(dcdrIndexMetadata.getPrimaryIndex())) {
                continue;
            }
            shouldDeleteDCDRIndexMetadataMap.put(dcdrIndexMetadata.name(), dcdrIndexMetadata);
        }

        if (shouldDeleteDCDRIndexMetadataMap.isEmpty()) {
            return;
        }

        clusterService.submitStateUpdateTask(
            "batch-delete-index-replication-when-index-deleted",
            new ClusterStateUpdateTask() {
                @Override
                public String taskType() {
                    return "batch-delete-index-replication-when-index-deleted";
                }

                @Override
                public ClusterState execute(ClusterState currentState) throws Exception {
                    ClusterState.Builder newState = ClusterState.builder(currentState);
                    DCDRMetadata dcdrMetadata = currentState.metaData().custom(DCDRMetadata.TYPE);
                    if (dcdrMetadata == null) {
                        dcdrMetadata = DCDRMetadata.EMPTY;
                    }

                    SortedMap<String, DCDRIndexMetadata> replicaIndices = new TreeMap<>(dcdrMetadata.getReplicaIndices());

                    for (DCDRIndexMetadata shouldDeleteDCDRIndexMetadata : shouldDeleteDCDRIndexMetadataMap.values()) {
                        replicaIndices.remove(shouldDeleteDCDRIndexMetadata.name());
                    }

                    DCDRMetadata newMetadata = new DCDRMetadata(replicaIndices, dcdrMetadata.getReplicaTemplates());
                    newState.metaData(
                        MetaData.builder(currentState.getMetaData())
                            .putCustom(DCDRMetadata.TYPE, newMetadata)
                            .build()
                    );
                    return newState.build();

                }

                @Override
                public void onFailure(String source, Exception e) {
                    // TODO ZHZ 失败重试
                    logger.error(
                        new ParameterizedMessage(
                            "Failed to batch-delete-index-replication-when-index-deleted for [{}]",
                            shouldDeleteDCDRIndexMetadataMap.keySet()
                        ),
                        e
                    );

                }

                @Override
                public TimeValue timeout() {
                    return super.timeout();
                }

                @Override
                public Priority priority() {
                    return Priority.URGENT;
                }

            }
        );

    }

    private void processIndexDCDRChangeEvent(ClusterChangedEvent event) {
        DCDRMetadata dcdrMetadata = event.state().metaData().custom(DCDRMetadata.TYPE);

        // TODO dcdrMetadata 为null或者为empty的处理

        RoutingNode localRoutingNode = event.state().getRoutingNodes().node(event.state().nodes().getLocalNodeId());
        if (localRoutingNode == null) {
            return;
        }

        // node级别，按index构建shard的map
        Map<String, Set<ShardId>> indices = new HashMap<>();
        for (ShardRouting shardRouting : localRoutingNode) {
            if (!shardRouting.primary()) {
                continue;
            }

            Set<ShardId> shards = indices.get(shardRouting.index().getName());
            if (shards == null) {
                shards = new HashSet<>();
                indices.put(shardRouting.index().getName(), shards);
            }

            shards.add(shardRouting.shardId());
        }

        // dcdr配置级别，按index构建dcdr index map
        Map<String, Set<DCDRIndexMetadata>> indicesDcdr = new HashMap<>();
        Map<String, DCDRIndexMetadata> dcdrs = dcdrMetadata != null ? dcdrMetadata.getReplicaIndices() : new HashMap<>();
        dcdrs.forEach((k, dcdrIndexMetadata) -> {
            Set<DCDRIndexMetadata> dcdrSet = indicesDcdr.get(dcdrIndexMetadata.getPrimaryIndex());
            if (dcdrSet == null) {
                dcdrSet = new HashSet<>();
                indicesDcdr.put(dcdrIndexMetadata.getPrimaryIndex(), dcdrSet);
            }

            dcdrSet.add(dcdrIndexMetadata);
        });

        // 获得node和dcdr配置的共同index
        Set<String> addIndices = new HashSet<>();
        indices.forEach((index, shards) -> {
            if (indicesDcdr.containsKey(index)) {
                addIndices.add(index);
            }
        });

        logger.debug("node[{}] have (count={}) index in dcdr", localRoutingNode.nodeId(), addIndices.size());

        // 下线不存在的index
        List<DCDRIndexMetadata> removed = replicationIndices.entrySet()
            .stream()
            .filter(e -> !addIndices.contains(e.getValue().getDcdrIndexMetadata().getPrimaryIndex()))
            .map(e -> e.getValue().getDcdrIndexMetadata())
            .collect(Collectors.toList());

        logger.debug("node[{}] remove (count={}) dcdr index", localRoutingNode.nodeId(), removed.size());

        for (DCDRIndexMetadata dcdrIndexMetadata : removed) {
            try {
                ReplicationIndexService replicationIndexService = replicationIndices.remove(dcdrIndexMetadata.name());
                if (replicationIndexService != null) {
                    replicationIndexService.close();
                }
            } catch (IOException e) {
                logger.warn("{} replicationIndexService close error", dcdrIndexMetadata, e);
            }
        }

        // 检查index的shard是否需要变更
        addIndices.forEach(
            (index) -> {
                Set<ShardId> shards = indices.get(index);
                Set<DCDRIndexMetadata> dcdrSet = indicesDcdr.get(index);
                dealDcdrIndexChange(shards, dcdrSet);
            }
        );
    }

    private void dealDcdrIndexChange(Set<ShardId> shards, Set<DCDRIndexMetadata> dcdrSet) {
        for (DCDRIndexMetadata dcdrIndexMetadata : dcdrSet) {
            ReplicationIndexService replicationIndexService = replicationIndices.get(dcdrIndexMetadata.name());
            if (replicationIndexService == null) {
                replicationIndexService = new ReplicationIndexService(
                    dcdrIndexMetadata,
                    indicesService,
                    replicationNodesService
                );
                replicationIndices.put(dcdrIndexMetadata.name(), replicationIndexService);
            }

            replicationIndexService.resetShards(shards);

            // 设置复制的状态
            replicationIndexService.resetReplicationState(dcdrIndexMetadata.getReplicationState());
        }
    }

    public ReplicationIndexService getReplicationIndexService(DCDRIndexMetadata dcdrIndexMetadata) {
        if (replicationIndices.containsKey(dcdrIndexMetadata.name())) {
            return replicationIndices.get(dcdrIndexMetadata.name());
        }
        return null;
    }
}

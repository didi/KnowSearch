package org.elasticsearch.dcdr.translog.primary;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.metadata.MetaDataIndexTemplateService;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.SuppressLoggerChecks;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.dcdr.action.CreateReplicationAction;

import com.carrotsearch.hppc.cursors.ObjectCursor;

/**
 * author weizijun
 * date：2019-08-08
 */
@SuppressLoggerChecks(reason = "safely delegates to logger")
public class ReplicationTemplateService implements ClusterStateListener {
    private static final Logger logger = LogManager.getLogger(ReplicationTemplateService.class);

    private Client client;
    private ClusterService clusterService;

    @Inject
    public ReplicationTemplateService(Client client, ClusterService clusterService) {
        clusterService.addListener(this);
        this.clusterService = clusterService;
        this.client = client;
    }

    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        if (event.localNodeMaster()) {
            createIndexReplication(event.state());
            clearTemplateReplicaForDeletedTemplate(event.state());
        }
    }

    private void clearTemplateReplicaForDeletedTemplate(ClusterState state) {
        DCDRMetadata dcdrMetadata = state.metaData().custom(DCDRMetadata.TYPE);
        if (dcdrMetadata == null) {
            return;
        }

        Map<String, DCDRTemplateMetadata> clusterReplicaTemplates = dcdrMetadata.getReplicaTemplates();
        Map<String, DCDRTemplateMetadata> shouldDeleteDCDRTemplateMetadataMap = new HashMap<>();
        ImmutableOpenMap<String, IndexTemplateMetaData> clusterTemplates = state.metaData().templates();

        for (DCDRTemplateMetadata dcdrTemplateMetadata : clusterReplicaTemplates.values()) {
            if (clusterTemplates.containsKey(dcdrTemplateMetadata.getTemplate())) {
                continue;
            }
            shouldDeleteDCDRTemplateMetadataMap.put(dcdrTemplateMetadata.getName(), dcdrTemplateMetadata);
        }

        if (shouldDeleteDCDRTemplateMetadataMap.isEmpty()) {
            return;
        }

        clusterService.submitStateUpdateTask(
            "batch-delete-template-replication-when-template-deleted",
            new ClusterStateUpdateTask() {
                @Override
                public String taskType() {
                    return "batch-delete-template-replication-when-template-deleted";
                }

                @Override
                public ClusterState execute(ClusterState currentState) throws Exception {
                    ClusterState.Builder newState = ClusterState.builder(currentState);
                    DCDRMetadata dcdrMetadata = currentState.metaData().custom(DCDRMetadata.TYPE);
                    if (dcdrMetadata == null) {
                        dcdrMetadata = DCDRMetadata.EMPTY;
                    }

                    SortedMap<String, DCDRTemplateMetadata> replicaTemplates = new TreeMap<>(dcdrMetadata.getReplicaTemplates());

                    for (DCDRTemplateMetadata shouldDeleteDCDRTemplateMetadata : shouldDeleteDCDRTemplateMetadataMap.values()) {
                        replicaTemplates.remove(shouldDeleteDCDRTemplateMetadata.getName());
                    }

                    DCDRMetadata newMetadata = new DCDRMetadata(dcdrMetadata.getReplicaIndices(), replicaTemplates);
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
                            "Failed to batch-delete-template-replication-when-template-deleted for [{}]",
                            shouldDeleteDCDRTemplateMetadataMap.keySet()
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

    private void createIndexReplication(final ClusterState state) {
        DCDRMetadata dcdrMetadata = state.metaData().custom(DCDRMetadata.TYPE);
        if (dcdrMetadata == null) {
            return;
        }

        // 集群所有的索引集合
        ImmutableOpenMap<String, IndexMetaData> clusterTotalIndexMetaData = state.metaData().indices();
        if (clusterTotalIndexMetaData == null) {
            return;
        }

        Set<String> dcdrIndices;
        // 找到已经创建dcdr链路的索引集合
        Map<String, DCDRIndexMetadata> replicaIndices = dcdrMetadata.getReplicaIndices();
        if (replicaIndices != null) {
            dcdrIndices = dcdrMetadata.getReplicaIndices().keySet();
        } else {
            dcdrIndices = new HashSet<>();
        }

        // 平台dcdr模板配置
        Map<String, DCDRTemplateMetadata> totalDCDRTemplateMetadataMap = dcdrMetadata.getReplicaTemplates();
        Map<String, List<DCDRTemplateMetadata>> totalTemplate2DCDRTemplateMetadataMap = new HashMap<>();
        if (totalDCDRTemplateMetadataMap != null) {
            for (DCDRTemplateMetadata dcdrTemplateMetadata : totalDCDRTemplateMetadataMap.values()) {
                if (!totalTemplate2DCDRTemplateMetadataMap.containsKey(dcdrTemplateMetadata.getTemplate())) {
                    List<DCDRTemplateMetadata> dcdrTemplateMetadataList = new ArrayList<>();
                    totalTemplate2DCDRTemplateMetadataMap.put(dcdrTemplateMetadata.getTemplate(), dcdrTemplateMetadataList);
                }
                totalTemplate2DCDRTemplateMetadataMap.get(dcdrTemplateMetadata.getTemplate()).add(dcdrTemplateMetadata);
            }
        }

        for (ObjectCursor<String> objectCursor : clusterTotalIndexMetaData.keys()) {
            IndexMetaData indexMetaData = clusterTotalIndexMetaData.get(objectCursor.value);

            // 找到匹配的dcdr模板
            Map<String, DCDRTemplateMetadata> matchedCluster2DCDRTemplateMetadataMap = findMatchedDCDRTemplate(
                indexMetaData,
                totalTemplate2DCDRTemplateMetadataMap,
                dcdrIndices,
                state
            );

            if (matchedCluster2DCDRTemplateMetadataMap.size() > 0) {
                // 创建dcdr链路
                createReplication(objectCursor.value, matchedCluster2DCDRTemplateMetadataMap);
            }
        }
    }

    /**
     * 获取匹配到的dcdr模板
     * @param indexMetaData indexMetaData
     * @param totalTemplate2DCDRTemplateMetadataMap totalTemplate2DCDRTemplateMetadataMap
     * @param dcdrIndices dcdrIndices
     * @param state state
     * @return Map
     */
    private Map<String, DCDRTemplateMetadata> findMatchedDCDRTemplate(
        IndexMetaData indexMetaData,
        Map<String, List<DCDRTemplateMetadata>> totalTemplate2DCDRTemplateMetadataMap,
        Set<String> dcdrIndices,
        ClusterState state
    ) {
        List<DCDRTemplateMetadata> matchedDCDRTemplateMetadatas = new ArrayList<>();
        String indexTemplate = indexMetaData.getSettings().get("index.template");
        if (Strings.hasLength(indexTemplate)) {
            List<DCDRTemplateMetadata> dcdrTemplateMetadatas = totalTemplate2DCDRTemplateMetadataMap.get(indexTemplate);
            if (dcdrTemplateMetadatas != null) {
                matchedDCDRTemplateMetadatas.addAll(dcdrTemplateMetadatas);
            }
        } else {
            List<IndexTemplateMetaData> matchedTemplates = MetaDataIndexTemplateService.findTemplates(
                state.metaData(),
                indexMetaData.getIndex().getName()
            );
            for (IndexTemplateMetaData templateMetaData : matchedTemplates) {
                List<DCDRTemplateMetadata> dcdrTemplateMetadatas = totalTemplate2DCDRTemplateMetadataMap.get(templateMetaData.getName());
                if (dcdrTemplateMetadatas != null) {
                    matchedDCDRTemplateMetadatas.addAll(dcdrTemplateMetadatas);
                }
            }
        }

        Map<String, DCDRTemplateMetadata> result = new HashMap<>();
        for (DCDRTemplateMetadata dcdrTemplateMetadata : matchedDCDRTemplateMetadatas) {
            String indexName = indexMetaData.getIndex().getName();

            if (dcdrIndices.contains(DCDRIndexMetadata.name(indexName, indexName, dcdrTemplateMetadata.getReplicaCluster()))) {
                // 该链路已经存在
                continue;
            }

            result.putIfAbsent(dcdrTemplateMetadata.getReplicaCluster(), dcdrTemplateMetadata);
        }

        return result;
    }

    private void createReplication(String index, Map<String, DCDRTemplateMetadata> dcdrTemplateMetadataMap) {
        for (DCDRTemplateMetadata dcdrTemplateMetadata : dcdrTemplateMetadataMap.values()) {
            try {
                doCreateReplication(index, dcdrTemplateMetadata);
            } catch (Exception e) {
                logger.error("create index dcdr replication error [{}]", dcdrTemplateMetadata, e);
            }
        }
    }

    private void doCreateReplication(String index, DCDRTemplateMetadata dcdrTemplateMetadata) {
        CreateReplicationAction.Request request = new CreateReplicationAction.Request();
        request.setPrimaryIndex(index);
        request.setReplicaIndex(index);
        request.setReplicaCluster(dcdrTemplateMetadata.getReplicaCluster());

        client.execute(CreateReplicationAction.INSTANCE, request, new ActionListener<AcknowledgedResponse>() {
            @Override
            public void onResponse(AcknowledgedResponse acknowledgedResponse) {
                logger.debug("[{}][{}] create dcdr replication succ", dcdrTemplateMetadata, index);
            }

            @Override
            @SuppressLoggerChecks(reason = "safely delegates to logger")
            public void onFailure(Exception e) {
                // TODO check on failure
                logger.error("[{}][{}] create dcdr replication fail", dcdrTemplateMetadata, index, e);
            }
        });
    }

}

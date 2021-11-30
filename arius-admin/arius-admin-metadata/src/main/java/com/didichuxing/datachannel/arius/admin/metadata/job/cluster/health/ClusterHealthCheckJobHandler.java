package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckWhiteListPO;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.cluster.HealthCheckESDAO;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem.*;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

/**
 *
 */
@Component
@NoArgsConstructor
public class ClusterHealthCheckJobHandler extends AbstractMetaDataJob {

    @Autowired
    private HealthCheckESDAO healthCheckEsDao;

    @Autowired
    private TemplatePhyService templatePhyService;

    @Autowired
    private ClusterPhyService phyClusterService;

    @Autowired
    private AriusStatsIndexInfoESDAO ariusStatsInfoEsDao;

    @Value("${health.check.empty.indices.threshold.day}")
    private int     emptyIndicesThresholdDay;

    @Value("${health.check.index.doc.averge.size}")
    private int     indexDocSizeAverageSize;

    @Value("${health.check.index.doc.result.level}")
    private String  indexDocSizeResultLevel;

    @Value("${health.check.index.mapping.large.size}")
    private int     indexMappingLargeSize;

    @Value("${health.check.index.mapping.result.level}")
    private String  indexMappingResultLevel;

    @Value("${health.check.small.shard.size}")
    private int     smallShardSize;

    @Value("${health.check.small.shard.result.level}")
    private String  smallShardResultLevel;

    @Value("${health.check.small.shard.repair}")
    private boolean mallShardRepair;

    /**
     * 访问es集群客户端
     */
    @Autowired
    private ESOpClient esOpClient;

    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=ClusterHealthCheckJobHandler||method=handleJobTask||params={}", params);

        List<ICheckerItem> checkerItems  = genCheckItems();

        List<ClusterPhy> esClusterPhies = phyClusterService.listAllClusters();

        List<HealthCheckWhiteListPO> checkWhiteListPos = new ArrayList<>();

        for(ClusterPhy clusterPhy : esClusterPhies){
            String   clustr   = clusterPhy.getCluster();
            ESClient esClient = esOpClient.getESClient(clustr);

            List<IndexTemplatePhy> indexTemplatePhies = templatePhyService.getTemplateByClusterAndStatus(clustr, 1);

            if(CollectionUtils.isEmpty(indexTemplatePhies)){
                continue;
            }


            Map<String, IndexTemplatePhyWithLogic> indexTemplateMap = indexTemplatePhies.parallelStream().collect(
                    Collectors.toMap(IndexTemplatePhy::getName, i -> templatePhyService.getTemplateWithLogicById(i.getId()), (i1, i2) -> i1)
            );

            ClusterHealthCheckJobConfig clusterClusterHealthCheckJobConfig = genHealthCheckConfig(healthCheckEsDao, ariusStatsInfoEsDao,
                                                                             indexTemplateMap, esClient, clustr, checkWhiteListPos);

            for(ICheckerItem iCheckerItem : checkerItems){
                iCheckerItem.exec( clusterClusterHealthCheckJobConfig );
            }
        }

        return JOB_SUCCESS;
    }

    /**************************************** private methods ****************************************/
    private ClusterHealthCheckJobConfig genHealthCheckConfig(HealthCheckESDAO healthCheckEsDao,
                                                      AriusStatsIndexInfoESDAO ariusStatsInfoEsDao,
                                                      Map<String, IndexTemplatePhyWithLogic> indexTemplateMap,
                                                      ESClient esClient, String cluster,
                                                      List<HealthCheckWhiteListPO> checkWhiteListPos){
        ClusterHealthCheckJobConfig clusterClusterHealthCheckJobConfig = new ClusterHealthCheckJobConfig();
        clusterClusterHealthCheckJobConfig.setClusterName(cluster);
        clusterClusterHealthCheckJobConfig.setEsClient(esClient);
        clusterClusterHealthCheckJobConfig.setHealthCheckEsDao(healthCheckEsDao);
        clusterClusterHealthCheckJobConfig.setAriusStatsInfoEsDao(ariusStatsInfoEsDao);
        clusterClusterHealthCheckJobConfig.setIndexTemplateMap(indexTemplateMap);
        clusterClusterHealthCheckJobConfig.setHealthCheckWhiteListPOS(checkWhiteListPos);
        clusterClusterHealthCheckJobConfig.setEmptyIndicesThresholdDay(emptyIndicesThresholdDay);
        clusterClusterHealthCheckJobConfig.setIndexDocSizeAverageSize(indexDocSizeAverageSize);
        clusterClusterHealthCheckJobConfig.setIndexDocSizeResultLevel(indexDocSizeResultLevel);
        clusterClusterHealthCheckJobConfig.setIndexMappingLargeSize(indexMappingLargeSize);
        clusterClusterHealthCheckJobConfig.setIndexMappingResultLevel(indexMappingResultLevel);
        clusterClusterHealthCheckJobConfig.setSmallShardRepair(mallShardRepair);
        clusterClusterHealthCheckJobConfig.setSmallShardSize(smallShardSize);
        clusterClusterHealthCheckJobConfig.setSmallShardResultLevel(smallShardResultLevel);

        return clusterClusterHealthCheckJobConfig;
    }

    private List<ICheckerItem> genCheckItems(){
        List<ICheckerItem>  checkerItems = new ArrayList<>();

        checkerItems.add(new ClusterStatusItem());
        checkerItems.add(new EmptyIndicesItem());
        checkerItems.add(new IndexDocsSizeItem());
        checkerItems.add(new IndexExpireNotDeleteItem());
        checkerItems.add(new IndexMappingItem());
        checkerItems.add(new InvalidIndicesInEsItem());
        checkerItems.add(new LargeShardsItem());
        checkerItems.add(new NodeShardTooMuchItem());
        checkerItems.add(new PendingTaskNumItem());
        checkerItems.add(new PriRepShardDocsCountItem());
        checkerItems.add(new ShardIsAveragingItem());
        checkerItems.add(new ShardDocCountLargeItem());
        checkerItems.add(new SmallShardsItem());
        checkerItems.add(new UnassignedShardsItem());

        return checkerItems;
    }
}

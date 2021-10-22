package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health;

import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckWhiteListPO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.cluster.HealthCheckESDAO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterHealthCheckJobConfig {
    private AriusStatsIndexInfoESDAO ariusStatsInfoEsDao;
    private HealthCheckESDAO healthCheckEsDao;
    private ESClient                        esClient;

    private Map<String, IndexTemplatePhyWithLogic>  indexTemplateMap        = new HashMap<>();
    private List<HealthCheckWhiteListPO> healthCheckWhiteListPOS = new ArrayList<>();

    private String             clusterName;

    private int                emptyIndicesThresholdDay = 30;

    private int                indexDocSizeAverageSize = 2048;
    private String             indexDocSizeResultLevel;

    private int                indexMappingLargeSize = 5000;
    private String             indexMappingResultLevel;

    private int                largeShardDocNum = 50000000;
    private String             largeShardDocNumResultLevel;

    private int                shardTooMuchShardNu = 1000;

    private int                largeShardSize = 1000000000;
    private String             largeShardResultLevel;

    private int                smallShardSize = 60;
    private String             smallShardResultLevel;
    private boolean            smallShardRepair;
}

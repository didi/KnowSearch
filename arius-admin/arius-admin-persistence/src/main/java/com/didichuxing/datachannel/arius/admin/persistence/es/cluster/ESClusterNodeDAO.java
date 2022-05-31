package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ESClusterNodesStatsResponse;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_OPERATE_TIMEOUT;

/**
 * @author d06679
 */
@Repository
public class ESClusterNodeDAO extends BaseESDAO {

    /**
     * 获取节点上的索引个数
     * @param cluster 集群
     * @param nodes 节点
     * @return 个数
     */
    public int getIndicesCount(String cluster, String nodes) {
        ESClient client = esOpClient.getESClient(cluster);
        ESClusterNodesStatsResponse response = client.admin().cluster().prepareNodeStats().setNodesIds(nodes)
            .setIndices(true).level("indices").execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        int count = 0;
        Map<String, ClusterNodeStats> nodeStatsMap = response.getNodes();
        for (ClusterNodeStats nodeStats : nodeStatsMap.values()) {
            count += nodeStats.getIndices().getIndices().size();
        }

        return count;
    }

}
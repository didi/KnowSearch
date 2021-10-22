package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.nodessetting.ESClusterNodesSettingResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.nodesstats.ESClusterNodesStatsResponse;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.ES_OPERATE_TIMEOUT;

/**
 * @author d06679
 */
@Repository
public class ESClusterNodeDAO extends BaseESDAO {

    private static final ILog LOGGER = LogFactory.getLog(ESClusterNodeDAO.class);

    /**
     * 获取集群node属性
     * @param cluster 集群名称
     * @return client原生对象列表
     */
    public Map<String, ClusterNodeSettings> getSettingsByCluster(String cluster) {
        try {
            ESClient client = esOpClient.getESClient(cluster);
            ESClusterNodesSettingResponse response = client.admin().cluster().prepareNodesSetting().execute()
                .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
            return response.getNodes();
        } catch (Exception e) {
            LOGGER.warn("method=getSettingsByCluster||cluster={}||mg=get es setting fail", cluster, e);
            return null;
        }
    }

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
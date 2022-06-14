package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.ES_OPERATE_TIMEOUT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.didiglobal.logi.elasticsearch.client.request.cluster.nodestats.ESClusterNodesStatsRequest;
import com.didiglobal.logi.elasticsearch.client.request.query.query.ESQueryAction;
import com.didiglobal.logi.elasticsearch.client.request.query.query.ESQueryRequest;
import com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ESClusterNodesStatsResponse;
import com.google.common.collect.Lists;

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

    public List<ClusterNodeStats> syncGetNodesStats(String clusterName) {
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (esClient == null) {
            LOGGER.error(
                "class=ESClusterNodeServiceImpl||method=syncGetNodeFsStatsMap||clusterName={}||errMsg=esClient is null",
                clusterName);
            return Lists.newArrayList();
        }
        ESClusterNodesStatsResponse response = esClient.admin().cluster().prepareNodeStats().setFs(true).setOs(true)
            .setJvm(true).setThreadPool(true).level("node").execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        if (response.getNodes() != null) {
            return new ArrayList<>(response.getNodes().values());

        }
        return Lists.newArrayList();
    }

    /**
     * 获取nodes信息
     * @param cluster
     * @return
     */
    public List<ClusterNodeStats> getNodeState(String cluster) {
        ESClient esClient = esOpClient.getESClient(cluster);
        if (esClient == null) {
            LOGGER.error(
                    "class=ESClusterNodeServiceImpl||method=getNodeState||clusterName={}||errMsg=esClient is null",
                    cluster);
            return Lists.newArrayList();
        }
        ESClusterNodesStatsResponse response =  esClient.admin().cluster().nodeStats(new ESClusterNodesStatsRequest())
                .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        if (response.getNodes() != null) {
            return new ArrayList<>(response.getNodes().values());

        }
        return Lists.newArrayList();
    }

}
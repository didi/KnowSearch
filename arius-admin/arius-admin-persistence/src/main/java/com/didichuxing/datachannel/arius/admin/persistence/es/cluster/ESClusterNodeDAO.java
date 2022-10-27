package com.didichuxing.datachannel.arius.admin.persistence.es.cluster;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_OPERATE_TIMEOUT;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NullESClientException;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.ParsingExceptionUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.logi.elasticsearch.client.request.cluster.nodestats.ESClusterNodesStatsRequest;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ESClusterNodesStatsResponse;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Repository;

/**
 * @author d06679
 */
@Repository
public class ESClusterNodeDAO extends BaseESDAO {
    private static final String GET_NODE_PLUGINS = "/_nodes/plugins";
    private static final String NODES            = "nodes";
    private static final String NAME    = "name";
    private static final String MODULES = "modules";
    /**
     * 获取节点上的索引个数
     * @param cluster 集群
     * @param nodes 节点
     * @return 个数
     */
    public int getIndicesCount(String cluster, String nodes) throws ESOperateException {
        ESClient client = esOpClient.getESClient(cluster);
        if (client == null) {
            LOGGER.warn(
                    "class={}||method=getIndicesCount||clusterName={}||errMsg=esClient is null",
                    getClass().getSimpleName(), cluster);
            throw new NullESClientException(cluster);
        }
        try {
            ESClusterNodesStatsResponse response = client.admin().cluster().prepareNodeStats().setNodesIds(nodes)
                    .setIndices(true).level("indices").execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

            int count = 0;
            Map<String, ClusterNodeStats> nodeStatsMap = response.getNodes();
            for (ClusterNodeStats nodeStats : nodeStatsMap.values()) {
                count += nodeStats.getIndices().getIndices().size();
            }

            return count;
        } catch (Exception e) {
            LOGGER.error("class=ESClusterNodeDao||method=getIndicesCount||clusterName={}",
                    cluster);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return 0;
    }

    public List<ClusterNodeStats> syncGetNodesStats(String clusterName) throws ESOperateException {
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (esClient == null) {
            LOGGER.error(
                "class=ESClusterNodeServiceImpl||method=syncGetNodeFsStatsMap||clusterName={}||errMsg=esClient is null",
                clusterName);
            throw new NullESClientException(clusterName);
        }
        ESClusterNodesStatsResponse response=null;
        try {
        
            response = esClient.admin().cluster().prepareNodeStats().setFs(true).setOs(true).setJvm(true)
                    .setThreadPool(true).level("node").execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("class=ESClusterNodeServiceImpl||method=syncGetNodeFsStatsMap||clusterName={}", clusterName,
                    e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        return Optional.ofNullable(response).map(ESClusterNodesStatsResponse::getNodes).filter(MapUtils::isNotEmpty)
                .map(m->Lists.<ClusterNodeStats>newArrayList(m.values())).orElse(Lists.newArrayList());
    }

    /**
     * 获取nodes信息
     * @param cluster
     * @return
     */
    public List<ClusterNodeStats> getNodeState(String cluster) {
        ESClient esClient = esOpClient.getESClient(cluster);
        if (esClient == null) {
            LOGGER.error("class=ESClusterNodeServiceImpl||method=getNodeState||clusterName={}||errMsg=esClient is null",
                cluster);
            return Lists.newArrayList();
        }
        ESClusterNodesStatsResponse response = esClient.admin().cluster().nodeStats(new ESClusterNodesStatsRequest())
            .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        if (response.getNodes() != null) {
            return new ArrayList<>(response.getNodes().values());

        }
        return Lists.newArrayList();
    }
    
    public List<TupleTwo</*node name*/String,/*plugin names*/List<String>>> syncGetNodesPlugins(String clusterName){
        final DirectResponse directResponse = getDirectResponse(clusterName, "GET", GET_NODE_PLUGINS);
        if (directResponse == null) {
            return Lists.newArrayList();
        }
        JSONObject jsonObject = JSON.parseObject(directResponse.getResponseContent());
        if (jsonObject == null || !jsonObject.containsKey(NODES)) {
            return Lists.newArrayList();
        }
       return jsonObject.getJSONObject(NODES)
                .values()
                .stream()
                .filter(Objects::nonNull)
                .map(JSONObject.class::cast)
                .map(this::buildNodeNamePlugins)
                .collect(Collectors.toList());
                
    }
    private  TupleTwo<String,List<String>> buildNodeNamePlugins(JSONObject jsonObject){
        final String nodeName = jsonObject.getString(NAME);
        final List<String> pluginNames = jsonObject.getJSONArray(MODULES).stream().filter(Objects::nonNull)
                .map(plugin -> ((JSONObject) plugin).getString(NAME)).collect(Collectors.toList());
        return Tuples.of(nodeName,pluginNames);
    
    }

    public List<ClusterNodeStats> syncGetNodesStatsWithIndices(String clusterName) {
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (esClient == null) {
            LOGGER.error("class=ESClusterNodeServiceImpl||method=syncGetNodeFsStatsMap||clusterName={}||errMsg=esClient is null", clusterName);
            return Lists.newArrayList();
        }
        ESClusterNodesStatsResponse response = esClient.admin().cluster().prepareNodeStats().setFs(true).setOs(true).setIndices(true).setJvm(true).setThreadPool(true).level("node").execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);
        if (response.getNodes() != null) {
            return new ArrayList<>(response.getNodes().values());
        }
        return Lists.newArrayList();
    }
}
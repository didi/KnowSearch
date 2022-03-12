package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.BIG_SHARD;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.INSERT_PRDER;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.ONE_BILLION;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.PRIORITY;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.SOURCE;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.TASKS;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.TIME_IN_QUEUE;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.GET_MOVING_SHARD;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.GET_PENDING_TASKS;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.getBigIndicesRequestContent;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.getBigShardsRequestContent;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.getShardToNodeRequestContentByIndexName;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.BigIndexMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.BigShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexShardInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.MovingShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.PendingTask;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterNodeDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ESClusterNodesResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ESClusterNodesStatsResponse;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Created by linyunan on 2021-08-09
 */
@Service
public class ESClusterNodeServiceImpl implements ESClusterNodeService {
    private static final ILog LOGGER = LogFactory.getLog(ESClusterNodeServiceImpl.class);

    @Autowired
    private ESOpClient esOpClient;

    @Autowired
    private ESClusterNodeDAO esClusterNodeDAO;

    @Override
    public Map<String, ClusterNodeStats> syncGetNodeFsStatsMap(String clusterName) {
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (esClient == null) {
            LOGGER.error(
                    "class=ESClusterNodeServiceImpl||method=syncGetNodeFsStatsMap||clusterName={}||errMsg=esClient is null",
                    clusterName);
            return Maps.newHashMap();
        }

        ESClusterNodesStatsResponse response = esClient.admin().cluster().prepareNodeStats().setFs(true).execute()
                .actionGet(30, TimeUnit.SECONDS);
        return response.getNodes();
    }

    @Override
    public List<String> syncGetNodeHosts(String clusterName) {
        return syncGetNodeInfo(clusterName).values().stream().map(ClusterNodeInfo::getHost)
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, ClusterNodeInfo> syncGetNodeInfo(String clusterName) {
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (esClient == null) {
            LOGGER.error(
                "class=ESClusterNodeServiceImpl||method=syncGetNodeHosts||clusterName={}||errMsg=esClient is null",
                clusterName);
            return Maps.newHashMap();
        }

        ESClusterNodesResponse esClusterNodesResponse = esClient.admin().cluster().prepareNodes().addFlag("http").get();
        if (null == esClusterNodesResponse || null == esClusterNodesResponse.getNodes()) {
            return Maps.newHashMap();
        }

        return esClusterNodesResponse.getNodes();
    }

    @Override
    public List<String> syncGetNodeNames(String clusterName) {
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (esClient == null) {
            LOGGER.error(
                    "class=ESClusterNodeServiceImpl||method=syncGetNodeNames||clusterName={}||errMsg=esClient is null",
                    clusterName);
            return Lists.newArrayList();
        }

        ESClusterNodesResponse esClusterNodesResponse = esClient.admin().cluster().prepareNodes().get();
        if (null == esClusterNodesResponse || null == esClusterNodesResponse.getNodes()) {
            return Lists.newArrayList();
        }

        return esClusterNodesResponse.getNodes().values().stream().map(ClusterNodeInfo::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<PendingTask> syncGetPendingTask(String clusterName) {
        DirectResponse directResponse = esClusterNodeDAO.getDirectResponse(clusterName, "Get", GET_PENDING_TASKS);
        List<PendingTask> pendingTasks = Lists.newArrayList();
        if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {

            JSONObject jsonObject = JSON.parseObject(directResponse.getResponseContent());
            if (null != jsonObject && null != jsonObject.getJSONArray(TASKS)) {
                JSONArray pendingTasksObj = jsonObject.getJSONArray(TASKS);
                for (int i = 0; i < pendingTasksObj.size(); i++) {
                    PendingTask pendingTask = new PendingTask();
                    setPendingTaskField(pendingTasksObj, i, pendingTask);
                    pendingTasks.add(pendingTask);
                }
            }
        }

        return pendingTasks;
    }

    private void setPendingTaskField(JSONArray pendingTasksObj, int i, PendingTask pendingTask) {
        if (null != pendingTasksObj.getJSONObject(i).get(TIME_IN_QUEUE)) {
            pendingTask.setTimeInQueue(pendingTasksObj.getJSONObject(i).get(TIME_IN_QUEUE).toString());
        }

        if (null != pendingTasksObj.getJSONObject(i).get(SOURCE)) {
            pendingTask.setSource(pendingTasksObj.getJSONObject(i).get(SOURCE).toString());
        }

        if (null != pendingTasksObj.getJSONObject(i).get(PRIORITY)) {
            pendingTask.setPriority(pendingTasksObj.getJSONObject(i).get(PRIORITY).toString());
        }

        if (null != pendingTasksObj.getJSONObject(i).get(INSERT_PRDER)) {
            pendingTask.setInsertOrder(
                    Long.valueOf(pendingTasksObj.getJSONObject(i).get(INSERT_PRDER).toString()));
        }
    }

    @Override
    public List<MovingShardMetrics> syncGetMovingShards(String clusterName) {
        DirectResponse directResponse = esClusterNodeDAO.getDirectResponse(clusterName, "Get", GET_MOVING_SHARD);

        List<MovingShardMetrics> movingShardsMetrics = Lists.newArrayList();
        if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {

            movingShardsMetrics = ConvertUtil.str2ObjArrayByJson(directResponse.getResponseContent(),
                    MovingShardMetrics.class);

        }
        return movingShardsMetrics;
    }

    @Override
    public List<BigShardMetrics> syncGetBigShards(String clusterName) {
        String bigShardsRequestContent = getBigShardsRequestContent("20s");
        DirectResponse directResponse = esClusterNodeDAO.getDirectResponse(clusterName, "Get", bigShardsRequestContent);

        List<BigShardMetrics> bigShardsMetrics = Lists.newArrayList();
        if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            List<BigShardMetrics> bigShardsMetricsFromES = ConvertUtil
                    .str2ObjArrayByJson(directResponse.getResponseContent(), BigShardMetrics.class);

            return bigShardsMetricsFromES.stream().filter(this::filterBigShard).collect(Collectors.toList());
        }

        return bigShardsMetrics;
    }

    @Override
    public List<BigIndexMetrics> syncGetBigIndices(String clusterName) {

        String indicesRequestContent = getBigIndicesRequestContent("20s");

        DirectResponse directResponse = esClusterNodeDAO.getDirectResponse(clusterName, "Get", indicesRequestContent);

        List<BigIndexMetrics> bigIndicesMetrics = Lists.newArrayList();

        List<IndexResponse> indexResponses;

        if (directResponse.getRestStatus() == RestStatus.OK
                && StringUtils.isNoneBlank(directResponse.getResponseContent())) {

            indexResponses = ConvertUtil.str2ObjArrayByJson(directResponse.getResponseContent(), IndexResponse.class);

            indexResponses.stream().filter(index -> index.getDc() > ONE_BILLION).forEach(r -> {

                String requestContent = getShardToNodeRequestContentByIndexName(r.getIndex(), "20s");

                DirectResponse shardNodeResponse = esClusterNodeDAO.getDirectResponse(clusterName, "Get",
                        requestContent);

                if (shardNodeResponse.getRestStatus() == RestStatus.OK
                        && StringUtils.isNoneBlank(shardNodeResponse.getResponseContent())) {

                    List<IndexShardInfo> indexShardInfos = ConvertUtil
                            .str2ObjArrayByJson(shardNodeResponse.getResponseContent(), IndexShardInfo.class);

                    BigIndexMetrics bigIndexMetrics = new BigIndexMetrics();
                    bigIndexMetrics.setIndexName(r.getIndex());

                    bigIndexMetrics.setBelongNodeInfo(Lists.newArrayList(Sets.newHashSet(indexShardInfos)));

                    bigIndicesMetrics.add(bigIndexMetrics);
                }
            });
        }
        return bigIndicesMetrics;
    }

    @Override
    public int syncGetIndicesCount(String cluster, String nodes) {
        return esClusterNodeDAO.getIndicesCount(cluster,nodes);
    }

    /*********************************************private******************************************/
    private boolean filterBigShard(BigShardMetrics bigShardMetrics) {
        String store = bigShardMetrics.getStore();
        StringBuilder sb = new StringBuilder();
        if (null != store && store.endsWith("gb")) {
            for (int i = 0; i < store.length(); i++) {
                if ('g' == (store.charAt(i))) {
                    break;
                }
                sb.append(store.charAt(i));
            }

            return BIG_SHARD <= Double.valueOf(sb.toString());
        }

        return false;
    }
}

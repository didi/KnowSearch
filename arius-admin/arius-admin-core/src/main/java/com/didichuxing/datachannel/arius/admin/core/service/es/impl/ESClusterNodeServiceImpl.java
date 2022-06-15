package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.*;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.ES_OPERATE_TIMEOUT;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.CollectionUtils;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.NodeStateVO;
import com.didiglobal.logi.elasticsearch.client.response.model.os.OsNode;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.*;
import com.didichuxing.datachannel.arius.admin.common.Triple;
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
import com.didiglobal.logi.elasticsearch.client.response.model.fs.FSNode;
import com.didiglobal.logi.elasticsearch.client.response.model.fs.FSNode;
import com.didiglobal.logi.elasticsearch.client.response.model.os.OsNode;
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
    public Map<String, ClusterNodeStats> syncGetNodePartStatsMap(String clusterName) {
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (esClient == null) {
            LOGGER.error(
                    "class=ESClusterNodeServiceImpl||method=syncGetNodeFsStatsMap||clusterName={}||errMsg=esClient is null",
                    clusterName);
            return Maps.newHashMap();
        }

        ESClusterNodesStatsResponse response = esClient.admin().cluster().prepareNodeStats()
                .setFs(true)
                .setOs(true)
                .setJvm(true)
                .setThreadPool(true)
                .level("node")
                .execute()
                .actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getNodes();
    }

    @Override
    public List<String> syncGetNodeHosts(String clusterName) {
        return syncGetNodeInfo(clusterName).values().stream().map(ClusterNodeInfo::getHost)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> syncGetNodeIp(String clusterName) {
        return syncGetNodeInfo(clusterName).values().stream().map(ClusterNodeInfo::getIp)
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
    public Map<String/*node*/, Long /*shardNum*/> syncGetNode2ShardNumMap(String clusterName) {
        String bigShardsRequestContent = getShards2NodeRequestContent("20s");
        DirectResponse directResponse = esClusterNodeDAO.getDirectResponse(clusterName, "Get", bigShardsRequestContent);

        Map<String/*node*/, Long /*shardNum*/> node2ShardNumMap = Maps.newHashMap();
        if (directResponse.getRestStatus() == RestStatus.OK
            && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            List<ShardMetrics> bigShardsMetricsFromES = ConvertUtil
                .str2ObjArrayByJson(directResponse.getResponseContent(), ShardMetrics.class);

            Map<String, List<ShardMetrics>> node2ShardMetricsListMap = ConvertUtil
                .list2MapOfList(bigShardsMetricsFromES, ShardMetrics::getNode, ShardMetrics -> ShardMetrics);

            node2ShardMetricsListMap.forEach((key, value) -> {
                node2ShardNumMap.put(key, (long) value.size());
            });
            return node2ShardNumMap;
        }

        return node2ShardNumMap;
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

    @Override
    public ClusterMemInfo synGetClusterMem(String cluster) {
        ESClient esClient = esOpClient.getESClient(cluster);
        if (esClient == null) {
            LOGGER.error(
                    "class=ESClusterNodeServiceImpl||method=synGetClusterMem||clusterName={}||errMsg=esClient is null",
                    cluster);
            return null;
        }

        // 获取nodes中的os信息
        ESClusterNodesStatsResponse response = esClient.admin().cluster().prepareNodeStats().setOs(true).execute()
                .actionGet(30, TimeUnit.SECONDS);

        // 构建集群的内存使用信息对象
        ClusterMemInfo clusterMemInfo = ClusterMemInfo.builder().memFree(0L).memUsed(0L).memTotal(0L).build();
        Map<String, ClusterNodeStats> clusterNodeStatsMap = response.getNodes();
        if (MapUtils.isEmpty(clusterNodeStatsMap)) {
            return clusterMemInfo;
        }

        // 统计所有的节点的内存信息合成为集群的内存使用信息
        clusterNodeStatsMap.values()
                .stream()
                .map(ClusterNodeStats::getOs)
                .map(OsNode::getMem)
                .forEach(osMem -> {
                    clusterMemInfo.setMemFree(clusterMemInfo.getMemFree() + osMem.getFreeInBytes());
                    clusterMemInfo.setMemTotal(clusterMemInfo.getMemTotal() + osMem.getTotalInBytes());
                    clusterMemInfo.setMemUsed(clusterMemInfo.getMemUsed() + osMem.getUsedInBytes());
                });

        // 计算集群总的内存使用和空闲的百分比信息，保留小数点后3位
        BigDecimal memFreeDec = new BigDecimal(clusterMemInfo.getMemFree());
        BigDecimal totalSizeDec = new BigDecimal(clusterMemInfo.getMemTotal());
        clusterMemInfo.setMemFreePercent(memFreeDec.divide(totalSizeDec, 5, RoundingMode.HALF_UP).doubleValue() * 100);
        clusterMemInfo.setMemUsedPercent(100 - clusterMemInfo.getMemFreePercent());

        return clusterMemInfo;
    }

    @Override
    public Map<String, Triple<Long, Long, Double>> syncGetNodesDiskUsage(String cluster) {
        Map<String, Triple<Long, Long, Double>> diskUsageMap = new HashMap<>();
        List<ClusterNodeStats> nodeStatsList = esClusterNodeDAO.syncGetNodesStats(cluster);


        if (CollectionUtils.isNotEmpty(nodeStatsList)) {
            // 遍历节点，获得节点和对应的磁盘使用率
            nodeStatsList.forEach(nodeStats -> {

                FSNode fsNode = nodeStats.getFs();
                String nodeName = nodeStats.getName();
                long totalFsBytes = fsNode.getTotal().getTotalInBytes();
                long usageFsBytes = totalFsBytes - fsNode.getTotal().getFreeInBytes();
                //Triple<diskTotal, diskUsage, diskUsagePercent>
                Triple<Long, Long, Double> triple = new Triple<>();
                if (totalFsBytes > 0) {
                    triple.setV1(totalFsBytes);
                }
                if (usageFsBytes >= 0 && usageFsBytes <= totalFsBytes) {
                    triple.setV2(usageFsBytes);
                }

                if (StringUtils.isNotBlank(nodeName) && totalFsBytes > 0 && usageFsBytes >= 0
                    && usageFsBytes <= totalFsBytes) {
                    triple.setV3(BigDecimal.valueOf(usageFsBytes)
                        .divide(BigDecimal.valueOf(totalFsBytes), 5, RoundingMode.HALF_UP).doubleValue());
                }
                diskUsageMap.put(nodeName, triple);
            });
        }
        return diskUsageMap;
    }

    /*********************************************private******************************************/

    @Override
    public List<NodeStateVO> nodeStateAnalysis(String cluster) {
        List<ClusterNodeStats> nodeStats = esClusterNodeDAO.getNodeState(cluster);
        List<NodeStateVO> vos = new ArrayList<>();
        nodeStats.forEach(nodeStat->{
            NodeStateVO vo = new NodeStateVO();

            vo.setNodeName(nodeStat.getName());
            vo.setSegmentsMemory(nodeStat.getIndices().getSegments().getMemoryInBytes());
            vo.setOsCpu(nodeStat.getOs().getCpu().getPercent());
            vo.setLoadAverage1m(nodeStat.getOs().getCpu().getLoadAverage().getOneM());
            vo.setLoadAverage5m(nodeStat.getOs().getCpu().getLoadAverage().getFiveM());
            vo.setLoadAverage15m(nodeStat.getOs().getCpu().getLoadAverage().getFifteenM());
            vo.setJvmHeapUsedPercent(nodeStat.getJvm().getMem().getHeapUsedPercent());
            vo.setThreadsCount(nodeStat.getJvm().getThreads().getCount());
            vo.setCurrentOpen(nodeStat.getHttp().getCurrentOpen());
            vo.setThreadPoolWriteActive(nodeStat.getThreadPool().getWrite().getActive());
            vo.setThreadPoolWriteQueue(nodeStat.getThreadPool().getWrite().getQueue());
            vo.setThreadPoolWriteReject(nodeStat.getThreadPool().getWrite().getRejected());
            vo.setThreadPoolSearchActive(nodeStat.getThreadPool().getSearch().getActive());
            vo.setThreadPoolSearchReject(nodeStat.getThreadPool().getSearch().getRejected());
            vo.setThreadPoolSearchQueue(nodeStat.getThreadPool().getSearch().getQueue());
            vo.setThreadPoolManagementActive(nodeStat.getThreadPool().getManagement().getActive());
            vo.setThreadPoolManagementReject(nodeStat.getThreadPool().getManagement().getRejected());
            vo.setThreadPoolManagementQueue(nodeStat.getThreadPool().getManagement().getQueue());


            vos.add(vo);
        });
        return vos;
    }
}

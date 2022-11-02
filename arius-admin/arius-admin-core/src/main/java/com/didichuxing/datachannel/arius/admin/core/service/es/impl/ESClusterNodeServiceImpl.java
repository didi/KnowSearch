package com.didichuxing.datachannel.arius.admin.core.service.es.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.NodeStateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginConstant;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterNodeDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsNodeInfoESDAO;
import com.didiglobal.knowframework.elasticsearch.client.ESClient;
import com.didiglobal.knowframework.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.knowframework.elasticsearch.client.response.cluster.nodes.ESClusterNodesResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didiglobal.knowframework.elasticsearch.client.response.cluster.nodesstats.ESClusterNodesStatsResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.model.fs.FSNode;
import com.didiglobal.knowframework.elasticsearch.client.response.model.http.HttpNode;
import com.didiglobal.knowframework.elasticsearch.client.response.model.indices.CommonStat;
import com.didiglobal.knowframework.elasticsearch.client.response.model.indices.Segments;
import com.didiglobal.knowframework.elasticsearch.client.response.model.jvm.JvmMem;
import com.didiglobal.knowframework.elasticsearch.client.response.model.jvm.JvmNode;
import com.didiglobal.knowframework.elasticsearch.client.response.model.jvm.JvmThreads;
import com.didiglobal.knowframework.elasticsearch.client.response.model.os.LoadAverage;
import com.didiglobal.knowframework.elasticsearch.client.response.model.os.OsCpu;
import com.didiglobal.knowframework.elasticsearch.client.response.model.os.OsNode;
import com.didiglobal.knowframework.elasticsearch.client.response.model.threadpool.ThreadPoolNode;
import com.didiglobal.knowframework.elasticsearch.client.response.model.threadpool.ThreadPoolNodes;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.*;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_OPERATE_TIMEOUT;

/**
 * Created by linyunan on 2021-08-09
 */
@Service
public class ESClusterNodeServiceImpl implements ESClusterNodeService {
    private static final ILog LOGGER = LogFactory.getLog(ESClusterNodeServiceImpl.class);

    @Autowired
    private ESOpClient        esOpClient;

    @Autowired
    private ESClusterNodeDAO  esClusterNodeDAO;

    @Autowired
    private AriusStatsNodeInfoESDAO ariusStatsNodeInfoESDAO;

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

        ESClusterNodesStatsResponse response = esClient.admin().cluster().prepareNodeStats().setFs(true).setOs(true)
            .setJvm(true).setThreadPool(true).level("node").execute().actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);

        return response.getNodes();
    }

    @Override
    public List<String> syncGetNodeHosts(String clusterName) {
        return syncGetNodeInfo(clusterName).values().stream().map(ClusterNodeInfo::getHost)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> syncGetNodeIp(String clusterName) {
        return syncGetNodeInfo(clusterName).values().stream().map(ClusterNodeInfo::getIp).collect(Collectors.toList());
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
            pendingTask.setInsertOrder(Long.valueOf(pendingTasksObj.getJSONObject(i).get(INSERT_PRDER).toString()));
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
                BigIndexMetrics bigIndexMetrics = new BigIndexMetrics();
                bigIndexMetrics.setIndexName(r.getIndex());
                bigIndexMetrics.setDocsCount(r.getDc());
                bigIndicesMetrics.add(bigIndexMetrics);
            });
        }
        return bigIndicesMetrics;
    }

    @Override
    public int syncGetIndicesCount(String cluster, String nodes) {
        return esClusterNodeDAO.getIndicesCount(cluster, nodes);
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
        clusterNodeStatsMap.values().stream().map(ClusterNodeStats::getOs).map(OsNode::getMem).forEach(osMem -> {
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
        List<ClusterNodeStats> nodeStatsList = null;
        try {
            nodeStatsList = esClusterNodeDAO.syncGetNodesStats(cluster);
        } catch (ESOperateException e) {
            LOGGER.error(
                "class=ESClusterNodeServiceImpl||method=syncGetNodesDiskUsage||clusterName={}",
                cluster,e);
            return diskUsageMap;
        }
    
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

    @Override
    public Map<String, Tuple<Long, Long>> syncGetNodesMemoryAndDisk(String cluster) {
        Map<String, Tuple<Long, Long>> node2MemAndDiskMap = Maps.newHashMap();
        List<ClusterNodeStats> nodeStatsList;
        try {
            nodeStatsList = esClusterNodeDAO.syncGetNodesStats(cluster);
        } catch (ESOperateException e) {
            LOGGER.error("class={}||method=syncGetNodesDiskUsage||clusterName={}", getClass().getSimpleName(), cluster,
                    e);
            return node2MemAndDiskMap;
        }
    
        if (CollectionUtils.isNotEmpty(nodeStatsList)) {
            // 遍历节点，获得节点和对应的磁盘使用率
            nodeStatsList.forEach(nodeStats -> {
                String nodeName = nodeStats.getName();

                FSNode fsNode = nodeStats.getFs();
                long totalFsBytes = fsNode.getTotal().getTotalInBytes();
                long totalOsMemBytes = nodeStats.getOs().getMem().getTotalInBytes();
                if (totalFsBytes <= 0 || totalOsMemBytes <= 0) {
                    return;
                }
                //Tuple<memoryBytes, diskBytes>
                Tuple<Long, Long> tuple = new Tuple<>();
                tuple.setV1(totalOsMemBytes);
                tuple.setV2(totalFsBytes);

                node2MemAndDiskMap.put(nodeName, tuple);
            });
        }
        return node2MemAndDiskMap;
    }

    @Override
    public Map<String, Integer> syncGetNodesCpuNum(String cluster) {
        Map<String, Integer> node2CpuNumMap = Maps.newHashMap();
        //这里直接使用 esClient.admin().cluster().nodes(new ESClusterNodesRequest().flag("os")).actionGet(ES_OPERATE_TIMEOUT, TimeUnit.SECONDS);无法正确获取到数据，所以使用
        DirectResponse directResponse = esClusterNodeDAO.getDirectResponse(cluster, "GET", "/_nodes/os");

        if (directResponse.getRestStatus() == RestStatus.OK
            && StringUtils.isNotBlank(directResponse.getResponseContent())) {
            JSONObject nodes = null;
            try {
                JSONObject jsonObject = JSONObject.parseObject(directResponse.getResponseContent());
                nodes = jsonObject.getJSONObject("nodes");
            } catch (Exception e) {
                // pass 
            }
            Optional.ofNullable(nodes).ifPresent(nodesMap -> {
                nodesMap.values().forEach(obj -> {
                    Integer cpuNum = null;
                    String nodeName = null;
                    try {
                        JSONObject nodeInfo = JSONObject.parseObject(JSONObject.toJSONString(obj));
                        nodeName = nodeInfo.getString("name");
                        cpuNum = nodeInfo.getJSONObject("os").getInteger("available_processors");
                    } catch (Exception e) {
                        // pass
                    }
                    if (StringUtils.isNotBlank(nodeName) && null != cpuNum && cpuNum > 0) {
                        node2CpuNumMap.put(nodeName, cpuNum);
                    }

                });
            });
        }

        return node2CpuNumMap;
    }
    
    /**
     * 同步获取节点插件元组列表
     *
     * @param phyCluster phy集群
     * @return {@code List<TupleTwo<String, List<String>>>}
     */
    @Override
    public List<TupleTwo<String, List<String>>> syncGetNodePluginTupleList(String phyCluster) {
        return esClusterNodeDAO.syncGetNodesPlugins(phyCluster);
    }
    
    /**
     * @param phyClusterName
     * @return
     */
    @Override
    public TupleTwo<Boolean, Boolean> existDCDRAndPipelineModule(String phyClusterName) {
        //获取物理集群侧的插件列表
        final List<TupleTwo<String, List<String>>> syncGetNodePluginTupleList = syncGetNodePluginTupleList(
                phyClusterName);
        if (CollectionUtils.isEmpty(syncGetNodePluginTupleList)) {
            return Tuples.of(Boolean.FALSE, Boolean.FALSE);
        }
        //这里对于一个集群来说，不需要校验全部节点是否是存在dcdr和pipeline的;这里属于内置的module，默认拿一个验证即可
        final TupleTwo<String, List<String>> nodeNamePlugins = syncGetNodePluginTupleList.get(0);
        if (CollectionUtils.isEmpty(nodeNamePlugins.v2())) {
            return Tuples.of(Boolean.FALSE, Boolean.FALSE);
        }
        return Tuples.of(nodeNamePlugins.v2().contains(PluginConstant.DIDI_CROSS_DATACENTER_REPLICATION),
                nodeNamePlugins.v2().contains(PluginConstant.INGEST_INDEX_TEMPLATE));
    }

    @Override
    public Long getWriteRejectedNum(String cluster, String node) {
        return ariusStatsNodeInfoESDAO.getWriteRejectedNum(cluster, node);
    }

    @Override
    public Long getSearchRejectedNum(String cluster, String node) {
        return ariusStatsNodeInfoESDAO.getSearchRejectedNum(cluster, node);
    }

    @Override
    public List<ClusterNodeStats> syncGetNodeStats(String cluster) {
        return esClusterNodeDAO.getNodeState(cluster);
    }


    /*********************************************private******************************************/

    @Override
    public List<NodeStateVO> syncNodeStateAnalysis(String cluster) {
        List<ClusterNodeStats> nodeStats = esClusterNodeDAO.getNodeState(cluster);
        return nodeStats.stream().map(this::buildNodeStateVO).collect(Collectors.toList());
    }

    @NotNull
    private NodeStateVO buildNodeStateVO(ClusterNodeStats nodeStat) {
        NodeStateVO vo = new NodeStateVO();
        vo.setNodeName(nodeStat.getName());
        Optional.of(nodeStat).map(ClusterNodeStats::getIndices).map(CommonStat::getSegments).map(Segments::getMemoryInBytes).ifPresent(vo::setSegmentsMemory);
        Optional.of(nodeStat).map(ClusterNodeStats::getOs).map(OsNode::getCpu).map(OsCpu::getPercent).ifPresent(vo::setOsCpu);
        Optional.of(nodeStat).map(ClusterNodeStats::getOs).map(OsNode::getCpu).map(OsCpu::getLoadAverage).map(LoadAverage::getOneM).ifPresent(vo::setLoadAverage1m);
        Optional.of(nodeStat).map(ClusterNodeStats::getOs).map(OsNode::getCpu).map(OsCpu::getLoadAverage).map(LoadAverage::getFiveM).ifPresent(vo::setLoadAverage5m);
        Optional.of(nodeStat).map(ClusterNodeStats::getOs).map(OsNode::getCpu).map(OsCpu::getLoadAverage).map(LoadAverage::getFifteenM).ifPresent(vo::setLoadAverage15m);
        Optional.of(nodeStat).map(ClusterNodeStats::getJvm).map(JvmNode::getMem).map(JvmMem::getHeapUsedPercent).ifPresent(vo::setJvmHeapUsedPercent);
        Optional.of(nodeStat).map(ClusterNodeStats::getJvm).map(JvmNode::getThreads).map(JvmThreads::getCount).ifPresent(vo::setThreadsCount);
        Optional.of(nodeStat).map(ClusterNodeStats::getHttp).map(HttpNode::getCurrentOpen).ifPresent(vo::setCurrentOpen);
        Optional.of(nodeStat).map(ClusterNodeStats::getThreadPool).map(ThreadPoolNodes::getWrite).map(ThreadPoolNode::getActive).ifPresent(vo::setThreadPoolWriteActive);
        Optional.of(nodeStat).map(ClusterNodeStats::getThreadPool).map(ThreadPoolNodes::getWrite).map(ThreadPoolNode::getQueue).ifPresent(vo::setThreadPoolWriteQueue);
        Optional.of(nodeStat).map(ClusterNodeStats::getThreadPool).map(ThreadPoolNodes::getWrite).map(ThreadPoolNode::getRejected).ifPresent(vo::setThreadPoolWriteReject);
        Optional.of(nodeStat).map(ClusterNodeStats::getThreadPool).map(ThreadPoolNodes::getSearch).map(ThreadPoolNode::getActive).ifPresent(vo::setThreadPoolSearchActive);
        Optional.of(nodeStat).map(ClusterNodeStats::getThreadPool).map(ThreadPoolNodes::getSearch).map(ThreadPoolNode::getRejected).ifPresent(vo::setThreadPoolSearchReject);
        Optional.of(nodeStat).map(ClusterNodeStats::getThreadPool).map(ThreadPoolNodes::getSearch).map(ThreadPoolNode::getQueue).ifPresent(vo::setThreadPoolSearchQueue);
        Optional.of(nodeStat).map(ClusterNodeStats::getThreadPool).map(ThreadPoolNodes::getManagement).map(ThreadPoolNode::getActive).ifPresent(vo::setThreadPoolManagementActive);
        Optional.of(nodeStat).map(ClusterNodeStats::getThreadPool).map(ThreadPoolNodes::getManagement).map(ThreadPoolNode::getRejected).ifPresent(vo::setThreadPoolManagementReject);
        Optional.of(nodeStat).map(ClusterNodeStats::getThreadPool).map(ThreadPoolNodes::getManagement).map(ThreadPoolNode::getQueue).ifPresent(vo::setThreadPoolManagementQueue);
        return vo;
    }
}
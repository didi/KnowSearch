package com.didichuxing.datachannel.arius.admin.metadata.service;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.stats.ClusterLogicStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.ClusterLogicStatsPO;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterNodeDAO;
import com.didiglobal.knowframework.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.clusterindex.IndexStatusResult;
import com.didiglobal.knowframework.elasticsearch.client.response.model.fs.FSNode;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Service
public class ESClusterLogicStatsService {
    protected static final ILog logger                        = LogFactory
            .getLog(ESClusterLogicStatsService.class);
    private static final String STR_GREEN = "green";
    @Autowired
    private ESClusterService esClusterService;
    @Autowired
    private ESClusterNodeDAO esClusterNodeDAO;

    public ClusterLogicStatsPO getLogicClusterStats(Long logicClusterId, ClusterPhy clusterPhy,
                                                    Map<Integer, List<String>> logicClusterNodesNameMap,
                                                    Map<String, List<ClusterLogicStats>> phyClusterNodeStatsMap,
                                                    Map<Long, List<IndexStatusResult>> logicClusterIndicesMap, boolean isJob) {
        //初始化返回对象
        ClusterLogicStatsPO statsPO = new ClusterLogicStatsPO();
        //通过逻辑集群id获取节点统计信息
        long timeMillis = System.currentTimeMillis();
        List<String> logicCLusterNodesList = logicClusterNodesNameMap.get(logicClusterId.intValue());
        List<ClusterLogicStats> phyCLusterNodeStatsList = phyClusterNodeStatsMap.get(clusterPhy.getCluster());
        if (CollectionUtils.isNotEmpty(phyCLusterNodeStatsList) && CollectionUtils.isNotEmpty(logicCLusterNodesList)) {
            List<ClusterLogicStats> logicNodeStats = phyCLusterNodeStatsList.stream()
                    .filter(clusterLogicStats -> logicCLusterNodesList.contains(clusterLogicStats.getNodeName())).collect(Collectors.toList());
            Long docNu = logicNodeStats.stream().filter(Objects::nonNull).map(ClusterLogicStats::getDocCount)
                    .filter(Objects::nonNull).mapToLong(Long::longValue).sum();
            Double diskFree = logicNodeStats.stream().filter(Objects::nonNull).map(ClusterLogicStats::getFreeInBytes)
                    .filter(Objects::nonNull).mapToDouble(Long::doubleValue).sum();
            Double diskUsage = logicNodeStats.stream().filter(Objects::nonNull).map(ClusterLogicStats::getUsageFsBytes)
                    .filter(Objects::nonNull).mapToDouble(Long::doubleValue).sum();
            Double diskTotal = logicNodeStats.stream().filter(Objects::nonNull).map(ClusterLogicStats::getTotalFsBytes)
                    .filter(Objects::nonNull).mapToDouble(Long::doubleValue).sum();
            //设置节点数
            statsPO.setNumberDataNodes((long) logicNodeStats.size());
            //总文档数量
            statsPO.setDocNu(docNu);
            //被使用的存储大小
            statsPO.setUsedDisk(diskUsage);
            //没有被使用的存储大小
            statsPO.setFreeDisk(diskFree);
            //总存储大小
            statsPO.setTotalDisk(diskTotal);
            //cpu使用率
            logicNodeStats.stream().filter(Objects::nonNull).map(ClusterLogicStats::getCpuUsedPrecent)
                    .filter(nodeStats -> Objects.nonNull(nodeStats) && !nodeStats.isNaN())
                    .mapToDouble(Double::doubleValue).max().ifPresent(statsPO::setCpuUsedPercent);
            //调用方是定时任务，则采集下面的指标
            if (isJob) {
                // 节点存活率
                Integer clientAlivePrecent = getClientAlivePrecent(logicNodeStats, clusterPhy);
                statsPO.setAlivePercent(clientAlivePrecent);
                //通过逻辑集群id获取索引级别统计信息
                timeMillis = System.currentTimeMillis();
                handleIndicesStats(statsPO, String.valueOf(logicClusterId), logicClusterIndicesMap);
            }
        }
        return statsPO;
    }

    /**
     * @param phyClusterRegionMap         物理集群和单个物理机群下的region的关系
     * @param regionNodeMap               region和region下的node节点的关系
     * @param logicClusterIndicesNameMap  逻辑集群和逻辑集群下的索引名称的关系
     * @param phyClusterName              物理集群名称
     * @param logicClusterNodesNameMap    返回--逻辑集群和节点名称的关系
     * @param phyClusterNodeStatsMap      返回--物理集群和节点统计指标的关系
     * @param logicClusterIndicesStatsMap 返回--逻辑集群和索引统计信息的关系
     */
    public void buildLogicClusterStats(Map<String, List<ClusterRegion>> phyClusterRegionMap, Map<Integer,
            List<String>> regionNodeMap, Map<Long, List<String>> logicClusterIndicesNameMap,
                                       String phyClusterName, Map<Integer, List<String>> logicClusterNodesNameMap,
                                       Map<String, List<ClusterLogicStats>> phyClusterNodeStatsMap,
                                       Map<Long, List<IndexStatusResult>> logicClusterIndicesStatsMap) {
        //逻辑集群和节点的关系
        getLogicClusterNodeMap(phyClusterRegionMap, regionNodeMap, phyClusterName, logicClusterNodesNameMap);
        //物理集群节点信息
        List<ClusterLogicStats> phyClusterNodesStatsList = syncGetPhyNodesStats(phyClusterName);
        if (CollectionUtils.isNotEmpty(phyClusterNodesStatsList)) {
            phyClusterNodeStatsMap.put(phyClusterName, phyClusterNodesStatsList);
        }    //获取逻辑集群和索引统计信息之间的关系
        setLogicClusterIndiesStats(phyClusterName, logicClusterIndicesNameMap, logicClusterIndicesStatsMap);
    }

    private void getLogicClusterNodeMap(Map<String, List<ClusterRegion>> phyClusterRegionMap, Map<Integer, List<String>> regionNodeMap,
                                        String phyClusterName, Map<Integer, List<String>> logicClusterNodesNameMap) {
        List<ClusterRegion> clusterRegionList = phyClusterRegionMap.getOrDefault(phyClusterName, Lists.newArrayList());
        for (ClusterRegion clusterRegion : clusterRegionList) {
            List<String> nodeNameList = regionNodeMap.get(Math.toIntExact(clusterRegion.getId()));
            if (CollectionUtils.isNotEmpty(nodeNameList)) {
                //key是逻辑集群id
                Map<Integer, List<String>> logicId2ClusterLogicStats = Arrays.stream(StringUtils.split(clusterRegion.getLogicClusterIds(), ","))
                        .filter(StringUtils::isNumeric).map(i -> Tuples.of(Integer.parseInt(i), nodeNameList))
                        .collect(Collectors.toMap(TupleTwo::v1, TupleTwo::v2));
                logicClusterNodesNameMap.putAll(logicId2ClusterLogicStats);
            }
        }
    }

    /**
     * 设置逻辑集群和索引统计信息之间的关系
     *
     * @param phyClusterName
     * @param logicClusterIndicesNameMap
     * @param logicClusterIndicesStatsMap
     */
    private void setLogicClusterIndiesStats(String phyClusterName, Map<Long, List<String>> logicClusterIndicesNameMap,
                                            Map<Long, List<IndexStatusResult>> logicClusterIndicesStatsMap) {
        //获取物理集群按节点分组的索引统计信息
        Map<String, IndexStatusResult> phyClusterIndicesMap = getIndicesStats(phyClusterName);
        if (MapUtils.isNotEmpty(phyClusterIndicesMap)) {
            //配置逻辑集群和索引级别统计信息的关系

            for (Map.Entry<Long, List<String>> indexNameListEntry : logicClusterIndicesNameMap.entrySet()) {
                List<String> indexNameList = indexNameListEntry.getValue();
                List<IndexStatusResult> indiesStatsList = indexNameList.stream()
                        .map(indexName -> phyClusterIndicesMap.get(indexName))
                        .filter(indexStatusResult -> Objects.nonNull(indexStatusResult)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(indiesStatsList)) {
                    logicClusterIndicesStatsMap.put(indexNameListEntry.getKey(), indiesStatsList);
                }
            }
        }
    }

    /**
     * 节点指标信息采集
     *
     * @param phyClusterName
     * @return
     */
    public List<ClusterLogicStats> syncGetPhyNodesStats(String phyClusterName) {
        List<ClusterLogicStats> phyNodeStatsList = Lists.newArrayList();
        List<ClusterNodeStats> nodeStatsList = getNodeStats(phyClusterName);
        if (CollectionUtils.isNotEmpty(nodeStatsList)) {
            // 遍历节点，获得节点统计指标
            phyNodeStatsList = nodeStatsList.stream().map(nodeStats -> {
                try{
                    //doc文档数量
                    long docCount = nodeStats.getIndices().getDocs().getCount();
                    //总存储大小，已使用存储大小，未使用存储大小
                    FSNode fsNode = nodeStats.getFs();
                    long totalFsBytes = fsNode.getTotal().getTotalInBytes();
                    long freeInBytes = fsNode.getTotal().getFreeInBytes();
                    long usageFsBytes = totalFsBytes - freeInBytes;
                    int percent = nodeStats.getOs().getCpu().getPercent();
                    String host = nodeStats.getHost();
                    String nodeName = nodeStats.getName();
                    return new ClusterLogicStats(docCount, usageFsBytes, freeInBytes, totalFsBytes, (double) percent, host, nodeName);
                }catch (Exception e){
                    logger.error("class=ESClusterLogicStatsService||method=syncGetPhyNodeStats||nodeStats={}||errMsg",nodeStats,e.getMessage(),e);
                    return new ClusterLogicStats();
                }
            }).collect(Collectors.toList());
        }
        return phyNodeStatsList;
    }

    /**
     * 获取未分配shard数量，集群状态
     *
     * @param statsPO
     * @param logicClusterId
     * @param logicClusterIndicesMap
     */
    private void handleIndicesStats(ClusterLogicStatsPO statsPO, String logicClusterId,
                                    Map<Long, List<IndexStatusResult>> logicClusterIndicesMap) {
        byte clusterStatus = ClusterHealthStatus.fromString(STR_GREEN).value();
        //获取逻辑集群索引列表
        List<IndexStatusResult> logicClusterIndicesList = logicClusterIndicesMap.get(Long.parseLong(logicClusterId));
        //获取未分配索引列表
        statsPO.setUnAssignedShards(0L);
        statsPO.setStatus(STR_GREEN);
        statsPO.setStatusType((int) clusterStatus);
        statsPO.setIndexNu(0L);
        if (CollectionUtils.isNotEmpty(logicClusterIndicesList)) {
            statsPO.setIndexNu((long)logicClusterIndicesList.size());
            logicClusterIndicesList.stream().forEach(indexStatusResult -> {
                statsPO.setUnAssignedShards(indexStatusResult.getUnassignedShards() + statsPO.getUnAssignedShards());
                if (indexStatusResult.getStatusType() > statsPO.getStatusType()) {
                    statsPO.setStatusType(indexStatusResult.getStatusType());
                    statsPO.setStatus(indexStatusResult.getStatus());
                }
            });
        }
    }

    /**
     * 获取逻辑集群节点存活率
     *
     * @param clusterLogicStatsList
     * @param clusterPhy
     * @return
     */
    private Integer getClientAlivePrecent(List<ClusterLogicStats> clusterLogicStatsList, ClusterPhy clusterPhy) {
        if (CollectionUtils.isNotEmpty(clusterLogicStatsList)) {
            //查询物理集群的信息
            if (StringUtils.isBlank(clusterPhy.getHttpAddress())) {
                return 100;
            } else {
                int val = esClusterService.syncGetClientAlivePercent(clusterPhy.getCluster(),
                        clusterPhy.getPassword(), clusterPhy.getHttpAddress());
                return val;
            }
        }
        return 0;
    }

    /**
     * 获取节点统计信息
     *
     * @param phyClusterName
     * @return
     */

    private List<ClusterNodeStats> getNodeStats(String phyClusterName) {
        List<ClusterNodeStats> nodeStatsList = esClusterNodeDAO.syncGetNodesStatsWithIndices(phyClusterName);
        return nodeStatsList;
    }

    /**
     * 获取shard统计信息,粒度为索引级别
     *
     * @param phyClusterName
     * @return
     */
    private Map<String, IndexStatusResult> getIndicesStats(String phyClusterName) {
        Map<String, IndexStatusResult> indexListGroupByNode = new HashMap<>();
        ESClusterHealthResponse esClusterHealthResponse = esClusterService.syncGetClusterHealthAtIndicesLevel(phyClusterName);
        if (null == esClusterHealthResponse) {
            return indexListGroupByNode;
        }
        indexListGroupByNode = esClusterHealthResponse.getIndices();
        return indexListGroupByNode;
    }

}
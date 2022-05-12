package com.didichuxing.datachannel.arius.admin.metadata.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.ClusterLogicStatisPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.NodeRackStatisPO;
import com.didichuxing.datachannel.arius.admin.common.constant.index.IndexStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsNodeInfoESDAO;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.clusterindex.IndexStatusResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@NoArgsConstructor
@Service
public class ESClusterLogicStaticsService {
    protected static final Logger logger = LoggerFactory.getLogger(ESClusterLogicStaticsService.class);
    private static final String STR_GREEN = "green";
    private final Cache<String, ESClusterHealthResponse> phyClusterHealthCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).maximumSize(10000).build();
    private final Cache<String, Map<String, IndexStatusResult>> templateIndicesHealthMapCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).maximumSize(10000).build();
    @Autowired
    private ESClusterService esClusterService;
    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;
    @Autowired
    private AriusStatsNodeInfoESDAO ariusStatsNodeInfoEsDao;
    @Autowired
    private ClusterLogicService clusterLogicService;
    @Autowired
    private ClusterPhyService clusterPhyService;
    @Autowired
    private RegionRackService regionRackService;

    public ClusterLogicStatisPO getLogicClusterStats(Long logicClusterId, boolean isJob) {
        ClusterLogic logicCluster = clusterLogicService.getClusterLogicById(logicClusterId);
        List<ClusterLogicRackInfo> items = regionRackService.listLogicClusterRacks(logicClusterId);
        if (null == logicCluster) {
            return null;
        }
        Map<String/*phyClusterName*/, List<String>> phyClusterRackMap = logicItem2Map(items);
        Map<String, List<NodeRackStatisPO>> phyClusterRackMapInfo = new ConcurrentHashMap<>();
        AtomicInteger clientAlivePercent = new AtomicInteger(0);
        phyClusterRackMap.keySet().parallelStream().forEach(cluster -> {
            List<String> rack = phyClusterRackMap.get(cluster);
            List<NodeRackStatisPO> nodeRackStatsPOS = ariusStatsNodeInfoEsDao.getRackStatis(cluster, rack);
            if (!EnvUtil.isOnline()) {
                logger.error("class=ESClusterLogicStaticsService||method=getLogicClusterStats||cluster={}||rack={}||ret={}", cluster, rack, JSON.toJSONString(nodeRackStatsPOS));
            }
            if (CollectionUtils.isNotEmpty(nodeRackStatsPOS)) {
                phyClusterRackMapInfo.put(cluster, nodeRackStatsPOS);
            }
            /*采集节点存活率*/
            ClusterPhy clusterVO = clusterPhyService.getClusterByName(cluster);
            if (clusterVO != null && isJob) {
                if (StringUtils.isBlank(clusterVO.getHttpAddress())) {
                    clientAlivePercent.set(100);
                } else {
                    int val = esClusterService.syncGetClientAlivePercent(clusterVO.getCluster(), clusterVO.getPassword(), clusterVO.getHttpAddress());
                    clientAlivePercent.set(Math.max(clientAlivePercent.get(), val));
                }
            }
        });

        if (MapUtils.isEmpty(phyClusterRackMapInfo)) {
            logger.error("class=ESClusterLogicStaticsService||method=getLogicClusterStats||logicClusterId={}||msg=phyClusterRackMapInfo is empty! ", logicClusterId);
            return null;
        }

        ClusterLogicStatisPO statsPO = new ClusterLogicStatisPO();
        statsPO.setId(logicClusterId);
        statsPO.setName(logicCluster.getName());
        //设置节点存活率
        statsPO.setAlivePercent(clientAlivePercent.get());
        for (
                Map.Entry<String, List<NodeRackStatisPO>> entry : phyClusterRackMapInfo.entrySet()) {
            try {
                List<NodeRackStatisPO> nodeRackStats = entry.getValue();
                statsPO.setNumberDataNodes(items.size() * 2L);
                nodeRackStats.forEach(nodeStats -> {
                    statsPO.setIndexNu(statsPO.getIndexNu() + nodeStats.getIndexNu());
                    statsPO.setDocNu(statsPO.getDocNu() + nodeStats.getDocNu());
                    if (null != nodeStats.getTotalDiskG()) {
                        statsPO.setTotalDisk(statsPO.getTotalDisk() + nodeStats.getTotalDiskG());
                    }
                    if (null != nodeStats.getDiskFreeG()) {
                        statsPO.setFreeDisk(statsPO.getFreeDisk() + nodeStats.getDiskFreeG());
                    }
                    statsPO.setUsedDisk(statsPO.getTotalDisk() - statsPO.getFreeDisk());
                    if (null != nodeStats.getCpuUsedPercent()) {
                        statsPO.setCpuUsedPercent(Math.max(nodeStats.getCpuUsedPercent(), statsPO.getCpuUsedPercent()));
                    }
                });
            } catch (Exception e) {
                logger.error("class=ESClusterLogicStaticsService||method=getLogicClusterStats||errMsg={}", e.getMessage(), e);
            }
        }
        if (isJob) {
            handleOtherStats(items, statsPO, phyClusterRackMap);
        }
        return statsPO;
    }

    private void handleOtherStats(List<ClusterLogicRackInfo> items, ClusterLogicStatisPO statsPO, Map<String, List<String>> phyClusterRackMap) {    //物理集群级别的健康状态，只取numberOfPendingTasks
        String phyClusterName = "";
        if (null != items.get(0)) {
            phyClusterName = items.get(0).getPhyClusterName();
        }
        ESClusterHealthResponse eSClusterHealthResponse = getPhyClusterHealthByCache(phyClusterName);
        if (null != eSClusterHealthResponse) {
            statsPO.setNumberPendingTasks(eSClusterHealthResponse.getNumberOfPendingTasks());
        }
        final byte[] clusterStatus = {ClusterHealthStatus.fromString(STR_GREEN).value()};
        List<String> logicClusterIndexes = getLogicClusterIndexes(phyClusterRackMap);
        Map<String, IndexStatusResult> templateIndicesHealthMap = getTemplateIndicesHealthMapByCache(phyClusterName);
        if (null != templateIndicesHealthMap) {
            logicClusterIndexes.forEach(template -> {
                IndexStatusResult indexStatusResult = templateIndicesHealthMap.get(template);
                if (null == indexStatusResult) {
                    logger.warn("class=ClusterLogicService||method=getLogicClusterStatis||template={}||msg=can not get indicesHealth", template);
                } else {
                    //行内的逻辑集群只映射一个物理集群 GREEN 0 YELLOW 1 RED 2
                    byte status = ClusterHealthStatus.fromString(indexStatusResult.getStatus()).value();
                    clusterStatus[0] = (status > clusterStatus[0]) ? status : clusterStatus[0];
                    //unAssignedShards 是索引级的数据统计而来
                    statsPO.setUnAssignedShards(statsPO.getUnAssignedShards() + indexStatusResult.getUnassignedShards());
                }
            });
            String strClusterStatus = getStrClusterStatus(clusterStatus[0]);
            statsPO.setStatus(strClusterStatus);
            statsPO.setStatusType(clusterStatus[0]);
        }
    }

    /**
     * 从缓存中获取多type索引映射信息 * * @return
     */
    public Map<String, IndexStatusResult> getTemplateIndicesHealthMapByCache(String phyClusterName) {
        if (StringUtils.isNotEmpty(phyClusterName)) {
            try {
                templateIndicesHealthMapCache.get(phyClusterName, () -> getTemplateIndicesHealthMap(phyClusterName));
            } catch (Exception e) {
                logger.error("class=QueryAdminService||method=getPhyClusterHealthByCache||errMsg=exception!", e);
            }
            return getTemplateIndicesHealthMap(phyClusterName);
        }
        return null;
    }

    Map<String, IndexStatusResult> getTemplateIndicesHealthMap(String phyClusterName) {
        ESClusterHealthResponse eSClusterHealthResponse = getPhyClusterHealthByCache(phyClusterName);
        Map<String, IndexStatusResult> templateStatusMap = Maps.newHashMap();
        if (null == eSClusterHealthResponse) {
            return null;
        }
        Map<String, IndexStatusResult> indices = eSClusterHealthResponse.getIndices();
        indices.forEach((index, indexStatus) -> {
            String ofTemplate = IndexNameUtils.removeIndexNameDateIfHas(index);
            IndexStatusResult indexStatusResult = templateStatusMap.getOrDefault(ofTemplate, new IndexStatusResult());
            if (null != indexStatusResult.getUnassignedShards()) {
                indexStatusResult.setUnassignedShards(indexStatusResult.getUnassignedShards() + indexStatus.getUnassignedShards());
            } else {
                indexStatusResult.setUnassignedShards(indexStatus.getUnassignedShards());
            }
            String status = indexStatusResult.getStatus();
            Integer statusType = indexStatusResult.getStatusType();
            if (StringUtils.isEmpty(status)) {
                indexStatusResult.setStatus(IndexStatusEnum.GREEN.getStatus());
                indexStatusResult.setStatusType(0);
            } else if (IndexStatusEnum.YELLOW.getStatus().equals(status)) {
                indexStatusResult.setStatus(IndexStatusEnum.RED.getStatus().equals(indexStatus.getStatus()) ? IndexStatusEnum.RED.getStatus() : status);
                indexStatusResult.setStatusType(3 == indexStatus.getStatusType() ? 3 : statusType);
            }
            templateStatusMap.put(ofTemplate, indexStatusResult);
        });
        return templateStatusMap;
    }

    /**
     * 从缓存中获取物理集群在索引级别的健康状态
     *
     * @return
     */
    public ESClusterHealthResponse getPhyClusterHealthByCache(String phyClusterName) {
        if (StringUtils.isNotEmpty(phyClusterName)) {
            try {
                phyClusterHealthCache.get(phyClusterName, () -> esClusterService.syncGetClusterHealthAtIndicesLevel(phyClusterName));
            } catch (ExecutionException e) {
                logger.error("class=QueryAdminService||method=getPhyClusterHealthByCache||errMsg=exception!", e);
            }
            return esClusterService.syncGetClusterHealthAtIndicesLevel(phyClusterName);
        }
        return null;
    }

    //根据逻辑集群id找到逻辑集群所有的模板
    private List<String> getLogicClusterIndexes(Map<String/*phyClusterName*/, List<String>> phyClusterRackMap) {
        List<String> logicClusterIndexes = new ArrayList<>();
        List<IndexTemplatePhyWithLogic> indexTemplates = indexTemplatePhyService.listTemplateWithLogicWithCache();
        if (CollectionUtils.isNotEmpty(indexTemplates) && MapUtils.isNotEmpty(phyClusterRackMap)) {
            for (IndexTemplatePhyWithLogic indexTemplate : indexTemplates) {
                String phyIndexCluster = indexTemplate.getCluster();
                String[] indexRacks = StringUtils.split(indexTemplate.getRack(), ",");
                List<String> phyClusterRacks = phyClusterRackMap.getOrDefault(phyIndexCluster, Lists.newArrayList());
                if (null != indexRacks && phyClusterRacks.containsAll(Arrays.stream(indexRacks).collect(Collectors.toList()))) {
                    logicClusterIndexes.add(indexTemplate.getName());
                }
            }
        }
        return logicClusterIndexes;
    }

    private Map<String, List<String>> logicItem2Map(List<ClusterLogicRackInfo> items) {
        Map<String, List<String>> phyClusterRackMap = new HashMap<>();
        items.forEach(i -> {
            List<String> racks = phyClusterRackMap.get(i.getPhyClusterName());
            if (CollectionUtils.isEmpty(racks)) {
                List<String> rackList = new ArrayList<>();
                rackList.add(i.getRack());
                phyClusterRackMap.put(i.getPhyClusterName(), rackList);
            } else {
                racks.add(i.getRack());
            }
        });
        return phyClusterRackMap;
    }

    private String getStrClusterStatus(byte clusterStatus) {
        String strClusterStatus = STR_GREEN;
        if (clusterStatus == ClusterHealthStatus.YELLOW.value()) {
            strClusterStatus = "yellow";
        } else if (clusterStatus == ClusterHealthStatus.RED.value()) {
            strClusterStatus = "red";
        }
        return strClusterStatus;
    }
}
package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.LOGIC_CLUSTER;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.stats.ClusterLogicStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsCells;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.ClusterLogicStatsPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.event.metrics.MetricsMonitorLogicClusterEvent;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.monitortask.AriusMetaJobClusterDistributeService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.MonitorMetricsSender;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterLogicStatsService;
import com.didiglobal.logi.elasticsearch.client.response.indices.clusterindex.IndexStatusResult;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 集群维度采集监控数据，包含 es节点存活检查；es集群tps/qps掉底报警
 *
 * @author ohushenglin_v
 */
@Component
public class LogicClusterMonitorJobHandler extends AbstractMetaDataJob {

    @Value("${monitorJob.threadPool.maxsize:30}")
    private int maxPoolSize;
    @Autowired
    private ClusterLogicService clusterLogicService;
    @Autowired
    private AriusMetaJobClusterDistributeService ariusMetaJobClusterDistributeService;
    @Autowired
    private MonitorMetricsSender monitorMetricsSender;
    @Autowired
    private ESClusterLogicStatsService clusterLogicStaticsService;
    @Autowired
    private ClusterRegionService clusterRegionService;
    @Autowired
    private ClusterPhyService clusterPhyService;
    @Autowired
    private ClusterRoleHostService clusterRoleHostService;
    @Autowired
    private ESIndexCatService esIndexCatService;
    private final String hostName = HttpHostUtil.HOST_NAME;
    private FutureUtil<ESClusterStats> clusterLogicFutureUtil;
    private static final Integer SEARCH_SIZE = 5000;

    @Override
    public Object handleJobTask(String params) {
        handleLogicClusterStats();
        return JOB_SUCCESS;
    }

    @PostConstruct
    public void init() {
        clusterLogicFutureUtil = FutureUtil.init("LogicClusterMonitorJobHandler", 3 * maxPoolSize, 3 * maxPoolSize, 100);
    }

    private void handleLogicClusterStats() {
        // 获取单台机器监控采集的集群名称列表, 当分布式部署分组采集，可分摊采集压力
        List<ClusterPhy> monitorCluster = ariusMetaJobClusterDistributeService.getSingleMachineMonitorCluster(hostName);
        // 2. do handle
        if (CollectionUtils.isNotEmpty(monitorCluster)) {
            Set<String> monitorClusterSet = monitorCluster.stream().map(ClusterPhy::getCluster)
                    .collect(Collectors.toSet());
            doHandleLogicClusterStats(monitorClusterSet);
        }
    }

    private void doHandleLogicClusterStats(Set<String> monitorClusterSet) {
        List<ClusterRegion> regionList = clusterRegionService.listRegionByPhyClusterNames(Lists.newArrayList(monitorClusterSet)).stream()
                .filter(clusterRegion -> StringUtils.isNotBlank(clusterRegion.getLogicClusterIds())
                        && !AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID.equals(clusterRegion.getLogicClusterIds()))
                .collect(Collectors.toList());
        List<Long> logicClusterIds = regionList.stream().map(clusterRegion -> ListUtils.string2StrList(clusterRegion.getLogicClusterIds()))
                .flatMap(Collection::stream).distinct().map(Long::parseLong).collect(Collectors.toList());
        List<ClusterLogic> clusterLogicList = clusterLogicService.getClusterLogicListByIds(logicClusterIds);
        List<ClusterRoleHost> clusterRoleHostsList = clusterRoleHostService.listNodesByClusters(Lists.newArrayList(monitorClusterSet)).stream()
                .filter(i -> Objects.nonNull(i.getRegionId()) && !Objects.equals(-1, i.getRegionId())).collect(Collectors.toList());
        List<ClusterPhy> phyClusterList = clusterPhyService.listClustersByNames(Lists.newArrayList(monitorClusterSet));
        List<IndexCatCell> allCatIndexNameList = esIndexCatService.syncGetAllCatIndexNameListByClusters(SEARCH_SIZE,Lists.newArrayList(monitorClusterSet))
                .stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(clusterLogicList) || CollectionUtils.isEmpty(regionList)
                || CollectionUtils.isEmpty(clusterRoleHostsList) || CollectionUtils.isEmpty(phyClusterList)) {
            LOGGER.info("class=LogicClusterMonitorJobHandler||method=doHandleLogicClusterStats||msg=logicClusters is empty");
            return;
        }
        if (CollectionUtils.isEmpty(allCatIndexNameList)) {
            LOGGER.info("class=LogicClusterMonitorJobHandler||method=doHandleLogicClusterStats||msg=indices is empty");
            return;
        }
        //物理集群和region
        Map<String, List<ClusterRegion>> phyClusterRegionMap = ConvertUtil.list2MapOfList(regionList,
                clusterRegion -> clusterRegion.getPhyClusterName(), clusterRegionList -> clusterRegionList);
        //regionId和nodeName
        Map<Integer, List<String>> regionNodeMap = ConvertUtil.list2MapOfList(clusterRoleHostsList, ClusterRoleHost::getRegionId,
                ClusterRoleHost::getNodeSet);
        //物理集群和物理信息的关系
        Map<String, ClusterPhy> ClusterPhyMap = ConvertUtil.list2Map(phyClusterList, clusterPhy -> clusterPhy.getCluster(),
                clusterPhy -> clusterPhy);
        //逻辑集群和索引的关系
        Map<Long, List<String>> logicClusterIndicesNameMap = ConvertUtil.list2MapOfList(allCatIndexNameList,
                IndexCatCell::getResourceId, IndexCatCell::getIndex);
        //逻辑集群对象
        Map<Long, ClusterLogic> longClusterLogicMap = ConvertUtil.list2Map(clusterLogicList,
                ClusterLogic::getId, clusterLogic -> clusterLogic);
        //物理集群和逻辑集群
        Map<String, List<ClusterLogic>> phyClusterNameLogicCluster = Maps.newHashMap();
        Map<String, List<String>> phyClusterNameLogicCLusterIds = regionList.stream()
                .collect(Collectors.toMap(ClusterRegion::getPhyClusterName, clusterRegion ->
                        Lists.newArrayList(StringUtils.split(clusterRegion.getLogicClusterIds(), ","))
                        , (oldList, newList) -> {
            oldList.addAll(newList);
            return oldList;
        }));
        for (Map.Entry<String, List<String>> entry : phyClusterNameLogicCLusterIds.entrySet()) {
            List<ClusterLogic> logicList = entry.getValue().stream().
                    map(logicId -> longClusterLogicMap.get(Long.parseLong(logicId))).filter(Objects::nonNull).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(logicList)) {
                phyClusterNameLogicCluster.put(entry.getKey(), logicList);
            }
        }
        //维护逻辑集群和节点名称之间的关系，物理机群节点统计信息的关系，逻辑集群和索引统计信息的关系，方便后面计算
        Map<Integer, List<String>> logicClusterNodesNameMap = Maps.newConcurrentMap();
        Map<String, List<ClusterLogicStats>> nodeStatsMap = Maps.newConcurrentMap();
        Map<Long, List<IndexStatusResult>> logicClusterIndicesStatsMap = Maps.newConcurrentMap();
        for (String phyClusterName : monitorClusterSet) {
            clusterLogicFutureUtil.runnableTask(() -> {
                clusterLogicStaticsService.buildLogicClusterStats(phyClusterRegionMap, regionNodeMap,
                        logicClusterIndicesNameMap, phyClusterName, logicClusterNodesNameMap,
                        nodeStatsMap, logicClusterIndicesStatsMap);
                //指标采集
                List<ESClusterStats> esLogicClusterStatsList = Lists.newArrayList();
                long collectTime = CommonUtils.monitorTimestamp2min(System.currentTimeMillis());
                List<ClusterLogic> logicClusterListByPhyClusterName = phyClusterNameLogicCluster.get(phyClusterName);
                if (CollectionUtils.isNotEmpty(logicClusterListByPhyClusterName)) {
                    logicClusterListByPhyClusterName.stream()
                            .filter(logicCluster -> logicClusterNodesNameMap.containsKey(logicCluster.getId().intValue()))
                            .forEach(logicCluster -> {
                        ClusterLogicStatsPO clusterLogicStatisPO = clusterLogicStaticsService.getLogicClusterStats(logicCluster.getId(),
                                ClusterPhyMap.get(phyClusterName), logicClusterNodesNameMap,
                                nodeStatsMap, logicClusterIndicesStatsMap, true);
                        ESClusterStatsCells esClusterStatsBean = new ESClusterStatsCells();
                        esClusterStatsBean.setStatus(clusterLogicStatisPO.getStatus());
                        esClusterStatsBean.setStatusType(clusterLogicStatisPO.getStatusType());
                        esClusterStatsBean.setClusterName(logicCluster.getName());
                        esClusterStatsBean.setLevel(logicCluster.getLevel());
                        esClusterStatsBean.setClusterNu(1);
                        esClusterStatsBean.setTotalIndicesNu(clusterLogicStatisPO.getIndexNu());
                        esClusterStatsBean.setTotalDocNu(clusterLogicStatisPO.getDocNu());
                        Optional.ofNullable(clusterLogicStatisPO.getUsedDisk())
                                .ifPresent(usedDisk->esClusterStatsBean.setIndexStoreSize(usedDisk.longValue()));
                        Optional.ofNullable(clusterLogicStatisPO.getUsedDisk())
                                .ifPresent(usedDisk->esClusterStatsBean.setStoreSize(usedDisk.longValue()));
                        Optional.ofNullable(clusterLogicStatisPO.getFreeDisk())
                                .ifPresent(freeDisk->esClusterStatsBean.setFreeStoreSize(freeDisk.longValue()));
                        Optional.ofNullable(clusterLogicStatisPO.getTotalDisk())
                                .ifPresent(totalDisk->esClusterStatsBean.setTotalStoreSize(totalDisk.longValue()));
                        esClusterStatsBean.setNumberDataNodes(clusterLogicStatisPO.getNumberDataNodes());
                        esClusterStatsBean.setNumberPendingTasks(clusterLogicStatisPO.getNumberPendingTasks());
                        esClusterStatsBean.setUnAssignedShards(clusterLogicStatisPO.getUnAssignedShards());
                        esClusterStatsBean.setCpuUsage(clusterLogicStatisPO.getCpuUsedPercent());
                        esClusterStatsBean.setAlivePercent(clusterLogicStatisPO.getAlivePercent());
                        if (Objects.nonNull(esClusterStatsBean.getStoreSize()) && Objects.nonNull(esClusterStatsBean.getTotalStoreSize())
                                && 0.0 < esClusterStatsBean.getTotalStoreSize()) {
                            esClusterStatsBean.setDiskUsage(esClusterStatsBean.getStoreSize().doubleValue() / esClusterStatsBean.getTotalStoreSize());
                        }
                        ESClusterStats esClusterStats = new ESClusterStats();
                        esClusterStats.setStatis(esClusterStatsBean);
                        esClusterStats.setCluster(logicCluster.getName());
                        // 设置集群arius id
                        esClusterStats.setClusterId(String.valueOf(logicCluster.getId()));
                        esClusterStats.setPhysicCluster(LOGIC_CLUSTER);
                        esClusterStats.setTimestamp(collectTime);
                        esClusterStats.setDataCenter(logicCluster.getDataCenter());
                        esLogicClusterStatsList.add(esClusterStats);
                    });
                }
                // send cluster status to es and kafka
                if (CollectionUtils.isNotEmpty(esLogicClusterStatsList)) {
                    monitorMetricsSender.sendClusterStats(esLogicClusterStatsList);
                    SpringTool.publish(new MetricsMonitorLogicClusterEvent(this, esLogicClusterStatsList, hostName));
                }
            });
        }
        clusterLogicFutureUtil.waitExecute();
    }

    /**
     * 测试使用接口，传入监控机器名 * * @param hostName
     */
    public void testHandleLogicClusterStatsByHost(String hostName) {
        long timeMillis = System.currentTimeMillis();
        // 获取单台机器监控采集的集群名称列表, 当分布式部署分组采集，可分摊采集压力
        List<ClusterPhy> monitorCluster = ariusMetaJobClusterDistributeService.getSingleMachineMonitorCluster(hostName);
        // 2. do handle
        if (CollectionUtils.isEmpty(monitorCluster)) {
            LOGGER.info("class=LogicClusterMonitorJobHandler||method=handleLogicClusterStats||msg=test method monitorPhyClusters is empty");
        } else {
            Set<String> monitorClusterSet = monitorCluster.stream().map(ClusterPhy::getCluster).collect(Collectors.toSet());
            doHandleLogicClusterStats(monitorClusterSet);
            String info = String.format("测试采集时间=host:%s||集群数=%d||采集时间=%s||集群名=%s", hostName, monitorClusterSet.size(), (System.currentTimeMillis() - timeMillis), Arrays.toString(monitorClusterSet.toArray()));
            LOGGER.info(info);
        }
    }
}
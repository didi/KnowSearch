package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.LOGIC_CLUSTER;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsCells;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.ClusterLogicStatsPO;
import com.didichuxing.datachannel.arius.admin.common.constant.PercentilesEnum;
import com.didichuxing.datachannel.arius.admin.common.event.metrics.MetricsMonitorLogicClusterEvent;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.monitortask.AriusMetaJobClusterDistributeService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.MonitorMetricsSender;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterLogicStatsService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsNodeInfoESDAO;
import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*;
import com.google.common.collect.Maps;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

/**
 * 集群维度采集监控数据，包含 es节点存活检查；es集群tps/qps掉底报警
 * @author ohushenglin_v
 */
@Component
public class LogicClusterMonitorJobHandler extends AbstractMetaDataJob {

    @Value("${monitorJob.threadPool.maxsize:30}")
    private int                                  maxPoolSize;
    @Autowired
    private ClusterLogicService                  clusterLogicService;
    @Autowired
    private AriusMetaJobClusterDistributeService ariusMetaJobClusterDistributeService;
    @Autowired
    private MonitorMetricsSender                 monitorMetricsSender;
    @Autowired
    private ESClusterLogicStatsService           clusterLogicStaticsService;
    @Autowired
    private ClusterRegionService                 clusterRegionService;
    @Autowired
    private ClusterPhyService                    clusterPhyService;
    @Autowired
    private ClusterRoleHostService               clusterRoleHostService;
    @Autowired
    private AriusStatsNodeInfoESDAO              ariusStatsNodeInfoEsDao;

    private final String hostName = HttpHostUtil.HOST_NAME;

    private FutureUtil<ESClusterStats> clusterLogicFutureUtil;


    private long                                 timestamp = CommonUtils
            .monitorTimestamp2min(System.currentTimeMillis());

    @Override
    public Object handleJobTask(String params) {
        handleLogicClusterStats();
        return JOB_SUCCESS;
    }

    @PostConstruct
    public void init() {
        clusterLogicFutureUtil = FutureUtil.init("ClusterLogicMonitorJobHandler", 3 * maxPoolSize, 3 * maxPoolSize,
            100);
    }

    private void handleLogicClusterStats() {
        // 获取单台机器监控采集的集群名称列表, 当分布式部署分组采集，可分摊采集压力
        List<ClusterPhy> monitorCluster = ariusMetaJobClusterDistributeService.getSingleMachineMonitorCluster(hostName);
        monitorCluster = clusterPhyService.listAllClusters();
        // 2. do handle
        if (CollectionUtils.isNotEmpty(monitorCluster)) {
            Set<String> monitorClusterSet = monitorCluster.stream().map(ClusterPhy::getCluster)
                .collect(Collectors.toSet());
            doHandleLogicClusterStats(monitorClusterSet);
        }
    }

    private void doHandleLogicClusterStats(Set<String> monitorClusterSet) {
        List<ESClusterStats> esLogicClusterStatsList = Lists.newCopyOnWriteArrayList();
        List<ClusterLogic> clusterLogicList = clusterLogicService.listAllClusterLogics();
        List<ClusterRegion> regionList = clusterRegionService.listAllBoundRegions();
        if (CollectionUtils.isEmpty(clusterLogicList) || CollectionUtils.isEmpty(regionList)) {
            LOGGER.info("class=ClusterMonitorJobHandler||method=doHandleLogicClusterStats||msg=ClusterLogic is empty");
            return;
        }
        Set<Long> clusterIds = new HashSet<>();
        regionList.stream().filter(region -> monitorClusterSet.contains(region.getPhyClusterName()))
            .map(ClusterRegion::getLogicClusterIds).filter(StringUtils::isNotBlank).map(StringUtils::split)
            .forEach(ids -> {
                for (String str : ids) {
                    long id = Long.parseLong(str);
                    if (id > 0) {
                        clusterIds.add(Long.valueOf(str));
                    }
                }
            });
        long collectTime = CommonUtils.monitorTimestamp2min(System.currentTimeMillis());
        clusterLogicList.stream().filter(clusterLogic -> clusterIds.contains(clusterLogic.getId()))
            .forEach(logicCluster -> {
                clusterLogicFutureUtil.runnableTask(() -> {
                    ClusterLogicStatsPO clusterLogicStatsPO = null;
                    try {
                        clusterLogicStatsPO = clusterLogicStaticsService.getLogicClusterStats(logicCluster.getId(),
                            true);
                    } catch (Exception e) {
                        LOGGER.error(
                            "class=ClusterLogicMonitorJobHandler||method=doHandleLogicClusterStats||logicClusterId={}||"
                                     + "msg=failed to get LogicClusterStats",
                            logicCluster.getId());
                    }
                    if (null == clusterLogicStatsPO) {
                        return;
                    }
                    ESClusterStatsCells esClusterStatsBean = new ESClusterStatsCells();
                    esClusterStatsBean.setStatus(clusterLogicStatsPO.getStatus());
                    esClusterStatsBean.setStatusType(clusterLogicStatsPO.getStatusType());
                    esClusterStatsBean.setClusterName(logicCluster.getName());
                    esClusterStatsBean.setLevel(logicCluster.getLevel());
                    esClusterStatsBean.setClusterNu(1);
                    esClusterStatsBean.setTotalDocNu((long) clusterLogicStatsPO.getDocNu());
                    esClusterStatsBean.setIndexStoreSize(clusterLogicStatsPO.getUsedDisk());
                    esClusterStatsBean.setStoreSize(clusterLogicStatsPO.getUsedDisk());
                    esClusterStatsBean.setFreeStoreSize(clusterLogicStatsPO.getFreeDisk());
                    esClusterStatsBean.setTotalStoreSize(clusterLogicStatsPO.getTotalDisk());
                    esClusterStatsBean.setNumberDataNodes(clusterLogicStatsPO.getNumberDataNodes());
                    esClusterStatsBean.setNumberPendingTasks(clusterLogicStatsPO.getNumberPendingTasks());
                    esClusterStatsBean.setUnAssignedShards(clusterLogicStatsPO.getUnAssignedShards());
                    esClusterStatsBean.setCpuUsage(clusterLogicStatsPO.getCpuUsedPercent());
                    esClusterStatsBean.setAlivePercent(clusterLogicStatsPO.getAlivePercent());
                    if (0 != esClusterStatsBean.getTotalStoreSize()) {
                        esClusterStatsBean
                            .setDiskUsage(esClusterStatsBean.getStoreSize() / esClusterStatsBean.getTotalStoreSize());
                    }
                    ESClusterStats esClusterStats = new ESClusterStats();
                    esClusterStats.setStatis(esClusterStatsBean);
                    esClusterStats.setCluster(logicCluster.getName());
                    // 设置集群arius id
                    esClusterStats.setPhysicCluster(LOGIC_CLUSTER);
                    esClusterStats.setTimestamp(collectTime);
                    esClusterStats.setDataCenter(logicCluster.getDataCenter());
                    esLogicClusterStatsList.add(esClusterStats);
                });
            });
        clusterLogicFutureUtil.waitExecute(50);

        // send cluster status to es and kafka
        if (CollectionUtils.isNotEmpty(esLogicClusterStatsList)) {
            monitorMetricsSender.sendClusterStats(esLogicClusterStatsList);
            SpringTool.publish(new MetricsMonitorLogicClusterEvent(this, esLogicClusterStatsList, hostName));
        }
    }

    /**
     * 获取逻辑集群下的nodes
     * @param clusterLogicId
     * @return
     */
    private List<String> getNodesWithClusterLogic(Long  clusterLogicId) {
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogicId);
        Result<List<ClusterRoleHost>> result = clusterRoleHostService
                .listByRegionId(Math.toIntExact(clusterRegion.getId()));
        List<String> nodes = result.getData().stream().map(ClusterRoleHost::getNodeSet).collect(Collectors.toList());
        return nodes;
    }

    /**
     * 采集不同分位图的指标数据
     * @return  Map<String, ESClusterStatsCells>
     */
    private Map<String, ESClusterStatsCells> getPhysicalClusterStatsPercentiles(String clusterLogicName,List<String> nodes,ESClusterStatsCells esClusterStatsCells) {
        Map<String, ESClusterStatsCells> percentilesType2ESClusterStatsCellsMap = Maps.newHashMap();
        if (AriusObjUtils.isNull(clusterLogicName)) {
            LOGGER.warn(
                    "class=ClusterMonitorJobHandler||method=getPhysicalClusterStatsPercentiles||errMsg=clusterName is empty");
            return percentilesType2ESClusterStatsCellsMap;
        }
        AtomicReference<Map<String, Double>> clusterCpuAvgAndPercentilesAtomic = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterDiskFreeUsagePercentAvgAndPercentilesAtomic = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterCpuLoad1MinAvgAndPercentilesAtomic = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterCpuLoad5MinAvgAndPercentilesAtomic = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterCpuLoad15MinAvgAndPercentilesAtomic = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterTaskCostMinAvgAndPercentilesAtomic = new AtomicReference<>();

        clusterLogicFutureUtil
                .runnableTask(() -> clusterCpuAvgAndPercentilesAtomic
                        .set(ariusStatsNodeInfoEsDao.getClusterLogicCpuAvgAndPercentiles(nodes)))
                .runnableTask(() -> clusterDiskFreeUsagePercentAvgAndPercentilesAtomic
                        .set(ariusStatsNodeInfoEsDao.getClusterLogicDiskFreeUsagePercentAvgAndPercentiles(nodes)))
                .runnableTask(() -> clusterCpuLoad1MinAvgAndPercentilesAtomic
                        .set(ariusStatsNodeInfoEsDao.getClusterLogicCpuLoad1MinAvgAndPercentiles(nodes)))
                .runnableTask(() -> clusterCpuLoad5MinAvgAndPercentilesAtomic
                        .set(ariusStatsNodeInfoEsDao.getClusterLogicCpuLoad5MinAvgAndPercentiles(nodes)))
                .runnableTask(() -> clusterCpuLoad15MinAvgAndPercentilesAtomic
                        .set(ariusStatsNodeInfoEsDao.getClusterLogicCpuLoad15MinAvgAndPercentiles(nodes)))
//                .runnableTask(() -> clusterTaskCostMinAvgAndPercentilesAtomic
//                        .set(ariusStatsClusterTaskInfoESDAO.getTaskCostMinAvgAndPercentiles(clusterName)))
                .waitExecute();

        for (String type : PercentilesEnum.listUsefulType()) {
            ESClusterStatsCells esClusterStatsCellDeepCopy = ConvertUtil.obj2Obj(esClusterStatsCells,
                    ESClusterStatsCells.class);

            buildForPercentiles(esClusterStatsCellDeepCopy, type, clusterCpuAvgAndPercentilesAtomic.get(),
                    clusterCpuLoad1MinAvgAndPercentilesAtomic.get(), clusterCpuLoad5MinAvgAndPercentilesAtomic.get(),
                    clusterCpuLoad15MinAvgAndPercentilesAtomic.get(),
                    clusterDiskFreeUsagePercentAvgAndPercentilesAtomic.get(),
                    clusterTaskCostMinAvgAndPercentilesAtomic.get());

            percentilesType2ESClusterStatsCellsMap.put(type, esClusterStatsCellDeepCopy);

        }

        return percentilesType2ESClusterStatsCellsMap;
    }

    /**
     * @param esClusterStatsCellDeepCopy                       集群维度统计信息
     * @param type                                             avg、分位类型(99、95、75、55)
     * @param clusterCpuAvgAndPercentiles                      集群cpu平均值和分位值(key:99, value:值)
     * @param clusterDiskFreeUsagePercentAvgAndPercentiles     集群节点磁盘空闲率平均值和分位值(key:99, value:值)
     * @param clusterCpuLoad1MinAvgAndPercentiles              集群cpu load1平均值和分位值(key:99, value:值)
     * @param clusterCpuLoad5MinAvgAndPercentiles              集群cpu load5平均值和分位值(key:99, value:值)
     * @param clusterCpuLoad15MinAvgAndPercentiles             集群cpu load15平均值和分位值(key:99, value:值)
     * @param clusterTaskCostMinAvgAndPercentiles              集群task cost平均值和分位值(key:99, value:值)
     */
    private void buildForPercentiles(ESClusterStatsCells esClusterStatsCellDeepCopy, String type,
                                     Map<String, Double> clusterCpuAvgAndPercentiles,
                                     Map<String, Double> clusterCpuLoad1MinAvgAndPercentiles,
                                     Map<String, Double> clusterCpuLoad5MinAvgAndPercentiles,
                                     Map<String, Double> clusterCpuLoad15MinAvgAndPercentiles,
                                     Map<String, Double> clusterDiskFreeUsagePercentAvgAndPercentiles,
                                     Map<String, Double> clusterTaskCostMinAvgAndPercentiles) {
        if (null != clusterCpuAvgAndPercentiles && null != clusterCpuAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setCpuUsage(clusterCpuAvgAndPercentiles.get(type));
        }

        if (null != clusterCpuLoad1MinAvgAndPercentiles && null != clusterCpuLoad1MinAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setCpuLoad1M(clusterCpuLoad1MinAvgAndPercentiles.get(type));
        }

        if (null != clusterCpuLoad5MinAvgAndPercentiles && null != clusterCpuLoad5MinAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setCpuLoad5M(clusterCpuLoad5MinAvgAndPercentiles.get(type));
        }

        if (null != clusterCpuLoad15MinAvgAndPercentiles && null != clusterCpuLoad15MinAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setCpuLoad15M(clusterCpuLoad15MinAvgAndPercentiles.get(type));
        }

        String realType = convertSpecialTypeForDiskFreeUsage(type);
        if (null != clusterDiskFreeUsagePercentAvgAndPercentiles
                && null != clusterDiskFreeUsagePercentAvgAndPercentiles.get(realType)) {
            esClusterStatsCellDeepCopy.setDiskUsage(1 - clusterDiskFreeUsagePercentAvgAndPercentiles.get(realType));
        }

        if (null != clusterTaskCostMinAvgAndPercentiles && null != clusterTaskCostMinAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setTaskCost(clusterTaskCostMinAvgAndPercentiles.get(type));
        }
    }

    private String convertSpecialTypeForDiskFreeUsage(String type) {
        if (PercentilesEnum.ST99.getType().equals(type)) {
            return PercentilesEnum.ST1.getType();
        }

        if (PercentilesEnum.ST95.getType().equals(type)) {
            return PercentilesEnum.ST5.getType();
        }

        if (PercentilesEnum.ST75.getType().equals(type)) {
            return PercentilesEnum.ST25.getType();
        }

        if (PercentilesEnum.ST55.getType().equals(type)) {
            return PercentilesEnum.ST45.getType();
        }

        return PercentilesEnum.AVG.getType();
    }



    public static void main(String[] args) {
        List<String> nodes = new ArrayList<>();
        nodes.add("123");
        nodes.add("abc");
        System.out.println(JSON.toJSONString(nodes));
    }
}
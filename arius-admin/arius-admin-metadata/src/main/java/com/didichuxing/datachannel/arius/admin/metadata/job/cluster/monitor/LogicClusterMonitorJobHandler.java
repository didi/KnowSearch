package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsCells;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.ClusterLogicStatisPO;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.event.metrics.MetricsMonitorLogicClusterEvent;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpHostUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.monitorTask.ClusterMonitorTaskService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.MonitorMetricsSender;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterLogicStaticsService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.LOGIC_CLUSTER;

/**
 * 集群维度采集监控数据，包含 es节点存活检查；es集群tps/qps掉底报警
 */
@Component
public class LogicClusterMonitorJobHandler extends AbstractMetaDataJob {

    @Value("${monitorJob.threadPool.maxsize:30}")
    private int maxPoolSize;
    @Autowired
    private ClusterLogicService clusterLogicService;
    @Autowired
    private ClusterMonitorTaskService clusterMonitorTaskService;
    @Autowired
    private MonitorMetricsSender monitorMetricsSender;
    @Autowired
    private ESClusterLogicStaticsService clusterLogicStaticsService;
    @Autowired
    private RegionRackService regionRackService;

    private final String hostName = HttpHostUtil.HOST_NAME;

    private FutureUtil<Void> clusterLogicFutureUtil;

    @Override
    public Object handleJobTask(String params) {
        handleLogicClusterStats();
        return JOB_SUCCESS;
    }

    @PostConstruct
    public void init() {
        clusterLogicFutureUtil = FutureUtil.init("ClusterLogicMonitorJobHandler", 3 * maxPoolSize, 3 * maxPoolSize, 100);
    }

    private void handleLogicClusterStats() {
        // 获取单台机器监控采集的集群名称列表, 当分布式部署分组采集，可分摊采集压力
        List<ClusterPhy> monitorCluster = clusterMonitorTaskService.getSingleMachineMonitorCluster(hostName);
        // 2. do handle
        if (CollectionUtils.isNotEmpty(monitorCluster)) {
            Set<String> monitorClusterSet = monitorCluster.stream().map(ClusterPhy::getCluster).collect(Collectors.toSet());
            doHandleLogicClusterStats(monitorClusterSet);
        }
    }

    private void doHandleLogicClusterStats(Set<String> monitorClusterSet) {
        List<ESClusterStats> esLogicClusterStatsList = Lists.newCopyOnWriteArrayList();
        List<ClusterLogic> clusterLogicList = clusterLogicService.listAllClusterLogics();
        List<ClusterRegion> regionList = regionRackService.listAllBoundRegions();
        if (CollectionUtils.isEmpty(clusterLogicList) || CollectionUtils.isEmpty(regionList)) {
            LOGGER.info("class=ClusterMonitorJobHandler||method=doHandleLogicClusterStats||msg=ClusterLogic is empty");
            return;
        }
        Set<Long> clusterIds = new HashSet<>();
        regionList.stream().filter(region -> monitorClusterSet.contains(region.getPhyClusterName())).map(ClusterRegion::getLogicClusterIds).filter(StringUtils::isNotBlank).map(StringUtils::split).forEach(ids -> {
            for (String str : ids) {
                long id = Long.parseLong(str);
                if (id > 0) {
                    clusterIds.add(Long.valueOf(str));
                }
            }
        });
        long collectTime = CommonUtils.monitorTimestamp2min(System.currentTimeMillis());
        clusterLogicList.stream().filter(clusterLogic -> clusterIds.contains(clusterLogic.getId())).forEach(logicCluster -> {
            clusterLogicFutureUtil.runnableTask(() -> {
                ClusterLogicStatisPO clusterLogicStatisPO = null;
                try {
                    clusterLogicStatisPO = clusterLogicStaticsService.getLogicClusterStats(logicCluster.getId(), true);
                } catch (Exception e) {
                    LOGGER.error("class=ClusterLogicMonitorJobHandler||method=doHandleLogicClusterStats||logicClusterId={}||" + "msg=failed to get LogicClusterStats", logicCluster.getId());
                }
                if (null == clusterLogicStatisPO) {
                    return;
                }
                ESClusterStatsCells esClusterStatsBean = new ESClusterStatsCells();
                esClusterStatsBean.setStatus(clusterLogicStatisPO.getStatus());
                esClusterStatsBean.setStatusType(clusterLogicStatisPO.getStatusType());
                esClusterStatsBean.setClusterName(logicCluster.getName());
                esClusterStatsBean.setLevel(logicCluster.getLevel());
                esClusterStatsBean.setClusterNu(1);
                esClusterStatsBean.setTotalDocNu((long) clusterLogicStatisPO.getDocNu());
                esClusterStatsBean.setIndexStoreSize(clusterLogicStatisPO.getUsedDisk());
                esClusterStatsBean.setStoreSize(clusterLogicStatisPO.getUsedDisk());
                esClusterStatsBean.setFreeStoreSize(clusterLogicStatisPO.getFreeDisk());
                esClusterStatsBean.setTotalStoreSize(clusterLogicStatisPO.getTotalDisk());
                esClusterStatsBean.setNumberDataNodes(clusterLogicStatisPO.getNumberDataNodes());
                esClusterStatsBean.setNumberPendingTasks(clusterLogicStatisPO.getNumberPendingTasks());
                esClusterStatsBean.setUnAssignedShards(clusterLogicStatisPO.getUnAssignedShards());
                esClusterStatsBean.setCpuUsage(clusterLogicStatisPO.getCpuUsedPercent());
                esClusterStatsBean.setAlivePercent(clusterLogicStatisPO.getAlivePercent());
                if (0 != esClusterStatsBean.getTotalStoreSize()) {
                    esClusterStatsBean.setDiskUsage(esClusterStatsBean.getStoreSize() / esClusterStatsBean.getTotalStoreSize());
                }
                ESClusterStats esClusterStats = new ESClusterStats();
                esClusterStats.setStatis(esClusterStatsBean);
                esClusterStats.setCluster(logicCluster.getName());
                // 设置集群arius id
//                esClusterStats.setClusterId(String.valueOf(logicCluster.getId()));
//                esClusterStats.setClusterGuid(logicCluster.getGuid());
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
}
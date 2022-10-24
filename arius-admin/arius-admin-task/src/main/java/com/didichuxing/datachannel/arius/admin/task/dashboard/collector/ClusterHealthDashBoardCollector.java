package com.didichuxing.datachannel.arius.admin.task.dashboard.collector;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.ClusterPhyHealthMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.DashBoardStats;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * Created by linyunan on 3/11/22
 * dashboard汇总多个集群健康度采集器
 */
@Component
public class ClusterHealthDashBoardCollector extends BaseDashboardCollector {
    private static final ILog       LOGGER      = LogFactory.getLog(ClusterHealthDashBoardCollector.class);

    private static final FutureUtil FUTURE_UTIL = FutureUtil.init("ClusterHealthDashBoardCollector", 10, 10, 500);

    @Override
    public void collectSingleCluster(String cluster, long currentTime) {
    }

    @Override
    public void collectAllCluster(List<String> clusterList, long currentTime) {
        if (CollectionUtils.isEmpty(clusterList)) {
            LOGGER.warn("class=ClusterHealthDashBoardCollector||method=collectAllCluster||msg=clusterList is empty");
            return;
        }

        DashBoardStats dashBoardStats = buildInitDashBoardStats(currentTime);
        ClusterPhyHealthMetrics clusterPhyHealthMetrics = new ClusterPhyHealthMetrics();
        clusterPhyHealthMetrics.setTimestamp(currentTime);

        List<String> greenClusterList = Lists.newCopyOnWriteArrayList();
        List<String> yellowClusterList = Lists.newCopyOnWriteArrayList();
        List<String> redClusterList = Lists.newCopyOnWriteArrayList();
        List<String> unknownClusterList = Lists.newCopyOnWriteArrayList();
        for (String cluster : clusterList) {
            // do concurrent 
            FUTURE_UTIL.runnableTask(() -> {
                try {
                    switch (esClusterService.syncGetClusterHealthEnum(cluster)) {
                        case GREEN:
                            greenClusterList.add(cluster);
                            break;
                        case YELLOW:
                            yellowClusterList.add(cluster);
                            break;
                        case RED:
                            redClusterList.add(cluster);
                            break;
                        default:
                            unknownClusterList.add(cluster);
                    }
                } catch (Exception e) {
                    LOGGER.error("class=ClusterHealthDashBoardCollector||method=collectAllCluster||errMsg={}",
                        e.getMessage(), e);
                }
            });
        }

        // 阻塞等待采集结束
        FUTURE_UTIL.waitExecute();

        clusterPhyHealthMetrics.setTotalNum(clusterList.size());
        clusterPhyHealthMetrics.setGreenNum(greenClusterList.size());
        clusterPhyHealthMetrics.setYellowNum(yellowClusterList.size());
        clusterPhyHealthMetrics.setRedNum(redClusterList.size());
        clusterPhyHealthMetrics.setUnknownNum(unknownClusterList.size());

        clusterPhyHealthMetrics.setYellowClusterListStr(ListUtils.strList2String(yellowClusterList));
        clusterPhyHealthMetrics.setRedClusterListStr(ListUtils.strList2String(redClusterList));
        clusterPhyHealthMetrics.setUnknownClusterListStr(ListUtils.strList2String(unknownClusterList));
        clusterPhyHealthMetrics.setGreenClusterListStr(ListUtils.strList2String(greenClusterList));

        dashBoardStats.setClusterPhyHealth(clusterPhyHealthMetrics);
        monitorMetricsSender.sendDashboardStats(Lists.newArrayList(dashBoardStats));
    }

    @Override
    public String getName() {
        return "ClusterHealthDashBoardCollector";
    }
}
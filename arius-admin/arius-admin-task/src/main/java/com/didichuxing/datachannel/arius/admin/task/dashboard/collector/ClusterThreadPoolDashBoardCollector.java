package com.didichuxing.datachannel.arius.admin.task.dashboard.collector;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterThreadStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.ClusterThreadPoolQueueMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.DashBoardStats;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by linyunan on 3/11/22
 * dashboard单个集群线程池采集器
 */
@Component
public class ClusterThreadPoolDashBoardCollector extends BaseDashboardCollector {
    private static final ILog             LOGGER     = LogFactory.getLog(ClusterThreadPoolDashBoardCollector.class);

    @Override
    public void collectSingleCluster(String cluster, long currentTime) {
        DashBoardStats dashBoardStats = buildInitDashBoardStats(currentTime);
        ClusterThreadPoolQueueMetrics clusterThreadPoolQueueMetrics = new ClusterThreadPoolQueueMetrics();
        clusterThreadPoolQueueMetrics.setTimestamp(currentTime);
        clusterThreadPoolQueueMetrics.setCluster(cluster);

        //1集群线程池queue大小(management、refresh、flush、merge、search、write)
        buildClusterThreadPoolQueueMetrics(cluster, clusterThreadPoolQueueMetrics);

        dashBoardStats.setClusterThreadPoolQueue(clusterThreadPoolQueueMetrics);
        monitorMetricsSender.sendDashboardStats(Lists.newArrayList(dashBoardStats));
    }

    @Override
    public void collectAllCluster(List<String> clusterList, long currentTime) {

    }

    @Override
    public String getName() {
        return "NodeThreadPoolDashBoardCollector";
    }

    private void buildClusterThreadPoolQueueMetrics(String cluster, ClusterThreadPoolQueueMetrics clusterThreadPoolQueueMetrics) {
        try {
            ESClusterThreadStats esClusterThreadStats = esClusterService.syncGetThreadStatsByCluster(cluster);
            if (esClusterThreadStats != null) {
                org.springframework.beans.BeanUtils.copyProperties(clusterThreadPoolQueueMetrics, esClusterThreadStats);
            }
        } catch (Exception e) {
            LOGGER.error("buildClusterThreadPoolQueueMetrics error{}", e);
        }
    }
}

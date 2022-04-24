package com.didichuxing.datachannel.arius.admin.task.dashboard.collector;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.ClusterMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.DashBoardStats;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterPhyStaticsService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created by linyunan on 3/11/22
 * dashboard单个集群维度采集器
 */
@Component
public class ClusterDashBoardCollector extends BaseDashboardCollector {
    private static final ILog            LOGGER = LogFactory.getLog(ClusterDashBoardCollector.class);
    @Autowired
    protected ESClusterPhyStaticsService esClusterPhyStaticsService;

    private static final Map<String/*集群名称*/, ClusterMetrics /*上一次采集到的集群数据*/> cluster2LastTimeClusterMetricsMap = Maps.newConcurrentMap();

    @Override
    public void collectSingleCluster(String cluster, long currentTime) {
        DashBoardStats dashBoardStats = buildInitDashBoardStats(currentTime);

        ClusterMetrics clusterMetrics = cluster2LastTimeClusterMetricsMap.getOrDefault(cluster, new ClusterMetrics());
        clusterMetrics.setTimestamp(currentTime);
        clusterMetrics.setCluster(cluster);
        clusterMetrics.setTimestamp(currentTime);
        // 1. 写入耗时
        clusterMetrics.setIndexingLatency(esClusterPhyStaticsService.getClusterIndexingLatency(cluster));
        // 2. 查询耗时
        clusterMetrics.setSearchLatency(esClusterPhyStaticsService.getClusterSearchLatency(cluster));
        //4. 集群shard总数
        clusterMetrics.setShardNum(esClusterPhyStaticsService.getClustersShardTotal(cluster));
        // 5. 写入请求数
        clusterMetrics.setIndexReqNum(esClusterPhyStaticsService.getCurrentIndexTotal(cluster));
        // 6. 网关成功率、失败率
        Tuple<Double/*成功率*/, Double/*失败率*/> gatewaySuccessRateAndFailureRate = esClusterPhyStaticsService.getGatewaySuccessRateAndFailureRate(cluster);
        clusterMetrics.setGatewaySucPer(gatewaySuccessRateAndFailureRate.getV1());
        clusterMetrics.setGatewayFailedPer(gatewaySuccessRateAndFailureRate.getV2());
        // 7. 集群Pending task数
        clusterMetrics.setPendingTaskNum(esClusterPhyStaticsService.getPendingTaskTotal(cluster));
        // 8. 集群http连接数
        clusterMetrics.setHttpNum(esClusterPhyStaticsService.getHttpConnectionTotal(cluster));
        //9. 查询请求数突增量 （上个时间间隔请求数的两倍）
        clusterMetrics.setReqUprushNum(getReqUprushNum(cluster));
        //10.写入文档数突增量（上个时间间隔的写文档数的两倍
        clusterMetrics.setDocUprushNum(getDocUprushNum(cluster));

        dashBoardStats.setCluster(clusterMetrics);
        monitorMetricsSender.sendDashboardStats(Lists.newArrayList(dashBoardStats));

        // 暂存当前集群指标信息 针对特殊场景，即集群不可用后, 当前的策略是会使用上一次采集到的数据
        cluster2LastTimeClusterMetricsMap.put(cluster, clusterMetrics);
    }

    @Override
    public void collectAllCluster(List<String> clusterList, long currentTime) {

    }

    @Override
    public String getName() {
        return "ClusterDashBoardCollector";
    }

    /**
     * 计算索引写入文档突增量
     * @param cluster   集群名称
     * @return {@link Long}
     */
    private Long getDocUprushNum(String cluster) {
        ClusterMetrics clusterMetrics = cluster2LastTimeClusterMetricsMap.get(cluster);
        // web第一次启动无暂存采集数据
        if (null == clusterMetrics) { return 0L;}

        Long lastTimeDocNum     = clusterMetrics.getDocUprushNum();
        Long currentTimeDocNum  = esClusterPhyStaticsService.getCurrentIndexTotal(cluster);
        return MetricsUtils.computerUprushNum(currentTimeDocNum.doubleValue(), lastTimeDocNum.doubleValue()).longValue();
    }

    /**
     * 计算索引写入文档突增量
     * @param cluster  集群名称
     * @return {@link Long}
     */
    private Long getReqUprushNum(String cluster) {
        ClusterMetrics clusterMetrics = cluster2LastTimeClusterMetricsMap.get(cluster);
        // web第一次启动无暂存采集数据
        if (null == clusterMetrics) { return 0L;}

        Long lastTimeQueryTotal = clusterMetrics.getReqUprushNum();
        Long currentQueryTotal  = esClusterPhyStaticsService.getCurrentQueryTotal(cluster);

        return MetricsUtils.computerUprushNum(currentQueryTotal.doubleValue(), lastTimeQueryTotal.doubleValue()).longValue();
    }
}
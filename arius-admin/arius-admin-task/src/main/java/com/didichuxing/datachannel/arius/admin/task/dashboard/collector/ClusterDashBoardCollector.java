package com.didichuxing.datachannel.arius.admin.task.dashboard.collector;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.DashBoardMetricThresholdDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.ClusterMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.DashBoardStats;
import com.didichuxing.datachannel.arius.admin.common.util.AriusUnitUtil;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterPhyStatsService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.util.AriusUnitUtil.TIME;

/**
 * Created by linyunan on 3/11/22
 * dashboard单个集群维度采集器
 */
@Component
public class ClusterDashBoardCollector extends BaseDashboardCollector {
    private static final ILog                                                LOGGER                            = LogFactory
        .getLog(ClusterDashBoardCollector.class);
    @Autowired
    protected ESClusterPhyStatsService                                       esClusterPhyStatsService;
    @Autowired
    protected AriusConfigInfoService                                         ariusConfigInfoService;

    private static final Map<String/*集群名称*/, ClusterMetrics /*上一次采集到的集群数据*/> cluster2LastTimeClusterMetricsMap = Maps
        .newConcurrentMap();

    private static final long                                                FIVE_MINUTE                       = 5 * 60
                                                                                                                 * 1000;

    @Override
    public void collectSingleCluster(String cluster, long startTime) {
        DashBoardStats dashBoardStats = buildInitDashBoardStats(startTime);

        ClusterMetrics clusterMetrics = cluster2LastTimeClusterMetricsMap.getOrDefault(cluster, new ClusterMetrics());
        ESClusterStatsResponse clusterStats = esClusterService.syncGetClusterStats(cluster);
        clusterMetrics.setTimestamp(startTime);
        clusterMetrics.setCluster(cluster);
        // 1. 写入耗时
        //TODO 指标-最大值，各个节点当前写入耗时最大值
        clusterMetrics.setIndexingLatency(esClusterPhyStatsService.getClusterIndexingLatency(cluster));
        // 2. 查询耗时
        //TODO 指标-最大值，各节点当前查询耗时最大值
        clusterMetrics.setSearchLatency(esClusterPhyStatsService.getClusterSearchLatency(cluster));
        //TODO 指标-轻量级获取_cat/health?format=json
        //4. 集群shard总数
        clusterMetrics.setShardNum(clusterStats.getTotalShard());
        // 5. 写入请求数
        clusterMetrics.setIndexReqNum(esClusterPhyStatsService.getCurrentIndexTotal(cluster));
        // 6. 网关成功率、失败率
        Tuple<Double/*成功率*/, Double/*失败率*/> gatewaySuccessRateAndFailureRate = esClusterPhyStatsService
            .getGatewaySuccessRateAndFailureRate(cluster);
        clusterMetrics.setGatewaySucPer(gatewaySuccessRateAndFailureRate.getV1());
        clusterMetrics.setGatewayFailedPer(gatewaySuccessRateAndFailureRate.getV2());
        // 7. 集群Pending task数
        //TODO 指标-轻量级获取_cat/health?format=json
        clusterMetrics.setPendingTaskNum(clusterStats.getPendingTasks());
        // 8. 集群http连接数
        clusterMetrics.setHttpNum(esClusterPhyStatsService.getHttpConnectionTotal(cluster));
        //9. 查询请求数突增量 （上个时间间隔请求数的两倍）
        clusterMetrics.setReqUprushNum(getReqUprushNum(cluster));
        //10.写入文档数突增量（上个时间间隔的写文档数的两倍
        clusterMetrics.setDocUprushNum(getDocUprushNum(cluster));

        //11._cluster/stats 和_nodes/stats 消耗时间
        clusterMetrics.setClusterElapsedTime(esClusterPhyStatsService.getClusterStatusElapsedTime(cluster));
        clusterMetrics.setNodeElapsedTime(esClusterPhyStatsService.getNodeStatusElapsedTime(cluster));
        long collectorDelayed = getCollectorDelayed(cluster);
        long configCollectorDelayed = getConfigCollectorDelayed();
        //TODO 指标-_cluster/stats，加参数过滤
        //12.消耗时间是否大于5分钟,开始采集到结束采集的时间，指标看板的采集任务，当前时间到最近一次采集的时间
        clusterMetrics.setClusterElapsedTimeGte5Min(collectorDelayed > configCollectorDelayed);
        clusterMetrics.setCollectorDelayed(collectorDelayed);
        //13.集群下索引数量
        clusterMetrics.setIndexCount(clusterStats.getIndexCount());

        dashBoardStats.setCluster(clusterMetrics);
        monitorMetricsSender.sendDashboardStats(Lists.newArrayList(dashBoardStats));

        // 暂存当前集群指标信息 针对特殊场景，即集群不可用后, 当前的策略是会使用上一次采集到的数据
        cluster2LastTimeClusterMetricsMap.put(cluster, clusterMetrics);
    }

    /**
     * 获取采集延时
     * @param cluster 集群
     * @return
     */
    private long getCollectorDelayed(String cluster) {
        long currentTimeMillis = System.currentTimeMillis();
        //最近一个采集点的时间
        Long nearestPoint = esClusterPhyStatsService.getTimeDifferenceBetweenNearestPointAndNow(cluster);
        long collectorDelayed = currentTimeMillis - nearestPoint;
        return collectorDelayed;
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
        if (null == clusterMetrics) {
            return 0L;
        }

        Long lastTimeDocNum = clusterMetrics.getDocUprushNum();
        Long currentTimeDocNum = esClusterPhyStatsService.getCurrentIndexTotal(cluster);
        return MetricsUtils.computerUprushNum(currentTimeDocNum.doubleValue(), lastTimeDocNum.doubleValue())
            .longValue();
    }

    /**
     * 计算索引写入文档突增量
     * @param cluster  集群名称
     * @return {@link Long}
     */
    private Long getReqUprushNum(String cluster) {
        ClusterMetrics clusterMetrics = cluster2LastTimeClusterMetricsMap.get(cluster);
        // web第一次启动无暂存采集数据
        if (null == clusterMetrics) {
            return 0L;
        }

        Long lastTimeQueryTotal = clusterMetrics.getReqUprushNum();
        Long currentQueryTotal = esClusterPhyStatsService.getCurrentQueryTotal(cluster);

        return MetricsUtils.computerUprushNum(currentQueryTotal.doubleValue(), lastTimeQueryTotal.doubleValue())
            .longValue();
    }

    /**
     * 获取配置的采集延时
     * @return
     */
    private long getConfigCollectorDelayed() {
        return getConfigOrDefaultValue(DASHBOARD_CLUSTER_METRIC_COLLECTOR_DELAYED_THRESHOLD,DASHBOARD_CLUSTER_METRIC_COLLECTOR_DELAYED_DEFAULT_VALUE,TIME);
    }

    /**
     * 获取dashboard配置值
     * @param valueName    配置名称
     * @param defaultValue 默认值
     * @return
     */
    private long getConfigOrDefaultValue(String valueName,String defaultValue, String unitStyle){
        DashBoardMetricThresholdDTO configThreshold = null;
        try {
            String configValue = ariusConfigInfoService.stringSetting(ARIUS_DASHBOARD_THRESHOLD_GROUP, valueName, defaultValue);
            if (StringUtils.isNotBlank(configValue)) {
                configThreshold = JSONObject.parseObject(configValue, DashBoardMetricThresholdDTO.class);
            }
        } catch (Exception e) {
            configThreshold = JSONObject.parseObject(defaultValue, DashBoardMetricThresholdDTO.class);
        }
        return AriusUnitUtil.unitChange(configThreshold.getValue().longValue(),configThreshold.getUnit(),unitStyle);
    }
}
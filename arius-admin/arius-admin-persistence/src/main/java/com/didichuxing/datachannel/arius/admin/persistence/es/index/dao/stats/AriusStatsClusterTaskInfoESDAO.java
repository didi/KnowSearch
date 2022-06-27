package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.TopMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.ESClusterTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.ESConstant;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsConstant.*;

/**
 * @author didi
 * @date 2022-01-13 3:26 下午
 */
@Component
public class AriusStatsClusterTaskInfoESDAO extends BaseAriusStatsESDAO {

    public static final String RUNNING_TIME = "runningTime";
    private static final FutureUtil<Void> futureUtil = FutureUtil.init("AriusStatsClusterTaskInfoESDAO",  10,10,500);

    @PostConstruct
    public void init() {
        super.indexName = dataCentreUtil.getAriusStatsClusterTaskInfo();
        BaseAriusStatsESDAO.register(AriusStatsEnum.TASK_INFO, this);
    }

    public Map<String, Double> getTaskCostMinAvgAndPercentiles(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_CLUSTER_TASK_REAL_TIME_AVG_AND_PERCENT, cluster,
                NOW_2M, NOW_1M, RUNNING_TIME, RUNNING_TIME);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequestWithRouting(metadataClusterName, cluster, realIndex, TYPE, dsl,
                this::getAvgAndPercentilesFromESQueryResponse, 3);
    }

    public long getTaskCount(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_CLUSTER_TASK_COUNT, cluster,
                NOW_2M, NOW_1M);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);
        //todo response npe
        return gatewayClient.performRequestWithRouting(metadataClusterName, cluster, realIndex, TYPE, dsl,
                response -> Long.parseLong(response.getHits().getUnusedMap().getOrDefault(ESConstant.HITS_TOTAL, "0").toString()), 3);
    }

    /**
     * 获取最新时间间隔（NOW_1M ~ NOW_2M）集群各个节点的总任务耗时
     * @param cluster 集群名称
     * @return  map key:节点名称 value 节点任务耗时
     */
    public Map<String/*节点名称*/, Double/*值*/> getClusterNodesTaskTotalCost(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_CLUSTER_NODES_TASK_COST, cluster,
                NOW_2M, NOW_1M, clusterMaxNum);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequestWithRouting(metadataClusterName, cluster, realIndex, TYPE, dsl, s -> {

            Map<String/*节点名称*/, Double/*值*/> node2TotalTaskCostMap = Maps.newHashMap();
            if (null == s) { return node2TotalTaskCostMap;}
            if (null == s.getAggs()) { return node2TotalTaskCostMap;}
            if (null == s.getAggs().getEsAggrMap()) { return node2TotalTaskCostMap;}
            if (null == s.getAggs().getEsAggrMap().get(HIST)) { return node2TotalTaskCostMap;}
            if (CollectionUtils.isEmpty(s.getAggs().getEsAggrMap().get(HIST).getBucketList())) { return node2TotalTaskCostMap;}

            List<ESBucket> bucketList = s.getAggs().getEsAggrMap().get(HIST).getBucketList();
            for (ESBucket esBucket : bucketList) {
                // build key
                Map<String, Object> unusedMap = esBucket.getUnusedMap();
                if (MapUtils.isEmpty(unusedMap)) { continue;}

                String key = null == unusedMap.get(KEY) ? null : unusedMap.get(KEY).toString();
                if (null == key) { continue;}

                // build value
                Map<String, ESAggr> aggrMap = esBucket.getAggrMap();
                if (MapUtils.isNotEmpty(aggrMap) && null != aggrMap.get(HIST)
                            && null != aggrMap.get(HIST).getUnusedMap().get(VALUE)) {
                    Double value = Double.valueOf(aggrMap.get(HIST).getUnusedMap().get(VALUE).toString());
                    node2TotalTaskCostMap.put(key, value);
                    continue;
                }

                node2TotalTaskCostMap.put(key, 0d);
            }

            return node2TotalTaskCostMap;}, 3);
    }

    /**
     *  获取最新时间分片中指标数值前TopN的节点名称
     *  如果延迟后的最新时间分片的指标值为null，最新时间迭代 - 1, 直到不为空, 迭代上限为3次。
     *
     * @param clusterPhyName   集群名称
     * @param metricsTypes     指标类型
     * @param topNu            topN
     * @param aggTypes          聚合类型
     * @param esNodesMaxNum    聚合节点数量最大值（agg bucket number）
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @return
     */
    private List<TopMetrics> getTopNNodeMetricsInfo(String clusterPhyName, List<String> metricsTypes, Integer topNu,
                                                    List<String> aggTypes, int esNodesMaxNum, Long startTime, Long endTime) {

        int retryTime = 0;
        List<VariousLineChartMetrics> variousLineChartMetrics = new ArrayList<>();
        do {
            Long timePoint = getHasDataTime(clusterPhyName, startTime, endTime, DslsConstant.GET_HAS_CLUSTER_NODE_TASK_DATA_TIME);
            //没有数据则提前终止
            if (null == timePoint) {
                break;
            }

            Tuple<Long, Long> firstInterval = MetricsUtils.getSortInterval(endTime - startTime, timePoint);
            long startTimeForOneInterval    = firstInterval.getV1();
            long endTimeForOneInterval      = firstInterval.getV2();

            String interval = MetricsUtils.getInterval(endTime - startTime);

            String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_AGG_CLUSTER_NODE_TASK_INFO
                    , clusterPhyName, startTimeForOneInterval, endTimeForOneInterval, esNodesMaxNum,
                    interval, buildAggDSL(metricsTypes, aggTypes));

            String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTimeForOneInterval,
                    endTimeForOneInterval);

            variousLineChartMetrics = gatewayClient.performRequestWithRouting(metadataClusterName, clusterPhyName,
                    realIndexName, TYPE, dsl, s -> fetchMultipleAggMetrics(s, null, metricsTypes, topNu), 3);
        }while (retryTime++ > 3 && CollectionUtils.isEmpty(variousLineChartMetrics));

        return variousLineChartMetrics.stream().map(this::buildTopMetrics).collect(Collectors.toList());
    }

    public String buildAggDSL(List<String> metrics, List<String> aggTypes) {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < metrics.size(); i++) {
            JSONObject sub =  new JSONObject();
            jsonObject.put(metrics.get(i), sub);
            JSONObject subSub = new JSONObject();
            sub.put(aggTypes.get(i) == null ? DEFAULT_AGG : aggTypes.get(i), subSub);
            subSub.put(FIELD, METRICS + metrics.get(i));
        }
        return jsonObject.toString();
    }

    /**
     * 获取多个节点折线图指标信息
     *
     * @param clusterPhyName 集群名称
     * @param metricsTypes        指标类型
     * @param topNu          top
     * @param aggTypes        聚合类型
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @return List<VariousLineChartMetrics>
     */
    public List<VariousLineChartMetrics> getTopNNodeAggMetrics(String clusterPhyName, List<String> metricsTypes,
                                                               Integer topNu, List<String> aggTypes,
                                                               Long startTime, Long endTime) {
        List<VariousLineChartMetrics> buildMetrics = Lists.newCopyOnWriteArrayList();
        //获取TopN指标节点名称信息
        List<TopMetrics> topNIndexMetricsList = getTopNNodeMetricsInfo(clusterPhyName, metricsTypes, topNu, aggTypes,
                esNodesMaxNum, startTime, endTime);

        //构建多个指标TopN数据
        for (TopMetrics topMetrics : topNIndexMetricsList) {
            List<String> correspondenceAggType = Lists.newArrayList(aggTypes.get(metricsTypes.indexOf(topMetrics.getType())));
            futureUtil.runnableTask(() -> buildTopNSingleMetricsForNode(buildMetrics, clusterPhyName, correspondenceAggType,
                    esNodesMaxNum, startTime, endTime, topMetrics));
        }
        futureUtil.waitExecute();

        return buildMetrics;
    }

    private void buildTopNSingleMetricsForNode(List<VariousLineChartMetrics> buildMetrics,String clusterPhyName, List<String> aggTypes,
                                               int esNodesMaxNum, Long startTime, Long endTime,
                                               TopMetrics topMetrics) {
        String topNameStr = null;
        if (CollectionUtils.isNotEmpty(topMetrics.getTopNames())) {
            topNameStr = buildTopNameStr(topMetrics.getTopNames());
        }

        if (StringUtils.isBlank(topNameStr)) {
            return;
        }

        String interval = MetricsUtils.getInterval(endTime - startTime);
        List<String> metricsKeys = Lists.newArrayList(topMetrics.getType());

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_AGG_CLUSTER_TOP_NODE_TASK_INFO,
                 clusterPhyName, topNameStr, startTime, endTime, esNodesMaxNum,
                interval, startTime, endTime, buildAggDSL(metricsKeys, aggTypes));

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        List<VariousLineChartMetrics> variousLineChartMetrics = gatewayClient.performRequestWithRouting(metadataClusterName,
                clusterPhyName, realIndexName, TYPE, dsl, s -> fetchMultipleAggMetrics(s, null, metricsKeys, null), 3);
        buildMetrics.addAll(variousLineChartMetrics);
    }

    /**
     * 获取单个指标信息
     *
     * @param clusterPhyName    集群名称
     * @param metrics           指标类型
     * @param nodeName          节点名称
     * @param aggTypes           聚合类型
     * @param startTime         开始时间
     * @param endTime           结束时间
     * @return  List<VariousLineChartMetrics>
     */
    public List<VariousLineChartMetrics> getAggClusterPhySingleNodeMetrics(String clusterPhyName, List<String> metrics,
                                                                           String nodeName, List<String> aggTypes,
                                                                           long startTime, long endTime) {

        String interval = MetricsUtils.getInterval(endTime - startTime);

        String dsl = dslLoaderUtil.getFormatDslByFileName(
                DslsConstant.GET_AGG_CLUSTER_SINGLE_NODE_TASK_INFO,
                clusterPhyName, nodeName, startTime, endTime, interval, startTime, endTime, buildAggDSL(metrics, aggTypes));

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequestWithRouting(metadataClusterName, clusterPhyName, realIndexName, TYPE, dsl,
                s -> fetchSingleAggMetrics(s, metrics, nodeName), 3);
    }


    public List<ESClusterTaskDetail> getTaskDetailByNode(String cluster, String node, long startTime, long endTime) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_SINGLE_NODE_TASK_DETAIL_INFO, cluster,
                node, startTime, endTime);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequestWithRouting(metadataClusterName, cluster, realIndex, TYPE, dsl,
                this::buildTaskDetailInfo, 3);
    }
}
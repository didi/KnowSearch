package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.DashboardTopMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.list.MetricList;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.list.MetricListContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.ClusterPhyHealthMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricOtherTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricTopTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.OneLevelTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.logi.elasticsearch.client.response.query.query.hits.ESHit;
import com.didiglobal.logi.elasticsearch.client.response.query.query.hits.ESHits;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsConstant.FIELD;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricTopTypeEnum.listNoNegativeMetricTypes;
import static com.didichuxing.datachannel.arius.admin.common.constant.routing.ESRoutingConstant.CLUSTER_PHY_HEALTH_ROUTING;

@Component
public class AriusStatsDashBoardInfoESDAO extends BaseAriusStatsESDAO {
    private static final FutureUtil<Void> FUTURE_UTIL = FutureUtil.init("AriusStatsDashBoardInfoESDAO", 10, 10, 500);
    public static final String            GTE         = "gte";
    public static final String            CLUSTER     = "cluster";
    public static final String            EMPTY_STR   = "";

    @PostConstruct
    public void init() {
        super.indexName = dataCentreUtil.getAriusStatsDashBoardInfo();
        BaseAriusStatsESDAO.register(AriusStatsEnum.DASHBOARD_INFO, this);
    }

    /**
     * 获取dashboard大盘list列表类异常指标信息 针对dashboard_status 中 的flag字段
     * @see                  DashBoardMetricListTypeEnum
     * @param oneLevelType  一级指标 支持cluster node index template thread
     * @param metricsType   二级指标
     * @param sources       _source内容
     * @param flag          是否为异常列表 true 是， false 否
     * @param sortType      排序类型   asc decs
     * @return              List<MetricList>
     */
    public MetricList fetchListFlagMetric(String oneLevelType, String metricsType,String  sortItem,String valueMetric, List<String> sources, String flag,
                                          String sortType) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.FETCH_LIST_FLAG_METRIC, oneLevelType, sortItem,sortType,
                JSON.toJSONString(sources), oneLevelType, metricsType, flag, oneLevelType, NOW_6M, NOW_1M);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(metadataClusterName, realIndex, TYPE, dsl,
            s -> fetchRespMetrics(s, oneLevelType, metricsType, /*是否需要设置指标具体值*/true,valueMetric), 3);
    }


    /**
     * 获取dashboard大盘list列表指标信息
     * @see                  DashBoardMetricListTypeEnum
     * @param oneLevelType  一级指标 支持cluster node index template thread
     * @param metricsType   二级指标
     * @param aggType       聚合类型
     * @param sortType      排序类型   asc decs
     * @return              List<MetricList>
     */
    public MetricList fetchListValueMetrics(String oneLevelType, String metricsType, String aggType, String sortType) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.FETCH_LIST_VALUE_METRIC, oneLevelType,
            oneLevelType, oneLevelType, oneLevelType, metricsType, oneLevelType, NOW_6M, NOW_1M, oneLevelType,
            metricsType, oneLevelType, metricsType, sortType);

        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);
        return gatewayClient.performRequest(metadataClusterName, realIndex, TYPE, dsl,
            s -> fetchRespMetrics(s, oneLevelType, metricsType, /*是否需要设置指标具体值*/true,metricsType), 3);
    }

    /**
     * 处理返回结果
     *
     * @param s              返回结果
     * @param oneLevelType   一级指标项
     * @param metricsType    二级指标项
     * @param hasGetValue    是否需要设置指标项值
     * @param valueMetric    值的字段
     * @return               MetricList
     */
    private MetricList fetchRespMetrics(ESQueryResponse s, String oneLevelType, String metricsType,
                                        boolean hasGetValue,String valueMetric) {
        MetricList metricList = new MetricList();
        metricList.setType(metricsType);
        List<MetricListContent> metricListContents = Lists.newArrayList();
        // 去重列表
        List<String> repeatList = Lists.newArrayList();
        ESHits hits = s.getHits();
        if (null != hits && CollectionUtils.isNotEmpty(hits.getHits())) {
            for (ESHit hit : hits.getHits()) {
                if (null != hit.getSource() && null != ((JSONObject) hit.getSource()).getJSONObject(oneLevelType)
                    && null != ((JSONObject) hit.getSource()).getJSONObject(oneLevelType).getString(CLUSTER)
                    && null != ((JSONObject) hit.getSource()).getJSONObject(oneLevelType).getString(oneLevelType)){

                    JSONObject healthMetricsJb = ((JSONObject) hit.getSource()).getJSONObject(oneLevelType);
                    String cluster = healthMetricsJb.getString(CLUSTER);
                    String metricsTypeValue/*node template index thread-pool*/ = healthMetricsJb
                        .getString(oneLevelType);
                    if (AriusObjUtils.isBlank(metricsTypeValue)) {
                        continue;
                    }

                    // 去重处理
                    String repeatKey = cluster + "@" + metricsTypeValue;
                    if (repeatList.contains(repeatKey)) {
                        continue;
                    } else {
                        repeatList.add(repeatKey);
                    }

                    MetricListContent metricListContent = new MetricListContent();
                    metricListContent.setClusterPhyName(cluster);
                    metricListContent.setName(metricsTypeValue);

                    // 是否需要指标具体值
                    if (hasGetValue
                            && StringUtils.isNotBlank(valueMetric)
                            && null != ((JSONObject) hit.getSource()).getJSONObject(oneLevelType).getString(valueMetric)) {
                        Double value = healthMetricsJb.getDouble(valueMetric);
                        value = Double.valueOf(String.format("%.2f", value));
                        metricListContent.setValue(value);
                    }
                    metricListContents.add(metricListContent);
                }
            }
        }

        metricList.setMetricListContents(metricListContents);
        metricList.setCurrentTime(System.currentTimeMillis());
        return metricList;
    }
    
    
     /**
     * 处理阈值指标返回结果
     *
     * @param s              返回结果
     * @param oneLevelType   一级指标项
     * @param metricsType    二级指标项
     * @param valueName    值名称
     * @return               MetricList
     */
    private MetricList fetchRespThresholdsMetrics(ESQueryResponse s, String oneLevelType, String metricsType,
                                        String valueName) {
        MetricList metricList = new MetricList();
        metricList.setType(metricsType);
        List<MetricListContent> metricListContents = Lists.newArrayList();
        // 去重列表
        List<String> repeatList = Lists.newArrayList();
        ESHits hits = s.getHits();
        if (null != hits && CollectionUtils.isNotEmpty(hits.getHits())) {
            for (ESHit hit : hits.getHits()) {
                if (null != hit.getSource() && null != ((JSONObject) hit.getSource()).getJSONObject(oneLevelType)
                    && null != ((JSONObject) hit.getSource()).getJSONObject(oneLevelType).getString(CLUSTER)
                    && null != ((JSONObject) hit.getSource()).getJSONObject(oneLevelType).getString(oneLevelType)) {
                
                    JSONObject healthMetricsJb = ((JSONObject) hit.getSource()).getJSONObject(oneLevelType);
                    String cluster = healthMetricsJb.getString(CLUSTER);
                    String metricsTypeValue/*node template index thread-pool*/ = healthMetricsJb.getString(
                            oneLevelType);
                    if (AriusObjUtils.isBlank(metricsTypeValue)) {
                        continue;
                    }
                
                    // 去重处理
                    String repeatKey = cluster + "@" + metricsTypeValue;
                    if (repeatList.contains(repeatKey)) {
                        continue;
                    } else {
                        repeatList.add(repeatKey);
                    }
                
                    MetricListContent metricListContent = new MetricListContent();
                    metricListContent.setClusterPhyName(cluster);
                    metricListContent.setName(metricsTypeValue);
                
                    // 是否需要指标具体值
                    Double value = 0D;
                    if (healthMetricsJb.containsKey(valueName)) {
                        value = healthMetricsJb.getDouble(valueName);
                    }
                    value = Double.valueOf(String.format("%.2f", value));
                    metricListContent.setValue(value);
                
                    metricListContents.add(metricListContent);
                }
            }
        }
    
        metricList.setMetricListContents(metricListContents);
        metricList.setCurrentTime(System.currentTimeMillis());
        return metricList;
    }

    /**
     * 获取dashboard大盘TopN指标信息
     * @see   DashBoardMetricTopTypeEnum
     * @param oneLevelType        目前仅支持 cluster node template index
     * @param metricsTypes        指标类型列表
     * @param topNu               5 10 15 20
     * @param aggType             单个间隔内聚合类型
     * @param startTime           开始时间
     * @param endTime             结束时间
     * @return
     */
    public List<VariousLineChartMetrics> fetchTopMetric(String oneLevelType, List<String> metricsTypes, Integer topNu,
                                                        String aggType, Long startTime, Long endTime) {
        List<VariousLineChartMetrics> buildMetrics = Lists.newCopyOnWriteArrayList();
        //获取TopN指标节点/模板/索引/等名称信息
        List<DashboardTopMetrics> dashboardTopMetricsList = getTopMetricsForDashboard(oneLevelType, metricsTypes, topNu,
            aggType, esNodesMaxNum, startTime, endTime);

        //构建多个指标TopN数据
        for (DashboardTopMetrics dashboardTopMetrics : dashboardTopMetricsList) {
            FUTURE_UTIL.runnableTask(() -> buildTopNSingleMetrics(buildMetrics, oneLevelType, aggType, clusterMaxNum,
                startTime, endTime, dashboardTopMetrics));
        }
        FUTURE_UTIL.waitExecute();

        return buildMetrics;
    }

    /**
     * 获取dashboard大盘健康状态信息
     * @see        DashBoardMetricOtherTypeEnum
     * @return     ClusterPhyHealthMetrics
     */
    public ClusterPhyHealthMetrics fetchClusterHealthInfo() {
        String key = DashBoardMetricOtherTypeEnum.CLUSTER_HEALTH.getType();
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.FETCH_CLUSTER_HEALTH_INFO, key, key);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequestWithRouting(metadataClusterName, CLUSTER_PHY_HEALTH_ROUTING, realIndex, TYPE,
            dsl, s -> {
                ClusterPhyHealthMetrics clusterPhyHealthMetrics = new ClusterPhyHealthMetrics();
                if (null == s) {
                    LOGGER.warn(
                        "class=AriusStatsDashBoardInfoESDAO||method=fetchClusterHealthInfo||msg=response is null");
                    return clusterPhyHealthMetrics;
                }

                try {
                    ESHits hits = s.getHits();
                    if (null != hits && CollectionUtils.isNotEmpty(hits.getHits())) {
                        for (ESHit hit : hits.getHits()) {
                            if (null != hit.getSource()) {
                                JSONObject source = (JSONObject) hit.getSource();
                                JSONObject healthMetricsJb = source.getJSONObject(key);
                                return null == healthMetricsJb ? clusterPhyHealthMetrics
                                    : ConvertUtil.obj2ObjByJSON(healthMetricsJb, ClusterPhyHealthMetrics.class);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(
                        "class=AriusStatsDashBoardInfoESDAO||method=fetchClusterHealthInfo||sumKey={}||value={}||response={}",
                        e);
                }
                return clusterPhyHealthMetrics;
            }, 3);
    }

    /****************************************************private*************************************************************/
    /**
     *  获取最新时间分片中指标数值前TopN的节点名称
     *  如果延迟后的最新时间分片的指标值为null，最新时间迭代 - 1, 直到不为空, 迭代上限为3次。
     *
     *
     * @param oneLevelType     目前仅支持 cluster node template index
     * @param metricsTypes     指标类型
     * @param topNu            topN
     * @param aggType          聚合类型
     * @param dashboardClusterMaxNum    聚合节点数量最大值（agg bucket number）
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @return
     */
    private List<DashboardTopMetrics> getTopMetricsForDashboard(String oneLevelType, List<String> metricsTypes,
                                                                Integer topNu, String aggType,
                                                                int dashboardClusterMaxNum, Long startTime,
                                                                Long endTime) {
        // 获取有数据的第一个时间点
        Long timePoint = getDashboardHasDataTime(oneLevelType, startTime, endTime);
        // 查询剪支
        if (null == timePoint) {
            return Lists.newArrayList();
        }

        Tuple<Long, Long> firstInterval = MetricsUtils.getSortInterval(endTime - startTime, timePoint);
        long startInterval = firstInterval.getV1();
        long endInterval = firstInterval.getV2();

        // 注意这里不用MetricsUtils.getIntervalForDashBoard
        String interval = MetricsUtils.getInterval(endTime - startTime);
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startInterval, endInterval);

        Tuple<List<String>/*普通指标列表*/, List<String>/*指标值为非负类型指标列表*/> commonAndNoNegativeMetricsTuple = getCommonMetricsAndNoNegativeMetrics(
            metricsTypes);
        List<String> commonMetricTypeList = commonAndNoNegativeMetricsTuple.getV1();
        List<String> noNegativeMetricsList = commonAndNoNegativeMetricsTuple.getV2();
        List<DashboardTopMetrics> finalTopMetrics = Lists.newArrayList();

        // 处理普通指标
        if (CollectionUtils.isNotEmpty(commonMetricTypeList)) {
            String aggsDsl = dynamicBuildDashboardAggsDSLForTop(oneLevelType, commonMetricTypeList, aggType);
            String dsl = getFinalDslByOneLevelType(oneLevelType, startInterval, endInterval, dashboardClusterMaxNum,
                interval, aggsDsl);
            if (null == dsl) {
                return finalTopMetrics;
            }

            List<VariousLineChartMetrics> variousLineChartMetrics = gatewayClient.performRequest(metadataClusterName,
                realIndexName, TYPE, dsl, s -> fetchMultipleAggMetrics(s, oneLevelType, commonMetricTypeList, topNu),
                3);

            variousLineChartMetrics.stream().map(this::buildDashboardTopMetrics).forEach(finalTopMetrics::add);
        }

        // 处理非负类型指标
        if (CollectionUtils.isNotEmpty(noNegativeMetricsList)) {
            // 对非非负类型指标指标进行agg_filter模式过滤
            String aggsDsl = dynamicBuildDashboardNoNegativeAggsDSLForTop(oneLevelType, noNegativeMetricsList, aggType);
            String dsl = getFinalDslByOneLevelType(oneLevelType, startInterval, endInterval, dashboardClusterMaxNum,
                interval, aggsDsl);
            if (null == dsl) {
                return finalTopMetrics;
            }

            List<VariousLineChartMetrics> variousLineChartMetrics = gatewayClient.performRequest(metadataClusterName,
                realIndexName, TYPE, dsl,
                s -> fetchMultipleNoNegativeAggMetrics(s, oneLevelType, noNegativeMetricsList, topNu), 3);

            variousLineChartMetrics.stream().map(this::buildDashboardTopMetrics).forEach(finalTopMetrics::add);
        }
        return finalTopMetrics;
    }

    DashboardTopMetrics buildDashboardTopMetrics(VariousLineChartMetrics variousLineChartMetrics) {
        DashboardTopMetrics dashboardTopMetrics = new DashboardTopMetrics();
        dashboardTopMetrics.setType(variousLineChartMetrics.getType());
        List<Tuple<String/*集群名称*/, String/*索引名称/节点名称/集群名称/模板名称*/>> dashboardTopInfo = Lists.newArrayList();
        for (MetricsContent metricsContent : variousLineChartMetrics.getMetricsContents()) {
            Tuple<String/*集群名称*/, String/*索引名称/节点名称/集群名称/模板名称*/> cluster2NameTuple = new Tuple<>();
            String cluster = metricsContent.getCluster();
            // 兼容老版本, 上游查询补丁
            if (!AriusObjUtils.isBlank(cluster)) {
                cluster2NameTuple.setV1(cluster);
            } else {
                cluster2NameTuple.setV1(metricsContent.getName());
            }

            cluster2NameTuple.setV2(metricsContent.getName());
            dashboardTopInfo.add(cluster2NameTuple);
        }

        dashboardTopMetrics.setDashboardTopInfo(dashboardTopInfo);
        return dashboardTopMetrics;
    }

    /**
     * 由于非负型指标的结果不能再<em>query</em>条件进行<em>filter</em>过滤，问题是会导致召回结果数量不正常，
     * 这里使用<em>agg</em>的<em>filter</em>模式进行构建，同时可以拉去更多的召回数据，若后续对结果进行处理时，
     * 可参照{#fetchMultipleNoNegativeAggMetrics(com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse, java.lang.String, java.util.List, java.lang.Integer)(ESQueryResponse, List, Integer)}
     * 的方法进行结果处理
     * agg:
     *             "gatewaySucPer": {
     *               "filter": {
     *                 "range": {
     *                   "cluster.gatewaySucPer": {
     *                     "gte": 0
     *                   }
     *                 }
     *               },
     *               "aggs": {
     *                 "gatewaySucPer": {
     *                   "avg": {
     *                     "field": "cluster.gatewaySucPer",
     *                     "missing": 0
     *                   }
     *                 }
     *               }
     *             }
     * @param oneLevelType
     * @param noNegativeMetricsList
     * @param aggType 只支持：avg、sum、avg等统计类型指标
     * @return
     */
    private String dynamicBuildDashboardNoNegativeAggsDSLForTop(String oneLevelType, List<String> noNegativeMetricsList,
                                                                String aggType) {
        List<String> dslList = Lists.newArrayList();
        for (String field : noNegativeMetricsList) {
            final String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_AGG_FILTER_FRAGMENT, field,
                oneLevelType, field, GTE, 0, field, aggType, oneLevelType, field);
            dslList.add(dsl);

        }
        return String.join(",", dslList);
    }

    /**
     * 获取普通指标列表 非负类型指标列表(@link CLUSTER_GATEWAY_FAILED_PER CLUSTER_GATEWAY_SUC_PER)
     *
     * @param metricsTypes 二级指标类型
     * @return Tuple
     */
    private Tuple<List<String>/*普通指标列表*/, List<String>/*指标值为非负类型指标列表*/> getCommonMetricsAndNoNegativeMetrics(List<String> metricsTypes) {
        Tuple<List<String>, List<String>> noNegativeMetricsAndCommonMetricsTuple = new Tuple<>();
        List<String> commonMetricTypeList = Lists.newArrayList();
        List<String> noNegativeMetricTypeList = Lists.newArrayList();
        // 处理非负值的指标类型
        for (String metricsType : metricsTypes) {
            if (listNoNegativeMetricTypes().contains(metricsType)) {
                noNegativeMetricTypeList.add(metricsType);
            } else {
                commonMetricTypeList.add(metricsType);
            }
        }

        noNegativeMetricsAndCommonMetricsTuple.setV1(commonMetricTypeList);
        noNegativeMetricsAndCommonMetricsTuple.setV2(noNegativeMetricTypeList);
        return noNegativeMetricsAndCommonMetricsTuple;
    }

    /**
     * 根据一级指标类型构建最终查询dsl全文
     * @param oneLevelType             一级指标类型
     * @param startInterval            起始时刻
     * @param endInterval              结束时刻
     * @param dashboardClusterMaxNum   支持的聚合集群总量，平台集群量超出此数查询会异常
     * @param interval                 agg聚合间隔类型 如1m, 5m 10m 等等
     * @param aggsDsl                  动态构建的agg聚合子句
     * @return  dsl全文
     */
    private String getFinalDslByOneLevelType(String oneLevelType, long startInterval, long endInterval,
                                             int dashboardClusterMaxNum, String interval, String aggsDsl) {
        // 获取 clusterThreadPoolQueue维度 相关全文dsl
        if (OneLevelTypeEnum.CLUSTER_THREAD_POOL_QUEUE.getType().equals(oneLevelType)) {
            return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_AGG_DASHBOARD_CLUSTER_TOP_NAME_INFO,
                oneLevelType, startInterval, endInterval, oneLevelType, CLUSTER, dashboardClusterMaxNum, oneLevelType,
                interval, aggsDsl);
        }

        // 获取 cluster维度 相关全文dsl
        if (OneLevelTypeEnum.CLUSTER.getType().equals(oneLevelType)) {
            return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_AGG_DASHBOARD_CLUSTER_TOP_NAME_INFO,
                oneLevelType, startInterval, endInterval, oneLevelType, oneLevelType, dashboardClusterMaxNum,
                oneLevelType, interval, aggsDsl);
        }

        // 获取 非cluster维度 相关全文dsl
        if (OneLevelTypeEnum.listNoClusterOneLevelType().contains(oneLevelType)) {
            return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_AGG_DASHBOARD_NO_CLUSTER_TOP_NAME_INFO,
                oneLevelType, startInterval, endInterval, oneLevelType, oneLevelType, oneLevelType,
                dashboardClusterMaxNum, oneLevelType, interval, aggsDsl);
        }
        return null;
    }

    /**
     *
     * @param oneLevelType     一级指标类型
     * @param topClustersStr   top集群名称
     * @param topNameStr       top名称 （索引名称、集群名称、节点名称、模板名称）
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @param dashboardClusterMaxNum   统计集群最大数量
     * @param interval                 时间间隔
     * @param aggsDsl                  聚合指标项
     * @return
     */
    private String getFinalDslByOneLevelType(String oneLevelType, String topClustersStr, String topNameStr,
                                             Long startTime, Long endTime, int dashboardClusterMaxNum, String interval,
                                             String aggsDsl, String noNegativeStr) {

        // 处理特殊类型dsl（如 clusterThreadPoolQueue）
        if (OneLevelTypeEnum.CLUSTER_THREAD_POOL_QUEUE.getType().equals(oneLevelType)) {
            return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOP_DASHBOARD_CLUSTER_AGG_METRICS_INFO,
                noNegativeStr, oneLevelType, CLUSTER, topNameStr, oneLevelType, startTime, endTime, oneLevelType,
                CLUSTER, dashboardClusterMaxNum, oneLevelType, interval, startTime, endTime, aggsDsl);
        }

        // 获取 cluster维度 相关全文dsl
        if (OneLevelTypeEnum.CLUSTER.getType().equals(oneLevelType)) {
            return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOP_DASHBOARD_CLUSTER_AGG_METRICS_INFO,
                noNegativeStr, oneLevelType, oneLevelType, topNameStr, oneLevelType, startTime, endTime, oneLevelType,
                oneLevelType, dashboardClusterMaxNum, oneLevelType, interval, startTime, endTime, aggsDsl);
        }

        // 获取 非cluster维度 相关全文dsl
        if (OneLevelTypeEnum.listNoClusterOneLevelType().contains(oneLevelType)) {
            return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOP_DASHBOARD_NO_CLUSTER_AGG_METRICS_INFO,
                noNegativeStr, oneLevelType, topClustersStr, oneLevelType, oneLevelType, topNameStr, oneLevelType,
                startTime, endTime, oneLevelType, oneLevelType, oneLevelType, dashboardClusterMaxNum, oneLevelType,
                interval, startTime, endTime, aggsDsl);
        }
        return null;
    }

    /**
     * 构建单个TopN中的全量数据信息
     * @param buildMetrics              需要构建的元数据
     * @param oneLevelType              一级指标
     * @param aggType                   聚合类型
     * @param dashboardClusterMaxNum    dashboard允许最大的集群数
     * @param startTime                 开始时间
     * @param endTime                   结束时间
     * @param dashboardTopMetrics       查询信息，需要取出的topN数据信息（包括topN的集群/模板/索引/等名称）
     */
    private void buildTopNSingleMetrics(List<VariousLineChartMetrics> buildMetrics, String oneLevelType, String aggType,
                                        int dashboardClusterMaxNum, Long startTime, Long endTime,
                                        DashboardTopMetrics dashboardTopMetrics) {
        List<Tuple<String, String>> dashboardTopInfo = dashboardTopMetrics.getDashboardTopInfo();
        // 获取最终topN集群列表
        List<String> topDistinctClusters = dashboardTopInfo.stream().map(Tuple::getV1).filter(Objects::nonNull)
            .distinct().collect(Collectors.toList());
        String topClustersStr = CollectionUtils.isNotEmpty(topDistinctClusters) ? buildTopNameStr(topDistinctClusters)
            : null;
        if (StringUtils.isBlank(topClustersStr)) {
            return;
        }

        // 获取最终topN节点/模板/索引列表
        List<String> topDistinctNames = dashboardTopInfo.stream().map(Tuple::getV2).filter(Objects::nonNull).distinct()
            .collect(Collectors.toList());
        String topNameStr = CollectionUtils.isNotEmpty(topDistinctNames) ? buildTopNameStr(topDistinctNames) : null;
        if (StringUtils.isBlank(topNameStr)) {
            return;
        }

        String interval = MetricsUtils.getIntervalForDashBoard(endTime - startTime);
        List<String> metricsKeys = Lists.newArrayList(dashboardTopMetrics.getType());
        String noNegativeStr = EMPTY_STR;
        if (listNoNegativeMetricTypes().contains(dashboardTopMetrics.getType())) {
            noNegativeStr = buildTermNoNegativeDsl(dashboardTopMetrics.getType(), oneLevelType);
        }
        String aggsDsl = dynamicBuildDashboardAggsDSLForTop(oneLevelType, metricsKeys, aggType);
        String dsl = getFinalDslByOneLevelType(oneLevelType, topClustersStr, topNameStr, startTime, endTime,
            dashboardClusterMaxNum, interval, aggsDsl, noNegativeStr);
        if (null == dsl) {
            return;
        }

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        List<VariousLineChartMetrics> variousLineChartMetrics = gatewayClient.performRequestWithRouting(
            metadataClusterName, null, realIndexName, TYPE, dsl,
            s -> fetchMultipleAggMetrics(s, oneLevelType, metricsKeys, null), 3);

        // 过滤出有效指标项，解决不同集群存在相同节点/模板/索引名称的场景
        filterValidMetricsInfo(variousLineChartMetrics, dashboardTopMetrics);

        buildMetrics.addAll(variousLineChartMetrics);
    }

    /**
     * { *     "range":{ *         “field”:{ * *             gte:0 *         } *     } * },
     *
     * @param type * @return {@link String}
     */
    private String buildTermNoNegativeDsl(String type, String oneLevelType) {
        Map range = new HashMap<String, Map<String, Object>>() {
            {
                put("range", new HashMap<String, Object>() {
                    {
                        put(String.format("%s.%s", oneLevelType, type), new HashMap<String, Object>() {
                            {
                                put("gte", 0);
                            }
                        });
                    }
                });
            }
        };
        return String.format("%s,", JSON.toJSONString(range));
    }

    /**
     * 过滤出有效指标项，解决不同集群存在相同节点/模板/索引名称的场景
     * @param variousLineChartMetrics    源数据信息
     * @param dashboardTopMetrics        根据类中属性过滤条件
     */
    private void filterValidMetricsInfo(List<VariousLineChartMetrics> variousLineChartMetrics,
                                        DashboardTopMetrics dashboardTopMetrics) {

        for (VariousLineChartMetrics variousLineChartMetric : variousLineChartMetrics) {
            if (!dashboardTopMetrics.getType().equals(variousLineChartMetric.getType())) {
                return;
            }

            List<MetricsContent> metricsContents = variousLineChartMetric.getMetricsContents();
            if (CollectionUtils.isEmpty(metricsContents)) {
                return;
            }

            List<Tuple<String, String>> dashboardTopInfo = dashboardTopMetrics.getDashboardTopInfo();
            List<String> validMetricInfo = Lists.newArrayList();
            for (Tuple<String, String> cluster2NameTuple : dashboardTopInfo) {
                String cluster = cluster2NameTuple.getV1();
                String name = cluster2NameTuple.getV2();
                validMetricInfo.add(CommonUtils.getUniqueKey(cluster, name));
            }

            List<MetricsContent> validMetricsContentList = Lists.newArrayList();
            for (MetricsContent metricsContent : metricsContents) {
                String cluster = metricsContent.getCluster();
                String name = metricsContent.getName();
                if (validMetricInfo.contains(CommonUtils.getUniqueKey(cluster, name))) {
                    validMetricsContentList.add(metricsContent);
                }
            }
            // 更新
            variousLineChartMetric.setMetricsContents(validMetricsContentList);
        }
    }

    /**
     * 获取dashboard 指标信息中有数据的第一个时间点
     * @param oneLevelType
     * @param startTime
     * @param endTime
     * @return
     */
    private Long getDashboardHasDataTime(String oneLevelType, long startTime, long endTime) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_HAS_DASHBOARD_METRICS_DATA_TIME,
            oneLevelType, oneLevelType, startTime, endTime, oneLevelType);

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(metadataClusterName, realIndexName, TYPE, dsl, s -> {
            ESHits hits = s.getHits();
            if (null != hits && CollectionUtils.isNotEmpty(hits.getHits())) {
                for (ESHit hit : hits.getHits()) {
                    if (null != hit.getSource()) {
                        JSONObject source = (JSONObject) hit.getSource();
                        JSONObject healthMetricsJb = source.getJSONObject(oneLevelType);
                        return null == healthMetricsJb ? null : healthMetricsJb.getLongValue("timestamp");
                    }
                }
            }
            return null;
        }, 3);
    }

    /**
     * 对于topN场景, 动态构建agg子句
     * "cluster.indexingLatency": {
     *               "max": {
     *                 "field": "cluster.indexingLatency"
     *               }
     *             },
     *             "cluster.searchLatency": {
     *               "max": {
     *                 "field": "cluster.searchLatency"
     *               }
     *             }
     *
     * @param oneLevelType 一级指标类型
     * @param metrics      二级指标列表
     * @param aggType      聚合类型
     * @return             StringText
     */
    private String dynamicBuildDashboardAggsDSLForTop(String oneLevelType, List<String> metrics, String aggType) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < metrics.size(); i++) {
            String metricName = metrics.get(i);
            Map<String, String> aggsSubSubCellMap = Maps.newHashMap();
            aggsSubSubCellMap.put(FIELD, oneLevelType + "." + metricName);

            buildAggsDslMap(aggType, sb, metricName, aggsSubSubCellMap);
            if (i != metrics.size() - 1) {
                sb.append(",").append("\n");
            }
        }

        return sb.toString();
    }
    
    public MetricList fetchListThresholdsMetric(String oneLevelType, String metricsType, String valueName, String aggType, String flag, String sortType) {
       
       String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.FETCH_LIST_THRESHOLDS_METRIC, oneLevelType, sortType,
               oneLevelType, oneLevelType, oneLevelType,oneLevelType,valueName, oneLevelType, metricsType, flag,
               oneLevelType,NOW_6M, NOW_1M);

        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);
        return gatewayClient.performRequest(metadataClusterName, realIndex, TYPE, dsl,
            s -> fetchRespThresholdsMetrics(s, oneLevelType, metricsType, /*是否需要设置指标具体值*/valueName), 3);
    }
    
    public MetricList fetchListThresholdsSegmentNumMetric(String oneLevelType, String metricsType, String valueName,
                                                          String aggType, String flag, String sortType) {
       
       String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.FETCH_LIST_THRESHOLDS_METRIC, oneLevelType, sortType,
               oneLevelType, oneLevelType, oneLevelType,oneLevelType,valueName, oneLevelType, metricsType, flag,
               oneLevelType,NOW_6M, NOW_1M);

        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);
        return gatewayClient.performRequest(metadataClusterName, realIndex, TYPE, dsl,
            s -> fetchRespThresholdsMetrics(s, oneLevelType, metricsType, /*是否需要设置指标具体值*/valueName), 3);
    }
}
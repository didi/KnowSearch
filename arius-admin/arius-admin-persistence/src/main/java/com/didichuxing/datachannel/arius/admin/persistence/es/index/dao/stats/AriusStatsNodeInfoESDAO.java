package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum.*;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESBucket;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.TopMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESNodeStats;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterLogicDiskUsedInfoPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class AriusStatsNodeInfoESDAO extends BaseAriusStatsESDAO {
    private static final String           NOW_2M            = "now-2m";
    private static final String           NOW_1M            = "now-1m";
    public static final String            INDEX_TOTAL       = "index_total";
    public static final String            INDEX_TOTAL_FIELD = "indices-indexing-index_total";
    public static final String            QUERY_TOTAL       = "query_total";
    public static final String            QUERY_TOTAL_FIELD = "indices-search-query_total";
    public static final String            OPEN_HTTP         = "open_http";
    public static final String            OPEN_HTTP_FIELD   = "http-current_open";
    private static final String           VALUE             = "value";
    private static final FutureUtil<Void> futureUtil = FutureUtil.init("AriusStatsNodeInfoESDAO",  10,10,500);
    
   
    
    @PostConstruct
    public void init() {
        super.indexName = dataCentreUtil.getAriusStatsNodeInfo();

        BaseAriusStatsESDAO.register(AriusStatsEnum.NODE_INFO, this);
    }

    /**
     * 根据集群名称，获取集群[now-2m, now-1m]总的接收的流量
     * @param cluster
     * @return
     */
    public Double getClusterRx(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_REAL_TIME_RX_TX_INFO, cluster,
                NOW_2M, NOW_1M, TRANS_RX_SIZE.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, "sum"), 3);
    }

    /**
     * 根据集群名称，获取集群[now-2m, now-1m]总的发送的流量
     * @param cluster
     * @return
     */
    public Double getClusterTx(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_REAL_TIME_RX_TX_INFO, cluster,
                NOW_2M, NOW_1M, TRANS_TX_SIZE.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, "sum"), 3);
    }

    /**
     * 根据集群名称，获取集群[now-2m, now-1m]  cpu平均使用率 (求每个节点cpu的平均值)
     * @param cluster
     * @return
     */
    public Double getClusterCpuAvg(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_REAL_TIME_CPU_AVG_INFO, cluster,
                NOW_2M, NOW_1M, CPU_USAGE_PERCENT.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, "avg"), 3);
    }

    /**
     * 获取集群实时cpu分位值(统计节点维度)和平均使用率
     *
     * @param cluster
     * @return
     */
    public Map<String, Double> getClusterCpuAvgAndPercentiles(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_CLUSTER_REAL_TIME_AVG_AND_PERCENT, cluster,
                NOW_2M, NOW_1M, CPU_USAGE_PERCENT.getType(), CPU_USAGE_PERCENT.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(metadataClusterName, realIndex, TYPE, dsl,
                this::getAvgAndPercentilesFromESQueryResponse, 3);
    }

    /**
     * 获取集群实时cpu load 1m分位值(统计节点维度)和平均使用率
     *
     * @param cluster
     * @return
     */
    public Map<String, Double> getClusterCpuLoad1MinAvgAndPercentiles(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_CLUSTER_REAL_TIME_AVG_AND_PERCENT, cluster,
                NOW_2M, NOW_1M, CPU_LOAD_AVERAGE_1M.getType(), CPU_LOAD_AVERAGE_1M.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(metadataClusterName, realIndex, TYPE, dsl,
                this::getAvgAndPercentilesFromESQueryResponse, 3);
    }

    /**
     * 获取集群实时cpu load 5m分位值(统计节点维度)和平均使用率
     *
     * @param cluster
     * @return
     */
    public Map<String, Double> getClusterCpuLoad5MinAvgAndPercentiles(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_CLUSTER_REAL_TIME_AVG_AND_PERCENT, cluster,
                NOW_2M, NOW_1M, CPU_LOAD_AVERAGE_5M.getType(), CPU_LOAD_AVERAGE_5M.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(metadataClusterName, realIndex, TYPE, dsl,
                this::getAvgAndPercentilesFromESQueryResponse, 3);
    }

    /**
     * 获取集群实时cpu load 15m分位值(统计节点维度)和平均使用率
     *
     * @param cluster
     * @return
     */
    public Map<String, Double> getClusterCpuLoad15MinAvgAndPercentiles(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_CLUSTER_REAL_TIME_AVG_AND_PERCENT, cluster,
                NOW_2M, NOW_1M, CPU_LOAD_AVERAGE_15M.getType(), CPU_LOAD_AVERAGE_15M.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(metadataClusterName, realIndex, TYPE, dsl,
            this::getAvgAndPercentilesFromESQueryResponse, 3);

    }

    /**
     * 获取集群写入耗时
     */
    public double getClusterIndexingLatency(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_INDEXING_LATENCY_MAX, cluster,
                NOW_2M, NOW_1M, INDICES_INDEXING_LATENCY.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, "sum"), 3);
    }

    /**
     * 获取集群查询耗时
     */
    public double getClusterSearchLatency(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_SEARCH_LATENCY_MAX, cluster,
                NOW_2M, NOW_1M, INDICES_QUERY_LATENCY.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, "sum"), 3);
    }

    /**
     * 获取集群写入耗时分位值(统计节点维度)和平均使用率
     */
    public Map<String, Double> getClusterIndexingLatencyAvgAndPercentiles(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_CLUSTER_REAL_TIME_AVG_AND_PERCENT, cluster,
                NOW_2M, NOW_1M, INDICES_INDEXING_LATENCY.getType(), INDICES_INDEXING_LATENCY.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequestWithRouting(metadataClusterName, cluster, realIndex, TYPE, dsl,
            this::getAvgAndPercentilesFromESQueryResponse, 3);
    }

    /**
     * 获取集群查询耗时分位值(统计节点维度)和平均使用率
     */
    public Map<String, Double> getClusterSearchLatencyAvgAndPercentiles(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_CLUSTER_REAL_TIME_AVG_AND_PERCENT, cluster,
                NOW_2M, NOW_1M, INDICES_QUERY_LATENCY.getType(), INDICES_QUERY_LATENCY.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(metadataClusterName, realIndex, TYPE, dsl,
            this::getAvgAndPercentilesFromESQueryResponse, 3);
    }

    /**
     * 获取集群磁盘空闲使用率分位值(统计节点维度)和平均使用率
     */
    public Map<String, Double> getClusterDiskFreeUsagePercentAvgAndPercentiles(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(
            DslsConstant.AGG_CLUSTER_AVG_AND_PERCENT_FOR_DISK_FREE_USAGE_PERCENT, cluster, NOW_2M, NOW_1M);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(metadataClusterName, realIndex, TYPE, dsl,
            this::getAvgAndPercentilesFromESQueryResponse, 3);
    }

    /**
     * 获取所有集群节点的物理存储空间大小
     * @return
     */
    public Double getAllClusterNodePhyStoreSize(final boolean bWithOutCeph) {
        String realIndexName = IndexNameUtils.genCurrentDailyIndexName(indexName);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_ALL_CLUSTER_NODE_PHY_STORE_SIZE,
            SCROLL_SIZE);

        final Set<String> nodeNameSet = new HashSet<>();
        final double[] totalPhySize = new double[1];
        final String fs_total_size_key = "fs-total-total_in_bytes";

        gatewayClient.queryWithScroll(realIndexName, TYPE, dsl, SCROLL_SIZE, null, ESNodeStats.class, resultList -> {
            if (CollectionUtils.isEmpty(resultList)) {
                return;
            }

            for (ESNodeStats stats : resultList) {
                if (null == stats.getMetrics() || StringUtils.isBlank(stats.getMetrics().get(fs_total_size_key))) {
                    continue;
                }

                String node = stats.getNode();
                String fsSize = stats.getMetrics().get(fs_total_size_key);

                if (bWithOutCeph && node.contains("ceph")) {
                    continue;
                }

                //由于物理机上可能存在节点混部，而混部的节点es在统计的时候获取的是物理机的整个磁盘空间，所以获取一个节点统计的信息即可
                if (!nodeNameSet.contains(node)) {
                    totalPhySize[0] += Double.valueOf(fsSize);
                    nodeNameSet.add(node);
                }
            }
        });

        return totalPhySize[0];
    }

    public List<VariousLineChartMetrics> getTopNNodeAggMetricsWithStep(String clusterPhyName, List<String> nodeNamesUnderClusterLogic,List<String> metricsTypes,
                                                               Integer topNu,String topMethod,Integer topTimeStep, String aggType,
                                                               Long startTime, Long endTime) {
        List<VariousLineChartMetrics> buildMetrics = Lists.newCopyOnWriteArrayList();
        //获取TopN指标节点名称信息
        List<TopMetrics> topNIndexMetricsList = getTopNNodeMetricsInfoWithStep(clusterPhyName,nodeNamesUnderClusterLogic, metricsTypes, topNu,topMethod,topTimeStep, aggType,
                esNodesMaxNum, startTime, endTime);
        //构建多个指标TopN数据
        for (TopMetrics topMetrics : topNIndexMetricsList) {
            futureUtil.runnableTask(() -> buildTopNSingleMetricsForNode(buildMetrics, clusterPhyName, aggType,
                    esNodesMaxNum, startTime, endTime, topMetrics));
        }
        futureUtil.waitExecute();

        return buildMetrics;
    }

    /**
     * 获取多个节点折线图指标信息
     *
     * @param clusterPhyName 集群名称
     * @param metricsTypes        指标类型
     * @param topNu          top
     * @param aggType        聚合类型
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @return List<VariousLineChartMetrics>
     */
    public List<VariousLineChartMetrics> getTopNNodeAggMetrics(String clusterPhyName, List<String> metricsTypes,
                                                               Integer topNu, String aggType,
                                                               Long startTime, Long endTime) {
        List<VariousLineChartMetrics> buildMetrics = Lists.newCopyOnWriteArrayList();
        //获取TopN指标节点名称信息
        List<TopMetrics> topNIndexMetricsList = getTopNNodeMetricsInfo(clusterPhyName, metricsTypes, topNu, aggType,
                esNodesMaxNum, startTime, endTime);

        //构建多个指标TopN数据
        for (TopMetrics topMetrics : topNIndexMetricsList) {
            futureUtil.runnableTask(() -> buildTopNSingleMetricsForNode(buildMetrics, clusterPhyName, aggType,
                esNodesMaxNum, startTime, endTime, topMetrics));
        }
        futureUtil.waitExecute();

        return buildMetrics;
    }
    
    private void buildTopNSingleMetricsForNode(List<VariousLineChartMetrics> buildMetrics,String clusterPhyName, String aggType,
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

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOPN_NODE_AGG_METRICS_INFO,
                 clusterPhyName, topNameStr, startTime, endTime,
                esNodesMaxNum, interval, startTime, endTime, buildAggsDSL(metricsKeys, aggType));

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        List<VariousLineChartMetrics> variousLineChartMetrics = gatewayClient.performRequestWithRouting(metadataClusterName,
                null, realIndexName, TYPE, dsl, s -> fetchMultipleAggMetrics(s, null, metricsKeys, null), 3);
        buildMetrics.addAll(variousLineChartMetrics);
    }

    /**
     *  获取最新时间分片中指标数值前TopN的节点名称
     *  如果延迟后的最新时间分片的指标值为null，最新时间迭代 - 1, 直到不为空, 迭代上限为3次。
     *
     * @param clusterPhyName   集群名称
     * @param metricsTypes     指标类型
     * @param topNu            topN
     * @param aggType          聚合类型
     * @param esNodesMaxNum    聚合节点数量最大值（agg bucket number）
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @return
     */
    private List<TopMetrics> getTopNNodeMetricsInfo(String clusterPhyName, List<String> metricsTypes, Integer topNu,
                                                    String aggType, int esNodesMaxNum, Long startTime, Long endTime) {

        int retryTime = 0;
        List<VariousLineChartMetrics> variousLineChartMetrics = new ArrayList<>();
        do {
            // 获取有数据的第一个时间点
            Long timePoint = getHasDataTime(clusterPhyName, startTime, endTime, DslsConstant.GET_HAS_NODE_METRICS_DATA_TIME);
            //没有数据则提前终止
            if (null == timePoint) { break;}

            Tuple<Long, Long> firstInterval = MetricsUtils.getSortInterval(endTime - startTime, timePoint);
            long startTimeForOneInterval    = firstInterval.getV1();
            long endTimeForOneInterval      = firstInterval.getV2();

            String interval = MetricsUtils.getInterval(endTime - startTime);

            String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_AGG_CLUSTER_PHY_NODES_INFO, clusterPhyName,
                    startTimeForOneInterval, endTimeForOneInterval, esNodesMaxNum, interval, buildAggsDSL(metricsTypes, aggType));

            String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTimeForOneInterval,
                    endTimeForOneInterval);

            variousLineChartMetrics = gatewayClient.performRequestWithRouting(metadataClusterName, null,
                    realIndexName, TYPE, dsl, s -> fetchMultipleAggMetrics(s, null, metricsTypes, topNu), 3);
        }while (retryTime++ > 3 && CollectionUtils.isEmpty(variousLineChartMetrics));

        return variousLineChartMetrics.stream().map(this::buildTopMetrics).collect(Collectors.toList());
    }

    /**
     *  获取最新时间分片中指标数值前TopN的节点名称
     *  如果延迟后的最新时间分片的指标值为null，最新时间迭代 - 1, 直到不为空, 迭代上限为3次。
     * todo:这块在获取最大值和平均值的时候，放到es去做
     * @param clusterPhyName   集群名称
     * @param metricsTypes     指标类型
     * @param topNu            topN
     * @param aggType          聚合类型
     * @param esNodesMaxNum    聚合节点数量最大值（agg bucket number）
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @return
     */
    private List<TopMetrics> getTopNNodeMetricsInfoWithStep(String clusterPhyName,List<String> nodeNamesUnderClusterLogic, List<String> metricsTypes, Integer topNu, String topMethod, Integer topTimeStep,
                                                            String aggType, int esNodesMaxNum, Long startTime, Long endTime) {

        int retryTime = 0;
        List<VariousLineChartMetrics> variousLineChartMetrics = new ArrayList<>();
        do {
            // 获取有数据的第一个时间点
            Long timePoint = getHasDataTime(clusterPhyName, startTime, endTime, DslsConstant.GET_HAS_NODE_METRICS_DATA_TIME);
            //没有数据则提前终止
            if (null == timePoint) {
                return new ArrayList<>();
            }

            long startTimeForOneInterval = timePoint - topTimeStep * 60 * 1000;
            long endTimeForOneInterval = timePoint;


            String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_AGG_CLUSTER_NODE_INFO_WITH_STEP, clusterPhyName,
                    startTimeForOneInterval, endTimeForOneInterval, esNodesMaxNum, STEP_INTERVAL,buildAggsDSL(metricsTypes, aggType), buildAggsDSLWithStep(metricsTypes, aggType));

            String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTimeForOneInterval,
                    endTimeForOneInterval);

            variousLineChartMetrics = gatewayClient.performRequestWithRouting(metadataClusterName, null,
                    realIndexName, TYPE, dsl, s -> fetchMultipleAggMetricsWithStep(s, null, metricsTypes, topNu, topMethod,nodeNamesUnderClusterLogic), 3);
        } while (retryTime++ > 3 && CollectionUtils.isEmpty(variousLineChartMetrics));
        return variousLineChartMetrics.stream().map(this::buildTopMetrics).collect(Collectors.toList());
    }

    /**
     * 获取单个指标信息
     *
     * @param clusterPhyName    集群名称
     * @param metrics           指标类型
     * @param nodeName          节点名称
     * @param aggType           聚合类型
     * @param startTime         开始时间
     * @param endTime           结束时间
     * @return  List<VariousLineChartMetrics>
     */
    public List<VariousLineChartMetrics> getAggClusterPhySingleNodeMetrics(String clusterPhyName, List<String> metrics,
                                                                           String nodeName, String aggType,
                                                                           long startTime, long endTime) {

        String interval = MetricsUtils.getInterval(endTime - startTime);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_AGG_CLUSTER_PHY_SINGLE_NODE_NODE,
            clusterPhyName, nodeName, startTime, endTime, interval, buildAggsDSL(metrics, aggType));

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequestWithRouting(metadataClusterName, nodeName, realIndexName, TYPE, dsl,
            s -> fetchSingleAggMetrics(s, metrics, nodeName), 3);
    }

    /**
     * 获取集群写文档总数
     * @param cluster 集群名称
     * @return {@link Long}
     */
    public Long getCurrentIndexTotal(String cluster) {
        return commonGetCurrentAggMetrics(cluster, METRICS, INDEX_TOTAL, INDEX_TOTAL_FIELD);
    }

    /**
     * 获取集群写文档总数
     * @param cluster 集群名称
     * @return {@link Long}
     */
    public Long getCurrentQueryTotal(String cluster) {
        return commonGetCurrentAggMetrics(cluster, METRICS, QUERY_TOTAL, QUERY_TOTAL_FIELD);
    }

    /**
     * 集群http连接数
     *
     * @param cluster 集群
     * @return {@code Long}
     */
    public Long getHttpConnectionTotal(String cluster) {
        return commonGetCurrentAggMetrics(cluster, METRICS, OPEN_HTTP, OPEN_HTTP_FIELD);
    }
    
    /**************************************** private methods ****************************************/
    /**
     *
     * @param cluster          集群名称
     * @param oneLevelMetrics  dsl中一级指标
     * @param metricsType1     dsl中二级指标项1字段
     * @param metricsType2     dsl中二级指标项2字段
     * @return
     */
    private Long commonGetCurrentAggMetrics(String cluster, String oneLevelMetrics, String metricsType1, String metricsType2) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(
                DslsConstant.GET_FIELD_SUM_AND_RANGE_FIELD_TOTAL, cluster, TIMESTAMP, NOW_2M, NOW_1M, metricsType1, SUM,
                String.format("%s%s", oneLevelMetrics, metricsType2));
        String realIndexName = getIndexNameByNowTimestamp(indexName);
       return gatewayClient.performRequest(cluster, realIndexName, TYPE,
           dsl,response->Optional.ofNullable(response)
               .map(ESQueryResponse::getAggs)
               .map(ESAggrMap::getEsAggrMap)
               .map(map -> map.get(metricsType1))
               .map(ESAggr::getUnusedMap)
               .map(map -> map.get(VALUE))
               .map(String::valueOf)
               .map(Double::parseDouble)
               .map(Double::longValue)
               .orElse(0L),
           3);
    }

    /**
     * 根据现在时间戳获取IndexName
     *
     * @param indexName 索引名称
     * @return {@code String}
     */
    private String getIndexNameByNowTimestamp(String indexName){
      return IndexNameUtils.genCurrentDailyIndexName(indexName);
    }

    private ClusterLogicDiskUsedInfoPO buildDiskInfoESQueryResponse(ESQueryResponse esQueryResponse) {
        ClusterLogicDiskUsedInfoPO clusterLogicDiskUsedInfoPO = new ClusterLogicDiskUsedInfoPO();
        if (esQueryResponse != null && esQueryResponse.getAggs() != null) {
            Map<String, ESAggr> esAggrMap = esQueryResponse.getAggs().getEsAggrMap();
            if (esAggrMap != null && esAggrMap.containsKey("hist")) {
                ESAggr minuteBucketESAggr = esAggrMap.get("hist");
                List<ESBucket> esBuckets = minuteBucketESAggr.getBucketList();
                if (esBuckets.size()!=0){
                    Map<String, ESAggr> aggrMap =  esBuckets.get(0).getAggrMap();
                    Double total =Double.valueOf(aggrMap.get("diskTotal").getUnusedMap().get(VALUE).toString());
                    Double free =Double.valueOf( aggrMap.get("diskFree").getUnusedMap().get(VALUE).toString());
                    Double used = total- free;
                    Double percent = used/total;
                    clusterLogicDiskUsedInfoPO.setDiskTotal(total.longValue());
                    clusterLogicDiskUsedInfoPO.setDiskUsage(used.longValue());
                    clusterLogicDiskUsedInfoPO.setDiskUsagePercent(percent.doubleValue());
                }
            }
        }
        return clusterLogicDiskUsedInfoPO;
    }
}
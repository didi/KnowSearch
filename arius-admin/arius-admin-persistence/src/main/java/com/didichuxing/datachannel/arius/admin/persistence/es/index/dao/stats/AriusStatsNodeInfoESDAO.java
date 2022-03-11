package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.TopMetrics;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESBucket;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESNodeStats;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.NodeRackStatisPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum.*;

@Component
public class AriusStatsNodeInfoESDAO extends BaseAriusStatsESDAO {

    private static final String     NOW_2M     = "now-2m";
    private static final String     NOW_1M     = "now-1m";
    private static final String     VALUE      = "value";
    private static final FutureUtil<Void> futureUtil = FutureUtil.initBySystemAvailableProcessors("AriusStatsNodeInfoESDAO",  500);

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

        return gatewayClient.performRequestWithRouting(metadataClusterName, cluster,realIndex, TYPE, dsl,
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

        return gatewayClient.performRequestWithRouting(metadataClusterName, cluster, realIndex, TYPE, dsl,
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

        return gatewayClient.performRequestWithRouting(metadataClusterName, cluster, realIndex, TYPE, dsl,
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

        return gatewayClient.performRequestWithRouting(metadataClusterName, cluster, realIndex, TYPE, dsl,
            this::getAvgAndPercentilesFromESQueryResponse, 3);

    }

    /**
     * 获取集群写入耗时
     */
    public double getClusterIndexingLatency(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_INDEXING_LATENCY_MAX, cluster,
                NOW_2M, NOW_1M, INDICES_INDEXING_CONSUME.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, "sum"), 3);
    }

    /**
     * 获取集群写入耗时分位值(统计节点维度)和平均使用率
     */
    public Map<String, Double> getClusterIndexingLatencyAvgAndPercentiles(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_CLUSTER_REAL_TIME_AVG_AND_PERCENT, cluster,
                NOW_2M, NOW_1M, INDICES_INDEXING_CONSUME.getType(), INDICES_INDEXING_CONSUME.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequestWithRouting(metadataClusterName, cluster, realIndex, TYPE, dsl,
            this::getAvgAndPercentilesFromESQueryResponse, 3);
    }

    /**
     * 获取集群查询耗时
     */
    public double getClusterSearchLatency(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_SEARCH_LATENCY_MAX, cluster,
                NOW_2M, NOW_1M, INDICES_QUERY_CONSUME.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, "sum"), 3);
    }

    /**
     * 获取集群查询耗时分位值(统计节点维度)和平均使用率
     */
    public Map<String, Double> getClusterSearchLatencyAvgAndPercentiles(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_CLUSTER_REAL_TIME_AVG_AND_PERCENT, cluster,
                NOW_2M, NOW_1M, INDICES_QUERY_CONSUME.getType(), INDICES_QUERY_CONSUME.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequestWithRouting(metadataClusterName, cluster, realIndex, TYPE, dsl,
            this::getAvgAndPercentilesFromESQueryResponse, 3);
    }

    /**
     * 获取集群磁盘空闲使用率分位值(统计节点维度)和平均使用率
     */
    public Map<String, Double> getClusterDiskFreeUsagePercentAvgAndPercentiles(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(
            DslsConstant.AGG_CLUSTER_AVG_AND_PERCENT_FOR_DISK_FREE_USAGE_PERCENT, cluster, NOW_2M, NOW_1M);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequestWithRouting(metadataClusterName, cluster, realIndex, TYPE, dsl,
            this::getAvgAndPercentilesFromESQueryResponse, 3);
    }

    /**
     * 根据集群和rack信息获取rack相关的统计信息
     * @param clusterName
     * @param rackList
     * @return
     */
    public List<NodeRackStatisPO> getRackStatis(String clusterName, List<String> rackList) {
        Map<String/*rackName*/, NodeRackStatisPO> nodeRackStatisMap = Maps.newHashMap();

        // 由于近15分钟存在跨天情况，需要获取最近2天对应索引名称
        String indexNames = genIndexNames(2);
        String rackFormat = CommonUtils.strConcat(rackList);

        int minuteSpan = 15;

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_RECENT_NODE_METRICS_BY_CLUSTER, clusterName,
            rackFormat, minuteSpan);

        ESQueryResponse esQueryResponse = gatewayClient.performRequest(indexNames, TYPE, dsl);
        handleESQueryResponse(clusterName, nodeRackStatisMap, esQueryResponse);

        List<NodeRackStatisPO> nodeRackStatisPOS = Lists.newLinkedList();
        handleRackList(clusterName, rackList, nodeRackStatisMap, nodeRackStatisPOS);

        return nodeRackStatisPOS;
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

    /**
     * 获取多个节点折线图指标信息
     *
     * @param clusterPhyName 集群名称
     * @param metricsTypes        指标类型
     * @param topNu          top
     * @param aggType        聚合类型
     * @param esNodesMaxNum  聚合buckets个数
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @return List<VariousLineChartMetrics>
     */
    public List<VariousLineChartMetrics> getTopNNodeAggMetrics(String clusterPhyName, List<String> metricsTypes,
                                                               Integer topNu, String aggType, int esNodesMaxNum,
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
        if (CollectionUtils.isNotEmpty(topMetrics.getTopName())) {
            topNameStr = buildTopNameStr(topMetrics.getTopName());
        }

        if (StringUtils.isBlank(topNameStr)) {
            return;
        }

        String interval = MetricsUtils.getInterval(endTime - startTime);
        List<String> metricsKeys = getAggMetricsStr(Lists.newArrayList(topMetrics.getType()), aggType);

        String dsl = dslLoaderUtil.getDslByTopNNameInfo(DslsConstant.GET_TOPN_NODE_AGG_METRICS_INFO, interval,
                topNameStr, buildAggsDSL(metricsKeys, aggType), clusterPhyName, startTime, endTime, esNodesMaxNum, startTime, endTime);

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        List<VariousLineChartMetrics> variousLineChartMetrics = gatewayClient.performRequestWithRouting(metadataClusterName,
                null, realIndexName, TYPE, dsl, s -> fetchMultipleAggMetrics(s, metricsKeys, null, false), 3);
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
            Long timePoint = getHasDataTime(clusterPhyName, startTime, endTime, DslsConstant.GET_HAS_NODE_METRICS_DATA_TIME);
            //没有数据则提前终止
            if (null == timePoint) {
                break;
            }

            Tuple<Long, Long> firstInterval = MetricsUtils.getSortInterval(endTime - startTime, timePoint);
            long startTimeForOneInterval    = firstInterval.getV1();
            long endTimeForOneInterval      = firstInterval.getV2();

            String interval = MetricsUtils.getInterval(endTime - startTime);

            List<String> metricsKeys = getAggMetricsStr(metricsTypes, aggType);

            String dsl = dslLoaderUtil.getFormatDslByFileNameAndOtherParam(DslsConstant.GET_AGG_CLUSTER_PHY_NODES_INFO,
                    interval, buildAggsDSL(metricsKeys, aggType), clusterPhyName, startTimeForOneInterval, endTimeForOneInterval, esNodesMaxNum);

            String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTimeForOneInterval,
                    endTimeForOneInterval);

            variousLineChartMetrics = gatewayClient.performRequestWithRouting(metadataClusterName, null,
                    realIndexName, TYPE, dsl, s -> fetchMultipleAggMetrics(s, metricsKeys, topNu, true), 3);
        }while (retryTime++ > 3 && CollectionUtils.isEmpty(variousLineChartMetrics));

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

        List<String> metricsKeys = getAggMetricsStr(metrics, aggType);

        String dsl = dslLoaderUtil.getFormatDslByFileNameAndOtherParam(
            DslsConstant.GET_AGG_CLUSTER_PHY_SINGLE_NODE_NODE, interval, buildAggsDSL(metricsKeys, aggType),
            clusterPhyName, nodeName, startTime, endTime);

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequestWithRouting(metadataClusterName, nodeName, realIndexName, TYPE, dsl,
            s -> fetchSingleAggMetrics(s, metricsKeys, nodeName), 3);
    }

    /**************************************** private methods ****************************************/
    private void handleESQueryResponse(String clusterName, Map<String, NodeRackStatisPO> nodeRackStatisMap, ESQueryResponse esQueryResponse) {
        if (esQueryResponse != null && esQueryResponse.getAggs() != null) {
            Map<String, ESAggr> esAggrMap = esQueryResponse.getAggs().getEsAggrMap();
            if (esAggrMap != null && esAggrMap.containsKey("minute_bucket")) {
                ESAggr minuteBucketESAggr = esAggrMap.get("minute_bucket");
                handleMinuteBucketESAggr(clusterName, nodeRackStatisMap, minuteBucketESAggr);
            }
        }
    }

    private void handleMinuteBucketESAggr(String clusterName, Map<String, NodeRackStatisPO> nodeRackStatisMap, ESAggr minuteBucketESAggr) {
        if (minuteBucketESAggr != null && CollectionUtils.isNotEmpty(minuteBucketESAggr.getBucketList())) {
            for (ESBucket esBucket : minuteBucketESAggr.getBucketList()) {
                ESAggr groupByRackAggr = esBucket.getAggrMap().get("groupByRack");

                if (groupByRackAggr != null && CollectionUtils.isNotEmpty(groupByRackAggr.getBucketList())) {
                    handleBucketList(clusterName, nodeRackStatisMap, groupByRackAggr);
                }
            }
        }
    }

    private void handleRackList(String clusterName, List<String> rackList, Map<String, NodeRackStatisPO> nodeRackStatisMap, List<NodeRackStatisPO> nodeRackStatisPOS) {
        NodeRackStatisPO nodeRackStatisPO;
        for (String rack : rackList) {
            if (nodeRackStatisMap.containsKey(rack)) {
                nodeRackStatisPOS.add(nodeRackStatisMap.get(rack));
            } else {
                LOGGER.warn("class=AriusStatsNodeInfoEsDao||method=getRackStatis||msg={} {} set default value",
                        clusterName, rack);
                nodeRackStatisPO = new NodeRackStatisPO(clusterName, rack, 0.0, 0.0, 0, 0d, 0);

                nodeRackStatisPOS.add(nodeRackStatisPO);
            }
        }
    }

    private void handleBucketList(String clusterName, Map<String, NodeRackStatisPO> nodeRackStatisMap, ESAggr groupByRackAggr) {
        for (ESBucket rackBucket : groupByRackAggr.getBucketList()) {
            ESAggr sumFreeDiskAggr = rackBucket.getAggrMap().get("sumFreeDisk");
            ESAggr sumTotalDiskAggr = rackBucket.getAggrMap().get("sumTotalDisk");
            ESAggr avgCpuUsageAggr = rackBucket.getAggrMap().get("avgCpuUsage");
            ESAggr docsCountAggr = rackBucket.getAggrMap().get("docsCount");
            String rackName = rackBucket.getUnusedMap().get("key").toString();

            if (!nodeRackStatisMap.containsKey(rackName)) {
                handleNodeRackStatisPO(clusterName, nodeRackStatisMap, sumFreeDiskAggr, sumTotalDiskAggr, avgCpuUsageAggr, docsCountAggr, rackName);
            }
        }
    }

    private void handleNodeRackStatisPO(String clusterName, Map<String, NodeRackStatisPO> nodeRackStatisMap,
                                        ESAggr sumFreeDiskAggr, ESAggr sumTotalDiskAggr,
                                        ESAggr avgCpuUsageAggr, ESAggr docsCountAggr,
                                        String rackName) {
        NodeRackStatisPO nodeRackStatisPO;
        nodeRackStatisPO = new NodeRackStatisPO(clusterName, rackName, 0.0, 0.0, 0, 0d, 0);

        if (sumTotalDiskAggr != null && sumTotalDiskAggr.getUnusedMap().containsKey(VALUE)
                && sumTotalDiskAggr.getUnusedMap().get(VALUE) != null) {
            nodeRackStatisPO.setTotalDiskG(
                    Double.valueOf(sumTotalDiskAggr.getUnusedMap().get(VALUE).toString())
                            / ONE_GB);
        }
        if (sumFreeDiskAggr != null && sumFreeDiskAggr.getUnusedMap().containsKey(VALUE)
                && sumFreeDiskAggr.getUnusedMap().get(VALUE) != null) {
            nodeRackStatisPO.setDiskFreeG(
                    Double.valueOf(sumFreeDiskAggr.getUnusedMap().get(VALUE).toString())
                            / ONE_GB);
        }
        if (avgCpuUsageAggr != null && avgCpuUsageAggr.getUnusedMap().containsKey(VALUE)
                && avgCpuUsageAggr.getUnusedMap().get(VALUE) != null) {
            nodeRackStatisPO.setCpuUsedPercent(
                    Double.valueOf(avgCpuUsageAggr.getUnusedMap().get(VALUE).toString()));
        }
        if (docsCountAggr != null && docsCountAggr.getUnusedMap().containsKey(VALUE)
                && docsCountAggr.getUnusedMap().get(VALUE) != null) {
            nodeRackStatisPO.setDocNu(Double
                    .valueOf(docsCountAggr.getUnusedMap().get(VALUE).toString()).longValue());
        }

        nodeRackStatisMap.put(rackName, nodeRackStatisPO);
    }
}

package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContentCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.ESConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.GatewayMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsConstant;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.DSLSearchUtils;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didichuxing.datachannel.arius.admin.persistence.es.metric.BaseTopNMetricsDAO;
import com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class GatewayNodeMetricsDAO extends BaseTopNMetricsDAO {
    private static final String TYPE = "type";
    private static final String AGG_KEY_TIMESTAMP = "group_by_timeStamp";
    private static final String AGG_KEY_FIELD     = "group_by_field";
    private static final String KEY               = "key";
    /**
     * 成功率/失败率 百分比 最小值
     */
    private static final double ZERO = 0.0;
    /**
     * 成功率/失败率 百分百 总值
     */
    private static final double SUM_RATE              = 100.0;
    private static final String NOW_2M                = "now-2m";
    private static final String NOW_1M                = "now-1m";
    private static final String EMPTY_STR             = "";
    private static final String GATEWAY_NODE          = "gatewayNode";
    private static final String CLIENT_NODE           = "clientNode";
    private static final String CLUSTER_NAME          = "clusterName";
    private static final String QUERY_REQUEST         = "queryRequest";
    private static final String EXISTS                = "exists";
    private static final String TIMESTAMP             = "timeStamp";
    private static final String TERM                  = "term";
    private static final String TERMS                 = "terms";
    private static final String FIELD                 = "field";
    private static final String DSL_LEN               = "dslLen";
    private static final String DSL_LEN_AVG           = "dsl_len";
    private static final String AVG                   = "avg";
    private static final String PROJECT_ID            = "projectId";
    private static final String GATEWAY_SUCCESS_COUNT = "gatewaySuccessCount";
    private static final String DOC_COUNT             = "doc_count";
    private static final int    AGG_TERMS_SIZE        = 1000;
    
    @PostConstruct
    public void init() {
        super.indexName = dataCentreUtil.getAriusStatsGatewayInfo();
    }

    /**
     * 获取某 clientNode 读分布
     */
    public VariousLineChartMetrics getClientNodeAggFieldByRange(GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                                                Long startTime, Long endTime, Integer projectId,
                                                                String gatewayNodeIp, String clientNodeIp) {
        List<String> cellList = buildBaseTermCondition(gatewayNodeIp, startTime, endTime, projectId);
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(clientNodeIp, "clientNode"));
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(true, "queryRequest"));
        String condition = "[" + ListUtils.strList2String(cellList) + "]";

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLIENT_NODE_FIELD_BY_IP, condition, interval,
            startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldByIpAggMetrics(response, clientNodeIp, gatewayMetricsTypeEnum,
                interval),
            3);
    }

    /**
     * 获取 topN clientNode 读分布
     */
    public VariousLineChartMetrics getClientNodeAggFieldByRange(GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                                                Long startTime, Long endTime, Integer projectId,
                                                                Integer topNu, String gatewayNodeIp) {
        List<String> cellList = buildBaseTermCondition(gatewayNodeIp, startTime, endTime, projectId);
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(true, "queryRequest"));
        String condition = "[" + ListUtils.strList2String(cellList) + "]";
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLIENT_NODE_FIELD, condition, interval,
            startTime, endTime);
        VariousLineChartMetrics variousLineChartMetrics = gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldAggMetrics(response, gatewayMetricsTypeEnum, interval), 3);
        //根据第一个时间点的值进行倒排，取topNu
        List<MetricsContent> sortedList = variousLineChartMetrics.getMetricsContents().stream()
            .sorted(Comparator.comparing(x -> x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder()))
            .limit(topNu).collect(Collectors.toList());
        variousLineChartMetrics.setMetricsContents(sortedList);
        return variousLineChartMetrics;
    }

    /**
     * 获取 topN clientNode 分布
     */
    public VariousLineChartMetrics getClientNodeTopN(Long startTime, Long endTime, Integer projectId, Integer topNu,
                                                     String gatewayNodeIp,
                                                     GatewayMetricsTypeEnum gatewayMetricsTypeEnum) {
        List<String> cellList = buildBaseTermCondition(gatewayNodeIp, startTime, endTime, projectId);
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(false, "queryRequest"));
        String condition = "[" + ListUtils.strList2String(cellList) + "]";
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLIENT_NODE_WRITE, condition, interval,
            startTime, endTime);
        VariousLineChartMetrics variousLineChartMetrics = gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldAggMetrics(response, gatewayMetricsTypeEnum, interval), 3);
        //根据第一个时间点的值进行倒排，取topNu
        List<MetricsContent> sortedList = variousLineChartMetrics.getMetricsContents().stream()
            .sorted(Comparator.comparing(x -> x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder()))
            .limit(topNu).collect(Collectors.toList());
        variousLineChartMetrics.setMetricsContents(sortedList);
        return variousLineChartMetrics;
    }

    public VariousLineChartMetrics getClientNodeWrite(Long startTime, Long endTime, Integer projectId, Integer topNu,
                                                      String gatewayNodeIp) {
        return getClientNodeTopN(startTime, endTime, projectId, topNu, gatewayNodeIp,
            GatewayMetricsTypeEnum.WRITE_CLIENT_NODE);
    }

    public VariousLineChartMetrics getClientNodeDSLLENByIp(Long startTime, Long endTime, Integer projectId,
                                                           Integer topNu, String gatewayNodeIp) {
        return getClientNodeTopN(startTime, endTime, projectId, topNu, gatewayNodeIp,
            GatewayMetricsTypeEnum.DSLLEN_CLIENT_NODE);
    }

    /**
     * 获取某 clientNode 分布
     */
    public VariousLineChartMetrics getSingleClientNodeWriteByIp(Long startTime, Long endTime, Integer projectId,
                                                                String gatewayNodeIp, String clientNodeIp,
                                                                GatewayMetricsTypeEnum gatewayMetricsTypeEnum) {
        List<String> cellList = buildBaseTermCondition(gatewayNodeIp, startTime, endTime, projectId);
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(clientNodeIp, "clientNode"));
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(false, "queryRequest"));
        String condition = "[" + ListUtils.strList2String(cellList) + "]";
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLIENT_NODE_WRITE_BY_IP, condition, interval,
            startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldByIpAggMetrics(response, clientNodeIp, gatewayMetricsTypeEnum,
                interval),
            3);
    }

    public VariousLineChartMetrics getClientNodeWriteByIp(Long startTime, Long endTime, Integer projectId,
                                                          String gatewayNodeIp, String clientNodeIp) {
        return getSingleClientNodeWriteByIp(startTime, endTime, projectId, gatewayNodeIp, clientNodeIp,
            GatewayMetricsTypeEnum.WRITE_CLIENT_NODE);
    }

    public VariousLineChartMetrics getClientNodeDSLLENByIp(Long startTime, Long endTime, Integer projectId,
                                                           String gatewayNodeIp, String clientNodeIp) {
        return getSingleClientNodeWriteByIp(startTime, endTime, projectId, gatewayNodeIp, clientNodeIp,
            GatewayMetricsTypeEnum.DSLLEN_CLIENT_NODE);
    }

    /**
     * 获取 topN gatewayNode 读分布
     */
    public VariousLineChartMetrics getAggFieldByRange(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime,
                                                      Long endTime, Integer projectId, Integer topNu) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_NODE_FIELD, startTime, endTime,
            projectId, interval, startTime, endTime);
        VariousLineChartMetrics variousLineChartMetrics = gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldAggMetrics(response, gatewayMetricsTypeEnum, interval), 3);
        //根据第一个时间点的值进行倒排，取topNu
        List<MetricsContent> sortedList = variousLineChartMetrics.getMetricsContents().stream()
            .sorted(Comparator.comparing(x -> x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder()))
            .limit(topNu).collect(Collectors.toList());
        variousLineChartMetrics.setMetricsContents(sortedList);
        return variousLineChartMetrics;
    }

    /**
     * 获取某 gatewayNode 读分布
     */
    public VariousLineChartMetrics getAggFieldByRange(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime,
                                                      Long endTime, Integer projectId, String nodeIp) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_NODE_FIELD_BY_IP, nodeIp, startTime,
            endTime, projectId, interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldByIpAggMetrics(response, nodeIp, gatewayMetricsTypeEnum, interval),
            3);
    }

    /**
     * 获取 topN gatewayNode 分布
     */
    public VariousLineChartMetrics getWriteGatewayNodeTopN(Long startTime, Long endTime, Integer projectId,
                                                           Integer topNu,
                                                           GatewayMetricsTypeEnum gatewayMetricsTypeEnum) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_NODE_WRITE, startTime, endTime,
            projectId, interval, startTime, endTime);
        VariousLineChartMetrics variousLineChartMetrics = gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldAggMetrics(response, gatewayMetricsTypeEnum, interval), 3);
        //根据第一个时间点的值进行倒排，取topNu
        List<MetricsContent> sortedList = variousLineChartMetrics.getMetricsContents().stream()
            .sorted(Comparator.comparing(x -> x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder()))
            .limit(topNu).collect(Collectors.toList());
        variousLineChartMetrics.setMetricsContents(sortedList);
        return variousLineChartMetrics;
    }

    /**
     * 获取 topN gatewayNode 写分布
     */
    public VariousLineChartMetrics getWriteGatewayNode(Long startTime, Long endTime, Integer projectId, Integer topNu) {
        return getWriteGatewayNodeTopN(startTime, endTime, projectId, topNu, GatewayMetricsTypeEnum.WRITE_GATEWAY_NODE);
    }

    /**
     * 获取某 gatewayNode 写入的数据量
     */
    public VariousLineChartMetrics getSingleGatewayNodeWriteByIp(Long startTime, Long endTime, Integer projectId,
                                                                 String nodeIp,
                                                                 GatewayMetricsTypeEnum gatewayMetricsTypeEnum) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_NODE_WRITE_BY_IP, nodeIp, startTime,
            endTime, projectId, interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldByIpAggMetrics(response, nodeIp, gatewayMetricsTypeEnum, interval),
            3);
    }

    /**
     * 获取某 gatewayNode 写分布
     */
    public VariousLineChartMetrics getWriteGatewayNodeByIp(Long startTime, Long endTime, Integer projectId,
                                                           String nodeIp) {
        return getSingleGatewayNodeWriteByIp(startTime, endTime, projectId, nodeIp,
            GatewayMetricsTypeEnum.WRITE_GATEWAY_NODE);
    }

    /**
     * 获取 topN gatewayNode dsl 长度
     */
    public VariousLineChartMetrics getWriteGatewayDSLLen(Long startTime, Long endTime, Integer projectId,
                                                         Integer topNu) {
        return getWriteGatewayNodeTopN(startTime, endTime, projectId, topNu,
            GatewayMetricsTypeEnum.DSLLEN_GATEWAY_NODE);
    }

    /**
     * 获取某 gatewayNode dsl 长度
     */
    public VariousLineChartMetrics getWriteGatewayDSLLenByIp(Long startTime, Long endTime, Integer projectId,
                                                             String nodeIp) {
        return getSingleGatewayNodeWriteByIp(startTime, endTime, projectId, nodeIp,
            GatewayMetricsTypeEnum.DSLLEN_GATEWAY_NODE);
    }

    /**
     * 获取 gatewayNode 相关的 clientNode ip 信息
     */
    public List<String> getEsClientNodeIpListByGatewayNode(String gatewayNode, Long startTime, Long endTime,
                                                           Integer projectId) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        List<String> cellList = buildBaseTermCondition(gatewayNode, startTime, endTime, projectId);
        String condition = "[" + ListUtils.strList2String(cellList) + "]";

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLIENT_NODE_BY_GATEWAY_NODE, condition);

        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> {
            List<String> list = Lists.newArrayList();
            Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
                .orElse(null);
            if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_FIELD)) {
                for (ESBucket esBucket : esAggrMap.get(AGG_KEY_FIELD).getBucketList()) {
                    String clientNode = esBucket.getUnusedMap().get(KEY).toString();
                    list.add(clientNode);
                }
            }
            return list;
        }, 3);
    }

    /**
     * 网关成功率和失败率
     *
     * @param cluster 集群
     * @return {@code Tuple<Double, Double>} tuple.1:成功率；tuple.2:失败率
     */
    public Tuple<Double/*成功率*/, Double/*失败率*/> getGatewaySuccessRateAndFailureRate(String cluster) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, System.currentTimeMillis(),
            System.currentTimeMillis());//网关总数
        String gatewayCountDsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_COUNT, cluster, NOW_2M,
            NOW_1M);
        Tuple</*count总数*/Long, /*成功率总数*/Long> gatewayCountTotalAndGatewaySuccessCountTuple = gatewayClient
            .performRequest(cluster, realIndexName, TYPE, gatewayCountDsl,
                esQueryResponse -> Optional.ofNullable(esQueryResponse).map(response -> {
                    Long totalCount = Long
                        .valueOf(response.getHits().getUnusedMap().getOrDefault(ESConstant.HITS_TOTAL, "0").toString());
                    Long gatewaySuccessCount = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
                        .map(aggrMap -> aggrMap.get(GATEWAY_SUCCESS_COUNT)).map(ESAggr::getUnusedMap)
                        .map(aggrMap -> aggrMap.get(DOC_COUNT)).map(String::valueOf).map(Long::valueOf).orElse(0L);
                    return new Tuple<>(totalCount, gatewaySuccessCount);
                }).orElse(new Tuple<>(0L, 0L)), 3);
        // 网关成功数
        double successRate = CommonUtils.divideDoubleAndFormatDouble(
            gatewayCountTotalAndGatewaySuccessCountTuple.getV2().doubleValue(),
            gatewayCountTotalAndGatewaySuccessCountTuple.getV1().doubleValue(), 2, 1);
        double success = -1;
        double failed = -1;
        if (successRate > ZERO) {
            success = successRate * 100;
            failed = SUM_RATE - success;
        }
        return new Tuple<>(success, failed);
    }

    /**************************************** private methods ****************************************/
    private List<String> buildBaseTermCondition(String gatewayNode, Long startTime, Long endTime, Integer projectId) {
        List<String> cellList = Lists.newArrayList();
        cellList.add(DSLSearchUtils.getTermCellForRangeSearch(startTime, endTime, "timeStamp"));
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(projectId, "projectId"));
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(gatewayNode, "gatewayNode"));
        return cellList;
    }

    private Long getTotal(String cluster, String realIndexName, String dsl) {

        return gatewayClient.performRequestAndGetTotalCount(cluster, realIndexName, TYPE, dsl, 3);

    }

    private VariousLineChartMetrics fetchFieldAggMetrics(ESQueryResponse response,
                                                         GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                                         String interval) {
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(gatewayMetricsTypeEnum.getType());
        variousLineChartMetrics.setMetricsContents(Lists.newArrayList());
        Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
            .orElse(null);
        if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_FIELD)) {
            handleESBucket(gatewayMetricsTypeEnum, interval, variousLineChartMetrics, esAggrMap);
        }
        return variousLineChartMetrics;
    }

    private void handleESBucket(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String interval,
                                VariousLineChartMetrics variousLineChartMetrics, Map<String, ESAggr> esAggrMap) {
        for (ESBucket esBucket : esAggrMap.get(AGG_KEY_FIELD).getBucketList()) {
            String nodeName = esBucket.getUnusedMap().get(KEY).toString();
            MetricsContent metricsContent = new MetricsContent();
            metricsContent.setName(nodeName);
            metricsContent.setMetricsContentCells(Lists.newArrayList());
            variousLineChartMetrics.getMetricsContents().add(metricsContent);
            if (null != esBucket.getAggrMap() && null != esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP)) {
                for (ESBucket bucket : esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP).getBucketList()) {
                    long timeStamp = Long.parseLong(bucket.getUnusedMap().get(KEY).toString());
                    double value;
                    String aggKey = gatewayMetricsTypeEnum.getAggKey();
                    if (MetricsUtils.needConvertUnit(aggKey)) {
                        value = MetricsUtils.getDoubleValuePerMin(interval,
                            bucket.getUnusedMap().get(aggKey).toString());
                    } else if (GatewayMetricsTypeEnum.DSL_LEN.getAggKey().equals(aggKey)) {
                        value = MetricsUtils.getAggMapDoubleValue(bucket, aggKey);
                    } else {
                        value = Double.parseDouble(bucket.getUnusedMap().get(aggKey).toString());
                    }
                    metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
                }
            }
        }
    }

    private VariousLineChartMetrics fetchFieldByIpAggMetrics(ESQueryResponse response, String nodeIp,
                                                             GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                                             String interval) {
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(gatewayMetricsTypeEnum.getType());

        MetricsContent metricsContent = new MetricsContent();
        metricsContent.setName(nodeIp);
        metricsContent.setMetricsContentCells(Lists.newArrayList());

        variousLineChartMetrics.setMetricsContents(Lists.newArrayList(metricsContent));

        Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
            .orElse(null);
        if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_TIMESTAMP)) {
            for (ESBucket esBucket : esAggrMap.get(AGG_KEY_TIMESTAMP).getBucketList()) {
                long timeStamp = Long.parseLong(esBucket.getUnusedMap().get(KEY).toString());
                String aggKey = gatewayMetricsTypeEnum.getAggKey();
                double value;
                if (MetricsUtils.needConvertUnit(aggKey)) {
                    value = MetricsUtils.getDoubleValuePerMin(interval, esBucket.getUnusedMap().get(aggKey).toString());
                } else if (GatewayMetricsTypeEnum.DSLLEN_GATEWAY_NODE.getAggKey().equals(aggKey)) {
                    value = MetricsUtils.getAggMapDoubleValue(esBucket, aggKey);
                } else {
                    value = Double.parseDouble(esBucket.getUnusedMap().get(aggKey).toString());
                }
                metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
            }
        }
        return variousLineChartMetrics;
    }
    
    @Override
    public List<VariousLineChartMetrics> fetchTopMetric(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime,
                                                        Long endTime, Integer topNu, Integer projectId, String value)
            throws AdminOperateException {
        return fetchTopMetric(gatewayMetricsTypeEnum, startTime, endTime, topNu, projectId, value, null);
    }
    
    /**
     * 对于
     *
     * @param gatewayMetricsTypeEnum 网关指标类型枚举
     * @param startTime              开始时间
     * @param endTime                结束时间
     * @param topNu                  top n
     * @param projectId                  projectId
     * @param value                  单个指标值
     * @param nodeIp                 节点ip {@linkplain  GatewayMetricsTypeEnum CLIENT_NODE} 需要设置该参数
     * @return {@link List}<{@link VariousLineChartMetrics}>
     * @see MetricsConstant#CLIENT_NODE
     */
    public List<VariousLineChartMetrics> fetchTopMetric(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime,
                                                        Long endTime, Integer topNu, Integer projectId, String value,
                                                        String nodeIp) throws AdminOperateException {
        //构建hasDataTime的查询片段
        String queryDsl = buildExistField(gatewayMetricsTypeEnum, nodeIp);
        if (Objects.isNull(queryDsl)) {
            return Collections.emptyList();
        }
        //获取最终的dsl
        String hasDataTimeDsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_HAS_METRIC_INFO_TIME_BY_FIELD,
                startTime, endTime, queryDsl + buildTermByProjectId(projectId));
        //根据不同的gatewayMetricsTypeEnum来进行query 片段构建
        String queryDslByFiled =
                buildTermsQueryRequestFieldByValue(Collections.emptyList(), gatewayMetricsTypeEnum, nodeIp) + buildTermByProjectId(projectId);
        // 注意这里不用MetricsUtils.getIntervalForDashBoard
        String interval = MetricsUtils.Interval.FIVE_MIN.getStr();
        Object[] params;
        switch (gatewayMetricsTypeEnum) {
            case DSLLEN_GATEWAY_NODE:
                //queryRequest false
                // queryRequest false
            case WRITE_GATEWAY_NODE:
                params = new Object[] { queryDslByFiled, AGG_KEY_FIELD, GATEWAY_NODE, AGG_TERMS_SIZE, interval,
                                        DSL_LEN_AVG, AVG, DSL_LEN };
                //params = new Object[] { queryDslByFiled, AGG_KEY_FIELD, GATEWAY_NODE, AGG_TERMS_SIZE, interval,
                //                        DSL_LEN_AVG, AVG, DSL_LEN };
                break;
            case QUERY_GATEWAY_NODE:
                //queryRequest true
                params = new Object[] { queryDslByFiled, AGG_KEY_FIELD, GATEWAY_NODE, AGG_TERMS_SIZE, interval };
                //params = new Object[] { queryDslByFiled, AGG_KEY_FIELD, GATEWAY_NODE, AGG_TERMS_SIZE, interval };
                break;
            //queryRequest false
            case WRITE_CLIENT_NODE:
            case DSLLEN_CLIENT_NODE:
                params = new Object[] { queryDslByFiled, AGG_KEY_FIELD, CLIENT_NODE, AGG_TERMS_SIZE, interval,
                                        DSL_LEN_AVG, AVG, DSL_LEN };
                //params = new Object[] { queryDslByFiled, AGG_KEY_FIELD, CLIENT_NODE, AGG_TERMS_SIZE, interval,
                //                        DSL_LEN_AVG, AVG, DSL_LEN };
                break;
            //query true
            case QUERY_CLIENT_NODE:
                params = new Object[] { queryDslByFiled, AGG_KEY_FIELD, CLIENT_NODE, AGG_TERMS_SIZE, interval };
                //params = new Object[] { queryDslByFiled, AGG_KEY_FIELD, CLIENT_NODE, AGG_TERMS_SIZE, interval };
                break;
            default:
                return Collections.emptyList();
        }
        return super.performFetchTopMetric(gatewayMetricsTypeEnum, startTime, endTime,
                response -> fetchFieldAggMetricsByList(response, gatewayMetricsTypeEnum, interval, topNu),
                hasDataTimeDsl, value, params);
    }
    
    @Override
    public List<MetricsContent> getByRangeTopN(List<String> values, GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                               Long startTime, Long endTime, Integer projectId)
            throws AdminOperateException {
        return getByRangeTopN(values, gatewayMetricsTypeEnum, startTime, endTime, projectId, null);
    }
    
    /**
     * 查询指定范围的结果 *
     *
     * @param values                 第一阶段召回的结果
     * @param gatewayMetricsTypeEnum 网关指标类型枚举
     * @param startTime              开始时间
     * @param endTime                结束时间
     * @param projectId              应用程序id
     * @param nodeIp                 节点ip 该参数是来自于{@link MetricsConstant#CLIENT_NODE};
     *                               但是对于{@link MetricsConstant#NODE}默认是null
     * @return {@link List}<{@link MetricsContent}>
     */
    public List<MetricsContent> getByRangeTopN(List<String> values, GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                               Long startTime, Long endTime, Integer projectId, String nodeIp)
            throws AdminOperateException {
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }    //构建query terms 查询的片段
        String queryDslByFiled =
                buildTermsQueryRequestFieldByValue(values, gatewayMetricsTypeEnum, nodeIp) + buildTermByProjectId(projectId);
        String interval = MetricsUtils.getInterval((endTime - startTime));    //构建出必须包含的values结果，保证该结果可以正常返回
        String include = new JSONArray().fluentAddAll(values).toJSONString();
        Object[] params;
        switch (gatewayMetricsTypeEnum) {
            case DSLLEN_GATEWAY_NODE:
                //queryRequest false
                // queryRequest false
            case WRITE_GATEWAY_NODE:
                params = new Object[] { startTime, endTime, queryDslByFiled, AGG_KEY_FIELD, GATEWAY_NODE, values.size(),
                                        include, interval, startTime, endTime, DSL_LEN_AVG, AVG, DSL_LEN };
                break;
            case QUERY_GATEWAY_NODE:
                params = new Object[] { startTime, endTime, queryDslByFiled, AGG_KEY_FIELD, GATEWAY_NODE, values.size(),
                                        include, interval, startTime, endTime };
                //queryRequest true
                break;
            //queryRequest false
            case WRITE_CLIENT_NODE:
            case DSLLEN_CLIENT_NODE:
                params = new Object[] { startTime, endTime, queryDslByFiled, AGG_KEY_FIELD, CLIENT_NODE, values.size(),
                                        include, interval, startTime, endTime, DSL_LEN_AVG, AVG, DSL_LEN };
                break;
            //query true
            case QUERY_CLIENT_NODE:
                params = new Object[] { startTime, endTime, queryDslByFiled, AGG_KEY_FIELD, CLIENT_NODE, values.size(),
                                        include, interval, startTime, endTime };
                break;
            default:
                return Collections.emptyList();
        }
        return this.performGetByRangeTopN(gatewayMetricsTypeEnum, startTime, endTime,
                response -> fetchDslResultByFiled(response, gatewayMetricsTypeEnum, interval), params);
    }
    
    @Override
    public List<VariousLineChartMetrics> checkMetricsValue(GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                                           String metricsValue) {    // 当该指标存在时候，直接进行返回
        if (StringUtils.isNotBlank(metricsValue)) {
            VariousLineChartMetrics metrics = new VariousLineChartMetrics();
            metrics.setType(gatewayMetricsTypeEnum.getType());
            MetricsContent metricsContent = new MetricsContent();
            metricsContent.setName(metricsValue);
            metrics.setMetricsContents(Collections.singletonList(metricsContent));
            return Collections.singletonList(metrics);
        }
        return Collections.emptyList();
    }
    
    private List<VariousLineChartMetrics> fetchFieldAggMetricsByList(ESQueryResponse response,
                                                                     GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                                                     String interval, Integer topNu) {
        final VariousLineChartMetrics variousLineChartMetrics = fetchFieldAggMetrics(response, gatewayMetricsTypeEnum,
                interval, topNu);
        return Collections.singletonList(variousLineChartMetrics);
    }
    
    /**
     * 构建 queryRequest 的请求为：false或者true的term和{@linkplain  GatewayNodeMetricsDAO#GATEWAY_NODE/> 或者{@link  GatewayNodeMetricsDAO#CLIENT_NODE}
     * 的term dsl片段
     * <pre>
     *     ,{
     *     "term": {
     *     "queryRequest": false
     *     }
     *     }, {
     *     "terms": {
     *     "FIELD": [
     * *        "VALUE1",         "VALUE2"
     * ]     }
     * }
     * </pre>
     * 其中这里的<B>FIELD</B>为{@linkplain  GatewayNodeMetricsDAO#GATEWAY_NODE/> 或者{@link  GatewayNodeMetricsDAO#CLIENT_NODE}
     *  @param values
     *  @return {@link String}
     */
    private String buildTermsQueryRequestFieldByValue(List<String> values, GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                                      String nodeIp) {
        String byQueryRequestContent = buildByQueryRequestContent(gatewayMetricsTypeEnum);
        String termsContentByField = buildTermsContentByField(values, gatewayMetricsTypeEnum);
        if (Objects.nonNull(byQueryRequestContent) && CollectionUtils.isEmpty(values)) {
            return String.format(",%s", byQueryRequestContent)
                   //加入node ip 的query 查询
                   + (StringUtils.isNotBlank(nodeIp) ? "," + new JSONObject().fluentPut(TERM,
                    new JSONObject().fluentPut(GATEWAY_NODE, nodeIp)).toJSONString() : EMPTY_STR);
        } else if (Objects.nonNull(byQueryRequestContent) && Objects.nonNull(termsContentByField)) {
            return String.format(",%s,%s", byQueryRequestContent, termsContentByField) + (StringUtils.isNotBlank(nodeIp)
                    ? "," + new JSONObject().fluentPut(TERM, new JSONObject().fluentPut(GATEWAY_NODE, nodeIp))
                    .toJSONString()
                    : EMPTY_STR);
        }
        return null;
    }
    
    /**
     * 构建queryRequest查询请求
     * <pre>
     *     {
     *     "term": {
     *     "queryRequest": false
     *     }
     *     }
     * </pre>
     * or
     * <pre>
     *     {
     * "term": {
     * "queryRequest": true
     * }  }
     * </pre>
     *
     * @param gatewayMetricsTypeEnum 网关指标类型枚举
     * @return {@link JSONObject}
     */
    private String buildByQueryRequestContent(GatewayMetricsTypeEnum gatewayMetricsTypeEnum) {
        JSONObject content;
        switch (gatewayMetricsTypeEnum) {
            //queryRequest false
            case WRITE_CLIENT_NODE:
            case DSLLEN_CLIENT_NODE:
            case WRITE_GATEWAY_NODE:
            case DSLLEN_GATEWAY_NODE:
                //queryRequest false
                content = new JSONObject().fluentPut(QUERY_REQUEST, false);
                break;
            case QUERY_CLIENT_NODE:
            case QUERY_GATEWAY_NODE:
                //queryRequest true
                content = new JSONObject().fluentPut(QUERY_REQUEST, true);
                break;
            default:
                return null;
        }
        return new JSONObject().fluentPut(TERM, content).toJSONString();
    }
    
    /**
     * 构建t{@link  GatewayNodeMetricsDAO#GATEWAY_NODE/> 或者{@link  GatewayNodeMetricsDAO#CLIENT_NODE}的查询
     * <pre>
     *   {
     *           "terms": {
     *             "FIELD": [
     *               "VALUE1",
     *               "VALUE2"
     *             ]
     *           }
     *         }
     *  </pre>
     * 其中这里的<B>FIELD</B>为{@link  GatewayNodeMetricsDAO#GATEWAY_NODE/> 或者{@link  GatewayNodeMetricsDAO#CLIENT_NODE}
     *
     * @param values                 值
     * @param gatewayMetricsTypeEnum 网关指标类型枚举
     * @return {@link Optional}<{@link JSONObject}>
     */
    private String buildTermsContentByField(List<String> values, GatewayMetricsTypeEnum gatewayMetricsTypeEnum) {
        JSONObject content;
        switch (gatewayMetricsTypeEnum) {
            case WRITE_GATEWAY_NODE:
            case DSLLEN_GATEWAY_NODE:
            case QUERY_GATEWAY_NODE:
                content = new JSONObject().fluentPut(GATEWAY_NODE, values);
                break;
            case WRITE_CLIENT_NODE:
            case QUERY_CLIENT_NODE:
            case DSLLEN_CLIENT_NODE:
                content = new JSONObject().fluentPut(CLIENT_NODE, values);
                break;
            default:
                return null;
        }
        return new JSONObject().fluentPut(TERMS, content).toJSONString();
    }
    
    /**
     * 构建术语
     * <pre>
     *     {    "term": {
     *     "queryRequest": false
     *     }
     *     }
     *
     * </pre> *
     *
     * @param gatewayMetricsTypeEnum {@link  GatewayMetricsTypeEnum}
     * @param nodeIp                 {@linkplain GatewayNodeMetricsDAO GATEWAY_NODE} 的value
     * @return {@link String}
     */
    private String buildExistField(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String nodeIp) {
        String queryRequestFragment = buildByQueryRequestContent(gatewayMetricsTypeEnum);
        if (Objects.nonNull(queryRequestFragment)) {
            return String.format(",%s", queryRequestFragment)
                   //nodeIp 确定不为空
                   + (StringUtils.isNotBlank(nodeIp) ? "," + new JSONObject().fluentPut(TERM,
                    new JSONObject().fluentPut(GATEWAY_NODE, nodeIp)).toJSONString() : EMPTY_STR);
        }
        return null;
    }
    
    private List<MetricsContent> fetchDslResultByFiled(ESQueryResponse response,
                                                       GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String interval) {
        final Optional<Map<String, ESAggr>> esAggrMapOptional = Optional.ofNullable(response)
                .map(ESQueryResponse::getAggs).map(ESAggrMap::getEsAggrMap).filter(MapUtils::isNotEmpty);
        if (!esAggrMapOptional.isPresent()) {
            return Collections.emptyList();
        }
        String aggKey = gatewayMetricsTypeEnum.getAggKey();
        List<MetricsContent> metricsContents = Lists.newArrayList();
        for (ESBucket esBucket : esAggrMapOptional.get().get(AGG_KEY_FIELD).getBucketList()) {
            String nodeName = esBucket.getUnusedMap().get(KEY).toString();
            MetricsContent metricsContent = new MetricsContent();
            metricsContent.setName(nodeName);
            metricsContent.setMetricsContentCells(Lists.newArrayList());
            if (null != esBucket.getAggrMap() && null != esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP)) {
                if (esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP).getBucketList() != null) {
                    for (ESBucket bucket : esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP).getBucketList()) {
                        long timeStamp = Long.parseLong(bucket.getUnusedMap().get(KEY).toString());
                        double value;
                        if (MetricsUtils.needConvertUnit(aggKey)) {
                            value = MetricsUtils.getDoubleValuePerMin(interval,
                                    bucket.getUnusedMap().get(aggKey).toString());
                        } else if (GatewayMetricsTypeEnum.DSL_LEN.getAggKey().equals(aggKey)) {
                            value = MetricsUtils.getAggMapDoubleValue(bucket, aggKey);
                        } else {
                            value = Double.parseDouble(bucket.getUnusedMap().get(aggKey).toString());
                        }
                        metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
                    }
                } else {
                    esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP).toJson().getJSONObject(BUCKETS).values()
                            .forEach(groupByTimeStampBucket -> {
                                Long timeStamp = Long.valueOf(
                                        ((JSONObject) groupByTimeStampBucket).get(KEY).toString());
                                Double value;
                                if (MetricsUtils.needConvertUnit(aggKey)) {
                                    value = MetricsUtils.getDoubleValuePerMin(interval, Optional.ofNullable(
                                                    ((JSONObject) groupByTimeStampBucket).get(aggKey).toString())
                                            .map(Object::toString).orElse("0"));
                                } else if (GatewayMetricsTypeEnum.DSL_LEN.getAggKey().equals(aggKey)) {
                                    value = Optional.ofNullable(
                                                    ((JSONObject) groupByTimeStampBucket).getJSONObject(aggKey).get(VALUE))
                                            .map(d -> Double.valueOf(d.toString())).orElse(0.0);
                                } else {
                                    value = Optional.ofNullable(
                                                    ((JSONObject) groupByTimeStampBucket).getJSONObject(aggKey).get(VALUE))
                                            .map(d -> Double.valueOf(d.toString())).orElse(0.0);
                                }
                                metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
                            });
                }
            }
            metricsContents.add(metricsContent);
        }
        return metricsContents;
    }
    
    private VariousLineChartMetrics fetchFieldAggMetrics(ESQueryResponse response,
                                                         GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String interval,
                                                         Integer topNu) {
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(gatewayMetricsTypeEnum.getType());
        variousLineChartMetrics.setMetricsContents(Lists.newArrayList());
        Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
                .orElse(null);
        if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_FIELD)) {
            handleESBucket(gatewayMetricsTypeEnum, interval, variousLineChartMetrics, esAggrMap);
        }
        //根据第一个时间点的值进行倒排，取topNu
        List<MetricsContent> sortedList = variousLineChartMetrics.getMetricsContents().stream()
                .sorted(Comparator.nullsLast(Comparator.comparing(
                        x -> CollectionUtils.isEmpty(x.getMetricsContentCells())
                                ? -1
                                : x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder())))
                .limit(topNu).collect(Collectors.toList());
        variousLineChartMetrics.setMetricsContents(sortedList);
        return variousLineChartMetrics;
    }
    
    @Override
    protected String getFinalDslByOneStep(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Object[] args) {
        switch (gatewayMetricsTypeEnum) {
            case DSLLEN_GATEWAY_NODE:
                //queryRequest false
                // queryRequest false
            case WRITE_GATEWAY_NODE:
            case WRITE_CLIENT_NODE:
            case DSLLEN_CLIENT_NODE:
                return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOTAL_COST_BY_FIELD, args);
            case QUERY_GATEWAY_NODE:            //query true
            case QUERY_CLIENT_NODE:
                //queryRequest true
                return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_COUNT_BY_FILELD, args);
            default:
                return null;
        }
    }
    
    @Override
    protected String getFinalDslBySecondStep(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Object[] args) {
        switch (gatewayMetricsTypeEnum) {
            case DSLLEN_GATEWAY_NODE:            //queryRequest false
                // queryRequest false
            case WRITE_GATEWAY_NODE:
            case WRITE_CLIENT_NODE:
            case DSLLEN_CLIENT_NODE:
                return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOTAL_COST_EXTENDED_BOUNDS_BY_FIELD,
                        args);
            case QUERY_GATEWAY_NODE:
            case QUERY_CLIENT_NODE:
                //queryRequest true
                return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_COUNT_EXTENDED_BOUNDS_BY_FILELD,
                        args);
            default:
                return null;
        }
    }
}
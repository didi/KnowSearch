package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.GatewayOverviewMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContentCell;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.GatewayMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.DSLSearchUtils;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component
@NoArgsConstructor
public class GatewayOverviewMetricsDAO extends BaseESDAO {

    private static final String TYPE = "type";
    private static final String AGG_KEY = "group_by_timeStamp";
    private static final String KEY = "key";
    private String indexName;

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusStatsGatewayInfo();
    }


    /**
     * 获取总览视图公共部分指标
     */
    public List<GatewayOverviewMetrics> getAggCommonMetricsByRange(List<String> metricsTypes, Long startTime, Long endTime) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String aggDsl = getAggDsl(metricsTypes);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_COMMON_METRICS, startTime, endTime, interval, startTime, endTime, aggDsl);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchCommonAggMetrics(response, metricsTypes, interval), 3);
    }

    /**
     * 获取总览视图单项指标
     */
    public GatewayOverviewMetrics getAggSingleMetricsByRange(String dslTemplate, GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime, Long endTime) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(dslTemplate, startTime, endTime, interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchSingleAggMetrics(response, gatewayMetricsTypeEnum, interval), 3);
    }

    /**
     * 获取总览写入指标
     */
    public List<GatewayOverviewMetrics> getAggWriteMetricsByRange(List<String> metricsTypes, Long startTime, Long endTime) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String aggDsl = getAggDsl(metricsTypes);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_WRITE_METRICS, startTime, endTime, interval, startTime, endTime, aggDsl);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchCommonAggMetrics(response, metricsTypes, interval), 3);
    }

    /**************************************************** private methods ****************************************************/
    private List<GatewayOverviewMetrics> fetchCommonAggMetrics(ESQueryResponse response, List<String> metricsTypes, String interval) {
        Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap).orElse(null);
        List<GatewayOverviewMetrics> overviewMetricsArrayList = Lists.newArrayList();
        if (null != esAggrMap && null != esAggrMap.get(AGG_KEY)) {
            for (ESBucket esBucket : esAggrMap.get(AGG_KEY).getBucketList()) {
                handleESBucket(metricsTypes, interval, overviewMetricsArrayList, esBucket);
            }
        }
        return overviewMetricsArrayList;
    }

    private void handleESBucket(List<String> metricsTypes, String interval, List<GatewayOverviewMetrics> overviewMetricsArrayList, ESBucket esBucket) {
        Long timeStamp = Long.valueOf(esBucket.getUnusedMap().get(KEY).toString());

        for (String metricsType : metricsTypes) {
            String aggKey = GatewayMetricsTypeEnum.type2AggKey(metricsType);
            if (StringUtils.isBlank(aggKey)) {
                continue;
            }
            double value;
            if (MetricsUtils.needConvertUnit(aggKey)) {
                value = MetricsUtils.getDoubleValuePerMin(interval, esBucket.getUnusedMap().get(aggKey).toString());
            } else {
                value = MetricsUtils.getAggMapDoubleValue(esBucket, aggKey);
            }
            MetricsContentCell contentCell = new MetricsContentCell(value, timeStamp);
            GatewayOverviewMetrics gatewayOverviewMetrics = new GatewayOverviewMetrics();
            gatewayOverviewMetrics.setType(metricsType);
            if (overviewMetricsArrayList.contains(gatewayOverviewMetrics)) {
                overviewMetricsArrayList.get(overviewMetricsArrayList.indexOf(gatewayOverviewMetrics)).addMetrics(contentCell);
            } else {
                gatewayOverviewMetrics.setMetrics(Lists.newArrayList(contentCell));
                overviewMetricsArrayList.add(gatewayOverviewMetrics);
            }
        }
    }

    private GatewayOverviewMetrics fetchSingleAggMetrics(ESQueryResponse response, GatewayMetricsTypeEnum metricsType, String interval) {
        Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap).orElse(null);
        GatewayOverviewMetrics gatewayOverviewMetrics = new GatewayOverviewMetrics();
        gatewayOverviewMetrics.setType(metricsType.getType());
        gatewayOverviewMetrics.setMetrics(Lists.newArrayList());
        if (null != esAggrMap && null != esAggrMap.get(AGG_KEY)) {
            for (ESBucket esBucket : esAggrMap.get(AGG_KEY).getBucketList()) {
                Long timeStamp = Long.valueOf(esBucket.getUnusedMap().get(KEY).toString());
                String aggKey = metricsType.getAggKey();
                Double value;
                if (MetricsUtils.needConvertUnit(aggKey)) {
                    value = MetricsUtils.getDoubleValuePerMin(interval, esBucket.getUnusedMap().get(aggKey).toString());
                } else {
                    value = Double.valueOf(esBucket.getUnusedMap().get(aggKey).toString());
                }
                MetricsContentCell metricsContentCell = new MetricsContentCell(value, timeStamp);
                gatewayOverviewMetrics.addMetrics(metricsContentCell);
            }
        }
        return gatewayOverviewMetrics;
    }

    private String getAggDsl(List<String> metricsTypes) {
        JSONObject json = new JSONObject();
        for (String metricsType : metricsTypes) {
            if (GatewayMetricsTypeEnum.QUERY_TOTAL_HITS_AVG_COUNT.getType().equals(metricsType)) {
                json.put(GatewayMetricsTypeEnum.QUERY_TOTAL_HITS_AVG_COUNT.getAggKey(), DSLSearchUtils.buildAggItem("avg", "totalHits"));
            } else if (GatewayMetricsTypeEnum.QUERY_COST_AVG.getType().equals(metricsType)) {
                json.put(GatewayMetricsTypeEnum.QUERY_COST_AVG.getAggKey(), DSLSearchUtils.buildAggItem("avg", "totalCost"));
            } else if (GatewayMetricsTypeEnum.QUERY_TOTAL_SHARDS_AVG.getType().equals(metricsType)) {
                json.put(GatewayMetricsTypeEnum.QUERY_TOTAL_SHARDS_AVG.getAggKey(), DSLSearchUtils.buildAggItem("avg", "totalShards"));
            } else if (GatewayMetricsTypeEnum.QUERY_FAILED_SHARDS_AVG.getType().equals(metricsType)) {
                json.put(GatewayMetricsTypeEnum.QUERY_FAILED_SHARDS_AVG.getAggKey(), DSLSearchUtils.buildAggItem("avg", "failedShards"));
            } else if (GatewayMetricsTypeEnum.WRITE_TOTAL_COST.getType().equals(metricsType)) {
                json.put(GatewayMetricsTypeEnum.WRITE_TOTAL_COST.getAggKey(), DSLSearchUtils.buildAggItem("avg", "totalCost"));
            } else if (GatewayMetricsTypeEnum.WRITE_RESPONSE_LEN.getType().equals(metricsType)) {
                json.put(GatewayMetricsTypeEnum.WRITE_RESPONSE_LEN.getAggKey(), DSLSearchUtils.buildAggItem("avg", "responseLen"));
            } else if (GatewayMetricsTypeEnum.DSL_LEN.getType().equals(metricsType)) {
                json.put(GatewayMetricsTypeEnum.DSL_LEN.getAggKey(), DSLSearchUtils.buildAggItem("avg", "dslLen"));
            }
        }
        return json.toString();
    }
}

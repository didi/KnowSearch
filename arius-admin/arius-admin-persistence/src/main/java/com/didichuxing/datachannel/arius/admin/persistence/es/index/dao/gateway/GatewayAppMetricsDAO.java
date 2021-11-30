package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContentCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.GatewayMetricsTypeEnum;
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
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@NoArgsConstructor
public class GatewayAppMetricsDAO extends BaseESDAO {

    private static final String TYPE = "type";
    private static final String AGG_KEY_TIMESTAMP = "group_by_timeStamp";
    private static final String AGG_KEY_APP = "group_by_app";
    private static final String KEY = "key";
    private String indexName;

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusStatsGatewayInfo();
    }


    /**
     * 获取某个字段分布.(searchCost，totalCost)  topN
     */
    public List<VariousLineChartMetrics> getAggFieldByRange(Long startTime, Long endTime, List<String> metricsTypes, Integer topNu) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_APP_FIELD, startTime, endTime, interval);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchFieldAggMetrics(response, metricsTypes, topNu, interval), 3);
    }

    /**
     * 获取某个字段分布.(searchCost，totalCost) by appId
     */
    public List<VariousLineChartMetrics> getAggFieldByRange(Long startTime, Long endTime, List<String> metricsTypes, String appId) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_APP_FIELD_BY_APPID, appId, startTime, endTime, interval);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchFieldByAppIdAggMetrics(response, metricsTypes, appId, interval), 3);
    }

    /**
     * 获取各App查询量 count  topN
     */
    public VariousLineChartMetrics getAggAppCountByRange(Long startTime, Long endTime, Integer topNu) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_APP_FIELD_COUNT, startTime, endTime, interval);
        List<VariousLineChartMetrics> variousLineChartMetrics = gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) ->
                fetchFieldAggMetrics(response, Lists.newArrayList(GatewayMetricsTypeEnum.QUERY_APP_COUNT.getType()), topNu, interval), 3);
        return variousLineChartMetrics.get(0);
    }

    /**
     * 获取各App查询量 count by appId
     */
    public VariousLineChartMetrics getAggAppCountByRange(Long startTime, Long endTime, String appId) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_APP_FIELD_COUNT_BY_APPID, appId, startTime, endTime, interval);
        List<VariousLineChartMetrics> variousLineChartMetrics = gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) ->
                fetchFieldByAppIdAggMetrics(response, Lists.newArrayList(GatewayMetricsTypeEnum.QUERY_APP_COUNT.getType()), appId, interval), 3);
        return variousLineChartMetrics.get(0);
    }



    private List<VariousLineChartMetrics> fetchFieldAggMetrics(ESQueryResponse response, List<String> metricsTypes, Integer topNu, String interval) {
        List<VariousLineChartMetrics> list = Lists.newArrayList();
        for (String metricsType : metricsTypes) {
            VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
            variousLineChartMetrics.setType(metricsType);
            variousLineChartMetrics.setMetricsContents(Lists.newArrayList());
            list.add(variousLineChartMetrics);
            Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap).orElse(null);
            if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_APP)) {
                handleBucketList(interval, metricsType, variousLineChartMetrics, esAggrMap);

                //根据第一个时间点的值进行倒排，取topNu
                List<MetricsContent> sortedList = variousLineChartMetrics.getMetricsContents().stream()
                        .sorted(Comparator.comparing(x -> x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder()))
                        .limit(topNu)
                        .collect(Collectors.toList());
                variousLineChartMetrics.setMetricsContents(sortedList);
            }

        }

        return list;
    }

    private void handleBucketList(String interval, String metricsType, VariousLineChartMetrics variousLineChartMetrics, Map<String, ESAggr> esAggrMap) {
        for (ESBucket esBucket : esAggrMap.get(AGG_KEY_APP).getBucketList()) {
            String appId = esBucket.getUnusedMap().get(KEY).toString();
            MetricsContent metricsContent = new MetricsContent();
            metricsContent.setName(appId);
            metricsContent.setMetricsContentCells(Lists.newArrayList());
            variousLineChartMetrics.getMetricsContents().add(metricsContent);
            if (null != esBucket.getAggrMap() && null != esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP)) {
                for (ESBucket bucket : esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP).getBucketList()) {
                    Long timeStamp = Long.valueOf(bucket.getUnusedMap().get(KEY).toString());
                    String aggKey = GatewayMetricsTypeEnum.type2AggKey(metricsType);
                    Double value;
                    if (aggKey.endsWith("_count")) {
                        value = MetricsUtils.getDoubleValuePerMin(interval, bucket.getUnusedMap().get(aggKey).toString());
                    } else {
                        value = MetricsUtils.getAggMapDoubleValue(bucket, aggKey);
                    }
                    metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
                }
            }
        }
    }

    private List<VariousLineChartMetrics> fetchFieldByAppIdAggMetrics(ESQueryResponse response, List<String> metricsTypes, String appId, String interval) {
        List<VariousLineChartMetrics> vos = Lists.newArrayList();
        for (String metricsType : metricsTypes) {
            VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
            variousLineChartMetrics.setType(metricsType);

            MetricsContent metricsContent = new MetricsContent();
            metricsContent.setName(appId);
            metricsContent.setMetricsContentCells(Lists.newArrayList());

            variousLineChartMetrics.setMetricsContents(Lists.newArrayList(metricsContent));
            vos.add(variousLineChartMetrics);

            Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap).orElse(null);
            if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_TIMESTAMP)) {
                for (ESBucket esBucket : esAggrMap.get(AGG_KEY_TIMESTAMP).getBucketList()) {
                    Long timeStamp = Long.valueOf(esBucket.getUnusedMap().get(KEY).toString());
                    Double value;
                    String aggKey = GatewayMetricsTypeEnum.type2AggKey(metricsType);
                    if (aggKey.endsWith("_count")) {
                        value = MetricsUtils.getDoubleValuePerMin(interval, esBucket.getUnusedMap().get(aggKey).toString());
                    } else {
                        value = MetricsUtils.getAggMapDoubleValue(esBucket, aggKey);
                    }
                    metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
                }
            }
        }

        return vos;
    }

}

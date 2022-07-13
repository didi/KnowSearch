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
public class GatewayDslMetricsDAO extends BaseESDAO {

    private static final String TYPE              = "type";
    private static final String AGG_KEY_TIMESTAMP = "group_by_timeStamp";
    private static final String AGG_KEY_DSL       = "group_by_dsl";
    private static final String KEY               = "key";
    private String              indexName;

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusStatsGatewayInfo();
    }

    /**
     * 获取各查询模版访问量 count  topN
     */
    public VariousLineChartMetrics getDslCountByRange(Long startTime, Long endTime, Integer topNu, Integer projectId) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_DSL_COUNT, startTime, endTime,
            projectId, interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchDslResult(response, GatewayMetricsTypeEnum.QUERY_DSL_COUNT, topNu,
                interval),
            3);
    }

    /**
     * 获取某个查询模版访问量 count by dslTemplateMd5
     */
    public VariousLineChartMetrics getDslCountByRangeAndMd5(Long startTime, Long endTime, String dslMd5,
                                                            Integer projectId) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_DSL_COUNT_BY_MD5, dslMd5, startTime,
            endTime, projectId, interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchDslResultByMd5(response, GatewayMetricsTypeEnum.QUERY_DSL_COUNT, dslMd5,
                interval),
            3);
    }

    /**
     * 获取各个查询模版访问耗时  topN
     */
    public VariousLineChartMetrics getDslTotalCostByRange(Long startTime, Long endTime, Integer topNu,
                                                          Integer projectId) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_DSL_TOTAL_COST, startTime, endTime,
            projectId, interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchDslResult(response, GatewayMetricsTypeEnum.QUERY_DSL_TOTAL_COST, topNu,
                interval),
            3);
    }

    /**
     * 获取某个查询模版访问耗时  by dslTemplateMd5 GET_GATEWAY_DSLMD5_BY_PROJECT_ID
     */
    public VariousLineChartMetrics getDslTotalCostByRangeAndMd5(Long startTime, Long endTime, String dslMd5,
                                                                Integer projectId) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_DSL_TOTAL_COST_BY_MD5, dslMd5,
            startTime, endTime, projectId, interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchDslResultByMd5(response, GatewayMetricsTypeEnum.QUERY_DSL_TOTAL_COST,
                dslMd5, interval),
            3);
    }

    /**
     * 获取某个projectId下的dslTemplateMd5
     */
    public List<String> getDslMd5List(Long startTime, Long endTime, Integer projectId) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_DSL_MD5_BY_PROJECT_ID, projectId,
            startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> {
            List<String> list = Lists.newArrayList();
            Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
                .orElse(null);
            if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_DSL)) {
                for (ESBucket esBucket : esAggrMap.get(AGG_KEY_DSL).getBucketList()) {
                    String dslMd5 = esBucket.getUnusedMap().get(KEY).toString();
                    list.add(dslMd5);
                }
            }
            return list;
        }, 3);
    }

    private VariousLineChartMetrics fetchDslResult(ESQueryResponse response,
                                                   GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Integer topNu,
                                                   String interval) {
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(gatewayMetricsTypeEnum.getType());
        variousLineChartMetrics.setMetricsContents(Lists.newArrayList());

        Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
            .orElse(null);
        if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_DSL)) {
            for (ESBucket esBucket : esAggrMap.get(AGG_KEY_DSL).getBucketList()) {
                String dslMd5 = esBucket.getUnusedMap().get(KEY).toString();
                MetricsContent metricsContent = new MetricsContent();
                metricsContent.setName(dslMd5);
                metricsContent.setMetricsContentCells(Lists.newArrayList());
                variousLineChartMetrics.getMetricsContents().add(metricsContent);
                if (null != esBucket.getAggrMap() && null != esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP)) {
                    handleBucketList(gatewayMetricsTypeEnum, interval, esBucket, metricsContent);
                }
            }

            // 根据第一个时间点的值进行倒排，取topNu
            List<MetricsContent> sortedList = variousLineChartMetrics.getMetricsContents().stream()
                .sorted(
                    Comparator.comparing(x -> x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder()))
                .limit(topNu).collect(Collectors.toList());
            variousLineChartMetrics.setMetricsContents(sortedList);
        }

        return variousLineChartMetrics;
    }

    private void handleBucketList(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String interval, ESBucket esBucket,
                                  MetricsContent metricsContent) {
        for (ESBucket bucket : esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP).getBucketList()) {
            Long timeStamp = Long.valueOf(bucket.getUnusedMap().get(KEY).toString());
            String aggKey = gatewayMetricsTypeEnum.getAggKey();
            Double value;
            if (MetricsUtils.needConvertUnit(aggKey)) {
                value = MetricsUtils.getDoubleValuePerMin(interval, bucket.getUnusedMap().get(aggKey).toString());
            } else {
                value = MetricsUtils.getAggMapDoubleValue(bucket, aggKey);
            }
            metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
        }
    }

    private VariousLineChartMetrics fetchDslResultByMd5(ESQueryResponse response,
                                                        GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String dslMd5,
                                                        String interval) {
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(gatewayMetricsTypeEnum.getType());

        MetricsContent metricsContent = new MetricsContent();
        metricsContent.setName(dslMd5);
        metricsContent.setMetricsContentCells(Lists.newArrayList());

        variousLineChartMetrics.setMetricsContents(Lists.newArrayList(metricsContent));

        Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
            .orElse(null);
        if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_TIMESTAMP)) {
            for (ESBucket esBucket : esAggrMap.get(AGG_KEY_TIMESTAMP).getBucketList()) {
                Long timeStamp = Long.valueOf(esBucket.getUnusedMap().get(KEY).toString());
                String aggKey = gatewayMetricsTypeEnum.getAggKey();
                Double value;
                if (MetricsUtils.needConvertUnit(aggKey)) {
                    value = MetricsUtils.getDoubleValuePerMin(interval, esBucket.getUnusedMap().get(aggKey).toString());
                } else {
                    value = MetricsUtils.getAggMapDoubleValue(esBucket, aggKey);
                }
                metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
            }
        }

        return variousLineChartMetrics;
    }

}
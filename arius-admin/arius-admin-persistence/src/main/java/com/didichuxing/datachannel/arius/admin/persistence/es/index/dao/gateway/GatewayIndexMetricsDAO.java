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
public class GatewayIndexMetricsDAO extends BaseESDAO {

    private static final String TYPE              = "type";
    private static final String AGG_KEY_TIMESTAMP = "group_by_timeStamp";
    private static final String AGG_KEY_TEMPLATE  = "group_by_template";
    private static final String KEY               = "key";
    private String              indexName;

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusStatsGatewayInfo();
    }

    /**
     * 获取各索引写入. topNu
     */
    public List<VariousLineChartMetrics> getWriteIndex(List<String> metricsTypes, Long startTime, Long endTime,
                                                       Integer projectId, Integer topNu) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_INDEX_WRITE, projectId, startTime,
            endTime, interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldAggMetrics(response, metricsTypes, topNu, interval), 3);
    }

    /**
     * 获取某个索引写入 by templateName
     */
    public List<VariousLineChartMetrics> getWriteIndexByTemplateName(List<String> metricsTypes, Long startTime,
                                                                     Long endTime, Integer projectId,
                                                                     String templateName) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_INDEX_WRITE_BY_TEMPLATE_NAME,
            projectId, templateName, startTime, endTime, interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldByTemplateAggMetrics(response, metricsTypes, templateName,
                interval),
            3);
    }

    /**
     * 获取各索引查询. topNu
     */
    public List<VariousLineChartMetrics> getSearchIndex(List<String> metricsTypes, Long startTime, Long endTime,
                                                        Integer projectId, Integer topNu) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_INDEX_SEARCH, projectId, startTime,
            endTime, interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldAggMetrics(response, metricsTypes, topNu, interval), 3);
    }

    /**
     * 获取某个索引查询 by templateName
     */
    public List<VariousLineChartMetrics> getSearchIndexByTemplateName(List<String> metricsTypes, Long startTime,
                                                                      Long endTime, Integer projectId,
                                                                      String templateName) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_INDEX_SEARCH_BY_TEMPLATE_NAME,
            projectId, templateName, startTime, endTime, interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldByTemplateAggMetrics(response, metricsTypes, templateName,
                interval),
            3);
    }

    private List<VariousLineChartMetrics> fetchFieldAggMetrics(ESQueryResponse response, List<String> metricsTypes,
                                                               Integer topNu, String interval) {
        List<VariousLineChartMetrics> list = Lists.newArrayList();
        for (String metricsType : metricsTypes) {
            VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
            variousLineChartMetrics.setType(metricsType);
            variousLineChartMetrics.setMetricsContents(Lists.newArrayList());
            list.add(variousLineChartMetrics);
            Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
                .orElse(null);
            if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_TEMPLATE)) {
                handleBucketList(interval, metricsType, variousLineChartMetrics, esAggrMap);

                //根据第一个时间点的值进行倒排，取topNu
                List<MetricsContent> sortedList = variousLineChartMetrics
                    .getMetricsContents().stream().sorted(Comparator
                        .comparing(x -> x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder()))
                    .limit(topNu).collect(Collectors.toList());
                variousLineChartMetrics.setMetricsContents(sortedList);
            }

        }

        return list;
    }

    private void handleBucketList(String interval, String metricsType, VariousLineChartMetrics variousLineChartMetrics,
                                  Map<String, ESAggr> esAggrMap) {
        for (ESBucket esBucket : esAggrMap.get(AGG_KEY_TEMPLATE).getBucketList()) {
            String indexTemplate = esBucket.getUnusedMap().get(KEY).toString();
            MetricsContent metricsContent = new MetricsContent();
            metricsContent.setName(indexTemplate);
            metricsContent.setMetricsContentCells(Lists.newArrayList());
            variousLineChartMetrics.getMetricsContents().add(metricsContent);
            if (null != esBucket.getAggrMap() && null != esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP)) {
                for (ESBucket bucket : esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP).getBucketList()) {
                    Long timeStamp = Long.valueOf(bucket.getUnusedMap().get(KEY).toString());
                    String aggKey = GatewayMetricsTypeEnum.type2AggKey(metricsType);
                    Double value;
                    if (MetricsUtils.needConvertUnit(aggKey)) {
                        value = MetricsUtils.getDoubleValuePerMin(interval,
                            bucket.getUnusedMap().get(aggKey).toString());
                    } else {
                        value = MetricsUtils.getAggMapDoubleValue(bucket, aggKey);
                    }
                    metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
                }
            }
        }
    }

    private List<VariousLineChartMetrics> fetchFieldByTemplateAggMetrics(ESQueryResponse response,
                                                                         List<String> metricsTypes, String templateName,
                                                                         String interval) {
        List<VariousLineChartMetrics> list = Lists.newArrayList();
        for (String metricsType : metricsTypes) {
            VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
            variousLineChartMetrics.setType(metricsType);

            MetricsContent metricsContent = new MetricsContent();
            metricsContent.setName(templateName);
            metricsContent.setMetricsContentCells(Lists.newArrayList());

            variousLineChartMetrics.setMetricsContents(Lists.newArrayList(metricsContent));
            list.add(variousLineChartMetrics);

            Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
                .orElse(null);
            if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_TIMESTAMP)) {
                for (ESBucket esBucket : esAggrMap.get(AGG_KEY_TIMESTAMP).getBucketList()) {
                    Long timeStamp = Long.valueOf(esBucket.getUnusedMap().get(KEY).toString());
                    String aggKey = GatewayMetricsTypeEnum.type2AggKey(metricsType);
                    Double value;
                    if (MetricsUtils.needConvertUnit(aggKey)) {
                        value = MetricsUtils.getDoubleValuePerMin(interval,
                            esBucket.getUnusedMap().get(aggKey).toString());
                    } else {
                        value = MetricsUtils.getAggMapDoubleValue(esBucket, aggKey);
                    }
                    metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
                }
            }
        }
        return list;
    }

}
package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContentCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.GatewayMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
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
public class GatewayIndexMetricsDAO extends BaseTopNMetricsDAO {
    private static final String TYPE = "type";
    private static final String  AGG_KEY_TIMESTAMP         = "group_by_timeStamp";
    private static final String  AGG_KEY_TEMPLATE          = "group_by_template";
    private static final String  DEST_TEMPLATE_NAME        = "destTemplateName";
    private static final String  TOTAL_COST                = "totalCost";
    private static final String  TOTAL_COST_AVG            = "total_cost_avg";
    private static final String  AVG                       = "avg";
    private static final String  QUERY_REQUEST             = "queryRequest";
    private static final boolean QUERY_REQUEST_VALUE_TRUE  = true;
    private static final boolean QUERY_REQUEST_VALUE_FALSE = false;
    private static final String  KEY                       = "key";
    private static final String  TERMS                     = "terms";
    private static final String  TERM                      = "term";
    private static final String  EXISTS                    = "exists";
    private static final String  FIELD                     = "field";
    private static final Long    AGG_TERMS_SIZE            = 5000L;
    
    @PostConstruct
    public void init() {
        super.indexName = dataCentreUtil.getAriusStatsGatewayInfo();
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
    
    @Override
    public List<VariousLineChartMetrics> fetchTopMetric(GatewayMetricsTypeEnum metricsType, Long startTime,
                                                        Long endTime, Integer topNu, Integer projectId,
                                                        String destTemplateName) throws AdminOperateException {
        
        if (Objects.isNull(buildQueryRequestFragment(metricsType))) {
            return Collections.emptyList();
        }
        //获取第一个时间点的dsl 片段
        String queryDslByHasDataTime = "," + buildQueryRequestFragment(metricsType);
        String queryDslByFiled = queryDslByHasDataTime + buildTermByProjectId(projectId);
        //构建存在第一个时间位点的dsl
        String hasDataTimeDsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_HAS_METRIC_INFO_TIME_BY_FIELD,
                startTime, endTime, queryDslByFiled);
       
              
        // 注意这里不用MetricsUtils.getIntervalForDashBoard
        String interval = MetricsUtils.Interval.FIVE_MIN.getStr();
        Object[] param = new Object[] { queryDslByFiled, AGG_KEY_TEMPLATE, DEST_TEMPLATE_NAME, AGG_TERMS_SIZE, interval,
                                        TOTAL_COST_AVG, AVG, TOTAL_COST };
        
        return this.performFetchTopMetric(metricsType, startTime, endTime,
                (ESQueryResponse response) -> fetchFieldAggMetrics(response, metricsType, topNu, interval),
                hasDataTimeDsl, destTemplateName, param);
    }
    
    @Override
    public List<MetricsContent> getByRangeTopN(List<String> values, GatewayMetricsTypeEnum metricsType, Long startTime,
                                               Long endTime, Integer projectId) throws AdminOperateException {
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        final String valueInclude = new JSONArray().fluentAddAll(values).toJSONString();
        String queryDslByFiled =
                buildTermsByDslTemplateMd5AndQueryRequest(values, metricsType) + buildTermByProjectId(projectId);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        Object[] param = new Object[] { startTime, endTime, queryDslByFiled, AGG_KEY_TEMPLATE, DEST_TEMPLATE_NAME,
                                        values.size(), valueInclude, interval, startTime, endTime, TOTAL_COST_AVG, AVG,
                                        TOTAL_COST };
        return this.performGetByRangeTopN(metricsType, startTime, endTime,
                (ESQueryResponse response) -> fetchDslResultByFiled(response, metricsType, interval), param);
    }
    
    @Override
    public List<VariousLineChartMetrics> checkMetricsValue(GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                                           String metricsValue) {
        // 当该指标存在时候，直接进行返回
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
    
    private List<MetricsContent> fetchDslResultByFiled(ESQueryResponse response, GatewayMetricsTypeEnum metricsType,
                                                       String interval) {
        Optional<List<ESBucket>> esBucketOptional = Optional.ofNullable(response).map(ESQueryResponse::getAggs)
                .map(ESAggrMap::getEsAggrMap)
                //确定map不为空/null
                .filter(MapUtils::isNotEmpty)
                //确定含有key
                .filter(esAggrMap -> esAggrMap.containsKey(AGG_KEY_TEMPLATE))
                .map(esAggrMap -> esAggrMap.get(AGG_KEY_TEMPLATE).getBucketList())
                //确定buckets不为空
                .filter(CollectionUtils::isNotEmpty);
        List<MetricsContent> metricsContents = Lists.newArrayList();
        String aggKey = GatewayMetricsTypeEnum.type2AggKey(metricsType.getType());
        if (esBucketOptional.isPresent()) {
            for (ESBucket esBucket : esBucketOptional.get()) {            //group_by_dsl 的key值
                String indexTemplate = esBucket.getUnusedMap().get(KEY).toString();
                MetricsContent metricsContent = new MetricsContent();
                metricsContent.setName(indexTemplate);
                metricsContent.setMetricsContentCells(
                        Lists.newArrayList());            //group_by_timeStamp 的buckets的结果值
                if (esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP).getBucketList() != null) {
                    for (ESBucket groupByTimeStampBucket : esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP)
                            .getBucketList()) {
                        Long timeStamp = Long.valueOf(groupByTimeStampBucket.getUnusedMap().get(KEY).toString());
                        Double value;
                        if (MetricsUtils.needConvertUnit(aggKey)) {
                            value = MetricsUtils.getDoubleValuePerMin(interval,
                                    groupByTimeStampBucket.getUnusedMap().get(aggKey).toString());
                        } else {
                            value = MetricsUtils.getAggMapDoubleValue(groupByTimeStampBucket, aggKey);
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
                                } else {
                                    value = Optional.ofNullable(
                                                    ((JSONObject) groupByTimeStampBucket).getJSONObject(aggKey).get(VALUE))
                                            .map(d -> Double.valueOf(d.toString())).orElse(0.0);
                                }
                                metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
                            });
                }
                metricsContents.add(metricsContent);
            }
        }
        return metricsContents;
    }
    
    /**
     * <B>write</B>
     * <pre>
     *      ,{
     *           "term": {
     *             "queryRequest": false
     *           }
     *         },
     *         {
     *           "terms": {
     *             "destTemplateName": [
     *               "VALUE1",
     *               "VALUE2"
     *             ]
     *           }
     *         }
     *
     *
     * </pre>  or
     * <B>search</B>
     * <pre>
     *     ,
     *      {
     *           "term": {
     *             "queryRequest": true
     *           }
     *         },
     *         {
     *           "terms": {
     *             "destTemplateName": [
     *               "VALUE1",
     *               "VALUE2"
     *             ]
     *           }
     *         }
     * </pre>
     * @param destTemplateNames
     * {@linkplain GatewayIndexMetricsDAO DEST_TEMPLATE_NAME}的values
     * @param metricsType {@link GatewayMetricsTypeEnum}
     * * @return {@link String}
     */
    private String buildTermsByDslTemplateMd5AndQueryRequest(List<String> destTemplateNames,
                                                             GatewayMetricsTypeEnum metricsType) {
        String queryRequestFragment = buildQueryRequestFragment(metricsType);
        if (Objects.isNull(queryRequestFragment)) {
            return null;
        }
        return String.format(",%s,%s", queryRequestFragment,
                new JSONObject().fluentPut(TERMS, new JSONObject().fluentPut(DEST_TEMPLATE_NAME, destTemplateNames))
                        .toJSONString());
    }
    
    /**
     * 建立查询请求片段
     * <pre>
     *     {
     *     "term": {
     *       "queryRequest": false
     *     }
     *   }
     * </pre>
     * or
     * <pre>
     *     {
     *     "term": {
     *       "queryRequest": false
     *     }
     *   }
     * </pre>
     * @param metricsType 指标类型
     * @return {@link String}
     */
    private String buildQueryRequestFragment(GatewayMetricsTypeEnum metricsType) {
        switch (metricsType) {
            //wirte
            // queryRequest  fasle
            case WRITE_INDEX_COUNT:
            case WRITE_INDEX_TOTAL_COST:
                return new JSONObject().fluentPut(TERM,
                        new JSONObject().fluentPut(QUERY_REQUEST, QUERY_REQUEST_VALUE_FALSE)).toJSONString();
            //search  queryRequest true
            case SEARCH_INDEX_COUNT:
            case SEARCH_INDEX_TOTAL_COST:
                return new JSONObject().fluentPut(TERM,
                        new JSONObject().fluentPut(QUERY_REQUEST, QUERY_REQUEST_VALUE_TRUE)).toJSONString();
            default:
                return null;
        }
    }
    
    
    
    private List<VariousLineChartMetrics> fetchFieldAggMetrics(ESQueryResponse response,
                                                               GatewayMetricsTypeEnum metricsType, Integer topNu,
                                                               String interval) {
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(metricsType.getType());
        variousLineChartMetrics.setMetricsContents(Lists.newArrayList());
        Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
                .orElse(null);
        if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_TEMPLATE)) {
            handleBucketList(interval, metricsType.getType(), variousLineChartMetrics, esAggrMap);
        }
        //根据第一个时间点的值进行倒排，取topNu
        List<MetricsContent> sortedList = variousLineChartMetrics.getMetricsContents().stream()
                .sorted(Comparator.nullsLast(Comparator.comparing(
                        x -> CollectionUtils.isEmpty(x.getMetricsContentCells())
                                ? -1
                                : x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder())))
                .limit(topNu).collect(Collectors.toList());
        variousLineChartMetrics.setMetricsContents(sortedList);
        return Collections.singletonList(variousLineChartMetrics);
    }
    
    @Override
    protected String getFinalDslByOneStep(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Object[] args) {
        return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOTAL_COST_BY_FIELD, args);
    }
    
    @Override
    protected String getFinalDslBySecondStep(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Object[] args) {
        return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOTAL_COST_EXTENDED_BOUNDS_BY_FIELD, args);
    }
   
    
    
    

}
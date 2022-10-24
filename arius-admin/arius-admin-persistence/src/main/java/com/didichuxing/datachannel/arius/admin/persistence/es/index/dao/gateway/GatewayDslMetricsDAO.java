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
public class GatewayDslMetricsDAO extends BaseTopNMetricsDAO {
    private static final String TYPE = "type";
    private static final String AGG_KEY_TIMESTAMP = "group_by_timeStamp";
    private static final String AGG_KEY_DSL       = "group_by_dsl";
    private static final String KEY               = "key";
    private static final String EXISTS            = "exists";
    private static final String FIELD             = "field";
    private static final String EMPTY_STR         = "";
    private static final Long   AGG_TERMS_SIZE    = 1000L;
    private static final String TERMS             = "terms";
    private static final String DSL_TEMPLATE_MD5  = "dslTemplateMd5";
    private static final String TOTAL_COST        = "totalCost";
    private static final String TOTAL_COST_AVG    = "total_cost_avg";
    private static final String AVG               = "avg";
    
    @PostConstruct
    public void init() {
        super.indexName = dataCentreUtil.getAriusStatsGatewayInfo();
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
    
    @Override
    protected String getFinalDslByOneStep(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Object[] args) {
        switch (gatewayMetricsTypeEnum) {
            case QUERY_DSL_COUNT:
                return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_COUNT_BY_FILELD, args);
            case QUERY_DSL_TOTAL_COST:
                return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOTAL_COST_BY_FIELD, args);
            default:
                return null;
        }
    }
    
    @Override
    protected String getFinalDslBySecondStep(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Object[] args) {
        if (gatewayMetricsTypeEnum.equals(GatewayMetricsTypeEnum.QUERY_DSL_COUNT)) {
            return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_COUNT_EXTENDED_BOUNDS_BY_FILELD, args);
        } else if (gatewayMetricsTypeEnum.equals(GatewayMetricsTypeEnum.QUERY_DSL_TOTAL_COST)) {
            return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOTAL_COST_EXTENDED_BOUNDS_BY_FIELD, args);
        }
        return null;
    }
    
    @Override
    public List<VariousLineChartMetrics> fetchTopMetric(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime,
                                                        Long endTime, Integer topNu, Integer projectId, String dslMd5)
            throws AdminOperateException {
        //构建has data time的第一个时间片段
        String queryDslFragmentByHasDataTime =  buildTermByProjectId(projectId);
        //构建has data time最终的query dsl
        String queryDslByHasDataTime = dslLoaderUtil.getFormatDslByFileName(
                DslsConstant.GET_HAS_METRIC_INFO_TIME_BY_FIELD, startTime, endTime, queryDslFragmentByHasDataTime);
        //获取query filter terms 片段
        String queryDslFragment = buildTermByProjectId(projectId);
        String interval = MetricsUtils.Interval.FIVE_MIN.getStr();
        Object[] param;
        switch (gatewayMetricsTypeEnum) {
            case QUERY_DSL_COUNT:
                param = new Object[] { queryDslFragment, AGG_KEY_DSL, DSL_TEMPLATE_MD5, AGG_TERMS_SIZE, interval };
                break;
            case QUERY_DSL_TOTAL_COST:
                param = new Object[] { queryDslFragment, AGG_KEY_DSL, DSL_TEMPLATE_MD5, AGG_TERMS_SIZE, interval,
                                       TOTAL_COST_AVG, AVG, TOTAL_COST };
                break;
            default:
                return Collections.emptyList();
        }
        return this.performFetchTopMetric(gatewayMetricsTypeEnum, startTime, endTime,
                (ESQueryResponse response) -> Collections.singletonList(
                        fetchDslResult(response, gatewayMetricsTypeEnum, topNu, interval)), queryDslByHasDataTime,
                dslMd5, param);
    }
   
    
    /**
     * <pre>     ,
     * {
     *           "terms": {
     *             "dslTemplateMd5": [
     *               "VALUE1",
     *               "VALUE2"
     *             ]
     *           }
     *         }
     *
     * </pre>
     * @param names
     * {@linkplain GatewayDslMetricsDAO DSL_TEMPLATE_MD5} 的 value 值 * @return str
     */
    private String buildTermsByDslTemplateMd5Field(List<String> names) {
        return String.format(",%s",
                new JSONObject().fluentPut(TERMS, new JSONObject().fluentPut(DSL_TEMPLATE_MD5, names)).toJSONString());
    }
    @Override
    public List<MetricsContent> getByRangeTopN(List<String> values, GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                               Long startTime, Long endTime, Integer projectId)
            throws AdminOperateException {
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String queryDslFragment = buildTermsByDslTemplateMd5Field(values) + buildTermByProjectId(projectId);
        final String valueInclude = new JSONArray().fluentAddAll(values).toJSONString();
        Object[] param;
        switch (gatewayMetricsTypeEnum) {
            case QUERY_DSL_COUNT:
                param = new Object[] { startTime, endTime, queryDslFragment, AGG_KEY_DSL, DSL_TEMPLATE_MD5,
                                       values.size(), valueInclude, interval, startTime, endTime };
                break;
            case QUERY_DSL_TOTAL_COST:
                param = new Object[] { startTime, endTime, queryDslFragment, AGG_KEY_DSL, DSL_TEMPLATE_MD5,
                                       values.size(), valueInclude, interval, startTime, endTime, TOTAL_COST_AVG, AVG,
                                       TOTAL_COST };
                break;
            default:
                return Collections.emptyList();
        }
        return performGetByRangeTopN(gatewayMetricsTypeEnum, startTime, endTime,
                (ESQueryResponse response) -> fetchDslResultByFiled(response, gatewayMetricsTypeEnum, interval), param);
    }
    
    private List<MetricsContent> fetchDslResultByFiled(ESQueryResponse response,
                                                       GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String interval) {
        Optional<List<ESBucket>> esBucketOptional = Optional.ofNullable(response).map(ESQueryResponse::getAggs)
                .map(ESAggrMap::getEsAggrMap)
                //确定map不为空/null
                .filter(MapUtils::isNotEmpty)
                //确定含有key
                .filter(esAggrMap -> esAggrMap.containsKey(AGG_KEY_DSL))
                .map(esAggrMap -> esAggrMap.get(AGG_KEY_DSL).getBucketList())
                //确定buckets不为空
                .filter(CollectionUtils::isNotEmpty);
        List<MetricsContent> metricsContents = Lists.newArrayList();
        String aggKey = gatewayMetricsTypeEnum.getAggKey();
        if (esBucketOptional.isPresent()) {
            for (ESBucket esBucket : esBucketOptional.get()) {
                //group_by_dsl 的key值
                String fieldByValue = esBucket.getUnusedMap().get(KEY).toString();
                MetricsContent metricsContent = new MetricsContent();
                metricsContent.setName(fieldByValue);
                metricsContent.setMetricsContentCells(Lists.newArrayList());
                //group_by_timeStamp 的buckets的结果值
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
    
}
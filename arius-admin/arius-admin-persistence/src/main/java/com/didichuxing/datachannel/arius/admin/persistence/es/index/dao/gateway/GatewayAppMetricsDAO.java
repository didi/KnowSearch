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
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.aggs.ESBucket;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class GatewayAppMetricsDAO extends BaseTopNMetricsDAO {
    private static final String TYPE = "type";
    private static final String  AGG_KEY_TIMESTAMP = "group_by_timeStamp";
    private static final String  AGG_KEY_APP       = "group_by_app";
    private static final String  KEY               = "key";
    private static final String AVG         = "avg";
    
    private static final String SEARCH_COST = "searchCost";
    private static final String  TOTAL_COST        = "totalCost";
    private static final Integer AGG_SIZE          = 5000;
    
    @PostConstruct
    public void init() {
        super.indexName = dataCentreUtil.getAriusStatsGatewayInfo();
    }

    /**
     * 获取某个字段分布.(searchCost，totalCost)  topN
     */
    public List<VariousLineChartMetrics> getAggFieldByRange(Long startTime, Long endTime, List<String> metricsTypes,
                                                            Integer topNu) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_APP_FIELD, startTime, endTime,
            interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldAggMetrics(response, metricsTypes, topNu, interval), 3);
    }

    /**
     * 获取某个字段分布.(searchCost，totalCost) by projectId
     */
    public List<VariousLineChartMetrics> getAggFieldByRange(Long startTime, Long endTime, List<String> metricsTypes,
                                                            String projectId) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_APP_FIELD_BY_PROJECT_ID, projectId,
            startTime, endTime, interval, startTime, endTime);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldByProjectIdAggMetrics(response, metricsTypes, projectId, interval),
            3);
    }

    /**
     * 获取各App查询量 count  topN
     */
    public VariousLineChartMetrics getProjectCountByRange(Long startTime, Long endTime, Integer topNu) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_APP_FIELD_COUNT, startTime, endTime,
            interval, startTime, endTime);
        List<VariousLineChartMetrics> variousLineChartMetrics = gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldAggMetrics(response,
                Lists.newArrayList(GatewayMetricsTypeEnum.QUERY_APP_COUNT.getType()), topNu, interval),
            3);
        return variousLineChartMetrics.get(0);
    }

    /**
     * 获取各project查询量 count by projectId
     */
    public VariousLineChartMetrics getProjectCountByRange(Long startTime, Long endTime, String projectId) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_APP_FIELD_COUNT_BY_PROJECT_ID,
            projectId, startTime, endTime, interval, startTime, endTime);
        List<VariousLineChartMetrics> variousLineChartMetrics = gatewayClient.performRequest(realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchFieldByProjectIdAggMetrics(response,
                Lists.newArrayList(GatewayMetricsTypeEnum.QUERY_APP_COUNT.getType()), projectId, interval),
            3);
        return variousLineChartMetrics.get(0);
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
            if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_APP)) {
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
        for (ESBucket esBucket : esAggrMap.get(AGG_KEY_APP).getBucketList()) {
            String projectId = esBucket.getUnusedMap().get(KEY).toString();
            MetricsContent metricsContent = new MetricsContent();
            metricsContent.setName(projectId);
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

    private List<VariousLineChartMetrics> fetchFieldByProjectIdAggMetrics(ESQueryResponse response,
                                                                          List<String> metricsTypes, String projectId,
                                                                          String interval) {
        List<VariousLineChartMetrics> vos = Lists.newArrayList();
        for (String metricsType : metricsTypes) {
            VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
            variousLineChartMetrics.setType(metricsType);

            MetricsContent metricsContent = new MetricsContent();
            metricsContent.setName(projectId);
            metricsContent.setMetricsContentCells(Lists.newArrayList());

            variousLineChartMetrics.setMetricsContents(Lists.newArrayList(metricsContent));
            vos.add(variousLineChartMetrics);

            Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
                .orElse(null);
            if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_TIMESTAMP)) {
                for (ESBucket esBucket : esAggrMap.get(AGG_KEY_TIMESTAMP).getBucketList()) {
                    Long timeStamp = Long.valueOf(esBucket.getUnusedMap().get(KEY).toString());
                    Double value;
                    String aggKey = GatewayMetricsTypeEnum.type2AggKey(metricsType);
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

        return vos;
    }
    
    public List<VariousLineChartMetrics> fetchTopMetric(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime,
                                                        Long endTime, Integer topNu, String valueProjectId)
            throws AdminOperateException {
        return fetchTopMetric(gatewayMetricsTypeEnum, startTime, endTime, topNu, null, valueProjectId);
    }
    
    @Override
    public List<VariousLineChartMetrics> fetchTopMetric(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime,
                                                        Long endTime, Integer topNu, Integer projectId, String value)
            throws AdminOperateException {
        //构建一个空的query
        String queryHasDataTimeDsl = EMPTY_STR;
        String interval = MetricsUtils.Interval.ONE_MIN.getStr();
        Object[] param;
        switch (gatewayMetricsTypeEnum) {
            case QUERY_APP_SEARCH_COST:
                param = new Object[] { queryHasDataTimeDsl, AGG_KEY_APP, PROJECT_ID, AGG_SIZE, interval,
                                       gatewayMetricsTypeEnum.getAggKey(), AVG, SEARCH_COST };
                break;
            case QUERY_APP_TOTAL_COST:
                param = new Object[] { queryHasDataTimeDsl, AGG_KEY_APP, PROJECT_ID, AGG_SIZE, interval,
                                       gatewayMetricsTypeEnum.getAggKey(), AVG, TOTAL_COST };
                break;
            case QUERY_APP_COUNT:
                param = new Object[] { queryHasDataTimeDsl, AGG_KEY_APP, PROJECT_ID, AGG_SIZE, interval };
                break;
            default:
                return Collections.emptyList();
        }
        return this.performFetchTopMetric(gatewayMetricsTypeEnum, startTime, endTime,
                (ESQueryResponse response) -> Collections.singletonList(
                        fetchFieldAggMetrics(response, gatewayMetricsTypeEnum, topNu, interval)), queryHasDataTimeDsl,
                value, param);
    }
    
    public List<MetricsContent> getByRangeTopN(List<String> values, GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                               Long startTime, Long endTime) throws AdminOperateException {
        return getByRangeTopN(values, gatewayMetricsTypeEnum, startTime, endTime, null);
    }
    
    @Override
    public List<MetricsContent> getByRangeTopN(List<String> values, GatewayMetricsTypeEnum gatewayMetricsTypeEnum,
                                               Long startTime, Long endTime, Integer projectId)
            throws AdminOperateException {
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        final List<Object> projectIds = values.stream().map(Integer::parseInt).collect(Collectors.toList());
        String queryDslFragment = buildTermsByField(PROJECT_ID, projectIds);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        final String valueInclude = new JSONArray().fluentAddAll(values).toJSONString();
        Object[] param;
        switch (gatewayMetricsTypeEnum) {
            case QUERY_APP_SEARCH_COST:
                param = new Object[] { startTime, endTime, queryDslFragment, AGG_KEY_APP, PROJECT_ID, values.size(),
                                       valueInclude, interval, startTime, endTime, gatewayMetricsTypeEnum.getAggKey(),
                                       AVG, SEARCH_COST };
                break;
            case QUERY_APP_TOTAL_COST:
                param = new Object[] { startTime, endTime, queryDslFragment, AGG_KEY_APP, PROJECT_ID, values.size(),
                                       valueInclude, interval, startTime, endTime, gatewayMetricsTypeEnum.getAggKey(),
                                       AVG, TOTAL_COST };
                break;
            case QUERY_APP_COUNT:
                param = new Object[] { startTime, endTime, queryDslFragment, AGG_KEY_APP, PROJECT_ID, values.size(),
                                       valueInclude, interval, startTime, endTime };
                break;
            default:
                return Collections.emptyList();
        }
        return performGetByRangeTopN(gatewayMetricsTypeEnum, startTime, endTime,
                response -> fetchFieldAggMetrics(response, gatewayMetricsTypeEnum.getType(), interval), param);
    }
    
    private VariousLineChartMetrics fetchFieldAggMetrics(ESQueryResponse response, GatewayMetricsTypeEnum metricsTypes,
                                                         Integer topNu, String interval) {
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(metricsTypes.getType());
        variousLineChartMetrics.setMetricsContents(Lists.newArrayList());
        Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap)
                .orElse(null);
        if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_APP)) {
            handleBucketList(interval, metricsTypes.getType(), variousLineChartMetrics, esAggrMap);
            //根据第一个时间点的值进行倒排，取topNu
            List<MetricsContent> sortedList = variousLineChartMetrics.getMetricsContents().stream()
                    .sorted(Comparator.comparing(x -> CollectionUtils.isEmpty(x.getMetricsContentCells())
                            ? -1
                            : x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder())).limit(topNu)
                    .collect(Collectors.toList());
            variousLineChartMetrics.setMetricsContents(sortedList);
        }
        return variousLineChartMetrics;
    }
    
    @Override
    protected String getFinalDslByOneStep(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Object[] args) {
        switch (gatewayMetricsTypeEnum) {
            case QUERY_APP_SEARCH_COST:
            case QUERY_APP_TOTAL_COST:
                return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOTAL_COST_BY_FIELD, args);
            case QUERY_APP_COUNT:
                return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_COUNT_BY_FILELD, args);
            default:
                return null;
        }
    }
      @Override
      protected String getFinalDslBySecondStep(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Object[] args) {
         
          switch (gatewayMetricsTypeEnum) {
              case QUERY_APP_SEARCH_COST:
              case QUERY_APP_TOTAL_COST:
                  return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOTAL_COST_EXTENDED_BOUNDS_BY_FIELD,
                          args);
              case QUERY_APP_COUNT:
                  return dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_COUNT_EXTENDED_BOUNDS_BY_FILELD,
                          args);
              default:
                  return null;
          }
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
    
    private List<MetricsContent> fetchFieldAggMetrics(ESQueryResponse response, String metricsType, String interval) {
        final Optional<List<ESBucket>> esBucketsOptional = Optional.ofNullable(response).map(ESQueryResponse::getAggs)
                .map(ESAggrMap::getEsAggrMap)
                //判断是否会存在AGG_KEY_APP
                .filter(esAggrMap -> esAggrMap.containsKey(AGG_KEY_APP))
                //获取到appke
                .map(esAggrMap -> esAggrMap.get(AGG_KEY_APP).getBucketList());
        if (!esBucketsOptional.isPresent()) {
            return Collections.emptyList();
        }
        String aggKey = GatewayMetricsTypeEnum.type2AggKey(metricsType);
        List<MetricsContent> metricsContents = Lists.newArrayList();
        for (ESBucket esBucket : esBucketsOptional.get()) {
            String projectId = esBucket.getUnusedMap().get(KEY).toString();
            MetricsContent metricsContent = new MetricsContent();
            metricsContent.setName(projectId);
            metricsContent.setMetricsContentCells(Lists.newArrayList());
            if (esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP).getBucketList() != null) {
                for (ESBucket bucket : esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP).getBucketList()) {
                    Long timeStamp = Long.valueOf(bucket.getUnusedMap().get(KEY).toString());
                    Double value;
                    if (MetricsUtils.needConvertUnit(aggKey)) {
                        value = MetricsUtils.getDoubleValuePerMin(interval,
                                bucket.getUnusedMap().get(aggKey).toString());
                    } else {
                        value = MetricsUtils.getAggMapDoubleValue(bucket, aggKey);
                    }
                    metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
                }
            } else {
                esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP).toJson().getJSONObject(BUCKETS).values()
                        .forEach(groupByTimeStampBucket -> {
                            Long timeStamp = Long.valueOf(((JSONObject) groupByTimeStampBucket).get(KEY).toString());
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
        return metricsContents;
    }
  

}
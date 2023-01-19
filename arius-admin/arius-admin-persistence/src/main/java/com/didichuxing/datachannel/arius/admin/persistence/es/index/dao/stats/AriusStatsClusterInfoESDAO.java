package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.GET_CLUSTER_STATS;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ESHttpRequestContent.GET_PENDING_TASKS;

import java.util.*;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.knowframework.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.knowframework.elasticsearch.client.response.query.query.hits.ESHit;
import com.google.common.collect.Maps;

@Component
public class AriusStatsClusterInfoESDAO extends BaseAriusStatsESDAO {

    @PostConstruct
    public void init() {
        super.indexName = dataCentreUtil.getAriusStatsClusterInfo();
        BaseAriusStatsESDAO.register(AriusStatsEnum.CLUSTER_INFO, this);
    }

    /**
     * 获取物理集群分位指标信息
     * @param clusterName         集群名称
     * @param clusterMetricsType  集群指标类型
     * @param aggType             聚合类型 avg sum min max
     * @param percentilesType     分位类型
     * @param startTime           开始时间
     * @param endTime             结束时间
     * @return
     */
    public Map<Long, Double> getAggSinglePercentilesMetrics(String clusterName,long clusterType, String clusterMetricsType,
                                                            String aggType, String percentilesType, Long startTime,
                                                            Long endTime) {
        Map<Long, Double> resultMap = Maps.newHashMap();
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval(endTime - startTime);
        try {
            String dsl = dslLoaderUtil.getFormatDslByFileName(
                DslsConstant.GET_CLUSTER_PHY_AGG_PERCENTILES_METRICS_BY_AGG_PARAM, clusterName,clusterType, percentilesType,
                startTime, endTime, interval, clusterMetricsType, aggType, clusterMetricsType);

            resultMap = gatewayClient.performRequestWithRouting(metadataClusterName, clusterName, realIndexName, TYPE,
                dsl,
                (ESQueryResponse response) -> fetchAggSinglePercentilesMetrics(response, clusterMetricsType, aggType),
                3);
        } catch (Exception e) {
            LOGGER.error(
                "class=AriusStatsClusterInfoESDAO||method=getAggSinglePercentilesMetrics||clusterName={}||clusterMetricsType={}"
                         + "percentilesType={}||startTime={}||endTime={}",
                clusterName, clusterMetricsType, percentilesType, startTime, endTime, e);
            return resultMap;
        }

        return resultMap;
    }

    /**
     * 获取物理集群其他统计信息
     */
    public <T> List<T> getAggClusterPhyMetrics(String clusterName,long clusterType, String aggType, Long startTime, Long endTime,
                                               Class<T> clazz) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        long intervalTime = endTime - startTime;

        String interval = MetricsUtils.getInterval(intervalTime);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_METRICS_BY_RANGE_AND_INTERVAL,
            clusterName,clusterType, startTime, endTime, interval, buildAggsDSL(clazz, aggType));

        return gatewayClient.performRequest(metadataClusterName, realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchAggClusterPhyMetrics(response, clazz), 3);
    }

    /**
     * 获取集群分片总数
     *
     * @param cluster 集群
     * @return {@code Long}
     */
    public Long getClustersShardTotal(String cluster) throws ESOperateException {
        Long value = null;
        int tryTimes = 3;
        do {
            value = Optional.ofNullable(this.getDirectResponse(cluster, "Get", GET_CLUSTER_STATS))
                .filter(directResponse -> directResponse.getRestStatus() == RestStatus.OK
                                          && StringUtils.isNotBlank(directResponse.getResponseContent()))
                .map(DirectResponse::getResponseContent).map(JSON::parseObject).map(json -> json.getJSONObject(INDICES))
                .map(json -> json.getJSONObject(SHARDS)).map(json -> json.getLong(TOTAL)).orElse(null);
        } while (tryTimes-- > 0 && Objects.isNull(value));

        return Objects.isNull(value) ? 0L : value;
    }

    /**
     * 获取pending task 数量
     *
     * @param cluster 集群
     * @return {@code Long}
     */
    public Long getPendingTaskTotal(String cluster) throws ESOperateException {

        Long value = null;
        int tryTimes = 3;
        do {
            value = Optional.ofNullable(this.getDirectResponse(cluster, "Get", GET_PENDING_TASKS))
                .filter(directResponse -> directResponse.getRestStatus() == RestStatus.OK
                                          && StringUtils.isNotBlank(directResponse.getResponseContent()))
                .map(DirectResponse::getResponseContent).map(JSON::parseObject).map(json -> json.getJSONArray(TASKS))
                .filter(array -> !ObjectUtils.isEmpty(array)).map(json -> (long) json.size()).orElse(null);
        } while (tryTimes-- > 0 && Objects.isNull(value));
        return Objects.isNull(value) ? 0L : value;
    }

    /************************************************private**************************************************/

    /**
     * 构建单个分位类型的指标数据
     * @param response            ES响应体
     * @param clusterMetricsType  指标类型
     * @param aggType             聚合类型
     * @return  Map<Long, Double>   time ——> value
     */
    private Map<Long, Double> fetchAggSinglePercentilesMetrics(ESQueryResponse response, String clusterMetricsType,
                                                               String aggType) {
        Map<Long, Double> timeSlip2ValueMap = Maps.newHashMap();
        if (null == response || null == response.getAggs()) {
            return timeSlip2ValueMap;
        }

        Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();
        List<Long> keys = new ArrayList<>();
        if (null != esAggrMap && null != esAggrMap.get(HIST)) {
            esAggrMap.get(HIST).getBucketList().forEach(r -> {
                //获取时间戳
                long timeSlip = 0;
                if (null != r.getUnusedMap() && null != r.getUnusedMap().get(KEY)) {
                    timeSlip = Long.valueOf(r.getUnusedMap().get(KEY).toString());
                }
                keys.add(timeSlip);
                timeSlip2ValueMap.put(timeSlip, 0d);
                //获取聚合值
                if (null != r.getAggrMap() && null != r.getAggrMap().get(clusterMetricsType)
                    && null != r.getAggrMap().get(clusterMetricsType).getUnusedMap().get(VALUE)) {
                    double aggCal = Double
                        .parseDouble(r.getAggrMap().get(clusterMetricsType).getUnusedMap().get(VALUE).toString());
                    if (aggCal > 0) {
                        timeSlip2ValueMap.put(timeSlip, aggCal);
                    }
                }
            });
        }

        return timeSlip2ValueMap;
    }

    /**
     * {
     *   "size": 1,
     *   "sort": [
     *     {
     *       "timestamp": {
     *         "order": "DESC"
     *       }
     *     }
     *   ]
     * }
     * @param cluster
     */
    public Long getTimeDifferenceBetweenNearestPointAndNow(String cluster) {
        long startTime = System.currentTimeMillis();
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, startTime);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TIME_DIFFERENCE_BETWEEN_NEAREST_POINT_AND_NOW,cluster);
        return gatewayClient.performRequest(metadataClusterName, realIndexName, TYPE, dsl,
                (ESQueryResponse response) -> getNearestTime(response), 3);
    }

    private Long getNearestTime(ESQueryResponse response) {
        if (null == response) {
            return 0L;
        }
        List<ESHit> hits = Optional.ofNullable(response.getHits().getHits()).orElse(new ArrayList<ESHit>());
        return hits.stream().map(esHit -> esHit.getSource()).filter(Objects::nonNull)
                .map(source -> ((JSONObject) source).getLong("timestamp")).findFirst().orElse(0L);
    }
}
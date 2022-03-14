package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterPhyMetricsContant.*;

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
    public Map<Long, Double> getAggSinglePercentilesMetrics(String clusterName,
                                                            String clusterMetricsType,
                                                            String aggType,
                                                            String percentilesType,
                                                            Long   startTime,
                                                            Long   endTime) {
        Map<Long, Double> resultMap = Maps.newHashMap();
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval(endTime - startTime);
        try {
            String dsl = dslLoaderUtil.getFormatDslByFileNameByAggParam(DslsConstant.GET_CLUSTER_PHY_AGG_PERCENTILES_METRICS_BY_AGG_PARAM,
                    clusterMetricsType, interval, aggType, clusterName, percentilesType, startTime, endTime);

            resultMap = gatewayClient.performRequestWithRouting(metadataClusterName, clusterName, realIndexName, TYPE, dsl,
                (ESQueryResponse response) -> fetchAggSinglePercentilesMetrics(response, clusterMetricsType, aggType),
                3);
        } catch (Exception e) {
            LOGGER.error("class=AriusStatsClusterInfoESDAO||method=getAggSinglePercentilesMetrics||clusterName={}||clusterMetricsType={}" +
                    "percentilesType={}||startTime={}||endTime={}", clusterName, clusterMetricsType, percentilesType, startTime, endTime, e);
            return resultMap;
        }

        return resultMap;
    }

    /**
     * 获取物理集群其他统计信息
     */
    public <T> List<T> getAggClusterPhyMetrics(String clusterName, String aggType, Long startTime, Long endTime,
                                               Class<T> clazz) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        long intervalTime = endTime - startTime;

        String interval = MetricsUtils.getInterval(intervalTime);

        String dsl = dslLoaderUtil.getFormatDslByFileNameAndOtherParam(
            DslsConstant.GET_CLUSTER_METRICS_BY_RANGE_AND_INTERVAL, interval, buildAggsDSL(clazz, aggType), clusterName,
            startTime, endTime);

        return gatewayClient.performRequest(metadataClusterName, realIndexName, TYPE, dsl,
            (ESQueryResponse response) -> fetchAggClusterPhyMetrics(response, aggType, clazz), 3);
    }



    /************************************************private**************************************************/

    /**
     * 构建单个分位类型的指标数据
     * @param response            ES响应体
     * @param clusterMetricsType  指标类型
     * @param aggType             聚合类型
     * @return  Map<Long, Double>   time ——> value
     */
    private Map<Long, Double> fetchAggSinglePercentilesMetrics(ESQueryResponse response, String clusterMetricsType, String aggType) {
        Map<Long, Double> timeSlip2ValueMap = Maps.newHashMap();
        if (null == response || null == response.getAggs()) {
            return timeSlip2ValueMap;
        }

        String statsKey = aggType + "_" + clusterMetricsType;

        Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();
        if (null != esAggrMap && null != esAggrMap.get(HIST)) {
            esAggrMap.get(HIST).getBucketList().forEach(r -> {
                //获取时间戳
                long timeSlip = 0;
                if (null != r.getUnusedMap() && null != r.getUnusedMap().get(KEY)) {
                    timeSlip = Long.valueOf(r.getUnusedMap().get(KEY).toString());
                }

                //获取聚合值
                if (null != r.getAggrMap() && null != r.getAggrMap().get(statsKey)
                        && null != r.getAggrMap().get(statsKey).getUnusedMap().get(VALUE)) {
                    double aggCal = Double.parseDouble(r.getAggrMap().get(statsKey).getUnusedMap().get(VALUE).toString());
                    if (aggCal > 0) {
                        timeSlip2ValueMap.put(timeSlip, aggCal);
                    } else {
                        timeSlip2ValueMap.put(timeSlip, 0d);
                    }
                }
            });
        }

        return timeSlip2ValueMap;
    }
}

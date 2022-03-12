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
public class GatewayNodeMetricsDAO extends BaseESDAO {

    private static final String TYPE = "type";
    private static final String AGG_KEY_TIMESTAMP = "group_by_timeStamp";
    private static final String AGG_KEY_FIELD = "group_by_field";
    private static final String KEY = "key";
    private String indexName;

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusStatsGatewayInfo();
    }


    /**
     * 获取某个字段分布.(gatewayNode，clientNode)  topN
     */
    public VariousLineChartMetrics getAggFieldByRange(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String field, Long startTime, Long endTime, Integer appId, Integer topNu) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_NODE_FIELD, startTime, endTime, appId, field, interval);
        VariousLineChartMetrics variousLineChartMetrics = gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchFieldAggMetrics(response, gatewayMetricsTypeEnum, interval), 3);
        //根据第一个时间点的值进行倒排，取topNu
        List<MetricsContent> sortedList = variousLineChartMetrics.getMetricsContents().stream()
                .sorted(Comparator.comparing(x -> x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder()))
                .limit(topNu)
                .collect(Collectors.toList());
        variousLineChartMetrics.setMetricsContents(sortedList);
        return variousLineChartMetrics;
    }

    /**
     * 获取某个字段分布.(gatewayNode，clientNode) by nodeIp
     */
    public VariousLineChartMetrics getAggFieldByRange(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String field, Long startTime, Long endTime, Integer appId, String nodeIp) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_NODE_FIELD_BY_IP, field, nodeIp, startTime, endTime, appId, interval);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchFieldByIpAggMetrics(response, nodeIp, gatewayMetricsTypeEnum, interval), 3);
    }

    /**
     * 获取各gatewayNode写入. topN
     */
    public VariousLineChartMetrics getWriteGatewayNode(Long startTime, Long endTime,Integer appId, Integer topNu) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_NODE_WRITE, startTime, endTime, appId, interval);
        VariousLineChartMetrics variousLineChartMetrics = gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchFieldAggMetrics(response, GatewayMetricsTypeEnum.WRITE_GATEWAY_NODE, interval), 3);
        //根据第一个时间点的值进行倒排，取topNu
        List<MetricsContent> sortedList = variousLineChartMetrics.getMetricsContents().stream()
                .sorted(Comparator.comparing(x -> x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder()))
                .limit(topNu)
                .collect(Collectors.toList());
        variousLineChartMetrics.setMetricsContents(sortedList);
        return variousLineChartMetrics;
    }

    /**
     * 获取某个gatewayNode写入 by nodeIp
     */
    public VariousLineChartMetrics getWriteGatewayNodeByIp(Long startTime, Long endTime, Integer appId, String nodeIp) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_NODE_WRITE_BY_IP, nodeIp, startTime, endTime, appId, interval);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchFieldByIpAggMetrics(response, nodeIp, GatewayMetricsTypeEnum.WRITE_GATEWAY_NODE, interval), 3);
    }


    /**************************************** private methods ****************************************/
    private VariousLineChartMetrics fetchFieldAggMetrics(ESQueryResponse response, GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String interval) {
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(gatewayMetricsTypeEnum.getType());
        variousLineChartMetrics.setMetricsContents(Lists.newArrayList());
        Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map( ESAggrMap::getEsAggrMap).orElse(null);
        if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_FIELD)) {
            handleESBucket(gatewayMetricsTypeEnum, interval, variousLineChartMetrics, esAggrMap);
        }
        return variousLineChartMetrics;
    }

    private void handleESBucket(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String interval, VariousLineChartMetrics variousLineChartMetrics, Map<String, ESAggr> esAggrMap) {
        for (ESBucket esBucket : esAggrMap.get(AGG_KEY_FIELD).getBucketList()) {
            String nodeName = esBucket.getUnusedMap().get(KEY).toString();
            MetricsContent metricsContent = new MetricsContent();
            metricsContent.setName(nodeName);
            metricsContent.setMetricsContentCells(Lists.newArrayList());
            variousLineChartMetrics.getMetricsContents().add(metricsContent);
            if (null != esBucket.getAggrMap() && null != esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP)) {
                for (ESBucket bucket : esBucket.getAggrMap().get(AGG_KEY_TIMESTAMP).getBucketList()) {
                    Long timeStamp = Long.valueOf(bucket.getUnusedMap().get(KEY).toString());
                    Double value;
                    String aggKey = gatewayMetricsTypeEnum.getAggKey();
                    if (aggKey.endsWith("_count")) {
                        value = MetricsUtils.getDoubleValuePerMin(interval, bucket.getUnusedMap().get(aggKey).toString());
                    } else {
                        value = Double.valueOf(bucket.getUnusedMap().get(aggKey).toString());
                    }
                    metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
                }
            }
        }
    }

    private VariousLineChartMetrics fetchFieldByIpAggMetrics(ESQueryResponse response, String nodeIp, GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String interval) {
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(gatewayMetricsTypeEnum.getType());

        MetricsContent metricsContent = new MetricsContent();
        metricsContent.setName(nodeIp);
        metricsContent.setMetricsContentCells(Lists.newArrayList());

        variousLineChartMetrics.setMetricsContents(Lists.newArrayList(metricsContent));

        Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap).orElse(null);
        if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_TIMESTAMP)) {
            for (ESBucket esBucket : esAggrMap.get(AGG_KEY_TIMESTAMP).getBucketList()) {
                Long timeStamp = Long.valueOf(esBucket.getUnusedMap().get(KEY).toString());
                String aggKey = gatewayMetricsTypeEnum.getAggKey();
                Double value;
                if (aggKey.endsWith("_count")) {
                    value = MetricsUtils.getDoubleValuePerMin(interval, esBucket.getUnusedMap().get(aggKey).toString());
                } else {
                    value = Double.valueOf(esBucket.getUnusedMap().get(aggKey).toString());
                }
                metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
            }
        }
        return variousLineChartMetrics;
    }
}

package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContentCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.GatewayMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.DSLSearchUtils;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
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
     * 获取某 clientNode 读分布
     */
    public VariousLineChartMetrics getClientNodeAggFieldByRange(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime, Long endTime, Integer appId, String gatewayNodeIp, String clientNodeIp) {
        List<String> cellList = buildBaseTermCondition(gatewayNodeIp, startTime, endTime, appId);
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(clientNodeIp, "clientNode"));
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(true, "queryRequest"));
        String condition = "[" + ListUtils.strList2String(cellList) +"]";

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLIENT_NODE_FIELD_BY_IP, condition, interval);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchFieldByIpAggMetrics(response, clientNodeIp, gatewayMetricsTypeEnum, interval), 3);
    }

    /**
     * 获取 topN clientNode 读分布
     */
    public VariousLineChartMetrics getClientNodeAggFieldByRange(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime, Long endTime, Integer appId, Integer topNu, String gatewayNodeIp) {
        List<String> cellList = buildBaseTermCondition(gatewayNodeIp, startTime, endTime, appId);
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(true, "queryRequest"));
        String condition = "[" + ListUtils.strList2String(cellList) +"]";
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLIENT_NODE_FIELD, condition, interval);
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
     * 获取 topN clientNode 写分布
     */
    public VariousLineChartMetrics getClientNodeWrite(Long startTime, Long endTime, Integer appId, Integer topNu, String gatewayNodeIp) {
        List<String> cellList = buildBaseTermCondition(gatewayNodeIp, startTime, endTime, appId);
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(false, "queryRequest"));
        String condition = "[" + ListUtils.strList2String(cellList) +"]";
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLIENT_NODE_WRITE, condition, interval);
        VariousLineChartMetrics variousLineChartMetrics = gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchFieldAggMetrics(response, GatewayMetricsTypeEnum.WRITE_CLIENT_NODE, interval), 3);
        //根据第一个时间点的值进行倒排，取topNu
        List<MetricsContent> sortedList = variousLineChartMetrics.getMetricsContents().stream()
                .sorted(Comparator.comparing(x -> x.getMetricsContentCells().get(0).getValue(), Comparator.reverseOrder()))
                .limit(topNu)
                .collect(Collectors.toList());
        variousLineChartMetrics.setMetricsContents(sortedList);
        return variousLineChartMetrics;
    }

    /**
     * 获取某 clientNode 写分布
     */
    public VariousLineChartMetrics getClientNodeWriteByIp(Long startTime, Long endTime, Integer appId, String gatewayNodeIp, String clientNodeIp) {
        List<String> cellList = buildBaseTermCondition(gatewayNodeIp, startTime, endTime, appId);
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(clientNodeIp, "clientNode"));
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(false, "queryRequest"));
        String condition = "[" + ListUtils.strList2String(cellList) +"]";
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLIENT_NODE_WRITE_BY_IP, condition, interval);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchFieldByIpAggMetrics(response, clientNodeIp, GatewayMetricsTypeEnum.WRITE_CLIENT_NODE, interval), 3);
    }

    /**
     * 获取 topN gatewayNode 读分布
     */
    public VariousLineChartMetrics getAggFieldByRange(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime, Long endTime, Integer appId, Integer topNu) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_NODE_FIELD, startTime, endTime, appId, interval);
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
     * 获取某 gatewayNode 读分布
     */
    public VariousLineChartMetrics getAggFieldByRange(GatewayMetricsTypeEnum gatewayMetricsTypeEnum, Long startTime, Long endTime, Integer appId, String nodeIp) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_NODE_FIELD_BY_IP, nodeIp, startTime, endTime, appId, interval);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchFieldByIpAggMetrics(response, nodeIp, gatewayMetricsTypeEnum, interval), 3);
    }

    /**
     * 获取 topN gatewayNode 写分布
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
     * 获取某 gatewayNode 写分布
     */
    public VariousLineChartMetrics getWriteGatewayNodeByIp(Long startTime, Long endTime, Integer appId, String nodeIp) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String interval = MetricsUtils.getInterval((endTime - startTime));
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_NODE_WRITE_BY_IP, nodeIp, startTime, endTime, appId, interval);
        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> fetchFieldByIpAggMetrics(response, nodeIp, GatewayMetricsTypeEnum.WRITE_GATEWAY_NODE, interval), 3);
    }

    /**
     * 获取 gatewayNode 相关的 clientNode ip 信息
     */
    public List<String> getEsClientNodeIpListByGatewayNode(String gatewayNode, Long startTime, Long endTime, Integer appId) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        List<String> cellList = buildBaseTermCondition(gatewayNode, startTime, endTime, appId);
        String condition = "[" + ListUtils.strList2String(cellList) +"]";

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLIENT_NODE_BY_GATEWAY_NODE, condition);

        return gatewayClient.performRequest(realIndexName, TYPE, dsl, (ESQueryResponse response) -> {
            List<String> list = Lists.newArrayList();
            Map<String, ESAggr> esAggrMap = Optional.ofNullable(response.getAggs()).map(ESAggrMap::getEsAggrMap).orElse(null);
            if (null != esAggrMap && null != esAggrMap.get(AGG_KEY_FIELD)) {
                for (ESBucket esBucket : esAggrMap.get(AGG_KEY_FIELD).getBucketList()) {
                    String clientNode = esBucket.getUnusedMap().get(KEY).toString();
                    list.add(clientNode);
                }
            }
            return list;
        }, 3);
    }

    /**************************************** private methods ****************************************/
    private List<String> buildBaseTermCondition(String gatewayNode, Long startTime, Long endTime, Integer appId) {
        List<String> cellList = Lists.newArrayList();
        cellList.add(DSLSearchUtils.getTermCellForRangeSearch(startTime, endTime, "timeStamp"));
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(appId, "appid"));
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(gatewayNode, "gatewayNode"));
        return cellList;
    }

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
                    long timeStamp = Long.parseLong(bucket.getUnusedMap().get(KEY).toString());
                    double value;
                    String aggKey = gatewayMetricsTypeEnum.getAggKey();
                    if (aggKey.endsWith("_count")) {
                        value = MetricsUtils.getDoubleValuePerMin(interval, bucket.getUnusedMap().get(aggKey).toString());
                    } else {
                        value = Double.parseDouble(bucket.getUnusedMap().get(aggKey).toString());
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
                long timeStamp = Long.parseLong(esBucket.getUnusedMap().get(KEY).toString());
                String aggKey = gatewayMetricsTypeEnum.getAggKey();
                double value;
                if (aggKey.endsWith("_count")) {
                    value = MetricsUtils.getDoubleValuePerMin(interval, esBucket.getUnusedMap().get(aggKey).toString());
                } else {
                    value = Double.parseDouble(esBucket.getUnusedMap().get(aggKey).toString());
                }
                metricsContent.getMetricsContentCells().add(new MetricsContentCell(value, timeStamp));
            }
        }
        return variousLineChartMetrics;
    }
}

package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.GatewayOverviewMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.GatewayMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.*;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by fitz on 2021-08-16
 */
@Service
public class GatewayMetricsService {

    @Autowired
    private GatewayOverviewMetricsDAO gatewayOverviewMetricsDAO;

    @Autowired
    private GatewayIndexMetricsDAO gatewayIndexMetricsDAO;

    @Autowired
    private GatewayNodeMetricsDAO gatewayNodeMetricsDAO;

    @Autowired
    private GatewayAppMetricsDAO gatewayAppMetricsDAO;

    @Autowired
    private GatewayDslMetricsDAO gatewayDslMetricsDAO;

    public List<GatewayOverviewMetrics> getOverviewCommonMetrics(List<String> metricsTypes, Long startTime, Long endTime) {
        return gatewayOverviewMetricsDAO.getAggCommonMetricsByRange(metricsTypes, startTime, endTime);
    }

    public GatewayOverviewMetrics getOverviewReadCountMetrics(Long startTime, Long endTime) {
        return gatewayOverviewMetricsDAO.getAggSingleMetricsByRange(DslsConstant.GET_GATEWAY_READ_COUNT, GatewayMetricsTypeEnum.READ_DOC_COUNT, startTime, endTime);
    }

    public GatewayOverviewMetrics getOverviewSearchTypeMetrics(Long startTime, Long endTime) {
        return gatewayOverviewMetricsDAO.getAggSingleMetricsByRange(DslsConstant.GET_GATEWAY_SEARCH_TYPE, GatewayMetricsTypeEnum.QUERY_SEARCH_TYPE, startTime, endTime);
    }

    public List<GatewayOverviewMetrics> getOverviewWriteMetrics(List<String> metricsTypes, Long startTime, Long endTime) {
        return gatewayOverviewMetricsDAO.getAggWriteMetricsByRange(metricsTypes, startTime, endTime);
    }

    public List<VariousLineChartMetrics> getGatewayIndexWriteMetrics(List<String> metricsTypes, Long startTime, Long endTime, Integer appId, Integer topNu) {
        return gatewayIndexMetricsDAO.getWriteIndex(metricsTypes, startTime, endTime, appId, topNu);
    }

    public List<VariousLineChartMetrics> getGatewayIndexSearchMetrics(List<String> metricsTypes, Long startTime, Long endTime, Integer appId, Integer topNu) {
        return gatewayIndexMetricsDAO.getSearchIndex(metricsTypes, startTime, endTime, appId, topNu);
    }

    public List<VariousLineChartMetrics> getGatewayIndexWriteMetrics(List<String> metricsTypes, Long startTime, Long endTime, Integer appId, String templateName) {
        return gatewayIndexMetricsDAO.getWriteIndexByTemplateName(metricsTypes, startTime, endTime, appId, templateName);
    }

    public List<VariousLineChartMetrics> getGatewayIndexSearchMetrics(List<String> metricsTypes, Long startTime, Long endTime, Integer appId, String templateName) {
        return gatewayIndexMetricsDAO.getSearchIndexByTemplateName(metricsTypes, startTime, endTime, appId, templateName);
    }

    public VariousLineChartMetrics getGatewayNodeWriteMetrics(Long startTime, Long endTime, Integer appId, Integer topNu) {
        return gatewayNodeMetricsDAO.getWriteGatewayNode(startTime, endTime, appId, topNu);
    }

    public VariousLineChartMetrics getGatewayNodeWriteMetrics(Long startTime, Long endTime, Integer appId, String nodeIp) {
        return gatewayNodeMetricsDAO.getWriteGatewayNodeByIp(startTime, endTime, appId, nodeIp);
    }

    public VariousLineChartMetrics getGatewayNodeMetrics(Long startTime, Long endTime, Integer appId, Integer topNu) {
        return gatewayNodeMetricsDAO.getAggFieldByRange(GatewayMetricsTypeEnum.QUERY_GATEWAY_NODE,  startTime, endTime, appId, topNu);
    }

    public VariousLineChartMetrics getGatewayNodeMetrics(Long startTime, Long endTime, Integer appId, String nodeIp) {
        return gatewayNodeMetricsDAO.getAggFieldByRange(GatewayMetricsTypeEnum.QUERY_GATEWAY_NODE, startTime, endTime, appId, nodeIp);
    }

    public VariousLineChartMetrics getClientNodeWriteMetrics(Long startTime, Long endTime, Integer appId, String gatewayNodeIp, String clientNodeIp) {
        return gatewayNodeMetricsDAO.getClientNodeWriteByIp(startTime, endTime, appId, gatewayNodeIp, clientNodeIp);
    }

    public VariousLineChartMetrics getClientNodeWriteMetrics(Long startTime, Long endTime, Integer appId, Integer topNu, String gatewayNodeIp) {
        return gatewayNodeMetricsDAO.getClientNodeWrite(startTime, endTime, appId, topNu, gatewayNodeIp);
    }

    public VariousLineChartMetrics getClientNodeMetrics(Long startTime, Long endTime, Integer appId, Integer topNu, String gatewayNodeIp) {
        return gatewayNodeMetricsDAO.getClientNodeAggFieldByRange(GatewayMetricsTypeEnum.QUERY_CLIENT_NODE, startTime, endTime, appId, topNu, gatewayNodeIp);
    }

    public VariousLineChartMetrics getClientNodeMetrics(Long startTime, Long endTime, Integer appId, String gatewayNodeIp, String clientNodeIp) {
        return gatewayNodeMetricsDAO.getClientNodeAggFieldByRange(GatewayMetricsTypeEnum.QUERY_CLIENT_NODE, startTime, endTime, appId, gatewayNodeIp, clientNodeIp);
    }

    public List<VariousLineChartMetrics> getAppCommonMetrics(Long startTime, Long endTime, List<String> metricsTypes, Integer topNu) {
        return gatewayAppMetricsDAO.getAggFieldByRange(startTime, endTime, metricsTypes, topNu);
    }

    public List<VariousLineChartMetrics> getAppCommonMetricsByAppId(Long startTime, Long endTime, List<String> metricsTypes, String appId) {
        return gatewayAppMetricsDAO.getAggFieldByRange(startTime, endTime, metricsTypes, appId);
    }

    public VariousLineChartMetrics getAppCountMetrics(Long startTime, Long endTime, Integer topNu) {
        return gatewayAppMetricsDAO.getAggAppCountByRange(startTime, endTime,  topNu);
    }

    public VariousLineChartMetrics getAppCountMetricsByAppId(Long startTime, Long endTime, String appId) {
        return gatewayAppMetricsDAO.getAggAppCountByRange(startTime, endTime, appId);
    }

    public VariousLineChartMetrics getDslCountMetrics(Long startTime, Long endTime, Integer topNu, Integer appId) {
        return gatewayDslMetricsDAO.getDslCountByRange(startTime, endTime, topNu, appId);
    }

    public VariousLineChartMetrics getDslCountMetricsByMd5(Long startTime, Long endTime, String dslMd5, Integer appId) {
        return gatewayDslMetricsDAO.getDslCountByRangeAndMd5(startTime, endTime, dslMd5, appId);
    }

    public VariousLineChartMetrics getDslTotalCostMetrics(Long startTime, Long endTime, Integer topNu, Integer appId) {
        return gatewayDslMetricsDAO.getDslTotalCostByRange(startTime, endTime, topNu, appId);
    }

    public VariousLineChartMetrics getDslTotalCostMetricsByMd5(Long startTime, Long endTime, String dslMd5, Integer appId) {
        return gatewayDslMetricsDAO.getDslTotalCostByRangeAndMd5(startTime, endTime, dslMd5, appId);
    }

    public List<String> getDslMd5List(Long startTime, Long endTime, Integer appId) {
        return gatewayDslMetricsDAO.getDslMd5List(startTime, endTime, appId);
    }

    public List<String> getEsClientNodeIpListByGatewayNode(String gatewayNode, Long startTime, Long endTime, Integer appId) {
        return gatewayNodeMetricsDAO.getEsClientNodeIpListByGatewayNode(gatewayNode, startTime, endTime, appId);
    }
}

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
    private GatewayIndexMetricsDAO    gatewayIndexMetricsDAO;

    @Autowired
    private GatewayNodeMetricsDAO     gatewayNodeMetricsDAO;

    @Autowired
    private GatewayAppMetricsDAO      gatewayAppMetricsDAO;

    @Autowired
    private GatewayDslMetricsDAO      gatewayDslMetricsDAO;

    public List<GatewayOverviewMetrics> getOverviewCommonMetrics(List<String> metricsTypes, Long startTime,
                                                                 Long endTime) {
        return gatewayOverviewMetricsDAO.getAggCommonMetricsByRange(metricsTypes, startTime, endTime);
    }

    public GatewayOverviewMetrics getOverviewReadCountMetrics(Long startTime, Long endTime) {
        return gatewayOverviewMetricsDAO.getAggSingleMetricsByRange(DslsConstant.GET_GATEWAY_READ_COUNT,
            GatewayMetricsTypeEnum.READ_DOC_COUNT, startTime, endTime);
    }

    public GatewayOverviewMetrics getOverviewSearchTypeMetrics(Long startTime, Long endTime) {
        return gatewayOverviewMetricsDAO.getAggSingleMetricsByRange(DslsConstant.GET_GATEWAY_SEARCH_TYPE,
            GatewayMetricsTypeEnum.QUERY_SEARCH_TYPE, startTime, endTime);
    }

    public List<GatewayOverviewMetrics> getOverviewWriteMetrics(List<String> metricsTypes, Long startTime,
                                                                Long endTime) {
        return gatewayOverviewMetricsDAO.getAggWriteMetricsByRange(metricsTypes, startTime, endTime);
    }

    public List<VariousLineChartMetrics> getGatewayIndexWriteMetrics(List<String> metricsTypes, Long startTime,
                                                                     Long endTime, Integer projectId, Integer topNu) {
        return gatewayIndexMetricsDAO.getWriteIndex(metricsTypes, startTime, endTime, projectId, topNu);
    }

    public List<VariousLineChartMetrics> getGatewayIndexSearchMetrics(List<String> metricsTypes, Long startTime,
                                                                      Long endTime, Integer projectId, Integer topNu) {
        return gatewayIndexMetricsDAO.getSearchIndex(metricsTypes, startTime, endTime, projectId, topNu);
    }

    public List<VariousLineChartMetrics> getGatewayIndexWriteMetrics(List<String> metricsTypes, Long startTime,
                                                                     Long endTime, Integer projectId,
                                                                     String templateName) {
        return gatewayIndexMetricsDAO.getWriteIndexByTemplateName(metricsTypes, startTime, endTime, projectId,
            templateName);
    }

    public List<VariousLineChartMetrics> getGatewayIndexSearchMetrics(List<String> metricsTypes, Long startTime,
                                                                      Long endTime, Integer projectId,
                                                                      String templateName) {
        return gatewayIndexMetricsDAO.getSearchIndexByTemplateName(metricsTypes, startTime, endTime, projectId,
            templateName);
    }

    public VariousLineChartMetrics getGatewayNodeWriteMetrics(Long startTime, Long endTime, Integer projectId,
                                                              Integer topNu) {
        return gatewayNodeMetricsDAO.getWriteGatewayNode(startTime, endTime, projectId, topNu);
    }

    public VariousLineChartMetrics getGatewayNodeWriteMetrics(Long startTime, Long endTime, Integer projectId,
                                                              String nodeIp) {
        return gatewayNodeMetricsDAO.getWriteGatewayNodeByIp(startTime, endTime, projectId, nodeIp);
    }

    public VariousLineChartMetrics getGatewayNodeDSLLenMetrics(Long startTime, Long endTime, Integer projectId,
                                                               String nodeIp) {
        return gatewayNodeMetricsDAO.getWriteGatewayDSLLenByIp(startTime, endTime, projectId, nodeIp);
    }

    public VariousLineChartMetrics getGatewayNodeDSLLenMetrics(Long startTime, Long endTime, Integer projectId,
                                                               Integer topNu) {
        return gatewayNodeMetricsDAO.getWriteGatewayDSLLen(startTime, endTime, projectId, topNu);
    }

    public VariousLineChartMetrics getGatewayNodeMetrics(Long startTime, Long endTime, Integer projectId,
                                                         Integer topNu) {
        return gatewayNodeMetricsDAO.getAggFieldByRange(GatewayMetricsTypeEnum.QUERY_GATEWAY_NODE, startTime, endTime,
            projectId, topNu);
    }

    public VariousLineChartMetrics getGatewayNodeMetrics(Long startTime, Long endTime, Integer projectId,
                                                         String nodeIp) {
        return gatewayNodeMetricsDAO.getAggFieldByRange(GatewayMetricsTypeEnum.QUERY_GATEWAY_NODE, startTime, endTime,
            projectId, nodeIp);
    }

    public VariousLineChartMetrics getClientNodeWriteMetrics(Long startTime, Long endTime, Integer projectId,
                                                             String gatewayNodeIp, String clientNodeIp) {
        return gatewayNodeMetricsDAO.getClientNodeWriteByIp(startTime, endTime, projectId, gatewayNodeIp, clientNodeIp);
    }

    public VariousLineChartMetrics getClientNodeWriteMetrics(Long startTime, Long endTime, Integer projectId,
                                                             Integer topNu, String gatewayNodeIp) {
        return gatewayNodeMetricsDAO.getClientNodeWrite(startTime, endTime, projectId, topNu, gatewayNodeIp);
    }

    public VariousLineChartMetrics getClientNodeDSLLENMetrics(Long startTime, Long endTime, Integer projectId,
                                                              Integer topNu, String gatewayNodeIp) {
        return gatewayNodeMetricsDAO.getClientNodeDSLLENByIp(startTime, endTime, projectId, topNu, gatewayNodeIp);
    }

    public VariousLineChartMetrics getClientNodeDSLLENMetrics(Long startTime, Long endTime, Integer projectId,
                                                              String gatewayNodeIp, String clientNodeIp) {
        return gatewayNodeMetricsDAO.getClientNodeDSLLENByIp(startTime, endTime, projectId, gatewayNodeIp,
            clientNodeIp);
    }

    public VariousLineChartMetrics getClientNodeMetrics(Long startTime, Long endTime, Integer projectId, Integer topNu,
                                                        String gatewayNodeIp) {
        return gatewayNodeMetricsDAO.getClientNodeAggFieldByRange(GatewayMetricsTypeEnum.QUERY_CLIENT_NODE, startTime,
            endTime, projectId, topNu, gatewayNodeIp);
    }

    public VariousLineChartMetrics getClientNodeMetrics(Long startTime, Long endTime, Integer projectId,
                                                        String gatewayNodeIp, String clientNodeIp) {
        return gatewayNodeMetricsDAO.getClientNodeAggFieldByRange(GatewayMetricsTypeEnum.QUERY_CLIENT_NODE, startTime,
            endTime, projectId, gatewayNodeIp, clientNodeIp);
    }

    public List<VariousLineChartMetrics> getAppCommonMetrics(Long startTime, Long endTime, List<String> metricsTypes,
                                                             Integer topNu) {
        return gatewayAppMetricsDAO.getAggFieldByRange(startTime, endTime, metricsTypes, topNu);
    }

    public List<VariousLineChartMetrics> getAppCommonMetricsByProjectId(Long startTime, Long endTime,
                                                                        List<String> metricsTypes, String projectId) {
        return gatewayAppMetricsDAO.getAggFieldByRange(startTime, endTime, metricsTypes, projectId);
    }

    public VariousLineChartMetrics getAppCountMetrics(Long startTime, Long endTime, Integer topNu) {
        return gatewayAppMetricsDAO.getProjectCountByRange(startTime, endTime, topNu);
    }

    public VariousLineChartMetrics getAppCountMetricsByProjectId(Long startTime, Long endTime, String projectId) {
        return gatewayAppMetricsDAO.getProjectCountByRange(startTime, endTime, projectId);
    }

    public VariousLineChartMetrics getDslCountMetrics(Long startTime, Long endTime, Integer topNu, Integer projectId) {
        return gatewayDslMetricsDAO.getDslCountByRange(startTime, endTime, topNu, projectId);
    }

    public VariousLineChartMetrics getDslCountMetricsByMd5(Long startTime, Long endTime, String dslMd5,
                                                           Integer projectId) {
        return gatewayDslMetricsDAO.getDslCountByRangeAndMd5(startTime, endTime, dslMd5, projectId);
    }

    public VariousLineChartMetrics getDslTotalCostMetrics(Long startTime, Long endTime, Integer topNu,
                                                          Integer projectId) {
        return gatewayDslMetricsDAO.getDslTotalCostByRange(startTime, endTime, topNu, projectId);
    }

    public VariousLineChartMetrics getDslTotalCostMetricsByMd5(Long startTime, Long endTime, String dslMd5,
                                                               Integer projectId) {
        return gatewayDslMetricsDAO.getDslTotalCostByRangeAndMd5(startTime, endTime, dslMd5, projectId);
    }

    public List<String> getDslMd5List(Long startTime, Long endTime, Integer projectId) {
        return gatewayDslMetricsDAO.getDslMd5List(startTime, endTime, projectId);
    }

    public List<String> getEsClientNodeIpListByGatewayNode(String gatewayNode, Long startTime, Long endTime,
                                                           Integer projectId) {
        return gatewayNodeMetricsDAO.getEsClientNodeIpListByGatewayNode(gatewayNode, startTime, endTime, projectId);
    }
}
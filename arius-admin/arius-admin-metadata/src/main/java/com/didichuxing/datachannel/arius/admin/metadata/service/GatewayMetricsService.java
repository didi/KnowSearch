package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.ClientNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayDslDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayIndexDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayMetricsDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayProjectDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.GatewayOverviewMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.GatewayMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsConstant;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayAppMetricsDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayDslMetricsDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayIndexMetricsDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayNodeMetricsDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayOverviewMetricsDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by fitz on 2021-08-16
 */
@Service
public class GatewayMetricsService {
    private static final ILog LOGGER = LogFactory.getLog(GatewayMetricsService.class);
    @Autowired
    private           GatewayOverviewMetricsDAO gatewayOverviewMetricsDAO;

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
    
    /**
     * 构建topnsingle指标
     *
     * @param buildMetrics           构建指标
     * @param startTime              开始时间
     * @param endTime                结束时间
     * @param variousLineChartMetric 各种各样折线图度量
     * @param projectId              projectId
     * @param gatewayMetricsTypeEnum {@link GatewayMetricsTypeEnum#getGroup()}
     * @param nodeIp                 节点ip {@linkplain  GatewayMetricsTypeEnum CLIENT_NODE} 需要设置该参数
     */
    private void buildTopNSingleMetrics(List<VariousLineChartMetrics> buildMetrics, Long startTime, Long endTime,
                                        VariousLineChartMetrics variousLineChartMetric, Integer projectId,
                                        GatewayMetricsTypeEnum gatewayMetricsTypeEnum, String nodeIp) {
        List<String> values = variousLineChartMetric.getMetricsContents().stream().map(MetricsContent::getName)
                .collect(Collectors.toList());
        String type = variousLineChartMetric.getType();
        VariousLineChartMetrics variousLineChartMetrics = new VariousLineChartMetrics();
        variousLineChartMetrics.setType(type);
        variousLineChartMetrics.setMetricsContents(Lists.newArrayList());
        List<MetricsContent> contents = null;
        try {
            switch (gatewayMetricsTypeEnum.getGroup()) {
                case MetricsConstant.NODE:
                    contents = gatewayNodeMetricsDAO.getByRangeTopN(values, gatewayMetricsTypeEnum, startTime, endTime,
                            projectId);
                    break;
                case MetricsConstant.CLIENT_NODE:
                    contents = gatewayNodeMetricsDAO.getByRangeTopN(values, gatewayMetricsTypeEnum, startTime, endTime,
                            projectId, nodeIp);
                    break;
                case MetricsConstant.INDEX:
                    contents = gatewayIndexMetricsDAO.getByRangeTopN(values, gatewayMetricsTypeEnum, startTime, endTime,
                            projectId);
                    break;
                case MetricsConstant.DSL:
                    contents = gatewayDslMetricsDAO.getByRangeTopN(values, gatewayMetricsTypeEnum, startTime, endTime,
                            projectId);
                    break;
                case MetricsConstant.APP:
                    contents = gatewayAppMetricsDAO.getByRangeTopN(values, gatewayMetricsTypeEnum, startTime, endTime);
                    break;
                default:
                    return;
            }
        } catch (AdminOperateException e) {
            LOGGER.error("class=GatewayMetricsService||method=buildTopNSingleMetrics||msg={}||msg=check fail!",
                    e.getMessage());
        }
    
        if (CollectionUtils.isNotEmpty(contents)) {
            variousLineChartMetrics.getMetricsContents().addAll(contents);
        }
        buildMetrics.add(variousLineChartMetrics);
    }
    
    public <T extends GatewayMetricsDTO> Optional<List<VariousLineChartMetrics>> getTopNMetrics(Integer projectId,
                                                                                                T dto,
                                                                                                GatewayMetricsTypeEnum gatewayMetricsTypeEnum) {
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();
        List<VariousLineChartMetrics> variousLineChartMetrics = Lists.newArrayList();
        List<VariousLineChartMetrics> buildMetrics = Lists.newCopyOnWriteArrayList();
        int topNu;    //第一阶段结果召回
        try {
            switch (gatewayMetricsTypeEnum.getGroup()) {
                case MetricsConstant.NODE:
                    topNu = ((GatewayNodeDTO) dto).getTopNu() == 0 ? 1 : ((GatewayNodeDTO) dto).getTopNu();
                    String nodeIp = ((GatewayNodeDTO) dto).getNodeIp();
                    variousLineChartMetrics.addAll(
                            gatewayNodeMetricsDAO.fetchTopMetric(gatewayMetricsTypeEnum, startTime, endTime, topNu,
                                    projectId, nodeIp));
                    break;
                case MetricsConstant.CLIENT_NODE:
                    topNu = ((ClientNodeDTO) dto).getTopNu() == 0 ? 1 : ((ClientNodeDTO) dto).getTopNu();
                    String clientNodeIp = ((ClientNodeDTO) dto).getClientNodeIp();
                    String nodeIpByClientNodeIp = ((ClientNodeDTO) dto).getNodeIp();
                    variousLineChartMetrics.addAll(
                            gatewayNodeMetricsDAO.fetchTopMetric(gatewayMetricsTypeEnum, startTime, endTime, topNu,
                                    projectId, clientNodeIp, nodeIpByClientNodeIp));
                    break;
                case MetricsConstant.INDEX:
                    topNu = ((GatewayIndexDTO) dto).getTopNu() == 0 ? 1 : ((GatewayIndexDTO) dto).getTopNu();
                    String indexName = ((GatewayIndexDTO) dto).getIndexName();
                    variousLineChartMetrics.addAll(
                            gatewayIndexMetricsDAO.fetchTopMetric(gatewayMetricsTypeEnum, startTime, endTime, topNu,
                                    projectId, indexName));
                    break;
                case MetricsConstant.DSL:
                    topNu = ((GatewayDslDTO) dto).getTopNu() == 0 ? 1 : ((GatewayDslDTO) dto).getTopNu();
                    String dslMd5 = ((GatewayDslDTO) dto).getDslMd5();
                    variousLineChartMetrics.addAll(
                            gatewayDslMetricsDAO.fetchTopMetric(gatewayMetricsTypeEnum, startTime, endTime, topNu,
                                    projectId, dslMd5));
                    break;
                default:
                    return Optional.empty();
            }
        } catch (AdminOperateException e) {
            LOGGER.error("class=GatewayMetricsService||method=getTopNMetrics||msg={}||msg=check fail!", e.getMessage());
        }
        
        //第二阶段结果召回
        for (VariousLineChartMetrics variousLineChartMetric : variousLineChartMetrics) {
            String nodeIpByClientNodeIp = null;
            if (gatewayMetricsTypeEnum.getGroup().equals(MetricsConstant.CLIENT_NODE) && dto instanceof ClientNodeDTO) {
                nodeIpByClientNodeIp = ((ClientNodeDTO) dto).getNodeIp();
            }
            String finalNodeIpByClientNodeIp = nodeIpByClientNodeIp;
            Integer finalProjectId = projectId;
            buildTopNSingleMetrics(buildMetrics, startTime, endTime, variousLineChartMetric, finalProjectId,
                    gatewayMetricsTypeEnum, finalNodeIpByClientNodeIp);
        }
        return Optional.of(buildMetrics);
    }
    
    public <T extends GatewayMetricsDTO> Optional<List<VariousLineChartMetrics>> getTopNMetrics(T dto,
                                                                                                GatewayMetricsTypeEnum gatewayMetricsTypeEnum) {
        final Long endTime = dto.getEndTime();
        final Long startTime = dto.getStartTime();
        final String group = dto.getGroup();
        List<VariousLineChartMetrics> variousLineChartMetrics = Lists.newArrayList();
        List<VariousLineChartMetrics> buildMetrics = Lists.newCopyOnWriteArrayList();
        try {
            switch (group) {
                case MetricsConstant.APP:
                    final String projectId = ((GatewayProjectDTO) dto).getProjectId();
                    final Integer topNu =
                            ((GatewayProjectDTO) dto).getTopNu() == 0 ? 1 : ((GatewayProjectDTO) dto).getTopNu();
                    variousLineChartMetrics.addAll(
                            gatewayAppMetricsDAO.fetchTopMetric(gatewayMetricsTypeEnum, startTime, endTime, topNu,
                                    projectId));
                    break;
                default:
                    return Optional.empty();
            }
        } catch (AdminOperateException e) {
            LOGGER.error("class=GatewayMetricsService||method=getTopNMetrics||msg={}||msg=check fail!", e.getMessage());
        }
        
        //第二阶段结果召回
        for (VariousLineChartMetrics variousLineChartMetric : variousLineChartMetrics) {
            buildTopNSingleMetrics(buildMetrics, startTime, endTime, variousLineChartMetric, null,
                    gatewayMetricsTypeEnum, null);
        }
        return Optional.of(buildMetrics);
    }
}
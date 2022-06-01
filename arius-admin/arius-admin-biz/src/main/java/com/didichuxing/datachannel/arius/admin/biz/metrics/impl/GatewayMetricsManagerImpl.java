package com.didichuxing.datachannel.arius.admin.biz.metrics.impl;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import com.didichuxing.datachannel.arius.admin.biz.metrics.GatewayMetricsManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.ClientNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayDslDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayIndexDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayOverviewDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayProjectDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MultiGatewayNodesDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.GlobalParams;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.GatewayOverviewMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContentCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.gateway.GatewayOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.GatewayMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.metadata.service.GatewayMetricsService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GatewayMetricsManagerImpl implements GatewayMetricsManager {

    private static final ILog LOGGER = LogFactory.getLog(GatewayMetricsManagerImpl.class);

    private static final String COMMON = "common";
    private static final String WRITE = "write";
    private static final String SEARCH = "search";
    private static final Long ONE_DAY = 24 * 60 * 60 * 1000L;

    @Autowired
    private GatewayMetricsService gatewayMetricsService;

    @Autowired
    private GatewayManager gatewayManager;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TemplateLogicManager templateLogicManager;

    @Override
    public Result<List<String>> getGatewayMetricsEnums(String group) {
        return Result.buildSucc(GatewayMetricsTypeEnum.getMetricsByGroup(group));
    }

    @Override
    public Result<List<String>> getDslMd5List(Integer projectId, Long startTime, Long endTime) {
        projectId = projectId != null ? projectId : GlobalParams.CURRENT_PROJECT_ID.get();
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (startTime == null) {
            startTime = endTime - ONE_DAY;
        }
        //超过一周时间容易引起熔断，不允许
        if ((endTime - startTime) > ONE_DAY * 7) {
            return Result.buildFail("时间跨度不要超过一周");

        }
        return Result.buildSucc(gatewayMetricsService.getDslMd5List(startTime, endTime, projectId));
    }

    @Override
    public Result<List<GatewayOverviewMetricsVO>> getGatewayOverviewMetrics(GatewayOverviewDTO dto) {
        List<GatewayOverviewMetrics> result = Lists.newCopyOnWriteArrayList();
        List<String> rawMetricsTypes = dto.getMetricsTypes().stream().collect(Collectors.toList());
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();
        //commonMetrics 只需要查一次， 就可以查出来若干个指标， 一个DSL搞定。
        List<String> commonMetrics = dto.getMetricsTypes().stream()
                .filter(GatewayMetricsTypeEnum.commonOverviewMetrics::contains)
                .collect(Collectors.toList());

        if (!commonMetrics.isEmpty()) {
            dto.getMetricsTypes().removeAll(commonMetrics);
            dto.getMetricsTypes().add(COMMON);
        }
        //写入指标也可以一次性查出来。
        List<String> writeMetrics = dto.getMetricsTypes().stream()
                .filter(GatewayMetricsTypeEnum.writeOverviewMetrics::contains)
                .collect(Collectors.toList());

        if (!writeMetrics.isEmpty()) {
            dto.getMetricsTypes().removeAll(writeMetrics);
            dto.getMetricsTypes().add(WRITE);
        }

        dto.getMetricsTypes().parallelStream().forEach(metricsType -> {
            if (COMMON.equals(metricsType)) {
                List<GatewayOverviewMetrics> overviewCommonMetrics = gatewayMetricsService.getOverviewCommonMetrics(commonMetrics, startTime, endTime);
                result.addAll(overviewCommonMetrics);
            } else if (WRITE.equals(metricsType)) {
                List<GatewayOverviewMetrics> overviewWriteMetrics = gatewayMetricsService.getOverviewWriteMetrics(writeMetrics, startTime, endTime);
                result.addAll(overviewWriteMetrics);
            } else if (GatewayMetricsTypeEnum.READ_DOC_COUNT.getType().equals(metricsType)) {
                GatewayOverviewMetrics overviewRequestTypeMetrics = gatewayMetricsService.getOverviewReadCountMetrics(startTime, endTime);
                result.add(overviewRequestTypeMetrics);
            } else if (GatewayMetricsTypeEnum.QUERY_SEARCH_TYPE.getType().equals(metricsType)) {
                GatewayOverviewMetrics overviewSearchTypeMetrics = gatewayMetricsService.getOverviewSearchTypeMetrics(startTime, endTime);
                result.add(overviewSearchTypeMetrics);
            }
        });

        //补充没有数据的指标
        List<Long> timeRange = getTimeRange(startTime, endTime);
        List<MetricsContentCell> list = timeRange.stream().map(e -> new MetricsContentCell(0.0, e)).collect(Collectors.toList());
        List<String> currentMetrics = result.stream().map(GatewayOverviewMetrics::getType).collect(Collectors.toList());

        rawMetricsTypes.stream().filter(x -> !currentMetrics.contains(x)).forEach(x -> {
            GatewayOverviewMetrics metrics = new GatewayOverviewMetrics();
            metrics.setType(x);
            metrics.setMetrics(list);
            result.add(metrics);
        });
        for (GatewayOverviewMetrics metrics : result) {
            if (metrics.getMetrics() == null || metrics.getMetrics().isEmpty()) {
                metrics.setMetrics(list);
            }
        }
        sortByList(rawMetricsTypes, result);

        return Result.buildSucc(ConvertUtil.list2List(result, GatewayOverviewMetricsVO.class));
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getGatewayNodeMetrics(GatewayNodeDTO dto, Integer appId) {
        List<VariousLineChartMetrics> result = Lists.newCopyOnWriteArrayList();
        List<String> rawMetricsTypes = dto.getMetricsTypes().stream().collect(Collectors.toList());
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();
        // 补齐数据用的
        List<String> nameList = Lists.newArrayList();
        if (StringUtils.isNotBlank(dto.getNodeIp())) {
            dto.getMetricsTypes().parallelStream().forEach(metricsType -> {
                if (GatewayMetricsTypeEnum.WRITE_GATEWAY_NODE.getType().equals(metricsType)) {
                    VariousLineChartMetrics gatewayMetricsVO = gatewayMetricsService.getGatewayNodeWriteMetrics(startTime, endTime, appId, dto.getNodeIp());
                    result.add(gatewayMetricsVO);
                } else if (GatewayMetricsTypeEnum.QUERY_GATEWAY_NODE.getType().equals(metricsType)) {
                    VariousLineChartMetrics gatewayMetricsVO = gatewayMetricsService.getGatewayNodeMetrics(startTime, endTime, appId, dto.getNodeIp());
                    result.add(gatewayMetricsVO);
                } else if (GatewayMetricsTypeEnum.DSLLEN_GATEWAY_NODE.getType().equals(metricsType)) {
                    VariousLineChartMetrics gatewayMetricsVO = gatewayMetricsService.getGatewayNodeDSLLenMetrics(startTime, endTime,appId, dto.getNodeIp());
                    result.add(gatewayMetricsVO);
                }
            });
            nameList.add(dto.getNodeIp());
        } else {
            dto.getMetricsTypes().parallelStream().forEach(metricsType -> {
                if (GatewayMetricsTypeEnum.WRITE_GATEWAY_NODE.getType().equals(metricsType)) {
                    VariousLineChartMetrics gatewayMetricsVO = gatewayMetricsService.getGatewayNodeWriteMetrics(startTime, endTime, appId, dto.getTopNu());
                    result.add(gatewayMetricsVO);
                } else if (GatewayMetricsTypeEnum.QUERY_GATEWAY_NODE.getType().equals(metricsType)) {
                    VariousLineChartMetrics gatewayMetricsVO = gatewayMetricsService.getGatewayNodeMetrics(startTime, endTime, appId, dto.getTopNu());
                    result.add(gatewayMetricsVO);
                } else if (GatewayMetricsTypeEnum.DSLLEN_GATEWAY_NODE.getType().equals(metricsType)) {
                    VariousLineChartMetrics gatewayMetricsVO = gatewayMetricsService.getGatewayNodeDSLLenMetrics(startTime, endTime, appId, dto.getTopNu());
                    result.add(gatewayMetricsVO);
                }
            });
            // 获取nodeNameList
            nameList.addAll(gatewayManager.getGatewayAliveNodeNames("Normal").getData());
        }
        fillSortData(result, rawMetricsTypes, nameList, startTime, endTime, dto.getTopNu());
        return Result.buildSucc(ConvertUtil.list2List(result, VariousLineChartMetricsVO.class));
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getMultiGatewayNodesMetrics(MultiGatewayNodesDTO dto, Integer appId) {
        List<VariousLineChartMetricsVO> result = new ArrayList<>();
        GatewayNodeDTO gatewayNodeDTO = ConvertUtil.obj2Obj(dto, GatewayNodeDTO.class);
        if (AriusObjUtils.isEmptyList(dto.getNodeIps())) {
            return getGatewayNodeMetrics(gatewayNodeDTO, appId);
        }

        for (String nodeIp : dto.getNodeIps()) {
            try {
                gatewayNodeDTO.setNodeIp(nodeIp);
                Result<List<VariousLineChartMetricsVO>> nodeMetrics = getGatewayNodeMetrics(gatewayNodeDTO, appId);
                if (nodeMetrics.success()) {
                    result.addAll(nodeMetrics.getData());
                }
            } catch (Exception e) {
                LOGGER.warn("class=GatewayMetricsManagerImpl||method=getMultiGatewayNodesMetrics||errMsg={}", e);
            }
        }
        return Result.buildSucc(MetricsUtils.joinDuplicateTypeVOs(result));
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getClientNodeMetrics(ClientNodeDTO dto, Integer appId) {
        List<VariousLineChartMetrics> result = Lists.newCopyOnWriteArrayList();
        List<String> rawMetricsTypes = dto.getMetricsTypes().stream().collect(Collectors.toList());
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();
        // 补齐数据用的
        List<String> clientNodeIpList = Lists.newArrayList();
        if (StringUtils.isNotBlank(dto.getClientNodeIp())) {
            dto.getMetricsTypes().parallelStream().forEach(metricsType -> {
                if (GatewayMetricsTypeEnum.WRITE_CLIENT_NODE.getType().equals(metricsType)) {
                    VariousLineChartMetrics gatewayMetricsVO = gatewayMetricsService.getClientNodeWriteMetrics(startTime, endTime, appId, dto.getNodeIp(), dto.getClientNodeIp());
                    result.add(gatewayMetricsVO);
                } else if (GatewayMetricsTypeEnum.QUERY_CLIENT_NODE.getType().equals(metricsType)) {
                    VariousLineChartMetrics gatewayMetricsVO = gatewayMetricsService.getClientNodeMetrics(startTime, endTime, appId, dto.getNodeIp(), dto.getClientNodeIp());
                    result.add(gatewayMetricsVO);
                } else if (GatewayMetricsTypeEnum.DSLLEN_CLIENT_NODE.getType().equals(metricsType)) {
                    VariousLineChartMetrics gatewayMetricsVO = gatewayMetricsService.getClientNodeDSLLENMetrics(startTime, endTime, appId, dto.getNodeIp(), dto.getClientNodeIp());
                    result.add(gatewayMetricsVO);
                }
            });
            clientNodeIpList.add(dto.getClientNodeIp());
        } else {
            dto.getMetricsTypes().parallelStream().forEach(metricsType -> {
                if (GatewayMetricsTypeEnum.WRITE_CLIENT_NODE.getType().equals(metricsType)) {
                    VariousLineChartMetrics gatewayMetricsVO = gatewayMetricsService.getClientNodeWriteMetrics(startTime, endTime, appId, dto.getTopNu(), dto.getNodeIp());
                    result.add(gatewayMetricsVO);
                } else if (GatewayMetricsTypeEnum.QUERY_CLIENT_NODE.getType().equals(metricsType)) {
                    VariousLineChartMetrics gatewayMetricsVO = gatewayMetricsService.getClientNodeMetrics(startTime, endTime, appId, dto.getTopNu(), dto.getNodeIp());
                    result.add(gatewayMetricsVO);
                } else if (GatewayMetricsTypeEnum.DSLLEN_CLIENT_NODE.getType().equals(metricsType)) {
                    VariousLineChartMetrics gatewayMericsVO = gatewayMetricsService.getClientNodeDSLLENMetrics(startTime, endTime, appId, dto.getTopNu(), dto.getNodeIp());
                    result.add(gatewayMericsVO);
                }
            });
            clientNodeIpList.addAll(gatewayMetricsService.getEsClientNodeIpListByGatewayNode(dto.getNodeIp(), dto.getStartTime(), dto.getEndTime(), appId));
        }
        fillSortData(result, rawMetricsTypes, clientNodeIpList, startTime, endTime, dto.getTopNu());
        return Result.buildSucc(ConvertUtil.list2List(result, VariousLineChartMetricsVO.class));
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getGatewayIndexMetrics(GatewayIndexDTO dto, Integer appId) {
        List<VariousLineChartMetrics> result = Lists.newCopyOnWriteArrayList();
        List<String> rawMetricsTypes = dto.getMetricsTypes().stream().collect(Collectors.toList());
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();
        // 补齐数据用的
        List<String> nameList = Lists.newArrayList();
        //写入指标也可以一次性查出来
        List<String> writeMetrics = dto.getMetricsTypes().stream()
                .filter(GatewayMetricsTypeEnum.writeIndexMetrics::contains)
                .collect(Collectors.toList());

        if (!writeMetrics.isEmpty()) {
            dto.getMetricsTypes().removeAll(writeMetrics);
            dto.getMetricsTypes().add(WRITE);
        }
        //查询指标也可以一次性查出来
        List<String> searchMetrics = dto.getMetricsTypes().stream()
                .filter(GatewayMetricsTypeEnum.searchIndexMetrics::contains)
                .collect(Collectors.toList());

        if (!searchMetrics.isEmpty()) {
            dto.getMetricsTypes().removeAll(searchMetrics);
            dto.getMetricsTypes().add(SEARCH);
        }
        if (StringUtils.isNotBlank(dto.getIndexName())) {
            dto.getMetricsTypes().parallelStream().forEach(metricsType -> {
                if (WRITE.equals(metricsType)) {
                    List<VariousLineChartMetrics> list = gatewayMetricsService.getGatewayIndexWriteMetrics(writeMetrics, startTime, endTime, appId, dto.getIndexName());
                    result.addAll(list);
                } else if (SEARCH.equals(metricsType)) {
                    List<VariousLineChartMetrics> list = gatewayMetricsService.getGatewayIndexSearchMetrics(searchMetrics, startTime, endTime, appId, dto.getIndexName());
                    result.addAll(list);
                }
            });
            nameList.add(dto.getIndexName());
        } else {
            dto.getMetricsTypes().parallelStream().forEach(metricsType -> {
                if (WRITE.equals(metricsType)) {
                    List<VariousLineChartMetrics> list = gatewayMetricsService.getGatewayIndexWriteMetrics(writeMetrics, startTime, endTime, appId, dto.getTopNu());
                    result.addAll(list);
                } else if (SEARCH.equals(metricsType)) {
                    List<VariousLineChartMetrics> list = gatewayMetricsService.getGatewayIndexSearchMetrics(searchMetrics, startTime, endTime, appId, dto.getTopNu());
                    result.addAll(list);
                }
            });
            // 补齐数据
            nameList.addAll(templateLogicManager.getTemplateLogicNames(appId));
        }
        fillSortData(result, rawMetricsTypes, nameList, startTime, endTime, dto.getTopNu() == 0 ? 1 : dto.getTopNu());
        return Result.buildSucc(ConvertUtil.list2List(result, VariousLineChartMetricsVO.class));
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getGatewayAppMetrics(GatewayProjectDTO dto) {
        List<VariousLineChartMetrics> result = Lists.newCopyOnWriteArrayList();
        List<String> rawMetricsTypes = dto.getMetricsTypes().stream().collect(Collectors.toList());
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();
        // 补齐数据用的
        List<String> nameList = Lists.newArrayList();
        //commonMetrics 只需要查一次， 就可以查出来若干个指标， 一个DSL搞定。
        List<String> commonMetrics = dto.getMetricsTypes().stream()
                .filter(GatewayMetricsTypeEnum.commonAppMetrics::contains)
                .collect(Collectors.toList());

        if (!commonMetrics.isEmpty()) {
            dto.getMetricsTypes().removeAll(commonMetrics);
            dto.getMetricsTypes().add(COMMON);
        }
        if (StringUtils.isNotBlank(dto.getProjectId())) {
            dto.getMetricsTypes().parallelStream().forEach(metricsType -> {
                if (COMMON.equals(metricsType)) {
                    List<VariousLineChartMetrics> list = gatewayMetricsService.getAppCommonMetricsByAppId(startTime, endTime, commonMetrics, dto.getProjectId());
                    result.addAll(list);
                } else if (GatewayMetricsTypeEnum.QUERY_APP_COUNT.getType().equals(metricsType)) {
                    VariousLineChartMetrics appCountMetrics = gatewayMetricsService.getAppCountMetricsByAppId(startTime, endTime,dto.getProjectId());
                    result.add(appCountMetrics);
                }
            });
            nameList.add(dto.getProjectId());
        } else {
            dto.getMetricsTypes().parallelStream().forEach(metricsType -> {
                if (COMMON.equals(metricsType)) {
                    List<VariousLineChartMetrics> list = gatewayMetricsService.getAppCommonMetrics(startTime, endTime, commonMetrics, dto.getTopNu());
                    result.addAll(list);
                } else if (GatewayMetricsTypeEnum.QUERY_APP_COUNT.getType().equals(metricsType)) {
                    VariousLineChartMetrics appCountMetrics = gatewayMetricsService.getAppCountMetrics(startTime, endTime, dto.getTopNu());
                    result.add(appCountMetrics);
                }
            });
            // 获取所有projectid
            List<String> projectIds = projectService.getProjectBriefList().stream().map(ProjectBriefVO::getId)
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            nameList.addAll(projectIds);
        }
        fillSortData(result, rawMetricsTypes, nameList, startTime, endTime, dto.getTopNu() == 0 ? 1 : dto.getTopNu());

        return Result.buildSucc(ConvertUtil.list2List(result, VariousLineChartMetricsVO.class));
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getGatewayDslMetrics(GatewayDslDTO dto, Integer appId) {
        List<VariousLineChartMetrics> result = Lists.newCopyOnWriteArrayList();
        List<String> rawMetricsTypes = dto.getMetricsTypes().stream().collect(Collectors.toList());
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();
        // 补齐数据用的
        List<String> nameList = Lists.newArrayList();
        if (StringUtils.isNotBlank(dto.getDslMd5())) {
            dto.getMetricsTypes().parallelStream().forEach(metricsType -> {
                if (GatewayMetricsTypeEnum.QUERY_DSL_COUNT.getType().equals(metricsType)) {
                    VariousLineChartMetrics dslCountMetrics = gatewayMetricsService.getDslCountMetricsByMd5(startTime, endTime, dto.getDslMd5(), appId);
                    result.add(dslCountMetrics);
                } else if (GatewayMetricsTypeEnum.QUERY_DSL_TOTAL_COST.getType().equals(metricsType)) {
                    VariousLineChartMetrics dslTotalCostMetrics = gatewayMetricsService.getDslTotalCostMetricsByMd5(startTime, endTime, dto.getDslMd5(), appId);
                    result.add(dslTotalCostMetrics);
                }
            });
            nameList.add(dto.getDslMd5());
        } else {
            dto.getMetricsTypes().parallelStream().forEach(metricsType -> {
                if (GatewayMetricsTypeEnum.QUERY_DSL_COUNT.getType().equals(metricsType)) {
                    VariousLineChartMetrics dslCountMetrics = gatewayMetricsService.getDslCountMetrics(startTime, endTime, dto.getTopNu(), appId);
                    result.add(dslCountMetrics);
                } else if (GatewayMetricsTypeEnum.QUERY_DSL_TOTAL_COST.getType().equals(metricsType)) {
                    VariousLineChartMetrics dslTotalCostMetrics = gatewayMetricsService.getDslTotalCostMetrics(startTime, endTime, dto.getTopNu(), appId);
                    result.add(dslTotalCostMetrics);
                }
            });
            // 获取所有dslMD5
            nameList.addAll(getDslMd5List(appId, null, null).getData());
        }
        // 获取
        fillSortData(result, rawMetricsTypes, nameList, startTime, endTime, dto.getTopNu() == 0 ? 1 : dto.getTopNu());
        return Result.buildSucc(ConvertUtil.list2List(result, VariousLineChartMetricsVO.class));
    }

    @Override
    public Result<List<String>> getClientNodeIdList(String gatewayNode, Long startTime, Long endTime, Integer appId) {
        long oneHour = 60 * 60 * 1000L;
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (startTime == null) {
            startTime = endTime - oneHour;
        }
        // 超过一周时间容易引起熔断，不允许
        if ((endTime - startTime) > oneHour * 24 * 7) {
            return Result.buildFail("时间跨度不要超过一周");
        }
        return Result.buildSucc(gatewayMetricsService.getEsClientNodeIpListByGatewayNode(gatewayNode, startTime, endTime, appId));
    }

    /********************************************************** private methods **********************************************************/
    /**
     * 填充没有数据的指标和缺失的指标
     * @param result 返回的结果
     * @param rawMetricsTypes 所有指标type
     * @param nameList 待添加展示空数据的线条nameList
     * @param startTime 时间段start
     * @param endTime 时间段end
     * @param topN 获取线条个数
     */
    private void fillSortData(List<VariousLineChartMetrics> result, List<String> rawMetricsTypes, List<String> nameList, long startTime, long endTime, Integer topN) {
        List<String> currentMetrics = result.stream()
                .map(VariousLineChartMetrics::getType).collect(Collectors.toList());
        // 获取指标展示时间分段信息
        List<Long> timeRange = getTimeRange(startTime, endTime);
        // 让时间节点由小到大 getTimeRange 获取默认是大到小
        Collections.reverse(timeRange);
        List<MetricsContentCell> list = timeRange.stream().map(e -> new MetricsContentCell(0.0, e)).collect(Collectors.toList());

        // 补充没有数据的指标（三个指标，但是result只有两个指标的数据）
        rawMetricsTypes.stream().filter(x -> !currentMetrics.contains(x)).forEach(x -> {
            VariousLineChartMetrics metrics = new VariousLineChartMetrics();
            metrics.setType(x);
            metrics.setMetricsContents(Lists.newArrayList());
            result.add(metrics);
        });

        // 补充某个指标下，折线展示不全的问题（某个指标下，一共有5条折线数据可展示，用户想要展示top3，但是返回只有2个数据，所以要补齐另3个）
        for (VariousLineChartMetrics metrics : result) {
            // 补齐当前指标类型下，某数据的时间段数据不齐全（时间段有k个，但是数据个数<k）
            List<MetricsContent> metricsContentList = metrics.getMetricsContents();
            for(MetricsContent metricsContent : metricsContentList) {
                // 补齐剩下的时间段数据
                for(int i = metricsContent.getMetricsContentCells().size(); i < timeRange.size(); i++) {
                    metricsContent.getMetricsContentCells().add(new MetricsContentCell(0.0, timeRange.get(i)));
                }
            }

            if(metrics.getMetricsContents().size() >= topN) {
                // 如果达到了用户所需展示的个数
                continue;
            }

            // 不达到用户所需展示的个数，补齐缺少的
            int cnt = topN - metrics.getMetricsContents().size();
            // 获取该指标类型下有数据的线条name集合
            Set<String> hasDataNameSet = metrics.getMetricsContents().stream().map(MetricsContent::getName).collect(Collectors.toSet());
            // 过滤掉有数据的线条name
            nameList = nameList.stream().filter(x -> !hasDataNameSet.contains(x)).collect(Collectors.toList());
            for(int i = 0; i < cnt && i < nameList.size(); i++) {
                MetricsContent content = new MetricsContent();
                content.setName(nameList.get(i));
                content.setMetricsContentCells(list);
                metrics.getMetricsContents().add(content);
            }
        }
        // 根据前端传过来的指标展示顺序进行排序
        result.sort(((o1, o2) -> {
            int io1 = rawMetricsTypes.indexOf(o1.getType());
            int io2 = rawMetricsTypes.indexOf(o2.getType());
            return io1 - io2;
        }));
    }

    private List<Long> getTimeRange(Long startTime, Long endTime) {
        String interval = MetricsUtils.getInterval(endTime - startTime);
        List<Long> timeRange = null;
        if (MetricsUtils.Interval.ONE_MIN.getStr().equals(interval)) {
            timeRange = MetricsUtils.timeRange(startTime, endTime, 1L, Calendar.MINUTE);
        } else if (MetricsUtils.Interval.TWENTY_MIN.getStr().equals(interval)) {
            timeRange = MetricsUtils.timeRange(startTime, endTime, 20L, Calendar.MINUTE);
        } else if (MetricsUtils.Interval.ONE_HOUR.getStr().equals(interval)) {
            timeRange = MetricsUtils.timeRange(startTime, endTime, 1L, Calendar.HOUR);
        } else {
            timeRange = MetricsUtils.timeRange(startTime, endTime, 1L, Calendar.MINUTE);
        }
        return timeRange;
    }

    /**
     * 根据orderList的顺序，排序targetList
     * @param orderList
     * @param targetList
     */
    private void sortByList(List<String> orderList, List<GatewayOverviewMetrics> targetList) {
        targetList.sort(((o1, o2) -> {
            int io1 = orderList.indexOf(o1.getType());
            int io2 = orderList.indexOf(o2.getType());
            return io1 - io2;
        }));
    }
}
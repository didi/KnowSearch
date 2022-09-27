package com.didichuxing.datachannel.arius.admin.biz.metrics.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import com.didichuxing.datachannel.arius.admin.biz.metrics.GatewayMetricsManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.GlobalParam;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.GatewayOverviewMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContentCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.gateway.GatewayOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.GatewayMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsConstant;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.metadata.service.GatewayMetricsService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;

@Component
public class GatewayMetricsManagerImpl implements GatewayMetricsManager {

    private static final ILog     LOGGER  = LogFactory.getLog(GatewayMetricsManagerImpl.class);

    private static final String   COMMON  = "common";
    private static final String   WRITE   = "write";
    private static final String   SEARCH  = "search";
    private static final Long             ONE_DAY     = 24 * 60 * 60 * 1000L;
    private static final FutureUtil<Void> FUTURE_UTIL = FutureUtil.init("GatewayMetricsManagerImpl", 10, 10, 500);
    @Autowired
    private              GatewayMetricsService gatewayMetricsService;

    @Autowired
    private GatewayManager        gatewayManager;

    @Autowired
    private ProjectService        projectService;

    @Autowired
    private TemplateLogicManager  templateLogicManager;

    @Override
    public Result<List<String>> getGatewayMetricsEnums(String group) {
        return Result.buildSucc(GatewayMetricsTypeEnum.getMetricsByGroup(group));
    }

    @Override
    public Result<List<String>> getDslMd5List(Integer projectId, Long startTime, Long endTime) {
        projectId = projectId != null ? projectId : GlobalParam.CURRENT_PROJECT_ID.get();
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
        List<String> rawMetricsTypes = new ArrayList<>(dto.getMetricsTypes());
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();
        //commonMetrics 只需要查一次， 就可以查出来若干个指标， 一个DSL搞定。
        List<String> commonMetrics = dto.getMetricsTypes().stream()
            .filter(GatewayMetricsTypeEnum.commonOverviewMetrics::contains).collect(Collectors.toList());

        if (!commonMetrics.isEmpty()) {
            dto.getMetricsTypes().removeAll(commonMetrics);
            dto.getMetricsTypes().add(COMMON);
        }
        //写入指标也可以一次性查出来。
        List<String> writeMetrics = dto.getMetricsTypes().stream()
            .filter(GatewayMetricsTypeEnum.writeOverviewMetrics::contains).collect(Collectors.toList());

        if (!writeMetrics.isEmpty()) {
            dto.getMetricsTypes().removeAll(writeMetrics);
            dto.getMetricsTypes().add(WRITE);
        }

        dto.getMetricsTypes().parallelStream().forEach(metricsType -> {
            if (COMMON.equals(metricsType)) {
                List<GatewayOverviewMetrics> overviewCommonMetrics = gatewayMetricsService
                    .getOverviewCommonMetrics(commonMetrics, startTime, endTime);
                result.addAll(overviewCommonMetrics);
            } else if (WRITE.equals(metricsType)) {
                List<GatewayOverviewMetrics> overviewWriteMetrics = gatewayMetricsService
                    .getOverviewWriteMetrics(writeMetrics, startTime, endTime);
                result.addAll(overviewWriteMetrics);
            } else if (GatewayMetricsTypeEnum.READ_DOC_COUNT.getType().equals(metricsType)) {
                GatewayOverviewMetrics overviewRequestTypeMetrics = gatewayMetricsService
                    .getOverviewReadCountMetrics(startTime, endTime);
                result.add(overviewRequestTypeMetrics);
            } else if (GatewayMetricsTypeEnum.QUERY_SEARCH_TYPE.getType().equals(metricsType)) {
                GatewayOverviewMetrics overviewSearchTypeMetrics = gatewayMetricsService
                    .getOverviewSearchTypeMetrics(startTime, endTime);
                result.add(overviewSearchTypeMetrics);
            }
        });

        //补充没有数据的指标
        List<Long> timeRange = getTimeRange(startTime, endTime);
        List<MetricsContentCell> list = timeRange.stream().map(e -> new MetricsContentCell(0.0, e))
            .collect(Collectors.toList());
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
    public Result<List<VariousLineChartMetricsVO>> getMultiGatewayNodesMetrics(MultiGatewayNodesDTO dto,
                                                                               Integer projectId) {
        List<VariousLineChartMetricsVO> result = new ArrayList<>();
        GatewayNodeDTO gatewayNodeDTO = ConvertUtil.obj2Obj(dto, GatewayNodeDTO.class);
        if (AriusObjUtils.isEmptyList(dto.getNodeIps())) {
            return getGatewayNodeMetrics(gatewayNodeDTO, projectId);
        }

        for (String nodeIp : dto.getNodeIps()) {
            try {
                gatewayNodeDTO.setNodeIp(nodeIp);
                Result<List<VariousLineChartMetricsVO>> nodeMetrics = getGatewayNodeMetrics(gatewayNodeDTO,
                    getProjectIdIsNotAdmin(projectId));
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
    public Result<List<VariousLineChartMetricsVO>> getClientNodeMetrics(ClientNodeDTO dto, Integer projectId) {
        final List<VariousLineChartMetrics> result = commonGetTopInfoByOneLevelType(dto, projectId,
                MetricsConstant.CLIENT_NODE);
        List<String> rawMetricsTypes = dto.getMetricsTypes();
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();
        // 补齐数据用的
         List<String> clientNodeIpList = Lists.newArrayList();
        if (StringUtils.isNotBlank(dto.getClientNodeIp())) {
            clientNodeIpList.add(dto.getClientNodeIp());
        } else {
            gatewayMetricsService.getEsClientNodeIpListByGatewayNode(dto.getNodeIp(), dto.getStartTime(),
                dto.getEndTime(), getProjectIdIsNotAdmin(projectId)).stream().map(Tuple::getV2)
                .forEach(clientNodeIpList::add);
        }
        fillSortData(result, rawMetricsTypes, clientNodeIpList, startTime, endTime, dto.getTopNu());
        return Result.buildSucc(ConvertUtil.list2List(result, VariousLineChartMetricsVO.class));
    }
    @Override
    public Result<List<VariousLineChartMetricsVO>> getGatewayNodeMetrics(GatewayNodeDTO dto, Integer projectId) {
        List<String> rawMetricsTypes = dto.getMetricsTypes();
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();
        final List<VariousLineChartMetrics> result = commonGetTopInfoByOneLevelType(dto, projectId, MetricsConstant.NODE);
        // 补齐数据用的
        List<String> nameList = Lists.newArrayList();
        if (StringUtils.isNotBlank(dto.getNodeIp())) {
            nameList.add(dto.getNodeIp());
        } else {
            // 获取nodeNameList
            nameList.addAll(gatewayManager.getGatewayAliveNodeNames("Normal").getData());
        }
        fillSortData(result, rawMetricsTypes, nameList, startTime, endTime, dto.getTopNu());
        return Result.buildSucc(ConvertUtil.list2List(result, VariousLineChartMetricsVO.class));
    }
    
    @Override
    public Result<List<VariousLineChartMetricsVO>> getGatewayIndexMetrics(GatewayIndexDTO dto, Integer projectId) {
        List<String> rawMetricsTypes = Lists.newArrayList(dto.getMetricsTypes());
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();
        // 补齐数据用的
        List<String> nameList = Lists.newArrayList();
        List<VariousLineChartMetrics> result = commonGetTopInfoByOneLevelType(dto, projectId, MetricsConstant.INDEX);
        if (StringUtils.isNotBlank(dto.getIndexName())) {
            nameList.add(dto.getIndexName());
        } else {
            // 补齐数据
            nameList.addAll(templateLogicManager.getTemplateLogicNames(projectId));
        }
        fillSortData(result, rawMetricsTypes, nameList, startTime, endTime, dto.getTopNu() == 0 ? 1 : dto.getTopNu());
        return Result.buildSucc(ConvertUtil.list2List(result, VariousLineChartMetricsVO.class));}

    @Override
    public Result<List<VariousLineChartMetricsVO>> getGatewayAppMetrics(GatewayProjectDTO dto) {
        List<VariousLineChartMetrics> result = commonGetTopInfoByOneLevelType(dto, dto.getGroup());
        List<String> rawMetricsTypes = dto.getMetricsTypes();
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();    // 补齐数据用的
        List<String> nameList = Lists.newArrayList();
        if (StringUtils.isNotBlank(dto.getProjectId())) {
            nameList.add(dto.getProjectId());
        } else {
            // 获取所有projectid
            List<String> projectIds = projectService.getProjectBriefList().stream().map(ProjectBriefVO::getId)
                    .map(String::valueOf).collect(Collectors.toList());
            nameList.addAll(projectIds);
        }
        fillSortData(result, rawMetricsTypes, nameList, startTime, endTime, dto.getTopNu() == 0 ? 1 : dto.getTopNu());
        return Result.buildSucc(ConvertUtil.list2List(result, VariousLineChartMetricsVO.class));
    
    }
    
    @Override
    public Result<List<VariousLineChartMetricsVO>> getGatewayDslMetrics(GatewayDslDTO dto, Integer projectId) {
        List<String> rawMetricsTypes = dto.getMetricsTypes();
        Long startTime = dto.getStartTime();
        Long endTime = dto.getEndTime();
        // 补齐数据用的
        List<String> nameList = Lists.newArrayList();
        if (StringUtils.isNotBlank(dto.getDslMd5())) {
            nameList.add(dto.getDslMd5());
        } else {          // 获取所有dslMD5
            nameList.addAll(getDslMd5List(projectId, null, null).getData());
        }
        List<VariousLineChartMetrics> result = commonGetTopInfoByOneLevelType(dto, projectId,
                MetricsConstant.DSL);    // 获取
        fillSortData(result, rawMetricsTypes, nameList, startTime, endTime, dto.getTopNu() == 0 ? 1 : dto.getTopNu());
        return Result.buildSucc(ConvertUtil.list2List(result, VariousLineChartMetricsVO.class));
    }

    @Override
    public Result<List<Tuple<String, String>>> getClientNodeIdList(String gatewayNode, Long startTime, Long endTime,
                                                                   Integer projectId) {
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
        return Result.buildSucc(
            gatewayMetricsService.getEsClientNodeIpListByGatewayNode(gatewayNode, startTime, endTime, getProjectIdIsNotAdmin(projectId)));
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
    private void fillSortData(List<VariousLineChartMetrics> result, List<String> rawMetricsTypes, List<String> nameList,
                              long startTime, long endTime, Integer topN) {
        List<String> currentMetrics = result.stream().map(VariousLineChartMetrics::getType)
            .collect(Collectors.toList());
        // 获取指标展示时间分段信息
        List<Long> timeRange = getTimeRange(startTime, endTime);
        // 让时间节点由小到大 getTimeRange 获取默认是大到小
        Collections.reverse(timeRange);
        List<MetricsContentCell> list = timeRange.stream().map(e -> new MetricsContentCell(0.0, e))
            .collect(Collectors.toList());

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
            for (MetricsContent metricsContent : metricsContentList) {
                // 补齐剩下的时间段数据
                for (int i = metricsContent.getMetricsContentCells().size(); i < timeRange.size(); i++) {
                    metricsContent.getMetricsContentCells().add(new MetricsContentCell(0.0, timeRange.get(i)));
                }
            }

            if (metrics.getMetricsContents().size() >= topN) {
                // 如果达到了用户所需展示的个数
                continue;
            }

            // 不达到用户所需展示的个数，补齐缺少的
            int cnt = topN - metrics.getMetricsContents().size();
            // 获取该指标类型下有数据的线条name集合
            Set<String> hasDataNameSet = metrics.getMetricsContents().stream().map(MetricsContent::getName)
                .collect(Collectors.toSet());
            // 过滤掉有数据的线条name
            nameList = nameList.stream().filter(x -> !hasDataNameSet.contains(x)).collect(Collectors.toList());
            for (int i = 0; i < cnt && i < nameList.size(); i++) {
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
        List<Long> timeRange;
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
    
    /**
     * 通过指定的{@link GatewayMetricsTypeEnum#getGroup()} 进行匹配 * * * @param dto dto * @param projectId 应用程序id * @param groupType
     * {@link GatewayMetricsTypeEnum#getGroup()} * @return {@link List}<{@link VariousLineChartMetrics}>
     */
    private <T extends GatewayMetricsDTO> List<VariousLineChartMetrics> commonGetTopInfoByOneLevelType(T dto,
                                                                                                       Integer projectId,
                                                                                                       String groupType) {
        List<String> rawMetricsTypes = dto.getMetricsTypes().stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(rawMetricsTypes)) {
            return Collections.emptyList();
        }
        List<VariousLineChartMetrics> result = Lists.newCopyOnWriteArrayList();
        for (String metricsType : rawMetricsTypes) {
            final Optional<GatewayMetricsTypeEnum> metricsTypeEnumOptional = getEnumByTypeAndGroupOptional(groupType,
                    metricsType);
            if (!metricsTypeEnumOptional.isPresent()) {
                return Collections.emptyList();
            }
            FUTURE_UTIL.runnableTask(() -> gatewayMetricsService.getTopNMetrics(getProjectIdIsNotAdmin(projectId), dto,
                    metricsTypeEnumOptional.get()).filter(CollectionUtils::isNotEmpty).ifPresent(result::addAll));
        }
        FUTURE_UTIL.waitExecute();
        return result;
    }
    
    /**
     * 通过指定的
     * {@link GatewayMetricsTypeEnum#getGroup()}
     * 进行匹配 *
     * @param dto dto
     * @param groupType
     * {@linkplain  GatewayMetricsTypeEnum group} * @return {@link List}<{@link VariousLineChartMetrics}>
     */
    private <T extends GatewayMetricsDTO> List<VariousLineChartMetrics> commonGetTopInfoByOneLevelType(T dto,
                                                                                                       String groupType) {
        List<String> rawMetricsTypes = dto.getMetricsTypes().stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(rawMetricsTypes)) {
            return Collections.emptyList();
        }
        List<VariousLineChartMetrics> result = Lists.newCopyOnWriteArrayList();
        for (String metricsType : rawMetricsTypes) {
            final Optional<GatewayMetricsTypeEnum> metricsTypeEnumOptional = getEnumByTypeAndGroupOptional(groupType,
                    metricsType);
            if (!metricsTypeEnumOptional.isPresent()) {
                return Collections.emptyList();
            }
            FUTURE_UTIL.runnableTask(() -> gatewayMetricsService.getTopNMetrics(dto, metricsTypeEnumOptional.get())
                    .filter(CollectionUtils::isNotEmpty).ifPresent(result::addAll));
        }
        FUTURE_UTIL.waitExecute();
        return result;
    }
    
    private Optional<GatewayMetricsTypeEnum> getEnumByTypeAndGroupOptional(String groupType, String metricsType) {
        return Arrays.stream(GatewayMetricsTypeEnum.values())
                .filter(enumValue -> enumValue.getType().equals(metricsType) && enumValue.getGroup().equals(groupType))
                .findFirst();
    }

    private Integer getProjectIdIsNotAdmin(Integer projectId) {
        return !AuthConstant.SUPER_PROJECT_ID.equals(projectId) ? projectId : null;
    }

}
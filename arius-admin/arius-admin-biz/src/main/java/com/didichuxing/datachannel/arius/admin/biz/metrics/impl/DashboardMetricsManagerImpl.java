package com.didichuxing.datachannel.arius.admin.biz.metrics.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_DASHBOARD_THRESHOLD_GROUP;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.INDEX_MAPPING_NUM_THRESHOLD;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.INDEX_SEGMENT_MEMORY_SIZE_THRESHOLD;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.INDEX_SEGMENT_NUM_THRESHOLD;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.INDEX_SHARD_SMALL_THRESHOLD;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.INDEX_TEMPLATE_SEGMENT_COUNT_THRESHOLD;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.INDEX_TEMPLATE_SEGMENT_MEMORY_SIZE_THRESHOLD;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.NODE_SHARD_BIG_THRESHOLD;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum.INDEX_MAPPING_NUM;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum.INDEX_SEGMENT_MEM_SIZE;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum.INDEX_SEGMENT_NUM;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum.INDEX_SMALL_SHARD;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum.NODE_SHARD_NUM;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum.TEMPLATE_SEGMENT_MEM_NUM;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum.TEMPLATE_SEGMENT_NUM;

import com.didichuxing.datachannel.arius.admin.biz.component.MetricsValueConvertUtils;
import com.didichuxing.datachannel.arius.admin.biz.metrics.DashboardMetricsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardListDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardTopNDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.list.MetricList;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.ClusterPhyHealthMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.list.MetricListVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.dashboard.ClusterPhyHealthMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricTopTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.OneLevelTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.metadata.service.DashBoardMetricsService;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by linyunan on 3/14/22
 */
@Component
public class DashboardMetricsManagerImpl implements DashboardMetricsManager {
    
    private static final FutureUtil<Void> futureUtil = FutureUtil.init("DashboardMetricsManagerImpl", 10, 10, 500);
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private DashBoardMetricsService dashBoardMetricsService;
    
    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;
    
    @Override
    public Result<List<VariousLineChartMetricsVO>> getTopClusterMetricsInfo(MetricsDashboardTopNDTO param,
                                                                            Integer projectId) {
        param.init();
        String oneLevelType = OneLevelTypeEnum.CLUSTER.getType();
        return commonGetTopInfoByOneLevelType(param, projectId, oneLevelType);
    }
    
    @Override
    public Result<List<VariousLineChartMetricsVO>> getTopNodeMetricsInfo(MetricsDashboardTopNDTO param,
                                                                         Integer projectId) {
        param.init();
        String oneLevelType = OneLevelTypeEnum.NODE.getType();
        return commonGetTopInfoByOneLevelType(param, projectId, oneLevelType);
    }
    
    @Override
    public Result<List<VariousLineChartMetricsVO>> getTopTemplateMetricsInfo(MetricsDashboardTopNDTO param,
                                                                             Integer projectId) {
        param.init();
        String oneLevelType = OneLevelTypeEnum.TEMPLATE.getType();
        return commonGetTopInfoByOneLevelType(param, projectId, oneLevelType);
    }
    
    @Override
    public Result<List<VariousLineChartMetricsVO>> getTopIndexMetricsInfo(MetricsDashboardTopNDTO param,
                                                                          Integer projectId) {
        param.init();
        String oneLevelType = OneLevelTypeEnum.INDEX.getType();
        return commonGetTopInfoByOneLevelType(param, projectId, oneLevelType);
    }
    
    @Override
    public Result<List<VariousLineChartMetricsVO>> getTopClusterThreadPoolQueueMetricsInfo(
            MetricsDashboardTopNDTO param, Integer projectId) {
        param.init();
        String oneLevelType = OneLevelTypeEnum.CLUSTER_THREAD_POOL_QUEUE.getType();
        return commonGetTopInfoByOneLevelType(param, projectId, oneLevelType);
    }
    
    @Override
    public Result<List<MetricListVO>> getListClusterMetricsInfo(MetricsDashboardListDTO param, Integer projectId) {
        String oneLevelType = OneLevelTypeEnum.CLUSTER.getType();
        return commonGetListInfoByOneLevelType(param, projectId, oneLevelType);
    }
    
    @Override
    public Result<List<MetricListVO>> getListNodeMetricsInfo(MetricsDashboardListDTO param, Integer projectId) {
        String oneLevelType = OneLevelTypeEnum.NODE.getType();
        return commonGetListInfoByOneLevelType(param, projectId, oneLevelType);
    }
    
    @Override
    public Result<List<MetricListVO>> getListTemplateMetricsInfo(MetricsDashboardListDTO param, Integer projectId) {
        String oneLevelType = OneLevelTypeEnum.TEMPLATE.getType();
        return commonGetListInfoByOneLevelType(param, projectId, oneLevelType);
    }
    
    @Override
    public Result<List<MetricListVO>> getListIndexMetricsInfo(MetricsDashboardListDTO param, Integer projectId) {
        String oneLevelType = OneLevelTypeEnum.INDEX.getType();
        return commonGetListInfoByOneLevelType(param, projectId, oneLevelType);
    }
    
    @Override
    public Result<ClusterPhyHealthMetricsVO> getClusterHealthInfo(Integer projectId) {
        Result<Void> checkCommonParamResult = checkCommonParam(MetricsConstant.CLUSTER, new BaseDTO(), projectId);
        if (checkCommonParamResult.failed()) {
            return Result.buildFrom(checkCommonParamResult);
        }
        
        ClusterPhyHealthMetrics clusterHealthInfo = dashBoardMetricsService.getClusterHealthInfo();
        // 计算平台各种集群状态的百分比
        clusterHealthInfo.computePercent();
        
        // 格式化 ClusterPhyHealthMetrics 中的异常集群列表
        ClusterPhyHealthMetricsVO clusterPhyHealthMetricsVO = ConvertUtil.obj2Obj(clusterHealthInfo,
                ClusterPhyHealthMetricsVO.class);
        String unknownClusterListStr = clusterHealthInfo.getUnknownClusterListStr();
        String redClusterListStr = clusterHealthInfo.getRedClusterListStr();
        String yellowClusterListStr = clusterHealthInfo.getYellowClusterListStr();
        String greenClusterListStr = clusterHealthInfo.getYellowClusterListStr();
        
        clusterPhyHealthMetricsVO.setUnknownClusterList(ListUtils.string2StrList(unknownClusterListStr));
        clusterPhyHealthMetricsVO.setRedClusterList(ListUtils.string2StrList(redClusterListStr));
        clusterPhyHealthMetricsVO.setYellowClusterList(ListUtils.string2StrList(yellowClusterListStr));
        //clusterPhyHealthMetricsVO.setGreenClusterList();
        return Result.buildSucc(clusterPhyHealthMetricsVO);
    }
    
    /***************************************************private**********************************************/
    /**
     * @param param        MetricsDashboardTopNDTO
     * @param projectId    项目
     * @param oneLevelType OneLevelTypeEnum
     * @return
     */
    private Result<List<VariousLineChartMetricsVO>> commonGetTopInfoByOneLevelType(MetricsDashboardTopNDTO param,
                                                                                   Integer projectId,
                                                                                   String oneLevelType) {
        Result<Void> checkCommonParamResult = checkCommonParam(oneLevelType, param, projectId);
        if (checkCommonParamResult.failed()) {
            return Result.buildFrom(checkCommonParamResult);
        }
        
        List<VariousLineChartMetrics> variousLineChartMetrics = dashBoardMetricsService.getToNMetrics(param,
                oneLevelType);
        // 毛刺点优化
        MetricsValueConvertUtils.doOptimizeQueryBurrForNodeOrIndicesMetrics(variousLineChartMetrics);
        
        return Result.buildSucc(ConvertUtil.list2List(variousLineChartMetrics, VariousLineChartMetricsVO.class));
    }
    
    /**
     * @param param        MetricsDashboardListDTO
     * @param projectId    项目
     * @param oneLevelType OneLevelTypeEnum
     * @return
     */
    private Result<List<MetricListVO>> commonGetListInfoByOneLevelType(MetricsDashboardListDTO param, Integer projectId,
                                                                       String oneLevelType) {
        Result<Void> checkCommonParamResult = checkCommonParam(oneLevelType, param, projectId);
        if (checkCommonParamResult.failed()) {
            return Result.buildFrom(checkCommonParamResult);
        }
        List<String> faultTypeList = DashBoardMetricListTypeEnum.getFaultTypeList();
        List<String> valueTypeList = DashBoardMetricListTypeEnum.getValueTypeList();
        List<MetricList> listMetrics = Lists.newCopyOnWriteArrayList();
        for (String metricsType : param.getMetricsTypes()) {
            futureUtil.runnableTask(() -> {
                if (faultTypeList.contains(metricsType)) {
                    listMetrics.add(
                            dashBoardMetricsService.getListFaultMetrics(oneLevelType, metricsType, param.getAggType(),
                                    param.getOrderByDesc()));
                } else if (valueTypeList.contains(metricsType)) {
                    listMetrics.add(
                            dashBoardMetricsService.getListValueMetrics(oneLevelType, metricsType, param.getAggType(),
                                    param.getOrderByDesc()));
                }
            });
        }
        futureUtil.waitExecute();
        try {
            filterBySystemConfiguration(listMetrics, oneLevelType);
        } catch (AdminOperateException e) {
            return Result.buildFail(e.getMessage());
        }
        return Result.buildSucc(ConvertUtil.list2List(listMetrics, MetricListVO.class));
    }
    
    /**
     * 根据系统配置筛选
     */
    private void filterBySystemConfiguration(List<MetricList> listMetrics, String oneLevelType)
            throws AdminOperateException {
        Map<DashBoardMetricListTypeEnum, Double> thresholdValues = getDashBoardMetricThresholdValues();
        
        for (MetricList metric : listMetrics) {
            DashBoardMetricListTypeEnum key = DashBoardMetricListTypeEnum.valueOfType(metric.getType());
            if (thresholdValues.get(key) != null && thresholdValues.containsKey(key)) {
                Double configValue = thresholdValues.get(metric.getType());
                metric.setMetricListContents(metric.getMetricListContents().stream()
                        .filter(metricListContent -> metricListContent.getValue() > configValue)
                        .collect(Collectors.toList()));
            }
        }
    }
    
    /**
     * 获取dashboard指标阈值
     *
     * @return
     */
    @NotNull
    private Map<DashBoardMetricListTypeEnum, Double> getDashBoardMetricThresholdValues() {
        Map<DashBoardMetricListTypeEnum, Double> thresholdValues = new HashMap<>();
        //根据系统配置筛选,如果库里有对应的指标，就使用配置的指标
        thresholdValues.put(INDEX_SMALL_SHARD,
                ariusConfigInfoService.doubleSetting(ARIUS_DASHBOARD_THRESHOLD_GROUP, INDEX_SHARD_SMALL_THRESHOLD, 1D));
        thresholdValues.put(NODE_SHARD_NUM,
                ariusConfigInfoService.doubleSetting(ARIUS_DASHBOARD_THRESHOLD_GROUP, NODE_SHARD_BIG_THRESHOLD, 500D));
        thresholdValues.put(TEMPLATE_SEGMENT_MEM_NUM,
                ariusConfigInfoService.doubleSetting(ARIUS_DASHBOARD_THRESHOLD_GROUP,
                        INDEX_TEMPLATE_SEGMENT_MEMORY_SIZE_THRESHOLD, 0D));
        thresholdValues.put(INDEX_SEGMENT_MEM_SIZE,
                ariusConfigInfoService.doubleSetting(ARIUS_DASHBOARD_THRESHOLD_GROUP,
                        INDEX_SEGMENT_MEMORY_SIZE_THRESHOLD, 0D));
        thresholdValues.put(TEMPLATE_SEGMENT_NUM, ariusConfigInfoService.doubleSetting(ARIUS_DASHBOARD_THRESHOLD_GROUP,
                INDEX_TEMPLATE_SEGMENT_COUNT_THRESHOLD, 1000D));
        thresholdValues.put(INDEX_SEGMENT_NUM,
                ariusConfigInfoService.doubleSetting(ARIUS_DASHBOARD_THRESHOLD_GROUP, INDEX_SEGMENT_NUM_THRESHOLD,
                        100D));
        thresholdValues.put(INDEX_MAPPING_NUM,
                ariusConfigInfoService.doubleSetting(ARIUS_DASHBOARD_THRESHOLD_GROUP, INDEX_MAPPING_NUM_THRESHOLD,
                        100D));
        return thresholdValues;
    }
    
    /**
     * 合法性检测
     *
     * @param oneLevelType OneLevelTypeEnum
     * @param param        instanceof MetricsDashboardTopNDTO or MetricsDashboardListDTO
     * @param projectId    项目
     * @return
     */
    private Result<Void> checkCommonParam(String oneLevelType, BaseDTO param, Integer projectId) {
        if (null == param) {
            return Result.buildParamIllegal("指标项为空");
        }
        
        if (null == projectId) {
            return Result.buildParamIllegal("projectId is empty");
        }
        
        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal(String.format("There is no projectId:%s", projectId));
        }
        
        if (param instanceof MetricsDashboardTopNDTO) {
            MetricsDashboardTopNDTO metricsDashboardTopNDTO = (MetricsDashboardTopNDTO) param;
            
            if (CollectionUtils.isEmpty(metricsDashboardTopNDTO.getMetricsTypes())) {
                return Result.buildParamIllegal("指标项为空");
            }
            for (String metricsType : metricsDashboardTopNDTO.getMetricsTypes()) {
                if (!DashBoardMetricTopTypeEnum.hasExist(oneLevelType, metricsType)) {
                    return Result.buildParamIllegal(String.format("TopN类型指标项[%s]不存在", metricsType));
                }
            }
        }
        
        if (param instanceof MetricsDashboardListDTO) {
            MetricsDashboardListDTO metricsDashboardListDTO = (MetricsDashboardListDTO) param;
            
            if (CollectionUtils.isEmpty(metricsDashboardListDTO.getMetricsTypes())) {
                return Result.buildParamIllegal("指标项为空");
            }
            for (String metricsType : metricsDashboardListDTO.getMetricsTypes()) {
                if (!DashBoardMetricListTypeEnum.hasExist(oneLevelType, metricsType)) {
                    return Result.buildParamIllegal(String.format("列表类型指标项[%s]不存在", metricsType));
                }
            }
        }
        
        return Result.buildSucc();
    }
    
    private <R> R conversionType(String value, Function<String, R> convertFunc, String errMsg)
            throws AdminOperateException {
        try {
            return convertFunc.apply(value);
        } catch (Exception e) {
            throw new AdminOperateException(errMsg);
        }
    }
}
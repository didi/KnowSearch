package com.didichuxing.datachannel.arius.admin.biz.metrics.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_DASHBOARD_THRESHOLD_GROUP;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricThresholdValueNameEnum.getAllDefaultThresholdValue;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricTopTypeEnum.CLUSTER_SHARD_NUM;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.component.MetricsValueConvertUtils;
import com.didichuxing.datachannel.arius.admin.biz.metrics.DashboardMetricsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.DashBoardMetricThresholdDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardListDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardTopNDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.config.AriusConfigInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.list.MetricList;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.list.MetricListContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.ClusterPhyHealthMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.AriusConfigInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.list.MetricListVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.dashboard.ClusterPhyHealthMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.metadata.service.DashBoardMetricsService;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.google.common.collect.Lists;

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

    @Autowired
    private ESIndexCatService esIndexCatService;

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
    public List<AriusConfigInfoVO> dashboardThresholds() {
        List<AriusConfigInfo> ariusConfigInfos = ariusConfigInfoService.getConfigByGroup(ARIUS_DASHBOARD_THRESHOLD_GROUP);
        return ConvertUtil.list2List(ariusConfigInfos,AriusConfigInfoVO.class);
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
        String greenClusterListStr = clusterHealthInfo.getGreenClusterListStr();
        
        clusterPhyHealthMetricsVO.setUnknownClusterList(ListUtils.string2StrList(unknownClusterListStr));
        clusterPhyHealthMetricsVO.setRedClusterList(ListUtils.string2StrList(redClusterListStr));
        clusterPhyHealthMetricsVO.setYellowClusterList(ListUtils.string2StrList(yellowClusterListStr));
        clusterPhyHealthMetricsVO.setGreenClusterList(ListUtils.string2StrList(greenClusterListStr));
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
        //设置索引数量
        setClusterIndexCount(listMetrics, oneLevelType);

        filterBySystemConfiguration(listMetrics, oneLevelType);
        return Result.buildSucc(ConvertUtil.list2List(listMetrics, MetricListVO.class));
    }
    
    /**
     * 根据系统配置筛选
     */
    private void filterBySystemConfiguration(List<MetricList> listMetrics, String oneLevelType) {
        Map<DashBoardMetricListTypeEnum, DashBoardMetricThresholdDTO> thresholdValues = getDashBoardMetricThresholdValues();
        
        for (MetricList metric : listMetrics) {
            DashBoardMetricListTypeEnum key = DashBoardMetricListTypeEnum.valueOfTypeAndOneLevelType(metric.getType(),oneLevelType);
            final DashBoardMetricThresholdDTO dashBoardMetricThresholdDTO = thresholdValues.get(key);
            if (Objects.nonNull(dashBoardMetricThresholdDTO)) {
                DashBoardMetricThresholdDTO thresholdDTO = thresholdValues.get(key);
                 Double value = Double.parseDouble(String.valueOf(SizeUtil.getDasboardUnitSize(thresholdDTO.getValue().intValue()+thresholdDTO.getUnit().toLowerCase())));
                
                metric.setMetricListContents(metric.getMetricListContents().stream()
                        .filter(Objects::nonNull)
                        .filter(metricListContent -> metricListContent.getValue() != null)
                        .filter(metricListContent -> judgeMetricListContent(metricListContent.getValue(),value,thresholdDTO.getCompare()))
                        .collect(Collectors.toList()));
            }
        }
    }

    /**
     * 根据符号判断
     * @param metricValue 统计值
     * @param configValue 配置值
     * @param compare 比较单位
     * @return
     */
    private boolean judgeMetricListContent(Double metricValue, Double configValue, String compare) {
        boolean res = true;
        if (Objects.isNull(configValue)) {
            return res;
        }
        switch (compare) {
            case ">":
                res = metricValue > configValue;
                break;
            case "<":
                res = metricValue < configValue;
                break;
            default:
                break;
        }
        return res;
    }

    /**
     * 获取dashboard指标阈值
     *
     * @return
     */
    @NotNull
    public Map<DashBoardMetricListTypeEnum, DashBoardMetricThresholdDTO> getDashBoardMetricThresholdValues() {
        Map<DashBoardMetricListTypeEnum, DashBoardMetricThresholdDTO> thresholdValues = new HashMap<>();
        List<DashBoardMetricThresholdValueNameEnum> thresholdValueNameEnums = getAllDefaultThresholdValue();
        List<AriusConfigInfo> ariusConfigInfos = ariusConfigInfoService.getConfigByGroup(ARIUS_DASHBOARD_THRESHOLD_GROUP);
        Map<String,String> ariusConfigInfoMap =ariusConfigInfos.stream().collect(Collectors.toMap(AriusConfigInfo::getValueName,AriusConfigInfo::getValue));

        for (DashBoardMetricThresholdValueNameEnum threshold : thresholdValueNameEnums) {

            DashBoardMetricThresholdDTO thresholdDTO = JSONObject.parseObject(threshold.getDefaultValue(),DashBoardMetricThresholdDTO.class);
            String configValue = Objects.nonNull(ariusConfigInfoMap.get(threshold.getConfigName()))?ariusConfigInfoMap.get(threshold.getConfigName()):"";
            if (StringUtils.isNotBlank(configValue)){
                try {
                    DashBoardMetricThresholdDTO configThreshold = JSONObject.parseObject(configValue,DashBoardMetricThresholdDTO.class);
                    thresholdDTO.setCompare(configThreshold.getCompare());
                    thresholdDTO.setUnit(configThreshold.getUnit());
                    thresholdDTO.setValue(configThreshold.getValue());
                }catch (Exception e){
                    continue;
                }
        }
            thresholdValues.put(threshold.getTypeEnum(),thresholdDTO);
        }
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

    /**
     * 当为dashboard的shard数的时候，设置indexCount
     * @param listMetrics
     * @param oneLevelType
     */
    private void setClusterIndexCount(List<MetricList> listMetrics, String oneLevelType) {
        if (CollectionUtils.isEmpty(listMetrics)){
            return;
        }
        final List<String> clusterPhyList = listMetrics.stream()
                .filter(v -> CLUSTER_SHARD_NUM.getType().equals(v.getType()) && oneLevelType.equals(
                        CLUSTER_SHARD_NUM.getOneLevelTypeEnum().getType())).map(MetricList::getMetricListContents)
                .flatMap(Collection::stream).map(MetricListContent::getClusterPhyName).filter(StringUtils::isNotBlank)
                .distinct().collect(Collectors.toList());
        final Map</*clusterPhy*/String,/*index count*/ Integer> ClusterPhy2CountMap =
                esIndexCatService.syncGetByClusterPhyList(clusterPhyList);



        listMetrics.stream()
               .filter(v -> CLUSTER_SHARD_NUM.getType().equals(v.getType()) && oneLevelType.equals(
                        CLUSTER_SHARD_NUM.getOneLevelTypeEnum().getType()))
                .map(MetricList::getMetricListContents)
                .flatMap(Collection::stream)

                .forEach(metricsContent->metricsContent.setIndexCount(ClusterPhy2CountMap.getOrDefault(metricsContent.getClusterPhyName(),0).longValue()));
    }
}
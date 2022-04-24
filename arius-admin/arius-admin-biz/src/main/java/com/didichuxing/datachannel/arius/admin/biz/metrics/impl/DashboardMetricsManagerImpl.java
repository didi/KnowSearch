package com.didichuxing.datachannel.arius.admin.biz.metrics.impl;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.biz.component.MetricsValueConvertUtils;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.other.dashboard.ClusterPhyHealthMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.list.MetricList;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.ClusterPhyHealthMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.OneLevelTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.metrics.DashboardMetricsManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsDashboardListDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsDashboardTopNDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.list.MetricListVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricTopTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.metadata.service.DashBoardMetricsService;

/**
 * Created by linyunan on 3/14/22
 */
@Component
public class DashboardMetricsManagerImpl implements DashboardMetricsManager {

    private static final FutureUtil<Void> futureUtil = FutureUtil.init("DashboardMetricsManagerImpl",  10,10,500);

    @Autowired
    private AppService              appService;

    @Autowired
    private DashBoardMetricsService dashBoardMetricsService;

    @Override
    public Result<List<VariousLineChartMetricsVO>> getTopClusterMetricsInfo(MetricsDashboardTopNDTO param, Integer appId) {
        param.init();
        String oneLevelType = OneLevelTypeEnum.CLUSTER.getType();
        return commonGetTopInfoByOneLevelType(param, appId, oneLevelType);
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getTopNodeMetricsInfo(MetricsDashboardTopNDTO param, Integer appId) {
        param.init();
        String oneLevelType = OneLevelTypeEnum.NODE.getType();
        return commonGetTopInfoByOneLevelType(param, appId, oneLevelType);
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getTopTemplateMetricsInfo(MetricsDashboardTopNDTO param, Integer appId) {
        param.init();
        String oneLevelType = OneLevelTypeEnum.TEMPLATE.getType();
        return commonGetTopInfoByOneLevelType(param, appId, oneLevelType);
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getTopIndexMetricsInfo(MetricsDashboardTopNDTO param, Integer appId) {
        param.init();
        String oneLevelType = OneLevelTypeEnum.INDEX.getType();
        return commonGetTopInfoByOneLevelType(param, appId, oneLevelType);
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getTopClusterThreadPoolQueueMetricsInfo(MetricsDashboardTopNDTO param,
                                                Integer appId) {
        param.init();
        String oneLevelType = OneLevelTypeEnum.CLUSTER_THREAD_POOL_QUEUE.getType();
        return commonGetTopInfoByOneLevelType(param, appId, oneLevelType);
    }

    @Override
    public Result<List<MetricListVO>> getListClusterMetricsInfo(MetricsDashboardListDTO param, Integer appId) {
        String oneLevelType = OneLevelTypeEnum.CLUSTER.getType();
        return commonGetListInfoByOneLevelType(param, appId, oneLevelType);
    }

    @Override
    public Result<List<MetricListVO>> getListNodeMetricsInfo(MetricsDashboardListDTO param, Integer appId) {
        String oneLevelType = OneLevelTypeEnum.NODE.getType();
        return commonGetListInfoByOneLevelType(param, appId, oneLevelType);
    }

    @Override
    public Result<List<MetricListVO>> getListTemplateMetricsInfo(MetricsDashboardListDTO param, Integer appId) {
        String oneLevelType = OneLevelTypeEnum.TEMPLATE.getType();
        return commonGetListInfoByOneLevelType(param, appId, oneLevelType);
    }

    @Override
    public Result<List<MetricListVO>> getListIndexMetricsInfo(MetricsDashboardListDTO param, Integer appId) {
        String oneLevelType = OneLevelTypeEnum.INDEX.getType();
        return commonGetListInfoByOneLevelType(param, appId, oneLevelType);
    }

    @Override
    public Result<ClusterPhyHealthMetricsVO> getClusterHealthInfo(Integer appId) {
        Result<Void> checkCommonParamResult = checkCommonParam(MetricsConstant.CLUSTER, new BaseDTO(), appId);
        if (checkCommonParamResult.failed()) { return Result.buildFrom(checkCommonParamResult);}

        ClusterPhyHealthMetrics clusterHealthInfo = dashBoardMetricsService.getClusterHealthInfo();
        // 计算平台各种集群状态的百分比
        clusterHealthInfo.computePercent();

        // 格式化 ClusterPhyHealthMetrics 中的异常集群列表
        ClusterPhyHealthMetricsVO clusterPhyHealthMetricsVO = ConvertUtil.obj2Obj(clusterHealthInfo, ClusterPhyHealthMetricsVO.class);
        String unknownClusterListStr = clusterHealthInfo.getUnknownClusterListStr();
        String redClusterListStr     = clusterHealthInfo.getRedClusterListStr();
        String yellowClusterListStr  = clusterHealthInfo.getYellowClusterListStr();

        clusterPhyHealthMetricsVO.setUnknownClusterList(ListUtils.string2StrList(unknownClusterListStr));
        clusterPhyHealthMetricsVO.setRedClusterList(ListUtils.string2StrList(redClusterListStr));
        clusterPhyHealthMetricsVO.setYellowClusterList(ListUtils.string2StrList(yellowClusterListStr));
        return Result.buildSucc(clusterPhyHealthMetricsVO);
    }

    /***************************************************private**********************************************/
    /**
     * 
     * @param param  MetricsDashboardTopNDTO
     * @param appId  项目
     * @param oneLevelType   OneLevelTypeEnum
     * @return
     */
    private Result<List<VariousLineChartMetricsVO>> commonGetTopInfoByOneLevelType(MetricsDashboardTopNDTO param, Integer appId, String oneLevelType) {
        Result<Void> checkCommonParamResult = checkCommonParam(oneLevelType, param, appId);
        if (checkCommonParamResult.failed()) {
            return Result.buildFrom(checkCommonParamResult);
        }

        List<VariousLineChartMetrics> variousLineChartMetrics = dashBoardMetricsService.getToNMetrics(param, oneLevelType);
        // 毛刺点优化
        MetricsValueConvertUtils.doOptimizeQueryBurrForNodeOrIndicesMetrics(variousLineChartMetrics);

        return Result.buildSucc(ConvertUtil.list2List(variousLineChartMetrics, VariousLineChartMetricsVO.class));
    }

    /**
     *
     * @param param            MetricsDashboardListDTO
     * @param appId            项目
     * @param oneLevelType     OneLevelTypeEnum
     * @return
     */
    private Result<List<MetricListVO>> commonGetListInfoByOneLevelType(MetricsDashboardListDTO param, Integer appId, String oneLevelType) {
        Result<Void> checkCommonParamResult = checkCommonParam(oneLevelType, param, appId);
        if (checkCommonParamResult.failed()) {
            return Result.buildFrom(checkCommonParamResult);
        }
        List<String> faultTypeList   = DashBoardMetricListTypeEnum.getFaultTypeList();
        List<String> valueTypeList   = DashBoardMetricListTypeEnum.getValueTypeList();
        List<MetricList> listMetrics = Lists.newCopyOnWriteArrayList();
        for (String metricsType : param.getMetricsTypes()) {
            futureUtil.runnableTask(() -> {
                if (faultTypeList.contains(metricsType)) {
                    listMetrics.add(dashBoardMetricsService.getListFaultMetrics(oneLevelType, metricsType, param.getAggType(),
                            param.getOrderByDesc()));
                } else if (valueTypeList.contains(metricsType)) {
                    listMetrics.add(dashBoardMetricsService.getListValueMetrics(oneLevelType, metricsType,
                        param.getAggType(), param.getOrderByDesc()));
                }
            });
        }
        futureUtil.waitExecute();

        return Result.buildSucc(ConvertUtil.list2List(listMetrics, MetricListVO.class));
    }

    /**
     * 合法性检测
     *
     * @param oneLevelType  OneLevelTypeEnum
     * @param param   instanceof MetricsDashboardTopNDTO or MetricsDashboardListDTO
     * @param appId   项目
     * @return
     */
    private Result<Void> checkCommonParam(String oneLevelType, BaseDTO param, Integer appId) {
        if (null == param) { return Result.buildParamIllegal("指标项为空");}

        if (null == appId) { return Result.buildParamIllegal("appId is empty");}

        if (!appService.isAppExists(appId)) {
            return Result.buildParamIllegal(String.format("There is no appId:%s", appId));
        }
        
        if (param instanceof MetricsDashboardTopNDTO) {
            MetricsDashboardTopNDTO metricsDashboardTopNDTO =  (MetricsDashboardTopNDTO) param;

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
}

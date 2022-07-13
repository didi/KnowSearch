package com.didichuxing.datachannel.arius.admin.biz.metrics;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardListDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardTopNDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.list.MetricListVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.dashboard.ClusterPhyHealthMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;

import java.util.List;

/**
 * Created by linyunan on 3/14/22
 *
 * 平台dashboard监控大盘
 */
public interface DashboardMetricsManager {

    /**
     * 获取dashboard大盘健康状态信息
     * @param projectId  项目
     * @return       ClusterPhyHealthMetricsVO
     */
    Result<ClusterPhyHealthMetricsVO> getClusterHealthInfo(Integer projectId);

    /**
     * 获取dashboard大盘TopN指标信息
     * @param param MetricsDashboardTopNDTO
     * @param projectId 项目
     * @return      批量指标信息
     */
    Result<List<VariousLineChartMetricsVO>> getTopClusterMetricsInfo(MetricsDashboardTopNDTO param, Integer projectId);

    Result<List<VariousLineChartMetricsVO>> getTopNodeMetricsInfo(MetricsDashboardTopNDTO param, Integer projectId);

    Result<List<VariousLineChartMetricsVO>> getTopTemplateMetricsInfo(MetricsDashboardTopNDTO param, Integer projectId);

    Result<List<VariousLineChartMetricsVO>> getTopIndexMetricsInfo(MetricsDashboardTopNDTO param, Integer projectId);

    Result<List<VariousLineChartMetricsVO>> getTopClusterThreadPoolQueueMetricsInfo(MetricsDashboardTopNDTO param,
                                                                                    Integer projectId);

    /**
     * 获取dashboard大盘list列表指标信息
     * @param param  MetricsDashboardListDTO
     * @param projectId  项目
     * @return       批量指标信息
     */
    Result<List<MetricListVO>> getListClusterMetricsInfo(MetricsDashboardListDTO param, Integer projectId);

    Result<List<MetricListVO>> getListNodeMetricsInfo(MetricsDashboardListDTO param, Integer projectId);

    Result<List<MetricListVO>> getListTemplateMetricsInfo(MetricsDashboardListDTO param, Integer projectId);

    Result<List<MetricListVO>> getListIndexMetricsInfo(MetricsDashboardListDTO param, Integer projectId);
}
package com.didichuxing.datachannel.arius.admin.metadata.service;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsConstant.FAULT_FLAG;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsDashboardTopNDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.list.MetricList;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.ClusterPhyHealthMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricOtherTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricTopTypeEnum;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsDashBoardInfoESDAO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by linyunan on 3/14/22
 */
@Service
public class DashBoardMetricsService {

    @Autowired
    private AriusStatsDashBoardInfoESDAO ariusStatsDashBoardInfoESDAO;

    /**
     * 获取dashboard大盘TopN指标信息
     *
     * @param   param dashboard类型
     * @param   oneLevelType 目前仅支持 cluster node template index
     * @see     DashBoardMetricTopTypeEnum
     * @return  List<VariousLineChartMetrics>
     */
    public List<VariousLineChartMetrics> getToNMetrics(MetricsDashboardTopNDTO param, String oneLevelType) {
        List<String> metricsTypes = param.getMetricsTypes();
        Long startTime = param.getStartTime();
        Long endTime = param.getEndTime();
        Integer topNu = param.getTopNu();
        String aggType = param.getAggType();

        return ariusStatsDashBoardInfoESDAO.fetchTopMetric(oneLevelType, metricsTypes, topNu, aggType, startTime,
            endTime);
    }

    /**
     * 获取dashboard大盘list列表类异常指标信息 针对dashboard_status 中 的flag字段
     * @param oneLevelType 一级指标类型 cluster node index template
     * @param metricsType  二级指标类型 DashBoardMetricListTypeEnum
     * @param aggType      聚合类型
     * @see DashBoardMetricListTypeEnum
     *
     * @return MetricList
     */
    public MetricList getListFaultMetrics(String oneLevelType, String metricsType, String aggType,
                                          Boolean orderByDesc) {
        String sortType = orderByDesc ? SortConstant.DESC : SortConstant.ASC;
        return ariusStatsDashBoardInfoESDAO.fetchListFlagMetric(oneLevelType, metricsType, aggType, FAULT_FLAG,
            sortType);
    }

    /**
     * 获取dashboard大盘list列表类指标信息(带额外值)
     * @param oneLevelType 一级指标类型 cluster node index template
     * @param metricsType  二级指标类型 DashBoardMetricListTypeEnum
     * @param aggType      聚合类型
     * @param orderByDesc  排序类型
     * @see DashBoardMetricListTypeEnum
     * @return
     */
    public MetricList getListValueMetrics(String oneLevelType, String metricsType, String aggType,
                                          Boolean orderByDesc) {
        String sortType = orderByDesc ? SortConstant.DESC : SortConstant.ASC;
        return ariusStatsDashBoardInfoESDAO.fetchListValueMetrics(oneLevelType, metricsType, aggType, sortType);
    }

    /**
     * 获取dashboard大盘健康状态信息
     *
     * @see    DashBoardMetricOtherTypeEnum
     * @return ClusterPhyHealthMetrics
     */
    public ClusterPhyHealthMetrics getClusterHealthInfo() {
        return ariusStatsDashBoardInfoESDAO.fetchClusterHealthInfo();
    }
    
    public MetricList getListThresholdsMetrics(String oneLevelType, String metricsType, String valueName,
                                               String aggType,
                                               Boolean orderByDesc) {
        String sortType = orderByDesc ? SortConstant.DESC : SortConstant.ASC;
        
        return ariusStatsDashBoardInfoESDAO.fetchListThresholdsMetric(oneLevelType, metricsType,valueName, aggType,
                FAULT_FLAG,
            sortType);
    }
}
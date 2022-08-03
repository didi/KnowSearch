package com.didichuxing.datachannel.arius.admin.biz.metrics.handle.handler;

import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.BaseClusterMetricsHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyIndicesDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESIndicesStatsService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum.getClusterPhyIndicesMetricsType;

/**
 *
 *
 * @author
 * @date 2022/05/24
 */
@Service("clusterPhyTemplateMetricsHandler")
public class PhyTemplateClusterMetricsHandler extends BaseClusterMetricsHandle {
    @Autowired
    private ESIndicesStatsService esIndicesStatsService;

    @Autowired
    private IndexTemplateService  indexTemplateService;
    
    private static final FutureUtil<Void> OPTIMIZE_QUERY_BURR_FUTURE_UTIL = FutureUtil.init("PhyTemplateClusterMetricsHandler",
            10, 10, 50);

    @Override
    protected Result<Void> checkSpecialParam(MetricsClusterPhyDTO param) {
        for (String metricsType : param.getMetricsTypes()) {
            if (!ClusterPhyIndicesMetricsEnum.hasExist(metricsType)) {
                return Result.buildParamIllegal("metricsType is error");
            }
        }

        return Result.buildSucc();
    }

    @Override
    protected List<VariousLineChartMetrics> getAggClusterPhyMetrics(MetricsClusterPhyDTO param) {
        List<VariousLineChartMetrics> aggClusterPhyTemplateMetrics =null;
        //先去查项目所属的逻辑模板id
        if (!AuthConstant.SUPER_PROJECT_ID.equals(param.getProjectId()) && Objects.isNull(
                ((MetricsClusterPhyTemplateDTO) param).getLogicTemplateId())) {
            List<Integer> belongToProjectIdLogicTemplateIdList = indexTemplateService.getLogicTemplateIdListByProjectId(
                    param.getProjectId());
            aggClusterPhyTemplateMetrics = esIndicesStatsService.getAggClusterPhyTemplateMetrics(
                    (MetricsClusterPhyTemplateDTO) param, belongToProjectIdLogicTemplateIdList);
        } else {
            aggClusterPhyTemplateMetrics = esIndicesStatsService.getAggClusterPhyTemplateMetrics(
                    (MetricsClusterPhyTemplateDTO) param);
        }
        
        
        
        // 逻辑模板id转化为逻辑模板名称
        convertTemplateIdToName(aggClusterPhyTemplateMetrics);

        return aggClusterPhyTemplateMetrics;
    }

    @Override
    protected void initMetricsClusterPhy(MetricsClusterPhyDTO param) {
        //指标类型为空, 获取默认指标
        if (CollectionUtils.isEmpty(param.getMetricsTypes())) {
            param.setMetricsTypes(getClusterPhyIndicesMetricsType());
        }

        MetricsClusterPhyIndicesDTO clusterPhyIndicesDTO = (MetricsClusterPhyIndicesDTO) param;
        if (!AriusObjUtils.isBlack(clusterPhyIndicesDTO.getIndexName())) {
            param.setTopNu(null);
        }
    }

    /**
     * 将采集数据中的逻辑模板id转化为对应的逻辑模板名称
     * @param aggClusterPhyTemplateMetrics 聚合的模板指标数据列表
     */
    private void convertTemplateIdToName(List<VariousLineChartMetrics> aggClusterPhyTemplateMetrics) {
        for (VariousLineChartMetrics variousLineChartMetrics : aggClusterPhyTemplateMetrics) {
            if (CollectionUtils.isEmpty(variousLineChartMetrics.getMetricsContents())) {
                continue;
            }
            OPTIMIZE_QUERY_BURR_FUTURE_UTIL.runnableTask(() -> {
            
                // 将逻辑模板的id转化为对应的逻辑模板名称，使用*进行数据库兜底操作
                for (MetricsContent param : variousLineChartMetrics.getMetricsContents()) {
                    String logicTemplate = indexTemplateService.getNameByTemplateLogicId(
                            Integer.parseInt(param.getName()));
                    param.setName(StringUtils.isBlank(logicTemplate)  ? "*" : logicTemplate);
                }
            
            });
        
        }
        OPTIMIZE_QUERY_BURR_FUTURE_UTIL.waitExecute();
    }
}
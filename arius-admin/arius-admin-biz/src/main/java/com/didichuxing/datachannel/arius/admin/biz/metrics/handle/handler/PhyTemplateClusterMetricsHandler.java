package com.didichuxing.datachannel.arius.admin.biz.metrics.handle.handler;

import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.BaseClusterMetricsHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyIndicesDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESIndicesStaticsService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum.getClusterPhyIndicesMetricsType;

@Service("clusterPhyTemplateMetricsHandler")
public class PhyTemplateClusterMetricsHandler extends BaseClusterMetricsHandle {
    @Autowired
    private ESIndicesStaticsService esIndicesStaticsService;

    @Autowired
    private IndexTemplateService indexTemplateService;

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
        List<VariousLineChartMetrics> aggClusterPhyTemplateMetrics = esIndicesStaticsService.getAggClusterPhyTemplateMetrics((MetricsClusterPhyTemplateDTO) param);
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

            // 将逻辑模板的id转化为对应的逻辑模板名称，使用*进行数据库兜底操作
            for (MetricsContent param : variousLineChartMetrics.getMetricsContents()) {
                IndexTemplate logicTemplate = indexTemplateService.getLogicTemplateById(Integer.parseInt(param.getName()));
                param.setName(logicTemplate == null ? "*" : logicTemplate.getName());
            }
        }
    }
}

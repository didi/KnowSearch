package com.didichuxing.datachannel.arius.admin.biz.metrics.handle.handler;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum.getClusterPhyIndicesMetricsType;

import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.BaseClusterMetricsHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyIndicesDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESIndicesStatsService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    
    private static final FutureUtil<TupleTwo</*logicTemplateId*/String,/*name*/String>> OPTIMIZE_QUERY_BURR_FUTURE_UTIL =
            FutureUtil.init(
            "PhyTemplateClusterMetricsHandler",
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
        //获取所有的模版id
        final List<Integer> logicTemplateIds = aggClusterPhyTemplateMetrics.stream()
                .map(VariousLineChartMetrics::getMetricsContents).filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream).map(MetricsContent::getName).filter(StringUtils::isNumeric)
                .map(Integer::parseInt).distinct().collect(Collectors.toList());
        //并行查询所有的模版
        for (Integer logicTemplateId : logicTemplateIds) {
            OPTIMIZE_QUERY_BURR_FUTURE_UTIL.callableTask(() -> {
                String logicTemplate = indexTemplateService.getNameByTemplateLogicId(logicTemplateId);
                return Tuples.of(logicTemplateId.toString(), logicTemplate);
            
            });
        }
        final Map<String, String> logicTemplateId2Name = ConvertUtil.list2Map(
                OPTIMIZE_QUERY_BURR_FUTURE_UTIL.waitResult(), TupleTwo::v1, TupleTwo::v2);
        //设置模版名称
        aggClusterPhyTemplateMetrics.stream().map(VariousLineChartMetrics::getMetricsContents)
                .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
                .forEach(metricsContent -> metricsContent.setName(logicTemplateId2Name.get(metricsContent.getName())));
       
    }
}
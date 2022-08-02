package com.didichuxing.datachannel.arius.admin.biz.metrics.handle.handler;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum.getClusterPhyIndicesMetricsType;

import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.BaseClusterMetricsHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyIndicesDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.AggMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESIndicesStatsService;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * phy集群指数指标
 *
 * @author
 * @date 2022/05/24
 */
@Service("clusterPhyIndicesMetricsHandler")
public class PhyIndicesClusterMetricsHandler extends BaseClusterMetricsHandle {
    @Autowired
    private ESIndicesStatsService esIndicesStatsService;
    @Autowired
    private              ESIndexCatService        esIndexCatService;
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
        //这里其实需要做修改；1.普通项目侧，我们从cat index info中获取项目侧创建的索引后，再然后再通过指标去查询，
        if (!AuthConstant.SUPER_PROJECT_ID.equals(param.getProjectId()) && StringUtils.isNotBlank(
                param.getClusterLogicName()) && StringUtils.isBlank(
                ((MetricsClusterPhyIndicesDTO) param).getIndexName())) {
            //找到平台侧属于该项目的索引
            List<String> belongToProjectIndexName = esIndexCatService.syncGetIndexListByProjectId(param.getProjectId(),
                    param.getClusterLogicName());
            return esIndicesStatsService.getAggClusterPhyIndicesMetrics((MetricsClusterPhyIndicesDTO) param,
                    belongToProjectIndexName);
            
        } else {
            //2.超级项目侧/有索引带入的时候，直接查询
            return esIndicesStatsService.getAggClusterPhyIndicesMetrics((MetricsClusterPhyIndicesDTO) param);
            
        }
        
    }

    @Override
    protected void initMetricsClusterPhy(MetricsClusterPhyDTO param) {
        param.setAggType(AggMetricsTypeEnum.MAX.getType());
        //指标类型为空, 获取默认指标
        if (CollectionUtils.isEmpty(param.getMetricsTypes())) {
            param.setMetricsTypes(getClusterPhyIndicesMetricsType());
        }

        MetricsClusterPhyIndicesDTO clusterPhyIndicesDTO = (MetricsClusterPhyIndicesDTO) param;
        if (!AriusObjUtils.isBlack(clusterPhyIndicesDTO.getIndexName())) {
            param.setTopNu(null);
        }
    }
}
package com.didichuxing.datachannel.arius.admin.biz.metrics.handle.handler;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum.getClusterPhyNodeMetricsType;

import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.BaseClusterMetricsHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.AggMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.metadata.service.NodeStatsService;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 *
 * @author
 * @date 2022/05/24
 */
@Service("clusterPhyNodeMetricsHandler")
public class PhyNodeClusterMetricsHandler extends BaseClusterMetricsHandle {
    @Autowired
    private NodeStatsService nodeStatsService;

    @Override
    protected Result<Void> checkSpecialParam(MetricsClusterPhyDTO param) {
        for (String metricsType : param.getMetricsTypes()) {
            if (!ClusterPhyNodeMetricsEnum.hasExist(metricsType)) {
                return Result.buildParamIllegal(String.format("metricsType:%s is error", metricsType));
            }
        }

        return Result.buildSucc();
    }

    @Override
    protected List<VariousLineChartMetrics> getAggClusterPhyMetrics(MetricsClusterPhyDTO param) {
        return nodeStatsService.getAggClusterPhyNodeMetrics((MetricsClusterPhyNodeDTO) param);
    }

    @Override
    protected void initMetricsClusterPhy(MetricsClusterPhyDTO param) {
        param.setAggType(AggMetricsTypeEnum.MAX.getType());
        //指标类型为空, 获取默认指标
        if (CollectionUtils.isEmpty(param.getMetricsTypes())) {
            param.setMetricsTypes(getClusterPhyNodeMetricsType());
        }

        MetricsClusterPhyNodeDTO clusterPhyNodeDTO = (MetricsClusterPhyNodeDTO) param;
        if (!AriusObjUtils.isBlack(clusterPhyNodeDTO.getNodeName())) {
            param.setTopNu(null);
        }
    }
}
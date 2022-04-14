package com.didichuxing.datachannel.arius.admin.biz.metrics.handle.handler;

import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.BaseClusterMetricsHandle;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.AggMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.metadata.service.NodeStatisService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum.getClusterPhyNodeMetricsType;

@Service("clusterPhyNodeMetricsHandler")
public class PhyNodeClusterMetricsHandler extends BaseClusterMetricsHandle {
    @Autowired
    private NodeStatisService nodeStatisService;

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
        return nodeStatisService.getAggClusterPhyNodeMetrics((MetricsClusterPhyNodeDTO) param);
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

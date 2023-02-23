package com.didichuxing.datachannel.arius.admin.biz.metrics.handle.handler;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyClusterMetricsEnum.getDefaultClusterPhyMetricsCode;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.BaseClusterMetricsHandle;
import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.ClusterLogicOverviewMetricsHandle;
import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.ClusterOverviewMetricsHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.MetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.AggMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyClusterMetricsEnum;

/**
 *
 *
 * @author
 * @date 2022/05/24
 */
@Service("clusterPhyOverviewMetricsHandler")
public class PhyOverviewClusterMetricsHandler extends BaseClusterMetricsHandle {
    @Autowired
    private ClusterOverviewMetricsHandle clusterOverviewMetricsHandle;

    @Autowired
    private ClusterLogicOverviewMetricsHandle clusterLogicOverviewMetricsHandle;

    @Override
    protected Result<Void> checkSpecialParam(MetricsClusterPhyDTO param) {
        for (String metricsType : param.getMetricsTypes()) {
            if (!ClusterPhyClusterMetricsEnum.hasExist(metricsType)) {
                return Result.buildParamIllegal("metricsType is error");
            }
        }

        return Result.buildSucc();
    }

    @Override
    protected void initMetricsClusterPhy(MetricsClusterPhyDTO param) {
        param.setAggType(AggMetricsTypeEnum.MAX.getType());
        //指标类型为空, 获取默认指标
        if (CollectionUtils.isEmpty(param.getMetricsTypes())) {
            param.setMetricsTypes(getDefaultClusterPhyMetricsCode());
        }
    }

    @Override
    protected MetricsVO buildClusterPhyMetricsVO(MetricsClusterPhyDTO param) {
        return clusterOverviewMetricsHandle.buildClusterPhyOverviewMetrics(param);
    }

    @Override
    protected MetricsVO buildClusterLogicMetricsVO(MetricsClusterPhyDTO param) {
        return clusterLogicOverviewMetricsHandle.buildClusterLogicOverviewMetrics(param);
    }
}
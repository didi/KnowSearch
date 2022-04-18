package com.didichuxing.datachannel.arius.admin.biz.metrics.impl;

import com.didichuxing.datachannel.arius.admin.biz.metrics.ClusterPhyMetricsManager;
import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.BaseClusterMetricsHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.MetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.MetricsConfigService;
import com.didichuxing.datachannel.arius.admin.metadata.service.NodeStatisService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyClusterMetricsEnum.getClusterPhyMetricsType;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum.getClusterPhyIndicesMetricsType;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum.getClusterPhyNodeMetricsType;

/**
 * Created by linyunan on 2021-07-30
 */
@Component
public class ClusterPhyMetricsManagerImpl implements ClusterPhyMetricsManager {

    private static final ILog            LOGGER = LogFactory.getLog(ClusterPhyMetricsManagerImpl.class);

    @Autowired
    private AppService                   appService;

    @Autowired
    private MetricsConfigService         metricsConfigService;

    @Autowired
    private ESIndexService               esIndexService;

    @Autowired
    private NodeStatisService            nodeStatisService;

    @Autowired
    private HandleFactory                handleFactory;

    @Override
    public List<String> getMetricsCode2TypeMap(String type) {
        switch (ClusterPhyTypeMetricsEnum.valueOfType(type)) {
            case CLUSTER:
                return getClusterPhyMetricsType();
            case NODE:
                return getClusterPhyNodeMetricsType();
            case INDICES:
            case TEMPLATES:
                return getClusterPhyIndicesMetricsType();
            default:
                return Lists.newArrayList();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Result<T> getClusterMetricsByMetricsType(MetricsClusterPhyDTO param, Integer appId, String domainAccount, ClusterPhyTypeMetricsEnum metricsTypeEnum) {
        try {
            T result = null;
            BaseClusterMetricsHandle metricsHandle = (BaseClusterMetricsHandle) handleFactory.getByHandlerNamePer(metricsTypeEnum.getType());
            if (AriusObjUtils.isNull(metricsHandle)) {
                LOGGER.warn("class=ClusterPhyMetricsManagerImpl||method=getClusterMetricsFromEs||errMsg=cannot get metricsHandle");
                return Result.buildFail();
            }

            if (metricsTypeEnum.isCollectCurveMetricsList()) {
                // 折线图数据
                Result<List<VariousLineChartMetricsVO>> clusterPhyMetricsResult = metricsHandle.getClusterPhyRelatedCurveMetrics(param, appId, domainAccount);
                result = clusterPhyMetricsResult.success() ? (T) clusterPhyMetricsResult.getData() : null;
            } else {
                // 折线图和列表图数据
                Result<MetricsVO> metricsVoResult = metricsHandle.getOtherClusterPhyRelatedMetricsVO(param, appId, domainAccount);
                result = metricsVoResult.success() ? (T) metricsVoResult.getData() : null;
            }

            return Result.buildSucc(result);
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyMetricsManagerImpl||method=getClusterMetricsFromEs||errMsg={}", e);
            return Result.buildFail();
        }
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getMultiClusterMetrics(MultiMetricsClusterPhyNodeDTO param, Integer appId, String domainAccount, ClusterPhyTypeMetricsEnum metricsTypeEnum) {
        MetricsClusterPhyNodeDTO phyNodeDTO;
        if (metricsTypeEnum == ClusterPhyTypeMetricsEnum.NODE) {
            phyNodeDTO = ConvertUtil.obj2Obj(param, MetricsClusterPhyNodeDTO.class);
        } else {
            phyNodeDTO = ConvertUtil.obj2Obj(param, MetricsClusterPhyNodeTaskDTO.class);
        }
        if (AriusObjUtils.isEmptyList(param.getNodeNames())) {
            return getClusterMetricsByMetricsType(phyNodeDTO, appId, domainAccount, metricsTypeEnum);
        }

        List<VariousLineChartMetricsVO> result = new ArrayList<>();
        for (String nodeName : param.getNodeNames()) {
            try {
                phyNodeDTO.setNodeName(nodeName);
                Result<List<VariousLineChartMetricsVO>> nodeMetrics = getClusterMetricsByMetricsType(phyNodeDTO, appId, domainAccount, metricsTypeEnum);
                if (nodeMetrics.success()) {
                    result.addAll(nodeMetrics.getData());
                }
            } catch (Exception e) {
                LOGGER.warn("class=ClusterPhyMetricsManagerImpl||method=getMultiClusterMetrics||errMsg={}", e);
            }
        }
        return Result.buildSucc(MetricsUtils.joinDuplicateTypeVOs(result));
    }

    @Override
    public Result<List<String>> getClusterPhyIndexName(String clusterPhyName, Integer appId) {
        if (!appService.isAppExists(appId)) {
            return Result.buildParamIllegal(String.format("There is no appId:%s", appId));
        }

        return Result.buildSucc(esIndexService.syncGetIndexName(clusterPhyName));
    }

    @Override
    public List<String> getDomainAccountConfigMetrics(MetricsConfigInfoDTO metricsConfigInfoDTO, String domainAccount) {
        metricsConfigInfoDTO.setDomainAccount(domainAccount);
        return metricsConfigService.getMetricsByTypeAndDomainAccount(metricsConfigInfoDTO);
    }

    @Override
    public Result<Integer> updateDomainAccountConfigMetrics(MetricsConfigInfoDTO param, String domainAccount) {
        param.setDomainAccount(domainAccount);
        Result<Integer> result = metricsConfigService.updateByMetricsByTypeAndDomainAccount(param);
        if(result.failed()) {
            LOGGER.warn("class=ClusterPhyMetricsManagerImpl||method=updateDomainAccountConfigMetrics||errMsg={}","用户指标配置信息更新出错");
        }
        return result;
    }

    @Override
    public Result<List<ESClusterTaskDetailVO>> getClusterPhyTaskDetail(String clusterPhyName, String node, String startTime, String endTime, Integer appId) {
        if (!appService.isAppExists(appId)) {
            return Result.buildParamIllegal(String.format("There is no appId:%s", appId));
        }
        return Result.buildSucc(ConvertUtil.list2List(nodeStatisService.getClusterTaskDetail(clusterPhyName, node, Long.parseLong(startTime), Long.parseLong(endTime)),
                ESClusterTaskDetailVO.class));
    }
}

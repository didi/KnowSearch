package com.didichuxing.datachannel.arius.admin.biz.metrics.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyClusterMetricsEnum.getClusterPhyMetricsType;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyClusterMetricsEnum.getDefaultClusterPhyMetricsCode;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum.getClusterPhyIndicesMetricsType;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum.getClusterPhyNodeMetricsType;

import com.didichuxing.datachannel.arius.admin.biz.component.MetricsValueConvertUtils;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.cluster.ESClusterOverviewMetricsVO;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.metrics.ClusterPhyMetricsManager;
import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.ClusterOverviewMetricsHandle;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyIndicesDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.linechart.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.AggMetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyClusterMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyTypeMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.MetricsConfigService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESIndicesStaticsService;
import com.didichuxing.datachannel.arius.admin.metadata.service.NodeStatisService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * Created by linyunan on 2021-07-30
 */
@Component
public class ClusterPhyMetricsManagerImpl implements ClusterPhyMetricsManager {

    private static final ILog            LOGGER = LogFactory.getLog(ClusterPhyMetricsManagerImpl.class);

    @Autowired
    private AppService                   appService;

    @Autowired
    private AriusUserInfoService         ariusUserInfoService;

    @Autowired
    private MetricsConfigService         metricsConfigService;

    @Autowired
    private ESIndexService               esIndexService;

    @Autowired
    private NodeStatisService            nodeStatisService;

    @Autowired
    private ESIndicesStaticsService      esIndicesStaticsService;

    @Autowired
    private ClusterOverviewMetricsHandle clusterOverviewMetricsHandle;

    @Override
    public List<String> getMetricsCode2TypeMap(String type) {
        switch (ClusterPhyTypeMetricsEnum.valueOfType(type)) {
            case CLUSTER:
                return getClusterPhyMetricsType();
            case NODE:
                return getClusterPhyNodeMetricsType();
            case INDICES:
                return getClusterPhyIndicesMetricsType();
            default:
                return Lists.newArrayList();
        }
    }

    @Override
    public Result<ESClusterOverviewMetricsVO> getOverviewMetrics(MetricsClusterPhyDTO param, Integer appId,
                                                                    String domainAccount) {
        //1. verification
        Result<Void> checkParamResult = checkParamForOverviewMetrics(param, appId, domainAccount);
        if (checkParamResult.failed()) {
            return Result.buildFrom(checkParamResult);
        }

        //2. initialization
        initMetricsClusterPhy(param);

        return Result.buildSucc(clusterOverviewMetricsHandle.buildClusterPhyOverviewMetrics(param));
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyNodesMetrics(MetricsClusterPhyNodeDTO param,
                                                                             Integer appId, String domainAccount) {
        //1. verification
        Result<Void> checkParamResult = checkParamForClusterPhyNodesMetrics(param, appId, domainAccount);
        if (checkParamResult.failed()) {
            return Result.buildFrom(checkParamResult);
        }

        //2. initialization
        initMetricsClusterPhyNode(param);

        //3. get metrics from es engine
        List<VariousLineChartMetrics> variousLineChartMetrics = nodeStatisService.getAggClusterPhyNodeMetrics(param);

        //4. uniform percentage unit
        MetricsValueConvertUtils.convertClusterPhyNodeMetricsPercent(variousLineChartMetrics);

        //5. optimize query burr
        MetricsValueConvertUtils.doOptimizeQueryBurrForNodeOrIndicesMetrics(variousLineChartMetrics);
        return Result.buildSucc(ConvertUtil.list2List(variousLineChartMetrics, VariousLineChartMetricsVO.class));
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyIndicesMetrics(MetricsClusterPhyIndicesDTO param,
                                                                               Integer appId, String domainAccount) {
        //1. verification
        Result<Void> checkParamResult = checkParamForClusterPhyIndicesMetrics(param, appId, domainAccount);
        if (checkParamResult.failed()) {
            return Result.buildFrom(checkParamResult);
        }

        //2. initialization
        initMetricsClusterPhyIndices(param);

        //3. get metrics from es engine
        List<VariousLineChartMetrics> aggClusterPhyIndicesMetrics = esIndicesStaticsService.getAggClusterPhyIndicesMetrics(param);

        //4. optimize query burr
        MetricsValueConvertUtils.doOptimizeQueryBurrForNodeOrIndicesMetrics(aggClusterPhyIndicesMetrics);
        return Result.buildSucc(ConvertUtil.list2List(aggClusterPhyIndicesMetrics, VariousLineChartMetricsVO.class));
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

    /***********************************************private***********************************************/

    private Result<Void> checkCommonParam(MetricsClusterPhyDTO metricsClusterPhyDTO, Integer appId, String domainAccount) {

        if (null == metricsClusterPhyDTO) {
            return Result.buildParamIllegal("param is empty");
        }

        if (null == appId) {
            return Result.buildParamIllegal("appId is empty");
        }

        if (null == ariusUserInfoService.getByName(domainAccount)) {
            return Result.buildParamIllegal("user info is empty");
        }

        if (!appService.isAppExists(appId)) {
            return Result.buildParamIllegal(String.format("There is no appId:%s", appId));
        }

        return Result.buildSucc();
    }

    private Result<Void> checkParamForClusterPhyNodesMetrics(MetricsClusterPhyNodeDTO param, Integer appId,
                                                       String domainAccount) {
        Result<Void> checkCommonParam = checkCommonParam(param, appId, domainAccount);
        if (checkCommonParam.failed()) {
            return checkCommonParam;
        }

        for (String metricsType : param.getMetricsTypes()) {
            if (!ClusterPhyNodeMetricsEnum.hasExist(metricsType)) {
                return Result.buildParamIllegal(String.format("metricsType:%s is error", metricsType));
            }
        }

        return Result.buildSucc();
    }

    private Result<Void> checkParamForOverviewMetrics(MetricsClusterPhyDTO param, Integer appId, String domainAccount) {
        Result<Void> checkCommonParam = checkCommonParam(param, appId, domainAccount);
        if (checkCommonParam.failed()) {
            return checkCommonParam;
        }

        for (String metricsType : param.getMetricsTypes()) {
            if (!ClusterPhyClusterMetricsEnum.hasExist(metricsType)) {
                return Result.buildParamIllegal("metricsType is error");
            }
        }

        return Result.buildSucc();
    }

    private Result<Void> checkParamForClusterPhyIndicesMetrics(MetricsClusterPhyIndicesDTO param, Integer appId,
                                                               String domainAccount) {
        Result<Void> checkCommonParam = checkCommonParam(param, appId, domainAccount);
        if (checkCommonParam.failed()) {
            return checkCommonParam;
        }

        for (String metricsType : param.getMetricsTypes()) {
            if (!ClusterPhyIndicesMetricsEnum.hasExist(metricsType)) {
                return Result.buildParamIllegal("metricsType is error");
            }
        }

        return Result.buildSucc();
    }

    private void initMetricsClusterPhy(MetricsClusterPhyDTO metricsClusterPhyDTO) {
        init(metricsClusterPhyDTO);

        //指标类型为空, 获取默认指标
        if (CollectionUtils.isEmpty(metricsClusterPhyDTO.getMetricsTypes())) {
            metricsClusterPhyDTO.setMetricsTypes(getDefaultClusterPhyMetricsCode());
        }
    }

    private void initMetricsClusterPhyNode(MetricsClusterPhyNodeDTO param) {
        init(param);

        //指标类型为空, 获取默认指标
        if (CollectionUtils.isEmpty(param.getMetricsTypes())) {
            param.setMetricsTypes(getClusterPhyNodeMetricsType());
        }

        if (!AriusObjUtils.isBlack(param.getNodeName())) {
            param.setTopNu(null);
        }
    }

    private void initMetricsClusterPhyIndices(MetricsClusterPhyIndicesDTO param) {
        init(param);

        //指标类型为空, 获取默认指标
        if (CollectionUtils.isEmpty(param.getMetricsTypes())) {
            param.setMetricsTypes(getClusterPhyIndicesMetricsType());
        }

        if (!AriusObjUtils.isBlack(param.getIndexName())) {
            param.setTopNu(null);
        }
    }

	private void init(MetricsClusterPhyDTO param) {
		if (AriusObjUtils.isBlack(param.getClusterPhyName())) {
			param.setClusterPhyName(ALL_CLUSTER);
		}

		if (0 == param.getEndTime() || null == param.getEndTime()) {
			param.setEndTime(System.currentTimeMillis());
		}

		if (0 == param.getStartTime() || null == param.getStartTime()) {
			param.setStartTime(param.getEndTime() - DEFAULT_TIME_INTERVAL);
		}

        //防止内存打爆, 触发熔断, 兜底方案, 结束时间近一周
        long intervalTime = param.getEndTime() - param.getStartTime();
        if (intervalTime > MAX_TIME_INTERVAL) {
            param.setStartTime(param.getEndTime() - MAX_TIME_INTERVAL);
        }

        param.setAggType(AggMetricsTypeEnum.MAX.getType());

		if (null != param.getTopNu()) {
			if (param.getTopNu() <= 0) {
				param.setTopNu(5);
			}

			if (param.getTopNu() > 20) {
				param.setTopNu(20);
			}
		}
	}
}

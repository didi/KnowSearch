package com.didichuxing.datachannel.arius.admin.biz.metrics.impl;

import com.didichuxing.datachannel.arius.admin.biz.metrics.ClusterPhyMetricsManager;
import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.BaseClusterMetricsHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.MetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyTypeMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ConfigTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.UserConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.service.NodeStatsService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyClusterMetricsEnum.getClusterPhyMetricsType;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum.getClusterPhyIndicesMetricsType;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum.getClusterPhyNodeMetricsType;

/**
 * @author Created by linyunan on
 * @date 2021-07-30
 */
@Component
public class ClusterPhyMetricsManagerImpl implements ClusterPhyMetricsManager {

    private static final ILog        LOGGER = LogFactory.getLog(ClusterPhyMetricsManagerImpl.class);

    @Autowired
    private ProjectService           projectService;

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private NodeStatsService         nodeStatsService;

    @Autowired
    private HandleFactory            handleFactory;

    @Autowired
    private ClusterLogicService      clusterLogicService;

    @Autowired
    private ClusterRegionService     clusterRegionService;

    @Autowired
    private ClusterRoleHostService   clusterRoleHostService;

    @Autowired
    private IndexTemplateService     indexTemplateService;

    @Autowired
    private ESIndexService           esIndexService;
    @Autowired
    private ESIndexCatService        esIndexCatService;

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
    public <T> Result<T> getClusterMetricsByMetricsType(MetricsClusterPhyDTO param, Integer projectId, String userName,
                                                        ClusterPhyTypeMetricsEnum metricsTypeEnum) {
        try {
            param.setProjectId(projectId);
            if (StringUtils.isNotBlank(param.getClusterLogicName())) {
                ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByNameThatNotContainsProjectId(param.getClusterLogicName());
                if (clusterLogic==null){
                    return Result.buildFail();
                }
                ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
                if (clusterRegion == null) {
                    return Result.buildFail();
                }
                List<String> itemNamesUnderClusterLogic;
                //获取逻辑集群下面的节点，索引，模板的名称列表
                itemNamesUnderClusterLogic = buildItemsUnderClusterLogic(metricsTypeEnum, clusterRegion);
                param.setItemNamesUnderClusterLogic(itemNamesUnderClusterLogic);
                param.setClusterPhyName(clusterRegion.getPhyClusterName());
            }
            T result;
            BaseClusterMetricsHandle metricsHandle = (BaseClusterMetricsHandle) handleFactory
                .getByHandlerNamePer(metricsTypeEnum.getType());
            if (AriusObjUtils.isNull(metricsHandle)) {
                LOGGER.warn(
                    "class=ClusterPhyMetricsManagerImpl||method=getClusterMetricsFromEs||errMsg=cannot get metricsHandle");
                return Result.buildFail();
            }

            if (metricsTypeEnum.isCollectCurveMetricsList()) {
                // 折线图数据
                Result<List<VariousLineChartMetricsVO>> clusterPhyMetricsResult = metricsHandle
                    .getClusterPhyRelatedCurveMetrics(param, projectId, userName);
                if (clusterPhyMetricsResult.failed()) {
                    return Result.buildFrom(clusterPhyMetricsResult);
                }
                result = (T) clusterPhyMetricsResult.getData();
            } else {
                // 折线图和列表图数据
                Result<MetricsVO> metricsVoResult = metricsHandle.getOtherClusterPhyRelatedMetricsVO(param, projectId,
                    userName);
                if (metricsVoResult.failed()) {
                    return Result.buildFrom(metricsVoResult);
                }
                result = (T) metricsVoResult.getData();
            }

            return Result.buildSucc(result);
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyMetricsManagerImpl||method=getClusterMetricsFromEs||errMsg={}", e);
            return Result.buildFail();
        }
    }

    @Override
    public Result<List<VariousLineChartMetricsVO>> getMultiClusterMetrics(MultiMetricsClusterPhyNodeDTO param,
                                                                          Integer projectId, String userName,
                                                                          ClusterPhyTypeMetricsEnum metricsTypeEnum) {
        MetricsClusterPhyNodeDTO phyNodeDTO;
        if (metricsTypeEnum == ClusterPhyTypeMetricsEnum.NODE) {
            phyNodeDTO = ConvertUtil.obj2Obj(param, MetricsClusterPhyNodeDTO.class);
        } else {
            phyNodeDTO = ConvertUtil.obj2Obj(param, MetricsClusterPhyNodeTaskDTO.class);
        }
        if (AriusObjUtils.isEmptyList(param.getNodeNames())) {
            return getClusterMetricsByMetricsType(phyNodeDTO, projectId, userName, metricsTypeEnum);
        }

        List<VariousLineChartMetricsVO> result = new ArrayList<>();
        for (String nodeName : param.getNodeNames()) {
            try {
                phyNodeDTO.setNodeName(nodeName);
                Result<List<VariousLineChartMetricsVO>> nodeMetrics = getClusterMetricsByMetricsType(phyNodeDTO,
                    projectId, userName, metricsTypeEnum);
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
    public List<String> getUserNameConfigMetrics(UserConfigInfoDTO userConfigInfoDTO, String userName, Integer projectId) {
        userConfigInfoDTO.setUserName(userName);
        userConfigInfoDTO.setProjectId(projectId);
        userConfigInfoDTO.setConfigType(ConfigTypeEnum.DASHBOARD_AND_METRICS_BOARD.getCode());
        return userConfigService.getMetricsByTypeAndUserName(userConfigInfoDTO);
    }

    @Override
    public Result<Integer> updateUserNameConfigMetrics(UserConfigInfoDTO param, String userName, Integer projectId) {
        param.setUserName(userName);
        param.setProjectId(projectId);
        param.setConfigType(ConfigTypeEnum.DASHBOARD_AND_METRICS_BOARD.getCode());
        Result<Integer> result = userConfigService.updateByMetricsByTypeAndUserName(param);
        if (result.failed()) {
            LOGGER.warn("class=ClusterPhyMetricsManagerImpl||method=updateDomainAccountConfigMetrics||errMsg={}",
                "用户指标配置信息更新出错");
        }
        return result;
    }

    @Override
    public Result<List<ESClusterTaskDetailVO>> getClusterPhyTaskDetail(String clusterPhyName, String node,
                                                                       String startTime, String endTime,
                                                                       Integer projectId) {
        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal(String.format("There is no project id:%s", projectId));
        }
        return Result.buildSucc(ConvertUtil.list2List(nodeStatsService.getClusterTaskDetail(clusterPhyName, node,
            Long.parseLong(startTime), Long.parseLong(endTime)), ESClusterTaskDetailVO.class));
    }

    /**
     * 获取逻辑集群下的节点，索引，模板信息
     * @param metricsTypeEnum 类型
     * @param clusterRegion 逻辑集群关联的region
     * @return  节点，索引，模板信息 名称集合
     */
    private List<String> buildItemsUnderClusterLogic(ClusterPhyTypeMetricsEnum metricsTypeEnum,
                                                     ClusterRegion clusterRegion) {
        //节点名称列表
        if (Objects.equals(metricsTypeEnum, ClusterPhyTypeMetricsEnum.NODE)) {
            Result<List<ClusterRoleHost>> result = clusterRoleHostService
                    .listByRegionId(Math.toIntExact(clusterRegion.getId()));
            return result.getData().stream().map(ClusterRoleHost::getNodeSet)
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }
}
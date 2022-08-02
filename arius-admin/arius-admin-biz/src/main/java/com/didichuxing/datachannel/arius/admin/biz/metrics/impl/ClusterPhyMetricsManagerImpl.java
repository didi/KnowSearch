package com.didichuxing.datachannel.arius.admin.biz.metrics.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyClusterMetricsEnum.getClusterPhyMetricsType;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum.getClusterPhyIndicesMetricsType;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum.getClusterPhyNodeMetricsType;

import com.didichuxing.datachannel.arius.admin.biz.metrics.ClusterPhyMetricsManager;
import com.didichuxing.datachannel.arius.admin.biz.metrics.handle.BaseClusterMetricsHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyNodeTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MultiMetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.MetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.ESClusterTaskDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyTypeMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.UserMetricsConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.service.NodeStatsService;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private UserMetricsConfigService userMetricsConfigService;

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
                itemNamesUnderClusterLogic = buildItemsUnderClusterLogic(metricsTypeEnum, clusterRegion,projectId);
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
    public List<String> getUserNameConfigMetrics(MetricsConfigInfoDTO metricsConfigInfoDTO, String userName) {
        metricsConfigInfoDTO.setUserName(userName);
        return userMetricsConfigService.getMetricsByTypeAndUserName(metricsConfigInfoDTO);
    }

    @Override
    public Result<Integer> updateUserNameConfigMetrics(MetricsConfigInfoDTO param, String userName) {
        param.setUserName(userName);
        Result<Integer> result = userMetricsConfigService.updateByMetricsByTypeAndUserName(param);
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
                                                     ClusterRegion clusterRegion, Integer projectId) {
        Predicate<IndexTemplate> filterBelongProjectIdIndexTemplatePre=indexTemplate -> AuthConstant.SUPER_PROJECT_ID
                                                                                                .equals(projectId)||
                                                                                        Objects.equals(indexTemplate.getProjectId(),projectId);
        List<String> nodeNamesUnderClusterLogic;
        //节点名称列表
        switch (metricsTypeEnum) {
            case NODE:
                Result<List<ClusterRoleHost>> result = clusterRoleHostService
                    .listByRegionId(Math.toIntExact(clusterRegion.getId()));
                nodeNamesUnderClusterLogic = result.getData().stream().map(ClusterRoleHost::getNodeSet)
                    .collect(Collectors.toList());
                break;
            case TEMPLATES:
                Result<List<IndexTemplate>> indexTemplates = indexTemplateService
                    .listByRegionId(Math.toIntExact(clusterRegion.getId()));
                nodeNamesUnderClusterLogic = indexTemplates.getData().stream()
                        .filter(filterBelongProjectIdIndexTemplatePre)
                        
                        .map(IndexTemplate::getName)
                    .collect(Collectors.toList());
                break;
            case INDICES:
                Result<List<IndexTemplate>> listResult = indexTemplateService
                    .listByRegionId(Math.toIntExact(clusterRegion.getId()));
                List<IndexTemplate> indexTemplatesList = listResult.getData().stream()
                        .filter(filterBelongProjectIdIndexTemplatePre).collect(Collectors.toList());
                List<CatIndexResult> catIndexResultList = new ArrayList<>();
                indexTemplatesList.forEach(indexTemplate -> catIndexResultList.addAll(esIndexService
                    .syncCatIndexByExpression(clusterRegion.getPhyClusterName(), indexTemplate.getExpression())));
                nodeNamesUnderClusterLogic = catIndexResultList.stream()
                        .map(CatIndexResult::getIndex)
                    .collect(Collectors.toList());
                break;
            default:
                nodeNamesUnderClusterLogic = new ArrayList<>();
        }
        return nodeNamesUnderClusterLogic;
    }
}
package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.CLUSTER_LOGIC;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.GREEN;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.RED;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.UNKNOWN;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.YELLOW;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.biz.page.ClusterLogicPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicStatis;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicAggregate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicTemplateIndexCountVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicTemplateIndexDetailDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterStatusVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterLogicEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ClusterUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterStaticsService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.enums.ResultCode;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClusterLogicManagerImpl implements ClusterLogicManager {

    private static final ILog             LOGGER     = LogFactory.getLog(ClusterLogicManagerImpl.class);

    @Autowired
    private ClusterLogicManager clusterLogicManager;

    @Autowired
    private ESIndexService                esIndexService;

    @Autowired
    private ESClusterStaticsService esClusterStaticsService;

    @Autowired
    private ClusterPhyService             clusterPhyService;

    @Autowired
    private ClusterLogicService           clusterLogicService;

    @Autowired
    private ClusterRegionService          clusterRegionService;

    @Autowired
    private ClusterRoleHostService       clusterRoleHostService;

    @Autowired
    private TemplateSrvManager            templateSrvManager;

    @Autowired
    private IndexTemplateService          indexTemplateService;

    @Autowired
    private TemplateLogicManager          templateLogicManager;

    @Autowired
    private IndexTemplatePhyService       indexTemplatePhyService;

    @Autowired
    private ProjectClusterLogicAuthService projectClusterLogicAuthService;

    @Autowired
    private OperateRecordService          operateRecordService;

    @Autowired
    private ESMachineNormsService         esMachineNormsService;

    @Autowired
    private ProjectService                projectService;

    @Autowired
    private ESClusterNodeService            eSClusterNodeService;

    @Autowired
    private ESGatewayClient               esGatewayClient;

    @Autowired
    private ClusterRegionManager          clusterRegionManager;

    @Autowired
    private ClusterContextManager         clusterContextManager;

    @Autowired
    private ESClusterService              esClusterService;

    @Autowired
    private HandleFactory                 handleFactory;

    @Autowired
    private IndicesManager                indicesManager;

    private static final FutureUtil<Void> futureUtil = FutureUtil.init("ClusterLogicManager", 10, 10, 100);


    /**
     * 构建运维页面的逻辑集群VO
     * @param logicClusters     逻辑集群列表
     * @return 逻辑集群列表
     */
    @Override
    public List<ClusterLogicVO> buildClusterLogics(List<ClusterLogic> logicClusters) {
        if (CollectionUtils.isEmpty(logicClusters)) {
            return Lists.newArrayList();
        }

        List<ClusterLogicVO> clusterLogicVOS = Lists.newArrayList();
        for (ClusterLogic logicCluster : logicClusters) {
            clusterLogicVOS.add(buildClusterLogic(logicCluster));
        }

        Collections.sort(clusterLogicVOS);
        return clusterLogicVOS;
    }

    /**
     * 构建运维页面的逻辑集群VO
     * @param clusterLogic    逻辑集群
     * @return
     */
    @Override
    public ClusterLogicVO buildClusterLogic(ClusterLogic clusterLogic) {
        ClusterLogicVO clusterLogicVO = ConvertUtil.obj2Obj(clusterLogic, ClusterLogicVO.class);

        futureUtil.runnableTask(() -> buildLogicClusterStatus(clusterLogicVO, clusterLogic))
                .runnableTask(() -> buildLogicRole(clusterLogicVO, clusterLogic))
                .runnableTask(() -> buildConsoleClusterVersions(clusterLogicVO));

        
        buildClusterNodeInfo(clusterLogicVO);
        Optional.ofNullable(projectService.getProjectBriefByProjectId(clusterLogic.getProjectId()))
                .map(ProjectBriefVO::getProjectName)
                .ifPresent(clusterLogicVO::setProjectName);

        return clusterLogicVO;
    }

    @Override
    public Result<Void> clearIndices(ConsoleTemplateClearDTO clearDTO, String operator) throws ESOperateException {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }

        if (CollectionUtils.isEmpty(clearDTO.getDelIndices())) {
            return Result.buildParamIllegal("删除索引为空");
        }
        final TupleTwo<Result<Void>, Integer> resultIntegerTuple2 = checkIndices(clearDTO.getDelIndices(),
                clearDTO.getLogicId());
        Result<Void> checkResult = resultIntegerTuple2.v1;
        if (checkResult.failed()) {
            return checkResult;
        }

        LOGGER.info("class=TemplateLogicServiceImpl||method=clearIndex||"
                        + "operator={}||logicId={}||delIndices={}||delQueryDsl={}", operator, clearDTO.getLogicId(),
                JSON.toJSONString(clearDTO.getDelIndices()), clearDTO.getDelQueryDsl());

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService.getLogicTemplateWithPhysicalsById(
                clearDTO.getLogicId());

        if (StringUtils.isNotBlank(clearDTO.getDelQueryDsl())) {
            Result<Void> deleteResult = batchDeletePhysicalTemplateIndicesByQuery(
                    templateLogicWithPhysical.getPhysicals(), clearDTO.getDelQueryDsl(), clearDTO.getDelIndices());
            if (deleteResult.failed()) {
                return deleteResult;
            }
        } else {
            Result<Void> deleteIndicesResult = batchDeletePhysicalTemplateIndices(
                    templateLogicWithPhysical.getPhysicals(), clearDTO.getDelIndices());
            if (deleteIndicesResult.failed()) {
                return deleteIndicesResult;
            }
        }
        operateRecordService.save(
                new OperateRecord.Builder()
                        .bizId(clearDTO.getLogicId())
                        .userOperation(operator)
                        .project(projectService.getProjectBriefByProjectId(resultIntegerTuple2.v2))
                        .content(JSON.toJSONString(clearDTO))
                        .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                        .operationTypeEnum(OperateTypeEnum.TEMPLATE_SERVICE_CLEAN)
                        .build()
                
        );
        if (StringUtils.isNotBlank(clearDTO.getDelQueryDsl())) {
            return Result.buildSucWithTips("删除任务下发成功，请到sense中执行查询语句，确认删除进度");
        } else {
            return Result.buildSucWithTips("索引删除成功");
        }
    }

    /**
     * 获取逻辑集群分派的物理集群列表
     *
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    @Override
    public List<ClusterPhy> getLogicClusterAssignedPhysicalClusters(Long logicClusterId) {
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(logicClusterId);
        if (clusterRegion == null) { return Lists.newArrayList();}

        return Lists.newArrayList(clusterPhyService.getClusterByName(clusterRegion.getPhyClusterName()));
    }

    @Override
    public Result<List<ClusterLogicVO>> getLogicClustersByProjectId(Integer projectId) {
        List<ClusterLogicVO> list = ConvertUtil.list2List(clusterLogicService.getHasAuthClusterLogicsByProjectId(projectId),
                ClusterLogicVO.class);
        for (ClusterLogicVO clusterLogicVO : list) {
            List<String> clusterPhyNames = clusterRegionService.listPhysicClusterNames(clusterLogicVO.getId());
            clusterLogicVO.setPhyClusterAssociated(!AriusObjUtils.isEmptyList(clusterPhyNames));
            clusterLogicVO.setAssociatedPhyClusterName(clusterPhyNames);
            Optional.ofNullable(clusterLogicVO.getProjectId())
                    .map(projectService::getProjectBriefByProjectId)
                    .map(ProjectBriefVO::getProjectName)
                    .ifPresent(clusterLogicVO::setProjectName);
        }
        return Result.buildSucc(list);
    }

    /**
     * 获取APP拥有的集群列表
     * @param projectId projectId
     * @return 集群列表
     */
    @Override
    public Result<List<ClusterLogicVO>> getProjectLogicClusters(Integer projectId) {
    
       if (!projectService.checkProjectExist(projectId)){
           return Result.build(ResultCode.PROJECT_NOT_EXISTS.getCode(),ResultCode.PROJECT_NOT_EXISTS.getMessage());
       }
        
        return Result.buildSucc(
                buildClusterLogics(clusterLogicService.getHasAuthClusterLogicsByProjectId(projectId)));
    }

    @Override
    public Result<List<Tuple<Long/*逻辑集群Id*/, String/*逻辑集群名称*/>>> listProjectClusterLogicIdsAndNames(Integer projectId) {
        List<Tuple<Long/*逻辑集群Id*/, String/*逻辑集群名称*/>> res = Lists.newArrayList();
        List<ClusterLogic> tempAuthLogicClusters = Lists.newArrayList();

        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) { tempAuthLogicClusters.addAll(clusterLogicService.listAllClusterLogics());}
        else { tempAuthLogicClusters.addAll(clusterLogicService.getHasAuthClusterLogicsByProjectId(projectId));}

        for (ClusterLogic clusterLogic : tempAuthLogicClusters) {
            Tuple<Long, String> logicClusterId2logicClusterNameTuple = new Tuple<>();
            logicClusterId2logicClusterNameTuple.setV1(clusterLogic.getId());
            logicClusterId2logicClusterNameTuple.setV2(clusterLogic.getName());
            res.add(logicClusterId2logicClusterNameTuple);
        }
        return Result.buildSucc(res);
    }

    /**
     *
     * @param clusterId
     * @param projectId
     * @return
     */
    @Override
    public Result<ClusterLogicVO> getProjectLogicClusters(Long clusterId, Integer projectId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(clusterId);
        if (clusterLogic == null) {
            return Result.buildNotExist("集群不存在");
        }

        return Result.buildSucc(buildClusterLogic(clusterLogic));
    }

    @Override
    public Result<List<ClusterLogicVO>> getProjectLogicClusterInfoByType(Integer projectId, Integer type) {
        ESLogicClusterDTO logicClusterDTO = new ESLogicClusterDTO();
        logicClusterDTO.setProjectId(projectId);
        logicClusterDTO.setType(type);
        return Result.buildSucc(
                ConvertUtil.list2List(clusterLogicService.listClusterLogics(logicClusterDTO), ClusterLogicVO.class));
    }

    /**
     * 获取逻辑集群所有逻辑模板列表
     * @param request
     * @param clusterId
     * @return
     */
    @Override
    public Result<List<ConsoleTemplateVO>> getClusterLogicTemplates(HttpServletRequest request, Long clusterId) {

        List<ConsoleTemplateVO> result = new ArrayList<>();

        Integer projectId = HttpRequestUtil.getProjectId(request, AuthConstant.SUPER_PROJECT_ID);
        List<IndexTemplateLogicAggregate> aggregates = templateLogicManager.getLogicClusterTemplatesAggregate(clusterId,
                projectId);
        if (CollectionUtils.isNotEmpty(aggregates)) {
            for (IndexTemplateLogicAggregate aggregate : aggregates) {
                result.add(templateLogicManager.fetchConsoleTemplate((aggregate)));
            }
        }

        LOGGER.info("class=ConsoleClusterController||method=getClusterLogicTemplates||clusterId={}||projectId={}",
                clusterId, projectId);

        return Result.buildSucc(result);
    }

    @Override
    public Result<List<ESClusterNodeSepcVO>> listMachineSpec() {
        List<ESMachineNormsPO> esMachineNormsPOS = esMachineNormsService.listMachineNorms();
        return Result.buildSucc(ConvertUtil.list2List(esMachineNormsPOS, ESClusterNodeSepcVO.class));
    }

    @Override
    public List<ClusterLogicVO> getClusterLogics(ESLogicClusterDTO param, Integer projectId) {
        List<ClusterLogic> clusterLogics = clusterLogicService.listClusterLogics(param);
        return buildClusterLogics(clusterLogics);
    }

    @Override
    public ClusterLogicVO getClusterLogic(Long clusterLogicId, Integer currentProjectId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(clusterLogicId);
        ClusterLogicVO clusterLogicVO = ConvertUtil.obj2Obj(clusterLogic, ClusterLogicVO.class);

        futureUtil.runnableTask(() -> buildLogicClusterStatus(clusterLogicVO, clusterLogic))
                .runnableTask(() -> buildOpLogicClusterPermission(clusterLogicVO, currentProjectId))
                .runnableTask(() -> Optional.ofNullable(projectService.getProjectBriefByProjectId(clusterLogicVO.getProjectId()
                        )).map(ProjectBriefVO::getProjectName).ifPresent(clusterLogicVO::setProjectName))
                .runnableTask(() -> buildClusterNodeInfo(clusterLogicVO)).waitExecute();

        return clusterLogicVO;
    }

    @Override
    public Result<Void> addLogicClusterAndClusterRegions(ESLogicClusterWithRegionDTO param, String operator) {
        return clusterRegionManager.batchBindRegionToClusterLogic(param, operator, Boolean.TRUE);
    }

    @Override
    public ClusterLogicVO getConsoleClusterVOByIdAndProjectId(Long clusterLogicId, Integer projectId) {
        if (AriusObjUtils.isNull(clusterLogicId)) {
            return null;
        }

        //这里必须clusterLogicManager为了走spring全局缓存
        List<ClusterLogicVO> clusterLogicList = clusterLogicManager.getClusterLogics(null, projectId);
        if (CollectionUtils.isNotEmpty(clusterLogicList)) {
            for (ClusterLogicVO clusterLogicVO : clusterLogicList) {
                if (clusterLogicId.equals(clusterLogicVO.getId())) {
                    return clusterLogicVO;
                }
            }
        }

        return null;
    }

    @Override
    public Result<Long> addLogicCluster(ESLogicClusterDTO param, String operator, Integer projectId) {
        Result<Long> result = clusterLogicService.createClusterLogic(param);

        if (result.success()) {
			SpringTool.publish(new ClusterLogicEvent(result.getData(), projectId));
            //添加逻辑集群日志
            operateRecordService.save(
                    new OperateRecord.Builder()
                            .project(projectService.getProjectBriefByProjectId(projectId))
                            .operationTypeEnum(OperateTypeEnum.MY_CLUSTER_APPLY)
                            .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                            .userOperation(operator)
                            .content(param.getName())
                            .bizId(result.getData().intValue())
                            .build());
        }
        return result;
    }

    @Override
    public Result<Void> deleteLogicCluster(Long logicClusterId, String operator, Integer projectId)
            throws AdminOperateException {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(logicClusterId);
        final Result<Void> checkProjectCorrectly = ProjectUtils.checkProjectCorrectly(ClusterLogic::getProjectId,
                clusterLogic, projectId);
        if (checkProjectCorrectly.failed()) {
            return checkProjectCorrectly;
        }
        Result<Void> result = clusterLogicService.deleteClusterLogicById(logicClusterId, operator, projectId);
        ClusterLogicTemplateIndexDetailDTO templateIndexVO = getTemplateIndexVO(logicClusterId, projectId);

        for (IndexTemplateLogicAggregate agg : templateIndexVO.getTemplateLogicAggregates()) {
            templateLogicManager.delTemplate(agg.getIndexTemplateLogicWithCluster().getId(), operator);
        }

        indicesManager.deleteIndex(templateIndexVO.getCatIndexResults(),projectId,operator);

        if (result.success()) {
			SpringTool.publish(new ClusterLogicEvent(logicClusterId, projectId));
            //操作记录 集群下线
            operateRecordService.save(
                    new OperateRecord.Builder()
                            .project(projectService.getProjectBriefByProjectId(projectId))
                            .operationTypeEnum(OperateTypeEnum.MY_CLUSTER_OFFLINE)
                            .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                            .content(clusterLogic.getName())
                            .bizId(logicClusterId.intValue())
                            .userOperation(operator)
                            .build());
        }
        return result;
    }

    private ClusterLogicTemplateIndexDetailDTO getTemplateIndexVO(Long logicClusterId, Integer projectId) {
        List<IndexTemplateLogicAggregate> templateLogicAggregates =
                templateLogicManager.getLogicClusterTemplatesAggregate(logicClusterId, projectId);

        List<IndexCatCellDTO> catIndexResults = Lists.newArrayList();
        templateLogicAggregates.forEach(tl -> {

            Integer templateLogic = tl.getIndexTemplateLogicWithCluster().getId();

            IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService.getLogicTemplateWithPhysicalsById(templateLogic);
            if (templateLogicWithPhysical != null) {

                List<IndexTemplatePhy> physicalMasters = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
                for (IndexTemplatePhy physicalMaster : physicalMasters) {
                    List<CatIndexResult> catIndexResultList = esIndexService.syncCatIndexByExpression(physicalMaster.getCluster(),
                            physicalMaster.getExpression());
                    catIndexResultList.forEach(catIndexResult -> {
                        IndexCatCellDTO indicesClearDTO = new IndexCatCellDTO();
                        indicesClearDTO.setIndex(catIndexResult.getIndex());
                        indicesClearDTO.setCluster(physicalMaster.getCluster());
                        catIndexResults.add(indicesClearDTO);
                    });
                }

            }
        });

        ClusterLogicTemplateIndexDetailDTO templateIndexVO = new ClusterLogicTemplateIndexDetailDTO();
        templateIndexVO.setCatIndexResults(catIndexResults);
        templateIndexVO.setTemplateLogicAggregates(templateLogicAggregates);
        return templateIndexVO;
    }

    @Override
	public Result<Void> editLogicCluster(ESLogicClusterDTO param, String operator, Integer projectId) {
        final ClusterLogic logic = clusterLogicService.getClusterLogicById(param.getId());
        Result<Void> result = clusterLogicService.editClusterLogic(param, operator,projectId);
        if (result.success()) {
            SpringTool.publish(new ClusterLogicEvent(param.getId(), projectId));
            //操作记录 我的集群信息修改
            if (StringUtils.isNotBlank(param.getMemo())){
                operateRecordService.save(
                    new OperateRecord.Builder()
                            .project(projectService.getProjectBriefByProjectId(projectId))
                            .operationTypeEnum(OperateTypeEnum.MY_CLUSTER_INFO_MODIFY)
                            .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                            .userOperation(operator)
                            .content(String.format("%s修改集群描述，%s-->%s",logic.getName(),logic.getMemo(),param.getMemo()))
                            .bizId(param.getId().intValue())
                            .build()
                    );
            }
            
        }
		return result;
	}

    @Override
    public PaginationResult<ClusterLogicVO> pageGetClusterLogics(ClusterLogicConditionDTO condition, Integer projectId) {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(CLUSTER_LOGIC.getPageSearchType());
        if (baseHandle instanceof ClusterLogicPageSearchHandle) {
            ClusterLogicPageSearchHandle pageSearchHandle = (ClusterLogicPageSearchHandle) baseHandle;
            return pageSearchHandle.doPage(condition, projectId);
        }

        LOGGER.warn(
                "class=ClusterLogicManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the ClusterLogicPageSearchHandle");
        return PaginationResult.buildFail("分页获取逻辑集群信息失败");
    }

    @Override
    public boolean updateClusterLogicHealth(Long clusterLogicId) {

        ESLogicClusterDTO updateLogicClusterDTO = new ESLogicClusterDTO();
        Set<Integer>      clusterHealthSet      = Sets.newHashSet();
        updateLogicClusterDTO.setId(clusterLogicId);
        try {
            ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(clusterLogicId);
            if (null == clusterLogicContext) {
                LOGGER.error(
                    "class=ClusterLogicManagerImpl||method=updateClusterLogicHealth||clusterLogicId={}||errMsg=clusterLogicContext is empty",
                    clusterLogicId);
                clusterHealthSet.add(UNKNOWN.getCode());
            }else {
                 List<String> associatedClusterPhyNames = clusterLogicContext.getAssociatedClusterPhyNames();
                clusterHealthSet.addAll(associatedClusterPhyNames
                        .stream()
                        .map(esClusterService::syncGetClusterHealthEnum)
                        .map(ClusterHealthEnum::getCode)
                        .collect(Collectors.toSet()));
            }

            updateLogicClusterDTO.setHealth(ClusterUtils.getClusterLogicHealthByClusterHealth(clusterHealthSet));
            clusterLogicService.editClusterLogicNotCheck(updateLogicClusterDTO, AriusUser.SYSTEM.getDesc());
        } catch (Exception e) {
            LOGGER.error("class=ClusterLogicManagerImpl||method=updateClusterLogicHealth||clusterLogicId={}||errMsg={}", clusterLogicId,
                    e.getMessage(), e);
            return false;
        }

        return true;
    }

    @Override
    public Result<ClusterLogicTemplateIndexCountVO> indexTemplateCount(Long clusterId, String operator, Integer projectId) {
        ClusterLogicTemplateIndexDetailDTO detailVO = getTemplateIndexVO(clusterId, projectId);
        ClusterLogicTemplateIndexCountVO countVO = new ClusterLogicTemplateIndexCountVO();
        countVO.setCatIndexResults(detailVO.getCatIndexResults().size());
        countVO.setTemplateLogicAggregates(detailVO.getTemplateLogicAggregates().size());
        return Result.buildSucc(countVO);
    }

    @Override
    public PaginationResult<ESClusterRoleHostVO> nodesPage(Long clusterLogicId,ClusterLogicNodeConditionDTO condition) {
        ClusterRegion clusterRegion =  clusterRegionService.getRegionByLogicClusterId(clusterLogicId);
        Result<List<ClusterRoleHost>> result = clusterRoleHostService.listByRegionId(Math.toIntExact(clusterRegion.getId()));
        List<ClusterRoleHost> nodes=Collections.emptyList();
        if (result.success()){
            nodes = result.getData();
        }else {
            return PaginationResult.buildFail("获取节点失败！");
        }
        if (StringUtils.isNotBlank(condition.getKeyword())) {
            nodes = nodes.stream().filter(n -> n.getNodeSet().contains(condition.getKeyword())).collect(Collectors.toList());
        }
        return PaginationResult.buildSucc(rowBounds(condition.getPage().intValue(), condition.getSize().intValue(), nodes),
                nodes.size(), condition.getPage(), condition.getSize());
    }

    @Override
    public Result<Long> estimatedDiskSize(Long clusterLogicId, Integer count) {
        ClusterRegion clusterRegion =  clusterRegionService.getRegionByLogicClusterId(clusterLogicId);
        if (clusterRegion == null) {
            return Result.buildFail("此逻集群未绑定regin！");
        }
        Map<String, Triple<Long, Long, Double>> map = eSClusterNodeService.syncGetNodesDiskUsage(clusterRegion.getPhyClusterName());
        Triple<Long, Long, Double> diskInfo = getFirstOrNull(map);
        Long size = 1073741824L;
        return Result.buildSucc(diskInfo == null?count*size:count * diskInfo.v1());
    }

    @Override
    public Result<List<String>> getProjectLogicClusterNameByType(Integer projectId, Integer type) {
        ESLogicClusterDTO logicClusterDTO = new ESLogicClusterDTO();
        logicClusterDTO.setProjectId(projectId);
        logicClusterDTO.setType(type);
        return Result.buildSucc(clusterLogicService.listClusterLogics(logicClusterDTO)
                .stream().map(ClusterLogic::getName).collect(Collectors.toList()));
    }

/**************************************************** private method ****************************************************/
    /**
     * 获取map中第⼀个数据值
     *
     * @param map 数据源
     * @return
     */
    private static Triple<Long, Long, Double> getFirstOrNull(Map<String, Triple<Long, Long, Double>> map) {
        Triple<Long, Long, Double> obj = null;
        for (Map.Entry<String, Triple<Long, Long, Double>> entry : map.entrySet()) {
            obj = entry.getValue();
            if (obj != null) {
                break;
            }
        }
        return  obj;
    }
    /**
     * 构建OP逻辑集群权限
     * @param clusterLogicVO  逻辑集群
     * @param projectIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     */
    private void buildOpLogicClusterPermission(ClusterLogicVO clusterLogicVO, Integer projectIdForAuthJudge) {
        if (clusterLogicVO == null) {
            return;
        }

        if (projectIdForAuthJudge == null) {
            // 未指定需要判断权限的app，取运维人员权限
            clusterLogicVO.setAuthId(null);
            clusterLogicVO.setAuthType(ProjectClusterLogicAuthEnum.OWN.getCode());
            clusterLogicVO.setPermissions(ProjectClusterLogicAuthEnum.OWN.getDesc());
        } else {
            // 指定了需要判断权限的app
            buildLogicClusterPermission(clusterLogicVO, projectIdForAuthJudge);
        }
    }

    /**
     * 构建ES集群版本
     * @param logicCluster 逻辑集群
     */
    private void buildConsoleClusterVersions(ClusterLogicVO logicCluster) {
        try {
            if (logicCluster != null) {
                List<ClusterPhy> physicalClusters = getLogicClusterAssignedPhysicalClusters(logicCluster.getId());
                if (CollectionUtils.isEmpty(physicalClusters)) { return;}

                List<String> esClusterVersions = physicalClusters.stream().map(ClusterPhy::getEsVersion).distinct().collect(Collectors.toList());
                logicCluster.setEsClusterVersions(esClusterVersions);
            }
        } catch (Exception e) {
            LOGGER.warn("class=LogicClusterManager||method=buildConsoleClusterVersions||logicClusterId={}",
                    logicCluster.getId(), e);
        }
    }

    /**
     * 设置app对指定逻辑集群的权限
     * @param logicClusterVO      逻辑集群
     * @param projectIdForAuthJudge 需要判断的app的ID
     */
    private void buildLogicClusterPermission(ClusterLogicVO logicClusterVO, Integer projectIdForAuthJudge) {
        try {
            if (logicClusterVO == null || projectIdForAuthJudge == null) {
                return;
            }
            if (AuthConstant.SUPER_PROJECT_ID.equals(projectIdForAuthJudge)) {
                logicClusterVO.setAuthType(   ProjectClusterLogicAuthEnum.OWN.getCode());
                logicClusterVO.setPermissions(ProjectClusterLogicAuthEnum.OWN.getDesc());
                return;
            }

            ProjectClusterLogicAuth auth = projectClusterLogicAuthService.getLogicClusterAuth(projectIdForAuthJudge,
                    logicClusterVO.getId());

            if (auth == null) {
                // 没有权限
                logicClusterVO.setAuthId(null);
                logicClusterVO.setAuthType(ProjectClusterLogicAuthEnum.NO_PERMISSIONS.getCode());
                logicClusterVO.setPermissions(ProjectClusterLogicAuthEnum.NO_PERMISSIONS.getDesc());
            } else {
                // 有权限
                logicClusterVO.setAuthId(auth.getId());
                logicClusterVO.setAuthType(ProjectClusterLogicAuthEnum.valueOf(auth.getType()).getCode());
                logicClusterVO.setPermissions(ProjectClusterLogicAuthEnum.valueOf(auth.getType()).getDesc());
            }


        } catch (Exception e) {
            LOGGER.warn("class=LogicClusterManager||method=buildLogicClusterPermission||logicClusterId={}",
                    logicClusterVO.getId(), e);
        }
    }

    /**
     * 更新逻辑集群状态信息
     */
    private void buildLogicClusterStatus(ClusterLogicVO logicCluster, ClusterLogic clusterLogic) {
        ClusterLogicStatis esClusterLogicStatus = buildDefaultLogicStatus();
        try {
            ClusterLogicStatis clusterLogicStatus = getClusterLogicStatus(clusterLogic.getId());
            if (null != clusterLogicStatus) {
                esClusterLogicStatus = clusterLogicStatus;
            }
        } catch (Exception e) {
            LOGGER.error("class=ClusterLogicManagerImpl||method=buildLogicClusterStatus||logicClusterId={}",
                    logicCluster.getId(), e);
        }

        logicCluster.setClusterStatus(ConvertUtil.obj2Obj(esClusterLogicStatus, ConsoleClusterStatusVO.class));
    }

    /**
     * 创建默认逻辑集群状态
     * @return
     */
    private ClusterLogicStatis buildDefaultLogicStatus() {
        ClusterLogicStatis logicStatus = new ClusterLogicStatis();
        logicStatus.setStatus( ClusterHealthEnum.RED.getDesc());
        logicStatus.setDocNu(0.0);
        logicStatus.setIndexNu(0);
        logicStatus.setTotalDisk(0.0);
        logicStatus.setUsedDisk(0.0);
        return logicStatus;
    }

    private void buildLogicRole(ClusterLogicVO logicCluster, ClusterLogic clusterLogic) {
        if (logicCluster != null) {
            try {
                Long logicClusterId = logicCluster.getId();

                List<String> phyClusterNames = clusterRegionService.listPhysicClusterNames(logicClusterId);
                if (CollectionUtils.isEmpty(phyClusterNames)) {
                    return;
                }

                //拿第一个物理集群的client、master信息，因为只有Arius维护的大公共共享集群才会有一个逻辑集群映射成多个物理集群
                ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyClusterNames.get(0));
                if (null == clusterPhy) {
                    return;
                }

                List<ClusterRoleInfo> esRolePhyClusters = clusterPhy.getClusterRoleInfos();
                List<ClusterRoleHost> esRolePhyClusterHosts = clusterPhy.getClusterRoleHosts();

                logicCluster.setEsClusterRoleVOS(buildESRoleClusterVOS(clusterLogic, logicClusterId, esRolePhyClusters, esRolePhyClusterHosts));
            } catch (Exception e) {
                LOGGER.warn("class=LogicClusterManager||method=buildLogicRole||logicClusterId={}", logicCluster.getId(),
                        e);
            }
        }
    }

    private List<ESClusterRoleVO> buildESRoleClusterVOS(ClusterLogic clusterLogic, Long logicClusterId,
                                                        List<ClusterRoleInfo> esRolePhyClusters,
                                                        List<ClusterRoleHost> esRolePhyClusterHosts) {
        List<ESClusterRoleVO> esClusterRoleVOS = new ArrayList<>();
        for (ClusterRoleInfo clusterRoleInfo : esRolePhyClusters) {
            ESClusterRoleVO esClusterRoleVO = ConvertUtil.obj2Obj(clusterRoleInfo, ESClusterRoleVO.class);

            List<ESClusterRoleHostVO> esClusterRoleHostVOS = new ArrayList<>();

            //如果是datanode节点，那么使用逻辑集群申请的节点个数和阶段规格配置
            if (DATA_NODE.getDesc().equals(clusterRoleInfo.getRoleClusterName())) {
                esClusterRoleVO.setPodNumber(clusterLogic.getDataNodeNu());
                esClusterRoleVO.setMachineSpec(clusterLogic.getDataNodeSpec());

                ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(logicClusterId);
                if (null != clusterRegion) {
                    Result<List<ClusterRoleHost>> ret = clusterRoleHostService.listByRegionId(clusterRegion.getId().intValue());
                    if (ret.success() && CollectionUtils.isNotEmpty(ret.getData())) {
                        for (ClusterRoleHost clusterRoleHost : ret.getData()) {
                            ESClusterRoleHostVO esClusterRoleHostVO = new ESClusterRoleHostVO();
                            esClusterRoleHostVO.setHostname(clusterRoleHost.getIp());
                            esClusterRoleHostVO.setRole(DATA_NODE.getCode());

                            esClusterRoleHostVOS.add(esClusterRoleHostVO);
                        }
                    }
                }
            } else {
                for (ClusterRoleHost clusterRoleHost : esRolePhyClusterHosts) {
                    if (clusterRoleHost.getRoleClusterId().longValue() == clusterRoleInfo.getId().longValue()) {
                        esClusterRoleHostVOS
                                .add(ConvertUtil.obj2Obj(clusterRoleHost, ESClusterRoleHostVO.class));
                    }
                }
            }

            esClusterRoleVO.setEsClusterRoleHostVO(esClusterRoleHostVOS);
            esClusterRoleVO.setPodNumber(esClusterRoleHostVOS.size());
            esClusterRoleVOS.add(esClusterRoleVO);
        }
        return esClusterRoleVOS;
    }

    private void buildLogicClusterTemplateSrvs(ClusterLogicVO logicCluster) {
        try {
            Result<List<ClusterTemplateSrv>> listResult = templateSrvManager.getLogicClusterTemplateSrv(
                    logicCluster.getId());
            if (null != listResult && listResult.success()) {
                logicCluster.setEsClusterTemplateSrvVOS(
                        ConvertUtil.list2List(listResult.getData(), ESClusterTemplateSrvVO.class));
            }
        } catch (Exception e) {
            LOGGER.warn("class=LogicClusterManager||method=buildLogicClusterTemplateSrvs||logicClusterId={}",
                    logicCluster.getId(), e);
        }
    }

    /**
     * 构建节点信息:
     * 1. 是否关联物理集群
     * 2. 获取关联物理集群列表
     * 3. 逻辑集群拥有的数据节点数
     * 4. 防止没有关联物理集群, 或者取消关联region, 逻辑集群状态为red
     * 5. 获取gateway地址
     */
    private void buildClusterNodeInfo(ClusterLogicVO clusterLogicVO) {
        ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(
                clusterLogicVO.getId());
        if (null == clusterLogicContext) {
            return;
        }

        //1. 是否关联物理集群
        clusterLogicVO.setPhyClusterAssociated(clusterLogicContext.getAssociatedPhyNum() > 0);

        //2. 获取关联物理集群列表
        if (CollectionUtils.isNotEmpty(clusterLogicContext.getAssociatedClusterPhyNames())) {
            clusterLogicVO.setAssociatedPhyClusterName(clusterLogicContext.getAssociatedClusterPhyNames());
        }

        //3. 逻辑集群拥有的数据节点数
        clusterLogicVO.setDataNodesNumber(clusterLogicContext.getAssociatedDataNodeNum());

        //4. 没有关联物理集群下, 逻辑集群状态至为red
        if (clusterLogicVO.getPhyClusterAssociated().equals(false)) {
            clusterLogicVO.getClusterStatus().setStatus(RED.getDesc());
        }

        //5. 获取gateway地址
        clusterLogicVO.setGatewayAddress(esGatewayClient.getGatewayAddress());
    }

    private TupleTwo<Result<Void>,/*projectId*/Integer> checkIndices(List<String> delIndices, Integer logicId) {
        for (String index : delIndices) {
            if (index.endsWith("*")) {
                return Tuples.of(Result.buildParamIllegal(
                        "索引名字不能以*结尾"),null);
            }
        }

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
                .getLogicTemplateWithPhysicalsById(logicId);
        IndexTemplatePhy templatePhysical = templateLogicWithPhysical.getMasterPhyTemplate();

        List<String> matchIndices = indexTemplatePhyService.getMatchNoVersionIndexNames(templatePhysical.getId());
        for (String index : delIndices) {
            if (!matchIndices.contains(index)) {
                return Tuples.of(Result.buildParamIllegal(index + "不属于该索引模板"),null);
            }
        }
        return Tuples.of(Result.buildSucc(),
                templateLogicWithPhysical.getProjectId());
    }

    /**
     * 批量删除物理模板对应分区索引
     * @param physicals 物理模板列表
     * @param delIndices 待删除分区索引列表
     * @return
     */
    private Result<Void> batchDeletePhysicalTemplateIndices(List<IndexTemplatePhy> physicals, List<String> delIndices) {
        for (IndexTemplatePhy templatePhysical : physicals) {
            if (templatePhysical.getVersion() > 0) {
                List<String> delIndicesWithVersion = genDeleteIndicesWithVersion(delIndices);

                LOGGER.info(
                        "class=TemplateLogicServiceImpl||method=clearIndex||templateName={}||version={}||indices={}",
                        templatePhysical.getName(), templatePhysical.getVersion(), delIndicesWithVersion);

                if (CollectionUtils.isNotEmpty(delIndicesWithVersion)) {
                    esIndexService.syncBatchDeleteIndices(templatePhysical.getCluster(), delIndicesWithVersion, 3);
                }
            }

            int count = delIndices.size();
            if (count != esIndexService.syncBatchDeleteIndices(templatePhysical.getCluster(), delIndices, 3)) {
                return Result.buildFail("删除索引失败，请重试");
            }
        }

        return Result.buildSucc();
    }

    /**
     * 通过请求的方式批量删除物理模板分区索引
     * @param physicals 物理模板
     * @param delQueryDsl DSL语句
     * @param delIndices 删除索引列表
     * @return
     */
    private Result<Void> batchDeletePhysicalTemplateIndicesByQuery(List<IndexTemplatePhy> physicals, String delQueryDsl,
                                                                   List<String> delIndices) throws ESOperateException {
        if (StringUtils.isNotBlank(delQueryDsl)) {
            for (IndexTemplatePhy templatePhysical : physicals) {
                if (!esIndexService.syncDeleteByQuery(templatePhysical.getCluster(), delIndices, delQueryDsl)) {
                    return Result.buildFail("删除索引失败，请重试");
                }
            }
        }

        return Result.buildSucc();
    }

    /**
     * 生成带有版本模式的待删除索引列表
     *
     * @param delIndices 待删除索引列表
     * @return
     */
    private List<String> genDeleteIndicesWithVersion(List<String> delIndices) {
        List<String> indicesWithVersion = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(delIndices)) {
            for (String delIndex : delIndices) {
                indicesWithVersion.add(delIndex + "_v*");
            }
        }

        return indicesWithVersion;
    }

    private ClusterLogicStatis getClusterLogicStatus(Long logicClusterId) {
        ClusterLogicStatis clusterLogicStatis = new ClusterLogicStatis();
        
        //获取集群上下文
        ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(logicClusterId);
        if (null == clusterLogicContext) {
            return null;
        }

        //设置逻辑集群名称
        clusterLogicStatis.setName(clusterLogicContext.getClusterLogicName());
        clusterLogicStatis.setId(logicClusterId);

        List<ESClusterStatsResponse> esClusterStatsResponseList = clusterLogicContext.getAssociatedClusterPhyNames()
                .stream().map(esClusterService::syncGetClusterStats).collect(Collectors.toList());

        //设置基础数据
        clusterLogicStatis.setIndexNu(esClusterStatsResponseList.stream().mapToLong(ESClusterStatsResponse::getIndexCount).sum());
        clusterLogicStatis.setDocNu(esClusterStatsResponseList.stream().mapToDouble(ESClusterStatsResponse::getDocsCount).sum());
        clusterLogicStatis.setTotalDisk(esClusterStatsResponseList.stream().mapToDouble(item -> item.getTotalFs().getBytes()).sum());
        clusterLogicStatis.setUsedDisk(esClusterStatsResponseList.stream().mapToDouble(item -> item.getTotalFs().getBytes() -
                item.getFreeFs().getBytes()).sum());

        //设置逻辑集群状态
        Set<String> statusSet = esClusterStatsResponseList.stream().map(ESClusterStatsResponse::getStatus).collect(Collectors.toSet());
        clusterLogicStatis.setStatus(getClusterLogicStatus(statusSet));
        return clusterLogicStatis;
    }

    /**
     * 获取逻辑集群状态
     * @return ClusterStatusEnum
     */
    private String getClusterLogicStatus(Set<String> statusSet) {
        if (statusSet.contains(RED.getDesc())) {
            return RED.getDesc();
        }

        if (statusSet.size() == 1 && statusSet.contains(GREEN.getDesc())) {
            return GREEN.getDesc();
        }

        if (statusSet.contains(YELLOW.getDesc())) {
            return YELLOW.getDesc();
        }
        return RED.getDesc();
    }

    public static List rowBounds(int pageNum, int pageSize, List list) {
        pageNum = pageNum - 1;
        pageNum = Math.max(pageNum, 0);
        // 默认至少返回5行
        pageSize = Math.max(pageSize, 5);
        int startRow = 0;
        int endRow = 0;
        if (list == null || list.size() == 0) {
            return list;
        }
        int totalCount = list.size();
        startRow = pageNum > 0 ? pageNum * pageSize : 0;
        endRow = startRow + pageSize;
        endRow = Math.min(endRow, totalCount);
        list = list.subList(startRow, endRow);
        return list;
    }

}
package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.CLUSTER_LOGIC;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.GREEN;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.RED;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.UNKNOWN;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.YELLOW;
import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.CLUSTER_LOGIC;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.UNKNOWN;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.RESOURCE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;

import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.biz.page.ClusterLogicPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicStatis;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterNodeSepc;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicAggregate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterStatusVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterLogicEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ClusterUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.enums.ResultCode;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Component
public class ClusterLogicManagerImpl implements ClusterLogicManager {

    private static final ILog             LOGGER     = LogFactory.getLog(ClusterLogicManagerImpl.class);

    @Autowired
    private ClusterLogicManager           clusterLogicManager;

    @Autowired
    private ESIndexService                esIndexService;

    @Autowired
    private ESClusterStaticsService       esClusterStaticsService;

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
    private ClusterNodeManager            clusterNodeManager;

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
    
    private static final FutureUtil<Void> futureUtil = FutureUtil.init("ClusterLogicManager", 10,10,100);

    /**
     * 构建运维页面的逻辑集群VO
     * @param logicClusters     逻辑集群列表
     * @param projectIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     * @return
     */
    @Override
    public List<ConsoleClusterVO> batchBuildOpClusterVOs(List<ClusterLogic> logicClusters,
                                                         Integer projectIdForAuthJudge) {
        if (CollectionUtils.isEmpty(logicClusters)) {
            return Lists.newArrayList();
        }

        List<ConsoleClusterVO> consoleClusterVOS = Lists.newArrayList();
        for (ClusterLogic logicCluster : logicClusters) {
            consoleClusterVOS.add(buildOpClusterVO(logicCluster, projectIdForAuthJudge));
        }

        Collections.sort(consoleClusterVOS);
        return consoleClusterVOS;
    }

    /**
     * 构建运维页面的逻辑集群VO
     * @param clusterLogic    逻辑集群
     * @param projectIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     * @return
     */
    @Override
    public ConsoleClusterVO buildOpClusterVO(ClusterLogic clusterLogic, Integer projectIdForAuthJudge) {
        ConsoleClusterVO consoleClusterVO = ConvertUtil.obj2Obj(clusterLogic, ConsoleClusterVO.class);

        futureUtil.runnableTask(() -> buildLogicClusterStatus(consoleClusterVO, clusterLogic))
            .runnableTask(() -> buildLogicRole(consoleClusterVO, clusterLogic))
            .runnableTask(() -> buildConsoleClusterVersions(consoleClusterVO))
            .runnableTask(() -> buildOpLogicClusterPermission(consoleClusterVO, projectIdForAuthJudge))
            .runnableTask(() -> buildLogicClusterTemplateSrvs(consoleClusterVO)).waitExecute();

        //依赖获取集群状态, 不能使用FutureUtil, 否则抛NPE
        buildClusterNodeInfo(consoleClusterVO);
        
        consoleClusterVO.setAppName(projectService.getProjectBriefByProjectId(consoleClusterVO.getProjectId()).getProjectName());

        return consoleClusterVO;
    }

    /**
     * 获取逻辑集群所有访问的APP
     *
     * @param logicClusterId 逻辑集群ID
     * @return
     */
   /**
    @Override
    public Result<List<ConsoleAppVO>> getAccessAppsOfLogicCluster(Long logicClusterId) {
        List<Integer> appsResult = esClusterStaticsService
                .getLogicClusterAccessInfo(logicClusterId, 7);

        if (CollectionUtils.isEmpty(appsResult)) {
            return Result.buildSucc();
        }

        List<App> apps = appService.listApps();
        Map<Integer, App> id2AppMap = ConvertUtil.list2Map(apps, App::getId);

        List<App> appsRet = appsResult.stream().map(id2AppMap::get).collect( Collectors.toList());
        return Result.buildSucc(ConvertUtil.list2List(appsRet, ConsoleAppVO.class));

    }
    **/

    @Override
    public Result<Void> clearIndices(ConsoleTemplateClearDTO clearDTO, String operator) throws ESOperateException {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }

        if (CollectionUtils.isEmpty(clearDTO.getDelIndices())) {
            return Result.buildParamIllegal("删除索引为空");
        }

        Result<Void> checkResult = checkIndices(clearDTO.getDelIndices(), clearDTO.getLogicId());
        if (checkResult.failed()) {
            return checkResult;
        }

        LOGGER.info("class=TemplateLogicServiceImpl||method=clearIndex||"
                        + "operator={}||logicId={}||delIndices={}||delQueryDsl={}",
                operator, clearDTO.getLogicId(), JSON.toJSONString(clearDTO.getDelIndices()), clearDTO.getDelQueryDsl());

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
                .getLogicTemplateWithPhysicalsById(clearDTO.getLogicId());

        if (StringUtils.isNotBlank(clearDTO.getDelQueryDsl())) {
            Result<Void> deleteResult = batchDeletePhysicalTemplateIndicesByQuery(templateLogicWithPhysical.getPhysicals(),
                    clearDTO.getDelQueryDsl(), clearDTO.getDelIndices());
            if (deleteResult.failed()) {
                return deleteResult;
            }
        } else {
            Result<Void> deleteIndicesResult = batchDeletePhysicalTemplateIndices(templateLogicWithPhysical.getPhysicals(),
                    clearDTO.getDelIndices());
            if (deleteIndicesResult.failed()) {
                return deleteIndicesResult;
            }
        }

        operateRecordService.save(TEMPLATE, DELETE_INDEX, clearDTO.getLogicId(), JSON.toJSONString(clearDTO), operator);
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
    public Result<List<ConsoleClusterVO>> getProjectLogicClusterInfo(Integer projectId) {
        List<ConsoleClusterVO> list = ConvertUtil.list2List(clusterLogicService.getHasAuthClusterLogicsByProjectId(
                projectId), ConsoleClusterVO.class);
        for(ConsoleClusterVO consoleClusterVO : list) {
            List<String> clusterPhyNames = clusterRegionService.listPhysicClusterNames(consoleClusterVO.getId());
            consoleClusterVO.setPhyClusterAssociated(!AriusObjUtils.isEmptyList(clusterPhyNames));
            consoleClusterVO.setAssociatedPhyClusterName(clusterPhyNames);
        }
        return Result.buildSucc(list);
    }

    /**
     * 获取APP拥有的集群列表
     * @param projectId
     * @return
     */
    @Override
    public Result<List<ConsoleClusterVO>> getProjectLogicClusters(Integer projectId) {
    
       if (projectService.checkProjectExist(projectId)){
           return Result.build(ResultCode.PROJECT_NOT_EXISTS.getCode(),ResultCode.PROJECT_NOT_EXISTS.getMessage());
       }
        

        return Result.buildSucc(batchBuildOpClusterVOs(clusterLogicService.getHasAuthClusterLogicsByProjectId(projectId),
                projectId));
    }

    @Override
    public Result<List<String>> getAppLogicOrPhysicClusterNames(Integer projectId) {
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return Result.buildSucc(clusterPhyService.listAllClusters().stream().map(ClusterPhy::getCluster).collect(Collectors.toList()));
        }
        List<ClusterLogic> appAuthLogicClusters = clusterLogicService.getHasAuthClusterLogicsByProjectId(projectId);
        return Result.buildSucc(appAuthLogicClusters.stream().map(ClusterLogic::getName).collect(Collectors.toList()));
    }

    /**
     * 获取平台所有的集群列表
     * @param projectId
     * @return
     */
    @Override
    public Result<List<ConsoleClusterVO>> getDataCenterLogicClusters(Integer projectId) {

        List<ClusterLogic> logicClusters = clusterLogicService.listAllClusterLogics();
        List<ConsoleClusterVO> consoleClusterVOS = ConvertUtil.list2List(logicClusters, ConsoleClusterVO.class);
        if (projectId != null &&projectService.checkProjectExist(projectId) && CollectionUtils.isNotEmpty(consoleClusterVOS)) {
            consoleClusterVOS.forEach(consoleClusterVO -> buildOpLogicClusterPermission(consoleClusterVO, projectId));
        }
        Collections.sort(consoleClusterVOS);
        return Result.buildSucc(consoleClusterVOS);
    }

    /**
     *
     * @param clusterId
     * @param projectId
     * @return
     */
    @Override
    public Result<ConsoleClusterVO> getProjectLogicClusters(Long clusterId, Integer projectId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(clusterId);
        if (clusterLogic == null) {
            return Result.buildNotExist("集群不存在");
        }

        return Result.buildSucc(buildOpClusterVO(clusterLogic, projectId));
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
        List<IndexTemplateLogicAggregate> aggregates =templateLogicManager.getLogicClusterTemplatesAggregate(clusterId,
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

    /**
     * 获取指定逻辑集群datanode的规格接口
     * @param clusterId
     * @return
     */
    @Override
    public Result<Set<ESClusterNodeSepcVO>> getLogicClusterDataNodeSpec(Long clusterId) {
        Set<RoleClusterNodeSepc> roleClusterNodeSepcs = clusterLogicService.getLogicDataNodeSepc(clusterId);

        return Result.buildSucc(ConvertUtil.set2Set(roleClusterNodeSepcs, ESClusterNodeSepcVO.class));
    }

    @Override
    public Result<List<ESClusterNodeSepcVO>> listMachineSpec() {
        List<ESMachineNormsPO> esMachineNormsPOS = esMachineNormsService.listMachineNorms();
        return Result.buildSucc(ConvertUtil.list2List(esMachineNormsPOS, ESClusterNodeSepcVO.class));
    }

    @Override
    public List<ConsoleClusterVO> getConsoleClusterVOS(ESLogicClusterDTO param, Integer projectId) {
        List<ClusterLogic> clusterLogics = clusterLogicService.listClusterLogics(param);
        return batchBuildOpClusterVOs(clusterLogics, projectId);
    }

    @Override
    public ConsoleClusterVO getConsoleCluster(Long clusterLogicId, Integer currentProjectId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(clusterLogicId);
        ConsoleClusterVO consoleClusterVO = ConvertUtil.obj2Obj(clusterLogic, ConsoleClusterVO.class);

        futureUtil.runnableTask(() -> buildLogicClusterStatus(consoleClusterVO, clusterLogic))
                    .runnableTask(() -> buildOpLogicClusterPermission(consoleClusterVO, currentProjectId))
                    .runnableTask(() -> consoleClusterVO.setAppName(projectService.getProjectBriefByProjectId(consoleClusterVO.getProjectId()).getProjectName()))
                    .runnableTask(() -> buildClusterNodeInfo(consoleClusterVO))
                    .waitExecute();

        return consoleClusterVO;
    }

    @Override
    public Result<Void> addLogicClusterAndClusterRegions(ESLogicClusterWithRegionDTO param, String operator) {
        return clusterRegionManager.batchBindRegionToClusterLogic(param, operator, Boolean.TRUE);
    }

	@Override
    public ConsoleClusterVO getConsoleClusterVOByIdAndProjectId(Long clusterLogicId, Integer projectId) {
        if(AriusObjUtils.isNull(clusterLogicId)){return null;}

        //这里必须clusterLogicManager为了走spring全局缓存
        List<ConsoleClusterVO> consoleClusterVOS = clusterLogicManager.getConsoleClusterVOS(null, projectId);
        if(CollectionUtils.isNotEmpty(consoleClusterVOS)){
            for (ConsoleClusterVO consoleClusterVO : consoleClusterVOS) {
                if (clusterLogicId.equals(consoleClusterVO.getId())) {
                    return consoleClusterVO;
                }
            }
        }

        return null;
    }

	@Override
	public Result<Long> addLogicCluster(ESLogicClusterDTO param, String operator,
			Integer projectId) {
		Result<Long> result = clusterLogicService.createClusterLogic(param);

		if (result.success()) {
			SpringTool.publish(new ClusterLogicEvent(result.getData(), projectId));
			operateRecordService.save(RESOURCE, ADD, result.getData(), "创建逻辑集群", operator);
		}
		return result;
	}

	@Override
	public Result<Void> deleteLogicCluster(Long logicClusterId, String operator, Integer projectId) throws AdminOperateException {
		Result<Void> result = clusterLogicService.deleteClusterLogicById(logicClusterId, operator);
		if (result.success()) {
			SpringTool.publish(new ClusterLogicEvent(logicClusterId, projectId));
			operateRecordService.save(RESOURCE, DELETE, logicClusterId, "", operator);
		}
		return result;
	}

	@Override
	public Result<Void> editLogicCluster(ESLogicClusterDTO param, String operator, Integer projectId) {
        Result<Void> result = clusterLogicService.editClusterLogic(param, operator);
        if (result.success()) {
            SpringTool.publish(new ClusterLogicEvent(param.getId(), projectId));
            operateRecordService.save(RESOURCE, EDIT, param.getId(), String.valueOf(param.getId()), operator);
        }
		return result;
	}

    @Override
    public PaginationResult<ConsoleClusterVO> pageGetConsoleClusterVOS(ClusterLogicConditionDTO condition, Integer projectId) {
        BaseHandle baseHandle     = handleFactory.getByHandlerNamePer(CLUSTER_LOGIC.getPageSearchType());
        if (baseHandle instanceof ClusterLogicPageSearchHandle) {
            ClusterLogicPageSearchHandle handle     =  (ClusterLogicPageSearchHandle) baseHandle;
            return handle.doPageHandle(condition, condition.getAuthType(), projectId);
        }

        LOGGER.warn("class=ClusterLogicManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the ClusterLogicPageSearchHandle");
        return PaginationResult.buildFail("分页获取逻辑集群信息失败");
    }

    @Override
    public List<ClusterLogic> getClusterLogicByProjectIdAndAuthType(Integer projectId, Integer authType) {
        if (projectService.checkProjectExist(projectId)) {
            return Lists.newArrayList();
        }

        //超级用户对所有模板都是管理权限
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId) && !ProjectClusterLogicAuthEnum.OWN.getCode().equals(authType)) {
            return Lists.newArrayList();
        }

        if (!ProjectClusterLogicAuthEnum.isExitByCode(authType)) {
            return Lists.newArrayList();
        }

        switch (ProjectClusterLogicAuthEnum.valueOf(authType)) {
            case OWN:
                if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
                    return clusterLogicService.listAllClusterLogics();
                } else {
                    return clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
                }
            case ACCESS:
                return getProjectAccessClusterLogicList(projectId);

            case NO_PERMISSIONS:
                List<Long> appOwnAuthClusterLogicIdList = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId)
                        .stream()
                        .map(ClusterLogic::getId)
                        .collect(Collectors.toList());

                List<Long> appAccessAuthClusterLogicIdList = getProjectAccessClusterLogicList(projectId)
                        .stream()
                        .map(ClusterLogic::getId)
                        .collect(Collectors.toList());

                List<ClusterLogic> allClusterLogicList  =  clusterLogicService.listAllClusterLogics();

                return allClusterLogicList.stream()
                        .filter(clusterLogic -> !appOwnAuthClusterLogicIdList.contains(clusterLogic.getId())
                                && !appAccessAuthClusterLogicIdList.contains(clusterLogic.getId()))
                        .collect(Collectors.toList());
            default:
                return Lists.newArrayList();

        }
    }

    @Override
    public List<ClusterLogic> getProjectAccessClusterLogicList(Integer projectId) {
        List<Long> clusterLogicIdList = projectClusterLogicAuthService.getLogicClusterAccessAuths(projectId)
                                        .stream()
                                        .map(ProjectClusterLogicAuth::getLogicClusterId)
                                        .collect(Collectors.toList());

        return clusterLogicService.getClusterLogicListByIds(clusterLogicIdList);
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
    public Result<List<ConsoleClusterVO>> getProjectLogicClusterInfoByType(Integer projectId, Integer type) {
        ESLogicClusterDTO logicClusterDTO = new ESLogicClusterDTO();
        logicClusterDTO.setProjectId(projectId);
        logicClusterDTO.setType(type);
        return Result.buildSucc(ConvertUtil.list2List(clusterLogicService.listClusterLogics(logicClusterDTO), ConsoleClusterVO.class));
    }

/**************************************************** private method ****************************************************/
    /**
     * 构建OP逻辑集群权限
     * @param consoleClusterVO  逻辑集群
     * @param projectIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     */
    private void buildOpLogicClusterPermission(ConsoleClusterVO consoleClusterVO, Integer projectIdForAuthJudge) {
        if (consoleClusterVO == null) {
            return;
        }

        if (projectIdForAuthJudge == null) {
            // 未指定需要判断权限的app，取运维人员权限
            consoleClusterVO.setAuthId(null);
            consoleClusterVO.setAuthType( ProjectClusterLogicAuthEnum.OWN.getCode());
            consoleClusterVO.setPermissions(ProjectClusterLogicAuthEnum.OWN.getDesc());
        } else {
            // 指定了需要判断权限的app
            buildLogicClusterPermission(consoleClusterVO, projectIdForAuthJudge);
        }
    }

    /**
     * 构建ES集群版本
     * @param logicCluster 逻辑集群
     */
    private void buildConsoleClusterVersions(ConsoleClusterVO logicCluster) {
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
    private void buildLogicClusterPermission(ConsoleClusterVO logicClusterVO, Integer projectIdForAuthJudge) {
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
    private void buildLogicClusterStatus(ConsoleClusterVO logicCluster, ClusterLogic clusterLogic) {
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

    private void buildLogicRole(ConsoleClusterVO logicCluster, ClusterLogic clusterLogic) {
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

    private List<ESClusterRoleVO> buildESRoleClusterVOS(ClusterLogic clusterLogic, Long logicClusterId, List<ClusterRoleInfo> esRolePhyClusters, List<ClusterRoleHost> esRolePhyClusterHosts) {
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

    private void buildLogicClusterTemplateSrvs(ConsoleClusterVO logicCluster) {
        try {
            Result<List<ClusterTemplateSrv>> listResult = templateSrvManager
                    .getLogicClusterTemplateSrv(logicCluster.getId());
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
    private void buildClusterNodeInfo(ConsoleClusterVO consoleClusterVO) {
        ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(consoleClusterVO.getId());
        if (null == clusterLogicContext) {
            return;
        }

        //1. 是否关联物理集群
        consoleClusterVO.setPhyClusterAssociated(clusterLogicContext.getAssociatedPhyNum() > 0);

        //2. 获取关联物理集群列表
        if (CollectionUtils.isNotEmpty(clusterLogicContext.getAssociatedClusterPhyNames())) {
            consoleClusterVO.setAssociatedPhyClusterName(clusterLogicContext.getAssociatedClusterPhyNames());
        }

        //3. 逻辑集群拥有的数据节点数
        consoleClusterVO.setDataNodesNumber(clusterLogicContext.getAssociatedDataNodeNum());

        //4. 没有关联物理集群下, 逻辑集群状态至为red
        if (consoleClusterVO.getPhyClusterAssociated().equals(false)) {
            consoleClusterVO.getClusterStatus().setStatus("red");
        }

        //5. 获取gateway地址
        consoleClusterVO.setGatewayAddress(esGatewayClient.getGatewayAddress());
    }

    private Result<Void> checkIndices(List<String> delIndices, Integer logicId) {
        for (String index : delIndices) {
            if (index.endsWith("*")) {
                return Result.buildParamIllegal("索引名字不能以*结尾");
            }
        }

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
                .getLogicTemplateWithPhysicalsById(logicId);
        IndexTemplatePhy templatePhysical = templateLogicWithPhysical.getAnyOne();

        List<String> matchIndices = indexTemplatePhyService.getMatchNoVersionIndexNames(templatePhysical.getId());
        for (String index : delIndices) {
            if (!matchIndices.contains(index)) {
                return Result.buildParamIllegal(index + "不属于该索引模板");
            }
        }
        return Result.buildSucc();
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
}
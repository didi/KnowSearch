package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.CLUSTER_LOGIC;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.GREEN;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.RED;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.UNKNOWN;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.YELLOW;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.RESOURCE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.DELETE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.DELETE_INDEX;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.biz.page.ClusterLogicPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
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
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterStatusVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterLogicEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ClusterUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterStaticsService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClusterLogicManagerImpl implements ClusterLogicManager {

    private static final ILog             LOGGER     = LogFactory.getLog(ClusterLogicManagerImpl.class);

    @Autowired
    private ClusterRegionService esClusterRackService;

    @Autowired
    private ClusterLogicNodeService       clusterLogicNodeService;

    @Autowired
    private ClusterLogicManager           clusterLogicManager;

    @Autowired
    private ESIndexService                esIndexService;

    @Autowired
    private ESClusterStaticsService       esClusterStaticsService;

    @Autowired
    private ClusterPhyService             esClusterPhyService;

    @Autowired
    private ClusterLogicService           clusterLogicService;

    @Autowired
    private ClusterRegionService clusterRegionService;

    @Autowired
    private TemplateSrvManager            templateSrvManager;

    @Autowired
    private IndexTemplateService indexTemplateService;
    @Autowired
    private ProjectService projectService;
    /**
     * 后续需要下线
     */
    @Autowired
    @Deprecated
    private AppService appService;

    @Autowired
    private TemplateLogicManager          templateLogicManager;

    @Autowired
    private TemplateQuotaManager          templateQuotaManager;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private AppClusterLogicAuthService appClusterLogicAuthService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private ESMachineNormsService         esMachineNormsService;

    

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
        
        consoleClusterVO.setAppName(projectService.getProjectDetailByProjectId(consoleClusterVO.getProjectId()).getProjectName());

        return consoleClusterVO;
    }

    /**
     * 获取逻辑集群所有访问的APP
     *
     * @param logicClusterId 逻辑集群ID
     * @return
     */
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

        //加入判断templateQuotaManager 不为空的情况
        if (Objects.nonNull(templateQuotaManager)&&!templateQuotaManager.controlAndPublish(clearDTO.getLogicId())) {
            LOGGER.warn(
                    "class=TemplateLogicServiceImpl||method=clearIndices||templateLogicId={}||msg=template quota publish failed!",
                    clearDTO.getLogicId());
        }

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
        // TODO： 原有根据物理集群寻找管理逻辑集群Logic可以重构下。
        // 目前可以不用Fix，未来所有跟Rack关联都可以换成是Region关联；
        List<ClusterPhy> logicClusterAssignedClusters = new ArrayList<>();
        if (logicClusterId != null) {
            Set<String> clusters = parseClusterNames(clusterRegionService.listLogicClusterRacks(logicClusterId));
            for (String cluster : clusters) {
                ClusterPhy physicalCluster = esClusterPhyService.getClusterByName(cluster);
                if (physicalCluster != null) {
                    logicClusterAssignedClusters.add(physicalCluster);
                }
            }

            LOGGER.info("class=ClusterManager||method=getLogicClusterAssignedPhysicalClusters"
                            + "||logicClusterId={}||clusters={}||logicClusterAssignedClusters={}",
                    logicClusterId, JSON.toJSONString(clusters), JSON.toJSONString(logicClusterAssignedClusters));
        }

        return logicClusterAssignedClusters;
    }

    @Override
    public Result<List<ConsoleClusterVO>> getAppLogicClusterInfo(Integer projectId) {
        List<ConsoleClusterVO> list = ConvertUtil.list2List(clusterLogicService.getHasAuthClusterLogicsByAppId(
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
    public Result<List<ConsoleClusterVO>> getAppLogicClusters(Integer projectId) {
    
        try {
            projectService.getProjectDetailByProjectId(projectId);
        } catch (LogiSecurityException logiSecurityException) {
            final String[] error = logiSecurityException.getMessage().split("-");
            return Result.build(Integer.parseInt(error[0]), error[1]);
        }
        

        return Result.buildSucc(batchBuildOpClusterVOs(clusterLogicService.getHasAuthClusterLogicsByAppId(projectId),
                projectId));
    }

    @Override
    public Result<List<String>> getAppLogicOrPhysicClusterNames(Integer projectId) {
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return Result.buildSucc(esClusterPhyService.listAllClusters().stream().map(ClusterPhy::getCluster).collect(Collectors.toList()));
        }
        List<ClusterLogic> appAuthLogicClusters = clusterLogicService.getHasAuthClusterLogicsByAppId(projectId);
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
        if (projectId != null && CollectionUtils.isNotEmpty(consoleClusterVOS)) {
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
    public Result<ConsoleClusterVO> getAppLogicClusters(Long clusterId, Integer projectId) {
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

        Integer appId = HttpRequestUtil.getProjectId(request, AdminConstant.DEFAULT_APP_ID);
        List<IndexTemplateLogicAggregate> aggregates =templateLogicManager.getLogicClusterTemplatesAggregate(clusterId,
                appId);
        if (CollectionUtils.isNotEmpty(aggregates)) {
            for (IndexTemplateLogicAggregate aggregate : aggregates) {
                result.add(templateLogicManager.fetchConsoleTemplate((aggregate)));
            }
        }

        LOGGER.info("class=ConsoleClusterController||method=getClusterLogicTemplates||clusterId={}||appId={}",
                clusterId, appId);

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
    public Result<List<ESClusterRoleHostVO>> getLogicClusterNodes(Long clusterId) {
        return Result.buildSucc(clusterNodeManager
                .convertClusterLogicNodes(clusterLogicNodeService.getLogicClusterNodesIncludeNonDataNodes(clusterId)));
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
                    .runnableTask(() -> consoleClusterVO.setAppName(projectService.getProjectDetailByProjectId(consoleClusterVO.getProjectId()).getProjectName()))
                    .runnableTask(() -> buildClusterNodeInfo(consoleClusterVO))
                    .waitExecute();

        return consoleClusterVO;
    }

    @Override
    public Result<Void> addLogicClusterAndClusterRegions(ESLogicClusterWithRegionDTO param, String operator) {
        return clusterRegionManager.batchBindRegionToClusterLogic(param, operator, Boolean.TRUE);
    }

	@Override
    public ConsoleClusterVO getConsoleClusterVOByIdAndAppId(Long clusterLogicId, Integer projectId) {
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
    public List<ClusterLogic> getClusterLogicByAppIdAndAuthType(Integer projectId, Integer authType) {
        final ProjectBriefVO projectBriefVO = projectService.getProjectBriefByProjectId(projectId);
        if (Objects.isNull(projectBriefVO)) {
            return Lists.newArrayList();
        }

        //超级用户对所有模板都是管理权限
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId) && !AppClusterLogicAuthEnum.OWN.getCode().equals(authType)) {
            return Lists.newArrayList();
        }

        if (!AppClusterLogicAuthEnum.isExitByCode(authType)) {
            return Lists.newArrayList();
        }

        switch (AppClusterLogicAuthEnum.valueOf(authType)) {
            case OWN:
                if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
                    return clusterLogicService.listAllClusterLogics();
                } else {
                    return clusterLogicService.getOwnedClusterLogicListByAppId(projectId);
                }
            case ACCESS:
                return getAppAccessClusterLogicList(projectId);

            case NO_PERMISSIONS:
                List<Long> appOwnAuthClusterLogicIdList = clusterLogicService.getOwnedClusterLogicListByAppId(projectId)
                        .stream()
                        .map(ClusterLogic::getId)
                        .collect(Collectors.toList());

                List<Long> appAccessAuthClusterLogicIdList = getAppAccessClusterLogicList(projectId)
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
    public List<ClusterLogic> getAppAccessClusterLogicList(Integer projectId) {
        List<Long> clusterLogicIdList = appClusterLogicAuthService.getLogicClusterAccessAuths(projectId)
                                        .stream()
                                        .map(AppClusterLogicAuth::getLogicClusterId)
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
    public Result<Void> checkTemplateDataSizeValidForCreate(Long logicClusterId, String templateSize) {
        //所有的磁盘大小类型统一到字节数进行比较
        float beSetDiskSizeOfTemplate = Float.valueOf(SizeUtil.getUnitSize(templateSize + "gb"));

        //获取逻辑集群绑定的region
        List<ClusterRegion> clusterRegions = clusterRegionService.listLogicClusterRegions(logicClusterId);
        if (CollectionUtils.isEmpty(clusterRegions)) {
            return Result.buildFail("指定的逻辑集群没有绑定任何物理集群的region");
        }

        //对于region按照物理集群名称进行分组
        Map<String, List<ClusterRegion>> cluster2RegionListMap = ConvertUtil.list2MapOfList(clusterRegions,
                ClusterRegion::getPhyClusterName, c -> c);

        //这里设置用户侧在模板中可以设置的最大的数据大小
        float canCreateTemplateDiskSize = 0F;

        //对于不同分组下的region的可使用磁盘大小进行计算
        for (Map.Entry</*物理集群名称*/String, /*逻辑绑定的物理集群下的region列表*/List<ClusterRegion>> entry : cluster2RegionListMap.entrySet()) {
            //获取指定物理集群的r关于rack的磁盘总量分布情况
            Map</*rack*/String, /*rack上的磁盘总量*/Float> allocationInfoOfRackMap = esClusterService.getAllocationInfoOfRack(entry.getKey());
            if (MapUtils.isEmpty(allocationInfoOfRackMap)) { continue;}

            //统计region可以使用的最大磁盘容量
            for (ClusterRegion clusterRegion : entry.getValue()) {
                canCreateTemplateDiskSize = Float.max(canCreateTemplateDiskSize, esClusterPhyService.getSurplusDiskSizeOfRacks(clusterRegion.getPhyClusterName(),
                        clusterRegion.getRacks(), allocationInfoOfRackMap));
            }

        }

        if (canCreateTemplateDiskSize > beSetDiskSizeOfTemplate) {
            return Result.buildSuccWithMsg("模板数据大小设置成功");
        }

        return Result.buildFail("当前数据大小超过最大可设置大小"
                + SizeUtil.getUnitSizeAndFormat((long) canCreateTemplateDiskSize, 0).replace(".0", "").toUpperCase(Locale.ROOT) + ",请输入正确数值");
    }

    @Override
    public Result<List<ConsoleClusterVO>> getAppLogicClusterInfoByType(Integer projectId, Integer type) {
        ESLogicClusterDTO logicClusterDTO = new ESLogicClusterDTO();
        logicClusterDTO.setAppId(projectId);
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
            consoleClusterVO.setAuthType( AppClusterLogicAuthEnum.OWN.getCode());
            consoleClusterVO.setPermissions(AppClusterLogicAuthEnum.OWN.getDesc());
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
            //共享类型物理集群版本去重
            Set<String> esClusterVersions = Sets.newHashSet();
            if (logicCluster != null) {
                List<ClusterPhy> physicalClusters = getLogicClusterAssignedPhysicalClusters(logicCluster.getId());

                for (ClusterPhy physicalCluster : physicalClusters) {
                    esClusterVersions.add(physicalCluster.getEsVersion());
                }

                logicCluster.setEsClusterVersions(Lists.newArrayList(esClusterVersions));
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
                logicClusterVO.setAuthType(   AppClusterLogicAuthEnum.OWN.getCode());
                logicClusterVO.setPermissions(AppClusterLogicAuthEnum.OWN.getDesc());
                return;
            }

            AppClusterLogicAuth auth = appClusterLogicAuthService.getLogicClusterAuth(projectIdForAuthJudge,
                    logicClusterVO.getId());

            if (auth == null) {
                // 没有权限
                logicClusterVO.setAuthId(null);
                logicClusterVO.setAuthType(AppClusterLogicAuthEnum.NO_PERMISSIONS.getCode());
                logicClusterVO.setPermissions(AppClusterLogicAuthEnum.NO_PERMISSIONS.getDesc());
            } else {
                // 有权限
                logicClusterVO.setAuthId(auth.getId());
                logicClusterVO.setAuthType(AppClusterLogicAuthEnum.valueOf(auth.getType()).getCode());
                logicClusterVO.setPermissions(AppClusterLogicAuthEnum.valueOf(auth.getType()).getDesc());
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

                List<String> phyClusterNames = esClusterRackService.listPhysicClusterNames(logicClusterId);
                if (CollectionUtils.isEmpty(phyClusterNames)) {
                    return;
                }

                //拿第一个物理集群的client、master信息，因为只有Arius维护的大公共共享集群才会有一个逻辑集群映射成多个物理集群
                ClusterPhy clusterPhy = esClusterPhyService.getClusterByName(phyClusterNames.get(0));
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

                List<ClusterRoleHost> clusterRoleHosts = clusterLogicNodeService
                        .getLogicClusterNodes(logicClusterId);

                for (ClusterRoleHost clusterRoleHost : clusterRoleHosts) {
                    ESClusterRoleHostVO esClusterRoleHostVO = new ESClusterRoleHostVO();
                    esClusterRoleHostVO.setHostname(clusterRoleHost.getIp());
                    esClusterRoleHostVO.setRole(DATA_NODE.getCode());

                    esClusterRoleHostVOS.add(esClusterRoleHostVO);
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

    /**
     * 解析集群名称
     *
     * @param racks RACK列表
     * @return
     */
    private Set<String> parseClusterNames(List<ClusterLogicRackInfo> racks) {
        Set<String> clusters = new HashSet<>();
        if (CollectionUtils.isNotEmpty(racks)) {
            for (ClusterLogicRackInfo rackInfo : racks) {
                clusters.add(rackInfo.getPhyClusterName());
            }
        }

        return clusters;
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
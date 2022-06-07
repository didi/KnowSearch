package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.biz.page.ClusterLogicPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicAggregate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
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
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.CLUSTER_LOGIC;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.UNKNOWN;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.RESOURCE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;

@Component
public class ClusterLogicManagerImpl implements ClusterLogicManager {

    private static final ILog LOGGER = LogFactory.getLog(ClusterLogicManagerImpl.class);

    @Autowired
    private ClusterRegionService esClusterRackService;

    @Autowired
    private ClusterLogicNodeService clusterLogicNodeService;

    @Autowired
    private ClusterLogicManager clusterLogicManager;

    @Autowired
    private ESIndexService esIndexService;

    @Autowired
    private ESClusterStaticsService esClusterStaticsService;

    @Autowired
    private ClusterPhyService esClusterPhyService;

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private ClusterRegionService clusterRegionService;

    @Autowired
    private TemplateSrvManager templateSrvManager;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private TemplateLogicManager templateLogicManager;

    @Autowired
    private TemplateQuotaManager templateQuotaManager;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private AppClusterLogicAuthService appClusterLogicAuthService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private ESMachineNormsService esMachineNormsService;

    @Autowired
    private AppService appService;

    @Autowired
    private ClusterNodeManager clusterNodeManager;

    @Autowired
    private ESGatewayClient esGatewayClient;

    @Autowired
    private ClusterRegionManager clusterRegionManager;

    @Autowired
    private ClusterContextManager clusterContextManager;

    @Autowired
    private ESClusterService esClusterService;

    @Autowired
    private HandleFactory handleFactory;

    @Autowired
    private IndicesManager indicesManager;

    private static final FutureUtil<Void> futureUtil = FutureUtil.init("ClusterLogicManager", 10, 10, 100);

    /**
     * 构建运维页面的逻辑集群VO
     *
     * @param logicClusters     逻辑集群列表
     * @param appIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     * @return 逻辑集群列表
     */
    @Override
    public List<ClusterLogicVO> batchBuildOpClusterVOs(List<ClusterLogic> logicClusters, Integer appIdForAuthJudge) {
        if (CollectionUtils.isEmpty(logicClusters)) {
            return Lists.newArrayList();
        }

        List<ClusterLogicVO> clusterLogicVOS = Lists.newArrayList();
        for (ClusterLogic logicCluster : logicClusters) {
            clusterLogicVOS.add(buildOpClusterVO(logicCluster, appIdForAuthJudge));
        }

        Collections.sort(clusterLogicVOS);
        return clusterLogicVOS;
    }

    /**
     * 构建运维页面的逻辑集群VO
     *
     * @param clusterLogic      逻辑集群
     * @param appIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     * @return
     */
    @Override
    public ClusterLogicVO buildOpClusterVO(ClusterLogic clusterLogic, Integer appIdForAuthJudge) {
        ClusterLogicVO clusterLogicVO = ConvertUtil.obj2Obj(clusterLogic, ClusterLogicVO.class);

        futureUtil.runnableTask(() -> buildLogicClusterStatus(clusterLogicVO, clusterLogic))
                .runnableTask(() -> buildLogicRole(clusterLogicVO, clusterLogic))
                .runnableTask(() -> buildConsoleClusterVersions(clusterLogicVO))
                .runnableTask(() -> buildOpLogicClusterPermission(clusterLogicVO, appIdForAuthJudge))
                .runnableTask(() -> buildLogicClusterTemplateSrvs(clusterLogicVO)).waitExecute();

        //依赖获取集群状态, 不能使用FutureUtil, 否则抛NPE
        buildClusterNodeInfo(clusterLogicVO);
        clusterLogicVO.setAppName(appService.getAppName(clusterLogicVO.getAppId()));

        return clusterLogicVO;
    }

    /**
     * 获取逻辑集群所有访问的APP
     *
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    @Override
    public Result<List<ConsoleAppVO>> getAccessAppsOfLogicCluster(Long logicClusterId) {
        List<Integer> appsResult = esClusterStaticsService.getLogicClusterAccessInfo(logicClusterId, 7);

        if (CollectionUtils.isEmpty(appsResult)) {
            return Result.buildSucc();
        }

        List<App> apps = appService.listApps();
        Map<Integer, App> id2AppMap = ConvertUtil.list2Map(apps, App::getId);

        List<App> appsRet = appsResult.stream().map(id2AppMap::get).collect(Collectors.toList());
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

        operateRecordService.save(TEMPLATE, DELETE_INDEX, clearDTO.getLogicId(), JSON.toJSONString(clearDTO), operator);

        //加入判断templateQuotaManager 不为空的情况
        if (Objects.nonNull(templateQuotaManager) && !templateQuotaManager.controlAndPublish(clearDTO.getLogicId())) {
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
                            + "||logicClusterId={}||clusters={}||logicClusterAssignedClusters={}", logicClusterId,
                    JSON.toJSONString(clusters), JSON.toJSONString(logicClusterAssignedClusters));
        }

        return logicClusterAssignedClusters;
    }

    @Override
    public Result<List<ClusterLogicVO>> getAppLogicClusterInfo(Integer appId) {
        List<ClusterLogicVO> list = ConvertUtil.list2List(clusterLogicService.getHasAuthClusterLogicsByAppId(appId),
                ClusterLogicVO.class);
        for (ClusterLogicVO clusterLogicVO : list) {
            List<String> clusterPhyNames = clusterRegionService.listPhysicClusterNames(clusterLogicVO.getId());
            clusterLogicVO.setPhyClusterAssociated(!AriusObjUtils.isEmptyList(clusterPhyNames));
            clusterLogicVO.setAssociatedPhyClusterName(clusterPhyNames);
        }
        return Result.buildSucc(list);
    }

    /**
     * 获取APP拥有的集群列表
     * @param appId appId
     * @return 集群列表
     */
    @Override
    public Result<List<ClusterLogicVO>> getAppLogicClusters(Integer appId) {

        if (appService.getAppById(appId) == null) {
            return Result.buildNotExist("应用不存在");
        }

        return Result.buildSucc(
                batchBuildOpClusterVOs(clusterLogicService.getHasAuthClusterLogicsByAppId(appId), appId));
    }

    @Override
    public Result<List<String>> getAppLogicOrPhysicClusterNames(Integer appId) {
        if (appService.isSuperApp(appId)) {
            return Result.buildSucc(esClusterPhyService.listAllClusters().stream().map(ClusterPhy::getCluster)
                    .collect(Collectors.toList()));
        }
        List<ClusterLogic> appAuthLogicClusters = clusterLogicService.getHasAuthClusterLogicsByAppId(appId);
        return Result.buildSucc(appAuthLogicClusters.stream().map(ClusterLogic::getName).collect(Collectors.toList()));
    }

    /**
     * 获取平台所有的集群列表
     *
     * @param appId
     * @return
     */
    @Override
    public Result<List<ClusterLogicVO>> getDataCenterLogicClusters(Integer appId) {

        List<ClusterLogic> logicClusters = clusterLogicService.listAllClusterLogics();
        List<ClusterLogicVO> clusterLogicVOS = ConvertUtil.list2List(logicClusters, ClusterLogicVO.class);
        if (appId != null && CollectionUtils.isNotEmpty(clusterLogicVOS)) {
            clusterLogicVOS.forEach(consoleClusterVO -> buildOpLogicClusterPermission(consoleClusterVO, appId));
        }
        Collections.sort(clusterLogicVOS);
        return Result.buildSucc(clusterLogicVOS);
    }

    /**
     * @param clusterId
     * @param appId
     * @return
     */
    @Override
    public Result<ClusterLogicVO> getAppLogicClusters(Long clusterId, Integer appId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(clusterId);
        if (clusterLogic == null) {
            return Result.buildNotExist("集群不存在");
        }

        return Result.buildSucc(buildOpClusterVO(clusterLogic, appId));
    }

    /**
     * 获取逻辑集群所有逻辑模板列表
     *
     * @param request
     * @param clusterId
     * @return
     */
    @Override
    public Result<List<ConsoleTemplateVO>> getClusterLogicTemplates(HttpServletRequest request, Long clusterId) {

        List<ConsoleTemplateVO> result = new ArrayList<>();

        Integer appId = HttpRequestUtils.getAppId(request, AdminConstant.DEFAULT_APP_ID);
        List<IndexTemplateLogicAggregate> aggregates = templateLogicManager.getLogicClusterTemplatesAggregate(clusterId,
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


    @Override
    public Result<List<ESClusterNodeSepcVO>> listMachineSpec() {
        List<ESMachineNormsPO> esMachineNormsPOS = esMachineNormsService.listMachineNorms();
        return Result.buildSucc(ConvertUtil.list2List(esMachineNormsPOS, ESClusterNodeSepcVO.class));
    }

    @Override
    public List<ClusterLogicVO> getConsoleClusterVOS(ESLogicClusterDTO param, Integer appId) {
        List<ClusterLogic> clusterLogics = clusterLogicService.listClusterLogics(param);
        return batchBuildOpClusterVOs(clusterLogics, appId);
    }

    @Override
    public ClusterLogicVO getConsoleCluster(Long clusterLogicId, Integer currentAppId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(clusterLogicId);
        ClusterLogicVO clusterLogicVO = ConvertUtil.obj2Obj(clusterLogic, ClusterLogicVO.class);

        futureUtil.runnableTask(() -> buildLogicClusterStatus(clusterLogicVO, clusterLogic))
                .runnableTask(() -> buildOpLogicClusterPermission(clusterLogicVO, currentAppId))
                .runnableTask(() -> clusterLogicVO.setAppName(appService.getAppName(clusterLogicVO.getAppId())))
                .runnableTask(() -> buildClusterNodeInfo(clusterLogicVO)).waitExecute();

        return clusterLogicVO;
    }

    @Override
    public Result<Void> addLogicClusterAndClusterRegions(ESLogicClusterWithRegionDTO param, String operator) {
        return clusterRegionManager.batchBindRegionToClusterLogic(param, operator, Boolean.TRUE);
    }

    @Override
    public ClusterLogicVO getConsoleClusterVOByIdAndAppId(Long clusterLogicId, Integer appId) {
        if (AriusObjUtils.isNull(clusterLogicId)) {
            return null;
        }

        //这里必须clusterLogicManager为了走spring全局缓存
        List<ClusterLogicVO> clusterLogicVOS = clusterLogicManager.getConsoleClusterVOS(null, appId);
        if (CollectionUtils.isNotEmpty(clusterLogicVOS)) {
            for (ClusterLogicVO clusterLogicVO : clusterLogicVOS) {
                if (clusterLogicId.equals(clusterLogicVO.getId())) {
                    return clusterLogicVO;
                }
            }
        }

        return null;
    }

    @Override
    public Result<Long> addLogicCluster(ESLogicClusterDTO param, String operator, Integer appId) {
        Result<Long> result = clusterLogicService.createClusterLogic(param);

        if (result.success()) {
            SpringTool.publish(new ClusterLogicEvent(result.getData(), appId));
            operateRecordService.save(RESOURCE, ADD, result.getData(), "创建逻辑集群", operator);
        }
        return result;
    }

    @Override
    public Result<Void> deleteLogicCluster(Long logicClusterId, String operator, Integer appId)
            throws AdminOperateException {
        Result<Void> result = clusterLogicService.deleteClusterLogicById(logicClusterId, operator);
        ClusterLogicTemplateIndexDetailDTO templateIndexVO = getTemplateIndexVO(logicClusterId, appId);

        for (IndexTemplateLogicAggregate agg : templateIndexVO.getTemplateLogicAggregates()) {
            templateLogicManager.delTemplate(agg.getIndexTemplateLogicWithCluster().getId(), operator);
        }

        indicesManager.batchDeleteIndex(templateIndexVO.getCatIndexResults(),appId,operator);

        if (result.success()) {
            SpringTool.publish(new ClusterLogicEvent(logicClusterId, appId));
            operateRecordService.save(RESOURCE, DELETE, logicClusterId, "", operator);
        }
        return result;
    }

    private ClusterLogicTemplateIndexDetailDTO getTemplateIndexVO(Long logicClusterId, Integer appId) {
        List<IndexTemplateLogicAggregate> templateLogicAggregates =
                templateLogicManager.getLogicClusterTemplatesAggregate(logicClusterId, appId);

        List<IndicesClearDTO> catIndexResults = Lists.newArrayList();
        templateLogicAggregates.forEach(tl -> {

            Integer templateLogic = tl.getIndexTemplateLogicWithCluster().getId();

            IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService.getLogicTemplateWithPhysicalsById(templateLogic);
            if (templateLogicWithPhysical != null) {

                List<IndexTemplatePhy> physicalMasters = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
                for (IndexTemplatePhy physicalMaster : physicalMasters) {
                    List<CatIndexResult> catIndexResultList = esIndexService.syncCatIndexByExpression(physicalMaster.getCluster(),
                            physicalMaster.getExpression());
                    catIndexResultList.forEach(catIndexResult -> {
                        IndicesClearDTO indicesClearDTO = new IndicesClearDTO();
                        indicesClearDTO.setIndex(catIndexResult.getIndex());
                        indicesClearDTO.setClusterPhyName(physicalMaster.getCluster());
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
    public Result<Void> editLogicCluster(ESLogicClusterDTO param, String operator, Integer appId) {
        Result<Void> result = clusterLogicService.editClusterLogic(param, operator);
        if (result.success()) {
            SpringTool.publish(new ClusterLogicEvent(param.getId(), appId));
            operateRecordService.save(RESOURCE, EDIT, param.getId(), String.valueOf(param.getId()), operator);
        }
        return result;
    }

    @Override
    public PaginationResult<ClusterLogicVO> pageGetClusterLogicVOS(ClusterLogicConditionDTO condition, Integer appId) {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(CLUSTER_LOGIC.getPageSearchType());
        if (baseHandle instanceof ClusterLogicPageSearchHandle) {
            ClusterLogicPageSearchHandle pageSearchHandle = (ClusterLogicPageSearchHandle) baseHandle;
            return pageSearchHandle.selectPage(condition, appId);
        }

        LOGGER.warn(
                "class=ClusterLogicManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the ClusterLogicPageSearchHandle");
        return PaginationResult.buildFail("分页获取逻辑集群信息失败");
    }

    @Override
    public List<ClusterLogic> getClusterLogicByAppIdAndAuthType(Integer appId, Integer authType) {
        if (!appService.isAppExists(appId)) {
            return Lists.newArrayList();
        }

        //超级用户对所有模板都是管理权限
        if (appService.isSuperApp(appId) && !AppClusterLogicAuthEnum.OWN.getCode().equals(authType)) {
            return Lists.newArrayList();
        }

        if (!AppClusterLogicAuthEnum.isExitByCode(authType)) {
            return Lists.newArrayList();
        }

        switch (AppClusterLogicAuthEnum.valueOf(authType)) {
            case OWN:
                if (appService.isSuperApp(appId)) {
                    return clusterLogicService.listAllClusterLogics();
                } else {
                    return clusterLogicService.getOwnedClusterLogicListByAppId(appId);
                }
            case ACCESS:
                return getAppAccessClusterLogicList(appId);

            case NO_PERMISSIONS:
                List<Long> appOwnAuthClusterLogicIdList = clusterLogicService.getOwnedClusterLogicListByAppId(appId)
                        .stream().map(ClusterLogic::getId).collect(Collectors.toList());

                List<Long> appAccessAuthClusterLogicIdList = getAppAccessClusterLogicList(appId).stream()
                        .map(ClusterLogic::getId).collect(Collectors.toList());

                List<ClusterLogic> allClusterLogicList = clusterLogicService.listAllClusterLogics();

                return allClusterLogicList.stream()
                        .filter(clusterLogic -> !appOwnAuthClusterLogicIdList.contains(clusterLogic.getId())
                                && !appAccessAuthClusterLogicIdList.contains(clusterLogic.getId()))
                        .collect(Collectors.toList());
            default:
                return Lists.newArrayList();

        }
    }

    @Override
    public List<ClusterLogic> getAppAccessClusterLogicList(Integer appId) {
        List<Long> clusterLogicIdList = appClusterLogicAuthService.getLogicClusterAccessAuths(appId).stream()
                .map(AppClusterLogicAuth::getLogicClusterId).collect(Collectors.toList());

        return clusterLogicService.getClusterLogicListByIds(clusterLogicIdList);
    }

    @Override
    public boolean updateClusterLogicHealth(Long clusterLogicId) {

        ESLogicClusterDTO updateLogicClusterDTO = new ESLogicClusterDTO();
        Set<Integer> clusterHealthSet = Sets.newHashSet();
        updateLogicClusterDTO.setId(clusterLogicId);
        try {
            ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(clusterLogicId);
            if (null == clusterLogicContext) {
                LOGGER.error(
                        "class=ClusterLogicManagerImpl||method=updateClusterLogicHealth||clusterLogicId={}||errMsg=clusterLogicContext is empty",
                        clusterLogicId);
                clusterHealthSet.add(UNKNOWN.getCode());
            } else {
                List<String> associatedClusterPhyNames = clusterLogicContext.getAssociatedClusterPhyNames();
                clusterHealthSet.addAll(
                        associatedClusterPhyNames.stream().map(esClusterService::syncGetClusterHealthEnum)
                                .map(ClusterHealthEnum::getCode).collect(Collectors.toSet()));
            }

            updateLogicClusterDTO.setHealth(ClusterUtils.getClusterLogicHealthByClusterHealth(clusterHealthSet));
            clusterLogicService.editClusterLogicNotCheck(updateLogicClusterDTO, AriusUser.SYSTEM.getDesc());
        } catch (Exception e) {
            LOGGER.error("class=ClusterLogicManagerImpl||method=updateClusterLogicHealth||clusterLogicId={}||errMsg={}",
                    clusterLogicId, e.getMessage(), e);
            return false;
        }

        return true;
    }

    @Override
    public Result<ClusterLogicTemplateIndexCountVO> indexTemplateCount(Long clusterId, String operator, Integer appId) {
        ClusterLogicTemplateIndexDetailDTO detailVO = getTemplateIndexVO(clusterId, appId);
        ClusterLogicTemplateIndexCountVO countVO = new ClusterLogicTemplateIndexCountVO();
        countVO.setCatIndexResults(detailVO.getCatIndexResults().size());
        countVO.setTemplateLogicAggregates(detailVO.getTemplateLogicAggregates().size());
        return Result.buildSucc(countVO);
    }

    @Override
    public PaginationResult<ESClusterRoleHostVO> nodesPage(List<ESClusterRoleHostVO> nodes, ClusterLogicNodeConditionDTO condition) {
        if (StringUtils.isNotBlank(condition.getKeyword())) {
            nodes = nodes.stream().filter(n -> n.getNodeSet().contains(condition.getKeyword())).collect(Collectors.toList());
        }
        return PaginationResult.buildSucc(rowBounds(condition.getPage().intValue(), condition.getSize().intValue(), nodes),
                nodes.size(), condition.getPage(), condition.getSize());
    }

    @Override
    public Result<List<ClusterLogicVO>> getAppLogicClusterInfoByType(Integer appId, Integer type) {
        ESLogicClusterDTO logicClusterDTO = new ESLogicClusterDTO();
        logicClusterDTO.setAppId(appId);
        logicClusterDTO.setType(type);
        return Result.buildSucc(
                ConvertUtil.list2List(clusterLogicService.listClusterLogics(logicClusterDTO), ClusterLogicVO.class));
    }

    /**************************************************** private method ****************************************************/
    /**
     * 构建OP逻辑集群权限
     *
     * @param clusterLogicVO  逻辑集群
     * @param appIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     */
    private void buildOpLogicClusterPermission(ClusterLogicVO clusterLogicVO, Integer appIdForAuthJudge) {
        if (clusterLogicVO == null) {
            return;
        }

        if (appIdForAuthJudge == null) {
            // 未指定需要判断权限的app，取运维人员权限
            clusterLogicVO.setAuthId(null);
            clusterLogicVO.setAuthType(AppClusterLogicAuthEnum.OWN.getCode());
            clusterLogicVO.setPermissions(AppClusterLogicAuthEnum.OWN.getDesc());
        } else {
            // 指定了需要判断权限的app
            buildLogicClusterPermission(clusterLogicVO, appIdForAuthJudge);
        }
    }

    /**
     * 构建ES集群版本
     *
     * @param logicCluster 逻辑集群
     */
    private void buildConsoleClusterVersions(ClusterLogicVO logicCluster) {
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
     *
     * @param logicClusterVO    逻辑集群
     * @param appIdForAuthJudge 需要判断的app的ID
     */
    private void buildLogicClusterPermission(ClusterLogicVO logicClusterVO, Integer appIdForAuthJudge) {
        try {
            if (logicClusterVO == null || appIdForAuthJudge == null) {
                return;
            }
            if (appService.isSuperApp(appIdForAuthJudge)) {
                logicClusterVO.setAuthType(AppClusterLogicAuthEnum.OWN.getCode());
                logicClusterVO.setPermissions(AppClusterLogicAuthEnum.OWN.getDesc());
                return;
            }

            AppClusterLogicAuth auth = appClusterLogicAuthService.getLogicClusterAuth(appIdForAuthJudge,
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
     *
     * @return
     */
    private ClusterLogicStatis buildDefaultLogicStatus() {
        ClusterLogicStatis logicStatus = new ClusterLogicStatis();
        logicStatus.setStatus(ClusterHealthEnum.RED.getDesc());
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

                logicCluster.setEsClusterRoleVOS(
                        buildESRoleClusterVOS(clusterLogic, logicClusterId, esRolePhyClusters, esRolePhyClusterHosts));
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

                List<ClusterRoleHost> clusterRoleHosts = clusterLogicNodeService.getLogicClusterNodes(logicClusterId);

                for (ClusterRoleHost clusterRoleHost : clusterRoleHosts) {
                    ESClusterRoleHostVO esClusterRoleHostVO = new ESClusterRoleHostVO();
                    esClusterRoleHostVO.setHostname(clusterRoleHost.getIp());
                    esClusterRoleHostVO.setRole(DATA_NODE.getCode());

                    esClusterRoleHostVOS.add(esClusterRoleHostVO);
                }
            } else {
                for (ClusterRoleHost clusterRoleHost : esRolePhyClusterHosts) {
                    if (clusterRoleHost.getRoleClusterId().longValue() == clusterRoleInfo.getId().longValue()) {
                        esClusterRoleHostVOS.add(ConvertUtil.obj2Obj(clusterRoleHost, ESClusterRoleHostVO.class));
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
            clusterLogicVO.getClusterStatus().setStatus("red");
        }

        //5. 获取gateway地址
        clusterLogicVO.setGatewayAddress(esGatewayClient.getGatewayAddress());
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

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService.getLogicTemplateWithPhysicalsById(
                logicId);
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
     *
     * @param physicals  物理模板列表
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
     *
     * @param physicals   物理模板
     * @param delQueryDsl DSL语句
     * @param delIndices  删除索引列表
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
        clusterLogicStatis.setIndexNu(
                esClusterStatsResponseList.stream().mapToLong(ESClusterStatsResponse::getIndexCount).sum());
        clusterLogicStatis.setDocNu(
                esClusterStatsResponseList.stream().mapToDouble(ESClusterStatsResponse::getDocsCount).sum());
        clusterLogicStatis.setTotalDisk(
                esClusterStatsResponseList.stream().mapToDouble(item -> item.getTotalFs().getBytes()).sum());
        clusterLogicStatis.setUsedDisk(esClusterStatsResponseList.stream()
                .mapToDouble(item -> item.getTotalFs().getBytes() - item.getFreeFs().getBytes()).sum());

        //设置逻辑集群状态
        Set<String> statusSet = esClusterStatsResponseList.stream().map(ESClusterStatsResponse::getStatus)
                .collect(Collectors.toSet());
        clusterLogicStatis.setStatus(getClusterLogicStatus(statusSet));
        return clusterLogicStatis;
    }

    /**
     * 获取逻辑集群状态
     *
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
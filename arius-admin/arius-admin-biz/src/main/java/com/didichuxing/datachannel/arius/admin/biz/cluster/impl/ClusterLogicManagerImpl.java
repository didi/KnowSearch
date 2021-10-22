package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.DELETE_INDEX;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.core.component.CacheSwitch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppLogicClusterAuthDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.ConsoleAppVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterStatusVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppLogicClusterAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterStatis;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterNodeSepc;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicAggregate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicClusterAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterStatisService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;

@Component
public class ClusterLogicManagerImpl implements ClusterLogicManager {

    private static final ILog          LOGGER     = LogFactory.getLog(ClusterLogicManagerImpl.class);

    @Autowired
    private ESRegionRackService        esClusterRackService;

    @Autowired
    private ESClusterLogicNodeService  esClusterLogicNodeService;

    @Autowired
    private ESIndexService             esIndexService;

    @Autowired
    private ESClusterStatisService     esClusterStatisService;

    @Autowired
    private ESClusterPhyService        esClusterPhyService;

    @Autowired
    private ESClusterLogicService      esClusterLogicService;

    @Autowired
    private ESRegionRackService        esRegionRackService;

    @Autowired
    private TemplateSrvManager         templateSrvManager;

    @Autowired
    private TemplateLogicService       templateLogicService;

    @Autowired
    private TemplateLogicManager       templateLogicManager;

    @Autowired
    private TemplateQuotaManager       templateQuotaManager;

    @Autowired
    private TemplatePhyService         templatePhyService;

    @Autowired
    private AppLogicClusterAuthService appLogicClusterAuthService;

    @Autowired
    private OperateRecordService       operateRecordService;

    @Autowired
    private ESMachineNormsService      esMachineNormsService;

    @Autowired
    private AppService                 appService;

    @Autowired
    private ClusterNodeManager         clusterNodeManager;

    @Autowired
    private ESGatewayClient            esGatewayClient;

    @Autowired
    private ClusterRegionManager       clusterRegionManager;

    @Autowired
    private ClusterContextManager      clusterContextManager;
    
    @Autowired
    private CacheSwitch                 cacheSwitch;

    private final Cache<Integer, List<ConsoleClusterVO>> consoleClusterVOSCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100).build();

    private final static FutureUtil    futureUtil = FutureUtil.init("LogicClusterManager");

    /**
     * 构建运维页面的逻辑集群VO
     * @param logicClusters     逻辑集群列表
     * @param appIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     * @return
     */
    @Override
    public List<ConsoleClusterVO> batchBuildOpClusterVOs(List<ESClusterLogic> logicClusters,
                                                         Integer appIdForAuthJudge) {
        if (CollectionUtils.isEmpty(logicClusters)) {
            return new ArrayList<>();
        }

        List<ConsoleClusterVO> consoleClusterVOS = new CopyOnWriteArrayList<>();

        logicClusters
            .forEach(esClusterLogic -> consoleClusterVOS.add(buildOpClusterVO(esClusterLogic, appIdForAuthJudge)));

        Collections.sort(consoleClusterVOS);
        return consoleClusterVOS;
    }

    /**
     * 构建运维页面的逻辑集群VO
     * @param esClusterLogic    逻辑集群
     * @param appIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     * @return
     */
    @Override
    public ConsoleClusterVO buildOpClusterVO(ESClusterLogic esClusterLogic, Integer appIdForAuthJudge) {
        ConsoleClusterVO consoleClusterVO = ConvertUtil.obj2Obj(esClusterLogic, ConsoleClusterVO.class);

        futureUtil.runnableTask(() -> buildLogicClusterStatus(consoleClusterVO, esClusterLogic))
                    .runnableTask(() -> buildLogicRole(consoleClusterVO, esClusterLogic))
                    .runnableTask(() -> buildConsoleClusterVersions(consoleClusterVO))
                    .runnableTask(() -> buildOpLogicClusterPermission(consoleClusterVO, appIdForAuthJudge))
                    .runnableTask(() -> buildLogicClusterTemplateSrvs(consoleClusterVO))
                    .runnableTask(() -> buildClusterNodeInfo(consoleClusterVO))
                    .waitExecute();

        //不结合futureUtil使用, 防止并发问题

        consoleClusterVO.setAppName(appService.getAppName(consoleClusterVO.getAppId()));

        return consoleClusterVO;
    }

    /**
     * 批量
     * @param logicClusters             逻辑集群列表
     * @param currentUserAppId          当前用户App Id
     * @return
     */
    @Override
    public List<ConsoleClusterVO> batchBuildConsoleClusters(List<ESClusterLogic> logicClusters,
                                                            Integer currentUserAppId) {
        if (CollectionUtils.isNotEmpty(logicClusters)) {
            List<ConsoleClusterVO> consoleClusterVOS = new CopyOnWriteArrayList<>();

            logicClusters.parallelStream().forEach(
                    esClusterLogic -> consoleClusterVOS.add(buildConsoleClusterVO(esClusterLogic, currentUserAppId)));

            Collections.sort(consoleClusterVOS);
            return consoleClusterVOS;
        }

        return Lists.newArrayList();
    }

    /**
     * @param esClusterLogic            逻辑集群元数据信息
     * @return
     */
    @Override
    public ConsoleClusterVO buildConsoleClusterVO(ESClusterLogic esClusterLogic, Integer currentUserAppId) {

        ConsoleClusterVO consoleClusterVO = ConvertUtil.obj2Obj(esClusterLogic, ConsoleClusterVO.class);
        futureUtil.runnableTask(() -> buildLogicClusterStatus(consoleClusterVO, esClusterLogic))
                .runnableTask(() -> buildLogicRole(consoleClusterVO, esClusterLogic))
                .runnableTask(() -> buildLogicClusterPermission(consoleClusterVO, currentUserAppId))
                .runnableTask(() -> buildConsoleClusterVersions(consoleClusterVO))
                .runnableTask(() -> buildLogicClusterTemplateSrvs(consoleClusterVO)).waitExecute();

        consoleClusterVO
                .setAppName( StringUtils.defaultString(appService.getAppName(esClusterLogic.getAppId()), "未知应用"));

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
        List<Integer> appsResult = esClusterStatisService.getLogicClusterAccessInfo(logicClusterId, 7);

        if (CollectionUtils.isEmpty(appsResult)) {
            return Result.buildSucc();
        }

        List<App> apps = appService.getApps();
        Map<Integer, App> id2AppMap = ConvertUtil.list2Map(apps, App::getId);

        List<App> appsRet = appsResult.stream().map(id2AppMap::get).collect( Collectors.toList());
        return Result.buildSucc(ConvertUtil.list2List(appsRet, ConsoleAppVO.class));

    }

    @Override
    public Result clearIndices(ConsoleTemplateClearDTO clearDTO, String operator) throws ESOperateException {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }

        if (CollectionUtils.isEmpty(clearDTO.getDelIndices())) {
            return Result.buildParamIllegal("删除索引为空");
        }

        Result checkResult = checkIndices(clearDTO.getDelIndices(), clearDTO.getLogicId());
        if (checkResult.failed()) {
            return checkResult;
        }

        LOGGER.info("class=TemplateLogicServiceImpl||method=clearIndex||"
                        + "operator={}||logicId={}||delIndices={}||delQueryDsl={}",
                operator, clearDTO.getLogicId(), JSON.toJSONString(clearDTO.getDelIndices()), clearDTO.getDelQueryDsl());

        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
                .getLogicTemplateWithPhysicalsById(clearDTO.getLogicId());

        if (StringUtils.isNotBlank(clearDTO.getDelQueryDsl())) {
            Result deleteResult = batchDeletePhysicalTemplateIndicesByQuery(templateLogicWithPhysical.getPhysicals(),
                    clearDTO.getDelQueryDsl(), clearDTO.getDelIndices());
            if (deleteResult.failed()) {
                return deleteResult;
            }
        } else {
            Result deleteIndicesResult = batchDeletePhysicalTemplateIndices(templateLogicWithPhysical.getPhysicals(),
                    clearDTO.getDelIndices());
            if (deleteIndicesResult.failed()) {
                return deleteIndicesResult;
            }
        }

        operateRecordService.save(TEMPLATE, DELETE_INDEX, clearDTO.getLogicId(), JSON.toJSONString(clearDTO), operator);

        if (!templateQuotaManager.controlAndPublish(clearDTO.getLogicId())) {
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
    public List<ESClusterPhy> getLogicClusterAssignedPhysicalClusters(Long logicClusterId) {
        // TODO： 原有根据物理集群寻找管理逻辑集群Logic可以重构下。
        // 目前可以不用Fix，未来所有跟Rack关联都可以换成是Region关联；
        List<ESClusterPhy> logicClusterAssignedClusters = new ArrayList<>();
        if (logicClusterId != null) {
            Set<String> clusters = parseClusterNames(esRegionRackService.listLogicClusterRacks(logicClusterId));
            for (String cluster : clusters) {
                ESClusterPhy physicalCluster = esClusterPhyService.getClusterByName(cluster);
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

    /**
     * 获取APP拥有的集群列表
     * @param appId
     * @return
     */
    @Override
    public Result<List<ConsoleClusterVO>> getAppLogicClusters(Integer appId) {

        if (appService.getAppById(appId) == null) {
            return Result.buildFrom(Result.buildNotExist("应用不存在"));
        }

        return Result.buildSucc(batchBuildConsoleClusters(esClusterLogicService.getHasAuthLogicClustersByAppId(appId), appId));
    }

    /**
     * 获取平台所有的集群列表
     * @param appId
     * @return
     */
    @Override
    public Result<List<ConsoleClusterVO>> getDataCenterLogicClusters(Integer appId) {
        if (appId == null) {
            appId = AdminConstant.DEFAULT_APP_ID;
        }

        return Result.buildSucc(batchBuildConsoleClusters(esClusterLogicService.listAllLogicClusters(), appId));
    }

    /**
     *
     * @param clusterId
     * @param appId
     * @return
     */
    @Override
    public Result<ConsoleClusterVO> getAppLogicClusters(Long clusterId, Integer appId) {
        ESClusterLogic esClusterLogic = esClusterLogicService.getLogicClusterById(clusterId);
        if (esClusterLogic == null) {
            return Result.buildFrom(Result.buildNotExist("集群不存在"));
        }

        return Result.buildSucc(buildConsoleClusterVO(esClusterLogic, appId));
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

        Integer appId = HttpRequestUtils.getAppId(request, AdminConstant.DEFAULT_APP_ID);
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
        Set<ESRoleClusterNodeSepc> esRoleClusterNodeSepcs = esClusterLogicService.getLogicDataNodeSepc(clusterId);

        return Result.buildSucc(ConvertUtil.set2Set(esRoleClusterNodeSepcs, ESClusterNodeSepcVO.class));
    }

    /**
     * 获取指定罗集群节点列表接口
     * @param clusterId
     * @return
     */
    @Override
    public Result<List<ESRoleClusterHostVO>> getLogicClusterNodes(Long clusterId) {
        return Result.buildSucc(clusterNodeManager
                .convertClusterNodes(esClusterLogicNodeService.getLogicClusterNodesIncludeNonDataNodes(clusterId)));
    }

    /**
     * 获取当前集群支持的套餐列表
     * @return
     */
    @Override
    public Result<List<ESClusterNodeSepcVO>> listMachineSpec() {
        List<ESMachineNormsPO> esMachineNormsPOS = esMachineNormsService.listMachineNorms();
        return Result.buildSucc(ConvertUtil.list2List(esMachineNormsPOS, ESClusterNodeSepcVO.class));
    }

    @Override
    public List<ConsoleClusterVO> getConsoleClusterVOS(ESLogicClusterDTO param, Integer appId) {
        List<ESClusterLogic> esClusterLogics = esClusterLogicService.listLogicClusters(param);
        

        if (cacheSwitch.clusterLogicCacheEnable()) {
            try {
                return consoleClusterVOSCache.get(appId,
                        () -> batchBuildOpClusterVOs(esClusterLogics, appId));
            } catch (ExecutionException e) {
                return batchBuildOpClusterVOs(esClusterLogics, appId);
            }
        }

        return batchBuildOpClusterVOs(esClusterLogics, appId);
    }

    @Override
    public Result<Long> addLogicClusterAndClusterRegions(ESLogicClusterWithRegionDTO param, String operator) {
        return clusterRegionManager.batchBindRegionToClusterLogic(param, operator, Boolean.TRUE);
    }

    @Override
    public ConsoleClusterVO getConsoleClusterVOByIdAndAppId(Long clusterLogicId, Integer appId) {
        if (AriusObjUtils.isNull(clusterLogicId)) {
            return null;
        }

        List<ConsoleClusterVO> consoleClusterVOS = consoleClusterVOSCache.getIfPresent(appId);

        if (CollectionUtils.isEmpty(consoleClusterVOS)) {
            consoleClusterVOS = getConsoleClusterVOS(null, appId);
        }

        return  Objects.requireNonNull(consoleClusterVOS)
                .stream()
                .filter(r -> clusterLogicId.equals(r.getId()))
                .findAny()
                .get();
    }

    /**************************************************** private method ****************************************************/
    /**
     * 构建OP逻辑集群权限
     * @param consoleClusterVO  逻辑集群
     * @param appIdForAuthJudge 用于判断权限的应用id（供应用管理页面获取关联集群列表使用）
     *                          ，为null则权限为运维人员权限（管理权限）
     */
    private void buildOpLogicClusterPermission(ConsoleClusterVO consoleClusterVO, Integer appIdForAuthJudge) {
        if (consoleClusterVO == null) {
            return;
        }

        if (appIdForAuthJudge == null) {
            // 未指定需要判断权限的app，取运维人员权限
            consoleClusterVO.setAuthId(null);
            consoleClusterVO.setAuthType( AppLogicClusterAuthEnum.OWN.getCode());
            consoleClusterVO.setPermissions(AppLogicClusterAuthEnum.OWN.getDesc());
        } else {
            // 指定了需要判断权限的app
            buildLogicClusterPermission(consoleClusterVO, appIdForAuthJudge);
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
                List<ESClusterPhy> physicalClusters = getLogicClusterAssignedPhysicalClusters(logicCluster.getId());

                for (ESClusterPhy physicalCluster : physicalClusters) {
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
     * @param appIdForAuthJudge 需要判断的app的ID
     */
    private void buildLogicClusterPermission(ConsoleClusterVO logicClusterVO, Integer appIdForAuthJudge) {
        try {
            if (logicClusterVO == null || appIdForAuthJudge == null) {
                return;
            }
            if (appService.isSuperApp(appIdForAuthJudge)) {
                logicClusterVO.setAuthType(   AppLogicClusterAuthEnum.OWN.getCode());
                logicClusterVO.setPermissions(AppLogicClusterAuthEnum.OWN.getDesc());
                return;
            }

            AppLogicClusterAuthDTO authDTO = appLogicClusterAuthService.getLogicClusterAuth(appIdForAuthJudge,
                    logicClusterVO.getId());

            if (authDTO == null) {
                // 没有权限
                logicClusterVO.setAuthId(null);
                logicClusterVO.setAuthType(AppLogicClusterAuthEnum.NO_PERMISSIONS.getCode());
                logicClusterVO.setPermissions(AppLogicClusterAuthEnum.NO_PERMISSIONS.getDesc());
            } else {
                // 有权限
                logicClusterVO.setAuthId(authDTO.getId());
                logicClusterVO.setAuthType(AppLogicClusterAuthEnum.valueOf(authDTO.getType()).getCode());
                logicClusterVO.setPermissions(AppLogicClusterAuthEnum.valueOf(authDTO.getType()).getDesc());
            }


        } catch (Exception e) {
            LOGGER.warn("class=LogicClusterManager||method=buildLogicClusterPermission||logicClusterId={}",
                    logicClusterVO.getId(), e);
        }
    }

    /**
     * 更新逻辑集群状态信息
     */
    private void buildLogicClusterStatus(ConsoleClusterVO logicCluster, ESClusterLogic esClusterLogic) {
        ESClusterStatis esClusterLogicStatus = buildDefaultLogicStatus();
        try {
            esClusterLogicStatus = getLogicClusterStatus(esClusterLogic.getId());
        } catch (Exception e) {
            LOGGER.warn("class=LogicClusterManager||method=buildLogicClusterStatus||logicClusterId={}",
                    logicCluster.getId(), e);
        }
        if (esClusterLogicStatus != null) {
            logicCluster.setClusterStatus(fetchConsoleCluster(esClusterLogicStatus));
        }
    }

    /**
     * 获取集群状态
     * @param esClusterLogicStatus 原始集群状态详情
     * @return
     */
    private ConsoleClusterStatusVO fetchConsoleCluster(ESClusterStatis esClusterLogicStatus) {
        if (esClusterLogicStatus != null) {
            ConsoleClusterStatusVO consoleClusterStatusVO = ConvertUtil.obj2Obj(esClusterLogicStatus,
                    ConsoleClusterStatusVO.class);
            consoleClusterStatusVO.setIndexNu(esClusterLogicStatus.getIndexNu());
            consoleClusterStatusVO.setDocNu((int) esClusterLogicStatus.getDocNu());

            return consoleClusterStatusVO;
        }

        return null;
    }

    /**
     * 创建默认逻辑集群状态
     * @return
     */
    private ESClusterStatis buildDefaultLogicStatus() {
        ESClusterStatis logicStatus = new ESClusterStatis();
        logicStatus.setStatus( ClusterStatusEnum.GREEN.getDesc());
        logicStatus.setDocNu(0.0);
        logicStatus.setIndexNu(0);
        logicStatus.setTotalDisk(0.0);
        logicStatus.setUsedDisk(0.0);
        return logicStatus;
    }

    private void buildLogicRole(ConsoleClusterVO logicCluster, ESClusterLogic esClusterLogic) {
        if (logicCluster != null) {
            try {
                Long logicClusterId = logicCluster.getId();

                List<String> phyClusterNames = esClusterRackService.listPhysicClusterNames(logicClusterId);
                if (CollectionUtils.isEmpty(phyClusterNames)) {
                    return;
                }

                //拿第一个物理集群的client、master信息，因为只有Arius维护的大公共共享集群才会有一个逻辑集群映射成多个物理集群
                ESClusterPhy esClusterPhy = esClusterPhyService.getClusterByName(phyClusterNames.get(0));
                if (null == esClusterPhy) {
                    return;
                }

                List<ESRoleClusterVO> esRoleClusterVOS = new ArrayList<>();

                List<ESRoleCluster> esRolePhyClusters = esClusterPhy.getRoleClusters();
                List<ESRoleClusterHost> esRolePhyClusterHosts = esClusterPhy.getRoleClusterHosts();

                for (ESRoleCluster esRoleCluster : esRolePhyClusters) {
                    ESRoleClusterVO esRoleClusterVO = ConvertUtil.obj2Obj(esRoleCluster, ESRoleClusterVO.class);

                    List<ESRoleClusterHostVO> esRoleClusterHostVOS = new ArrayList<>();

                    //如果是datanode节点，那么使用逻辑集群申请的节点个数和阶段规格配置
                    if (DATA_NODE.getDesc().equals(esRoleCluster.getRoleClusterName())) {
                        esRoleClusterVO.setPodNumber(esClusterLogic.getDataNodeNu());
                        esRoleClusterVO.setMachineSpec(esClusterLogic.getDataNodeSpec());

                        List<ESRoleClusterHost> esRoleClusterHosts = esClusterLogicNodeService
                                .getLogicClusterNodes(logicClusterId);

                        for (ESRoleClusterHost roleClusterHost : esRoleClusterHosts) {
                            ESRoleClusterHostVO esRoleClusterHostVO = new ESRoleClusterHostVO();
                            esRoleClusterHostVO.setHostname(roleClusterHost.getHostname());
                            esRoleClusterHostVO.setRole(DATA_NODE.getCode());

                            esRoleClusterHostVOS.add(esRoleClusterHostVO);
                        }
                    } else {
                        for (ESRoleClusterHost roleClusterHost : esRolePhyClusterHosts) {
                            if (roleClusterHost.getRoleClusterId().longValue() == esRoleCluster.getId().longValue()) {
                                esRoleClusterHostVOS
                                        .add(ConvertUtil.obj2Obj(roleClusterHost, ESRoleClusterHostVO.class));
                            }
                        }
                    }

                    esRoleClusterVO.setEsRoleClusterHostVO(esRoleClusterHostVOS);
                    esRoleClusterVO.setPodNumber(esRoleClusterHostVOS.size());
                    esRoleClusterVOS.add(esRoleClusterVO);
                }

                logicCluster.setEsRoleClusterVOS(esRoleClusterVOS);
            } catch (Exception e) {
                LOGGER.warn("class=LogicClusterManager||method=buildLogicRole||logicClusterId={}", logicCluster.getId(),
                        e);
            }
        }
    }

    private void buildLogicClusterTemplateSrvs(ConsoleClusterVO logicCluster) {
        try {
            Result<List<ESClusterTemplateSrv>> listResult = templateSrvManager
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
     * 1.是否关联物理集群
     * 5. 获取关联物理集群列表
     * 2.逻辑集群拥有的数据节点数
     * 3. 防止没有关联物理集群, 或者取消关联region, 逻辑集群状态为red
     * 4. 获取gateway地址
     *
     */
    private void buildClusterNodeInfo(ConsoleClusterVO consoleClusterVO) {
        //TODO:支持各种类型逻辑集群的物理集群
        ESClusterLogicContext esClusterLogicContext = clusterContextManager
            .getESClusterLogicContext(consoleClusterVO.getId());

        //1. 是否关联物理集群
        consoleClusterVO.setPhyClusterAssociated(esClusterLogicContext.getAssociatedPhyNum() > 0);

        //2. 获取关联物理集群列表
        if (esClusterLogicContext.getAssociatedClusterPhyNames().size() > 0) {
            consoleClusterVO.setAssociatedPhyClusterName(esClusterLogicContext.getAssociatedClusterPhyNames().get(0));
        }

        //3. 逻辑集群拥有的数据节点数
        consoleClusterVO.setDataNodesNumber(esClusterLogicContext.getAssociatedDataNodeNum());

        //4. 没有关联物理集群下, 逻辑集群状态至为red
        if (!consoleClusterVO.getPhyClusterAssociated()) {
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
    private Set<String> parseClusterNames(List<ESClusterLogicRackInfo> racks) {
        Set<String> clusters = new HashSet<>();
        if (CollectionUtils.isNotEmpty(racks)) {
            for (ESClusterLogicRackInfo rackInfo : racks) {
                clusters.add(rackInfo.getPhyClusterName());
            }
        }

        return clusters;
    }

    private Result checkIndices(List<String> delIndices, Integer logicId) {
        for (String index : delIndices) {
            if (index.endsWith("*")) {
                return Result.buildParamIllegal("索引名字不能以*结尾");
            }
        }

        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
                .getLogicTemplateWithPhysicalsById(logicId);
        IndexTemplatePhy templatePhysical = templateLogicWithPhysical.getAnyOne();

        List<String> matchIndices = templatePhyService.getMatchNoVersionIndexNames(templatePhysical.getId());
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
    private Result batchDeletePhysicalTemplateIndices(List<IndexTemplatePhy> physicals, List<String> delIndices) {
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
    private Result batchDeletePhysicalTemplateIndicesByQuery(List<IndexTemplatePhy> physicals, String delQueryDsl,
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

    private ESClusterStatis getLogicClusterStatus(Long logicClusterId) {
        return ConvertUtil.obj2Obj(esClusterStatisService.getLogicClusterStatisticsInfo(logicClusterId), ESClusterStatis.class);
    }
}

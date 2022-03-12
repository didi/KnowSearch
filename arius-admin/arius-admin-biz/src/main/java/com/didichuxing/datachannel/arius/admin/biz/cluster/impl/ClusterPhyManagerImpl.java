package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.*;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum.ES_HOST;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum.PRIVATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum.CN;
import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.CLUSTER_PHY;

import java.util.*;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.page.ClusterPhyPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.constant.RunModeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusScheduleThreadPool;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.didichuxing.datachannel.arius.admin.biz.app.AppClusterPhyAuthManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplatePhyMappingManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.TemplatePipelineManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESPluginVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterVO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppClusterPhyAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicLevelEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterPhyAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterPhyEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ClusterUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.RackUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.annotation.PostConstruct;

@Component
public class ClusterPhyManagerImpl implements ClusterPhyManager {

    private static final ILog                                LOGGER                        = LogFactory
        .getLog(ClusterPhyManagerImpl.class);

    private static final String                              NODE_NOT_EXISTS_TIPS          = "集群缺少类型为%s的节点";

    private static final String                              IP_DUPLICATE_TIPS             = "集群ip:%s重复, 请重新输入";

    private static final Map<String/*cluster*/, Triple<Long/*diskUsage*/, Long/*diskTotal*/, Double/*diskUsagePercent*/>> clusterName2ESClusterStatsTripleMap = Maps
        .newConcurrentMap();

    @Autowired
    private ClusterPhyManager                                clusterPhyManager;

    @Autowired
    private ESTemplateService                                esTemplateService;

    @Autowired
    private ClusterPhyService                                clusterPhyService;

    @Autowired
    private ClusterLogicService                              clusterLogicService;
    
    @Autowired
    private ClusterLogicManager                              clusterLogicManager;

    @Autowired
    private RoleClusterService                               roleClusterService;

    @Autowired
    private RoleClusterHostService                           roleClusterHostService;

    @Autowired
    private TemplatePhyService                               templatePhyService;

    @Autowired
    private TemplateSrvManager                               templateSrvManager;

    @Autowired
    private TemplatePhyMappingManager                        templatePhyMappingManager;

    @Autowired
    private TemplatePipelineManager                          templatePipelineManager;

    @Autowired
    private TemplateLogicService                             templateLogicService;

    @Autowired
    private TemplatePhyManager                               templatePhyManager;

    @Autowired
    private RegionRackService                                regionRackService;

    @Autowired
    private AppClusterLogicAuthService                       appClusterLogicAuthService;

    @Autowired
    private ESGatewayClient                                  esGatewayClient;

    @Autowired
    private ClusterNodeManager                               clusterNodeManager;

    @Autowired
    private ClusterContextManager                            clusterContextManager;

    @Autowired
    private AppService                                       appService;

    @Autowired
    private OperateRecordService                             operateRecordService;

    @Autowired
    private ESClusterNodeService                             esClusterNodeService;

    @Autowired
    private ESClusterService                                 esClusterService;

    @Autowired
    private HandleFactory                                    handleFactory;

    @Autowired
    private AppClusterPhyAuthManager                         appClusterPhyAuthManager;

    @Autowired
    private AriusScheduleThreadPool                          ariusScheduleThreadPool;

    @Autowired
    private ESOpClient                                       esOpClient;

    @PostConstruct
    private void init(){
        ariusScheduleThreadPool.submitScheduleAtFixTask(this::refreshClusterDistInfo,60,120);
    }

    @Override
    public boolean copyMapping(String cluster, int retryCount) {
        // 获取物理集群下的所有物理模板
        List<IndexTemplatePhy> physicals = templatePhyService.getNormalTemplateByCluster(cluster);
        if (CollectionUtils.isEmpty(physicals)) {
            LOGGER.info("class=ESClusterPhyServiceImpl||method=copyMapping||cluster={}||msg=copyMapping no template",
                cluster);
            return true;
        }

        int succeedCount = 0;
        // 遍历物理模板，copy mapping
        for (IndexTemplatePhy physical : physicals) {
            try {
                // 获取物理模板对应的逻辑模板
                IndexTemplateLogic templateLogic = templateLogicService.getLogicTemplateById(physical.getLogicId());
                // 同步索引的mapping到模板
                Result<MappingConfig> result = templatePhyMappingManager.syncMappingConfig(cluster, physical.getName(),
                    physical.getExpression(), templateLogic.getDateFormat());

                if (result.success()) {
                    succeedCount++;
                    if (!setTemplateSettingSingleType(cluster, physical.getName())) {
                        LOGGER.error(
                            "class=ESClusterPhyServiceImpl||method=copyMapping||errMsg=failedUpdateSingleType||cluster={}||template={}",
                            cluster, physical.getName());
                    }
                } else {
                    LOGGER.warn(
                        "class=ESClusterPhyServiceImpl||method=copyMapping||cluster={}||template={}||msg=copyMapping fail",
                        cluster, physical.getName());
                }
            } catch (Exception e) {
                LOGGER.error("class=ESClusterPhyServiceImpl||method=copyMapping||errMsg={}||cluster={}||template={}",
                    e.getMessage(), cluster, physical.getName(), e);
            }
        }

        return succeedCount * 1.0 / physicals.size() > 0.7;
    }

    @Override
    public void syncTemplateMetaData(String cluster, int retryCount) {
        // 获取物理集群下的所有物理模板
        List<IndexTemplatePhy> physicals = templatePhyService.getNormalTemplateByCluster(cluster);
        if (CollectionUtils.isEmpty(physicals)) {
            LOGGER.info(
                "class=ESClusterPhyServiceImpl||method=syncTemplateMetaData||cluster={}||msg=syncTemplateMetaData no template",
                cluster);
            return;
        }

        // 遍历物理模板
        for (IndexTemplatePhy physical : physicals) {
            try {
                // 同步模板元数据到ES集群（修改ES集群中的模板）
                templatePhyManager.syncMeta(physical.getId(), retryCount);
                // 同步最新元数据到ES集群pipeline
                templatePipelineManager.syncPipeline(physical,
                    templateLogicService.getLogicTemplateWithPhysicalsById(physical.getLogicId()));
            } catch (Exception e) {
                LOGGER.error(
                    "class=ESClusterPhyServiceImpl||method=syncTemplateMetaData||errMsg={}||cluster={}||template={}",
                    e.getMessage(), cluster, physical.getName(), e);
            }
        }
    }

    @Override
    public boolean isClusterExists(String clusterName) {
        return clusterPhyService.isClusterExists(clusterName);
    }

    @Override
    public Result<Void> releaseRacks(String cluster, String racks, int retryCount) {
        if (!isClusterExists(cluster)) {
            return Result.buildNotExist("集群不存在");
        }

        Set<String> racksToRelease = Sets.newHashSet(racks.split(AdminConstant.RACK_COMMA));

        // 获取分配到要释放的rack上的物理模板
        List<IndexTemplatePhy> templatePhysicals = templatePhyService.getNormalTemplateByClusterAndRack(cluster,
            racksToRelease);

        // 没有模板被分配在要释放的rack上
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return Result.buildSucc();
        }

        List<String> errMsgList = Lists.newArrayList();
        // 遍历模板，修改模板的rack设置
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            // 去掉要释放的rack后的剩余racks
            String tgtRack = RackUtils.removeRacks(templatePhysical.getRack(), racksToRelease);

            LOGGER.info("class=ClusterPhyManagerImpl||method=releaseRack||template={}||srcRack={}||tgtRack={}", templatePhysical.getName(),
                templatePhysical.getRack(), tgtRack);

            try {
                // 修改模板
                Result<Void> result = templatePhyManager.editTemplateRackWithoutCheck(templatePhysical.getId(), tgtRack,
                    AriusUser.SYSTEM.getDesc(), retryCount);

                if (result.failed()) {
                    errMsgList.add(templatePhysical.getName() + "失败：" + result.getMessage() + ";");
                }

            } catch (Exception e) {
                errMsgList.add(templatePhysical.getName() + "失败：" + e.getMessage() + ";");
                LOGGER.warn("class=ClusterPhyManagerImpl||method=releaseRack||template={}||srcRack={}||tgtRack={}||errMsg={}",
                    templatePhysical.getName(), templatePhysical.getRack(), tgtRack, e.getMessage(), e);
            }
        }

        if (CollectionUtils.isEmpty(errMsgList)) {
            return Result.buildSucc();
        }

        return Result.buildFail(String.join(",", errMsgList));
    }

    // @Cacheable(cacheNames = CACHE_GLOBAL_NAME, key = "#currentAppId + '@' + 'getConsoleClusterPhyVOS'")
    @Override
    public List<ConsoleClusterPhyVO> getConsoleClusterPhyVOS(ESClusterDTO param, Integer currentAppId) {

        List<ClusterPhy> esClusterPhies = clusterPhyService.listClustersByCondt(param);

        return buildConsoleClusterPhy(esClusterPhies, currentAppId);
    }

    @Override
    public ConsoleClusterPhyVO getConsoleClusterPhyVO(Integer clusterId, Integer currentAppId) {
        if (AriusObjUtils.isNull(clusterId)) {
            return null;
        }

        //这里必须clusterLogicManager为了走spring全局缓存
        List<ConsoleClusterPhyVO> consoleClusterPhyVOS = clusterPhyManager.getConsoleClusterPhyVOS(null, currentAppId);
        if (CollectionUtils.isNotEmpty(consoleClusterPhyVOS)) {
            for (ConsoleClusterPhyVO consoleClusterPhyVO : consoleClusterPhyVOS) {
                if (clusterId.equals(consoleClusterPhyVO.getId())) {
                    return consoleClusterPhyVO;
                }
            }
        }

        return null;
    }

    @Override
    public ConsoleClusterPhyVO getConsoleClusterPhy(Integer clusterId, Integer currentAppId) {
        // 获取基本信息
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(clusterId);
        if(clusterPhy == null) {
            return new ConsoleClusterPhyVO();
        }
        ConsoleClusterPhyVO consoleClusterPhyVO = ConvertUtil.obj2Obj(clusterPhy, ConsoleClusterPhyVO.class);
        // 构建overView信息
        buildWithOtherInfo(consoleClusterPhyVO, currentAppId);
        buildPhyClusterStatics(consoleClusterPhyVO);
        buildClusterRole(consoleClusterPhyVO);
        return consoleClusterPhyVO;
    }

    @Override
    public Result<List<String>> listCanBeAssociatedRegionOfClustersPhys(Integer clusterLogicType, Long clusterLogicId) {
        return clusterContextManager.getCanBeAssociatedClustersPhys(clusterLogicType, clusterLogicId);
    }

    @Override
    public Result<List<String>> listCanBeAssociatedClustersPhys(Integer clusterLogicType) {
        return clusterContextManager.getCanBeAssociatedClustersPhys(clusterLogicType, null);
    }

    @Override
    public Result<List<ESRoleClusterHostVO>> getClusterPhyRegionInfos(Integer clusterId) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(clusterId);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail(String.format("集群[%s]不存在", clusterId));
        }

        List<RoleClusterHost> nodesInfo = roleClusterHostService.getNodesByCluster(clusterPhy.getCluster());
        return Result.buildSucc(clusterNodeManager.convertClusterPhyNodes(nodesInfo, clusterPhy.getCluster()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Tuple<Long, String>> clusterJoin(ClusterJoinDTO param, String operator) {
        try {
            Result<Void> checkResult = validCheckAndInitForClusterJoin(param, operator);
            if (checkResult.failed())  return Result.buildFail(checkResult.getMessage());

            Result<Tuple<Long, String>> doClusterJoinResult = doClusterJoin(param, operator);
            if (doClusterJoinResult.success()) {
                SpringTool.publish(new ClusterPhyEvent(param.getCluster(), param.getAppId()));
                
                postProcessingForClusterJoin(param, doClusterJoinResult.getData(), operator);
            }

            return doClusterJoinResult;
        } catch (Exception e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=clusterJoin||logicCluster={}||clusterPhy={}||errMsg={}", param.getLogicCluster(),
                param.getCluster(), e.getMessage());
            // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("操作失败, 请联系管理员");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteClusterJoin(Integer clusterId, String operator) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(clusterId);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildParamIllegal("物理集群不存在");
        }

        try {
            doDeleteClusterJoin(clusterPhy, operator);
        } catch (AdminOperateException e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=deleteClusterJoin||errMsg={}||e={}||clusterId={}",
                e.getMessage(), e, clusterId);
            // 这里显示回滚处理特殊异常场景
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail(e.getMessage());
        }

        return Result.buildSucc();
    }

    @Override
    public Result<List<ESPluginVO>> listPlugins(String cluster) {
        return Result.buildSucc(ConvertUtil.list2List(clusterPhyService.listClusterPlugins(cluster), ESPluginVO.class));
    }

    @Override
    public Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> getPhyClusterDynamicConfigs(String cluster) {
        if (isClusterExists(cluster)) {
            Result.buildFail(String.format("集群[%s]不存在", cluster));
        }

        ESClusterGetSettingsAllResponse clusterSetting = esClusterService.syncGetClusterSetting(cluster);
        if (null == clusterSetting) {
            return Result.buildFail(String.format("获取集群动态配置信息失败, 请确认是否集群[%s]是否正常", cluster));
        }

        // 构建defaults和persistent的配置信息，transient中的配置信息并非是动态配置的内容
        Map<String, Object> clusterConfigMap = new HashMap<>();
        clusterConfigMap.putAll(ConvertUtil.directFlatObject(clusterSetting.getDefaults()));
        clusterConfigMap.putAll(ConvertUtil.directFlatObject(clusterSetting.getPersistentObj()));

        // Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>中Map的String表示的是动态配置的字段，例如cluster.routing.allocation.awareness.attributes
        // Object则是对应动态配置字段的值
        Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>> clusterDynamicConfigsTypeEnumMapMap = initClusterDynamicConfigs();
        for (ClusterDynamicConfigsEnum param : ClusterDynamicConfigsEnum.valuesWithoutUnknown()) {
            Map<String, Object> dynamicConfig = clusterDynamicConfigsTypeEnumMapMap
                .get(param.getClusterDynamicConfigsType());
            dynamicConfig.put(param.getName(), clusterConfigMap.get(param.getName()));
        }

        return Result.buildSucc(clusterDynamicConfigsTypeEnumMapMap);
    }

    @Override
    public Result<Boolean> updatePhyClusterDynamicConfig(ClusterSettingDTO param) {
        return clusterPhyService.updatePhyClusterDynamicConfig(param);
    }

    @Override
    public Result<Set<String>> getRoutingAllocationAwarenessAttributes(String cluster) {
        Set<String> routingAllocationAwarenessAttributes = clusterPhyService
            .getRoutingAllocationAwarenessAttributes(cluster);
        if (CollectionUtils.isEmpty(routingAllocationAwarenessAttributes)) {
            return Result.buildFail("获取集群动态配置信息失败, 请确认是否集群是否正常");
        }
        return Result.buildSucc(routingAllocationAwarenessAttributes);
    }

    @Override
    public List<String> getAppClusterPhyNames(Integer appId) {
        List<ClusterLogic> appAuthLogicClusters = clusterLogicService.getHasAuthClusterLogicsByAppId(appId);

        List<String> appClusterPhyNames = Lists.newArrayList();

        appAuthLogicClusters.stream()
            .map(clusterLogic -> clusterContextManager.getClusterLogicContext(clusterLogic.getId())).forEach(
                clusterLogicContext -> appClusterPhyNames.addAll(clusterLogicContext.getAssociatedClusterPhyNames()));

        return appClusterPhyNames;
    }

    @Override
    public List<String> getAppClusterPhyNodeNames(String clusterPhyName) {
        if (null == clusterPhyName) {
            LOGGER.error("class=ESClusterPhyServiceImpl||method=getAppClusterPhyNodeNames||cluster={}||errMsg=集群名称为空",
                clusterPhyName);
            return Lists.newArrayList();
        }
        return esClusterNodeService.syncGetNodeNames(clusterPhyName);
    }

    @Override
    public List<String> getAppNodeNames(Integer appId) {
        List<String> appAuthNodeNames = Lists.newCopyOnWriteArrayList();

        List<String> appClusterPhyNames = getAppClusterPhyNames(appId);
        appClusterPhyNames
            .forEach(clusterPhyName -> appAuthNodeNames.addAll(esClusterNodeService.syncGetNodeNames(clusterPhyName)));

        return appAuthNodeNames;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteClusterInfo(Integer clusterPhyId, String operator, Integer appId) {
        ClusterPhy  clusterPhy  = clusterPhyService.getClusterById(clusterPhyId);
        if (null == clusterPhy) {
            return Result.buildFail(String.format("物理集群Id[%s]不存在", clusterPhyId));
        }

        try {
            Result<Void> deleteHostResult = roleClusterHostService.deleteByCluster(clusterPhy.getCluster());
            if (deleteHostResult.failed()) {
                throw new AdminOperateException(String.format("删除集群[%s]节点信息失败", clusterPhy.getCluster()));
            }

            Result<Void> deleteRoleResult = roleClusterService.deleteRoleClusterByClusterId(clusterPhy.getId());
            if (deleteRoleResult.failed()) {
                throw new AdminOperateException(String.format("删除集群[%s]角色信息失败", clusterPhy.getCluster()));
            }

            Result<Boolean> deleteClusterResult  = clusterPhyService.deleteClusterById(clusterPhyId, operator);
            if (deleteClusterResult.failed()) {
                throw new AdminOperateException(String.format("删除集群[%s]信息失败", clusterPhy.getCluster()));
            }

            List<ClusterRegion> clusterRegionList = regionRackService.listPhyClusterRegions(clusterPhy.getCluster());
            if(!AriusObjUtils.isEmptyList(clusterRegionList)) {
                // 该物理集群有Region才删除
                Result<Void> deletePhyClusterRegionResult = regionRackService.deleteByClusterPhy(clusterPhy.getCluster(), operator);
                if (deletePhyClusterRegionResult.failed()) {
                    throw new AdminOperateException(String.format("删除集群[%s]Region新失败", clusterPhy.getCluster()));
                }
            }
        } catch (AdminOperateException e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=deleteClusterInfo||clusterName={}||errMsg={}||e={}",
                clusterPhy.getCluster(), e.getMessage(), e);
            // 这里显示回滚处理特殊异常场景
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("删除物理集群失败");
        }

        SpringTool.publish(new ClusterPhyEvent(clusterPhy.getCluster(), appId));

        return Result.buildSucc(true);
    }

    @Override
    public Result<Boolean> addCluster(ESClusterDTO param, String operator, Integer appId) {
        Result<Boolean> result = clusterPhyService.createCluster(param, operator);

        if (result.success()) {
            SpringTool.publish(new ClusterPhyEvent(param.getCluster(), appId));
            operateRecordService.save(ModuleEnum.CLUSTER, OperationEnum.ADD, param.getCluster(), null, operator);
        }
        return result;
    }

    @Override
    public Result<Boolean> editCluster(ESClusterDTO param, String operator, Integer appId) {
        return clusterPhyService.editCluster(param, operator);
    }

    @Override
    public PaginationResult<ConsoleClusterPhyVO> pageGetConsoleClusterPhyVOS(ClusterPhyConditionDTO condition, Integer appId) {
        BaseHandle baseHandle     = handleFactory.getByHandlerNamePer(CLUSTER_PHY.getPageSearchType());
        if (baseHandle instanceof ClusterPhyPageSearchHandle) {
            ClusterPhyPageSearchHandle handle =   (ClusterPhyPageSearchHandle) baseHandle;
            return handle.doPageHandle(condition, condition.getAuthType(), appId);
        }

        LOGGER.warn("class=ClusterPhyManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the ClusterPhyPageSearchHandle");

        return PaginationResult.buildFail("分页获取物理集群信息失败");
    }

    @Override
    public List<ClusterPhy> getClusterPhyByAppIdAndAuthType(Integer appId, Integer authType) {
        if (!appService.isAppExists(appId)) {
            return Lists.newArrayList();
        }

        //超级用户对所有模板都是管理权限
        if (appService.isSuperApp(appId) && !AppClusterPhyAuthEnum.OWN.getCode().equals(authType)) {
            return Lists.newArrayList();
        }

        if (!AppClusterPhyAuthEnum.isExitByCode(authType)) {
            return Lists.newArrayList();
        }

        switch (AppClusterPhyAuthEnum.valueOf(authType)) {
            case OWN:
                if (appService.isSuperApp(appId)) {
                    return clusterPhyService.listAllClusters();
                } else {
                    return getAppOwnAuthClusterPhyList(appId);
                }
            case ACCESS:
                return getAppAccessClusterPhyList(appId);

            case NO_PERMISSIONS:
                List<Integer> appOwnAuthClusterPhyIdList = getAppOwnAuthClusterPhyList(appId)
                                                            .stream()
                                                            .map(ClusterPhy::getId)
                                                            .collect(Collectors.toList());

                List<Integer> appAccessAuthClusterPhyIdList = getAppAccessClusterPhyList(appId)
                                                                .stream()
                                                                .map(ClusterPhy::getId)
                                                                .collect(Collectors.toList());

                List<ClusterPhy> allClusterPhyList  =  clusterPhyService.listAllClusters();

                return allClusterPhyList.stream()
                        .filter(clusterPhy -> !appAccessAuthClusterPhyIdList.contains(clusterPhy.getId())
                                           && !appOwnAuthClusterPhyIdList.contains(clusterPhy.getId()))
                        .collect(Collectors.toList());
            default:
                return Lists.newArrayList();

        }
    }

    @Override
    public List<ClusterPhy> getAppAccessClusterPhyList(Integer appId) {
        List<AppClusterPhyAuth> appAccessClusterPhyAuths = appClusterPhyAuthManager.getAppAccessClusterPhyAuths(appId);
        return appAccessClusterPhyAuths
                                .stream()
                                .map(r -> clusterPhyService.getClusterByName(r.getClusterPhyName()))
                                .collect(Collectors.toList());
    }

    @Override
    public List<ClusterPhy> getAppOwnAuthClusterPhyList(Integer appId) {
        List<ClusterPhy> appAuthClusterPhyList = Lists.newArrayList();

        List<ClusterLogic> clusterLogicList = clusterLogicService.getOwnedClusterLogicListByAppId(appId);
        if (CollectionUtils.isEmpty(clusterLogicList)) {
            return appAuthClusterPhyList;
        }

        //项目下的有管理权限逻辑集群会关联多个物理集群
        List<List<String>> appAuthClusterNameList = clusterLogicList
                            .stream()
                            .map(ClusterLogic::getId)
                            .map(clusterContextManager::getClusterLogicContext)
                            .map(ClusterLogicContext::getAssociatedClusterPhyNames)
                            .collect(Collectors.toList());

        for (List<String> clusterNameList : appAuthClusterNameList) {
            clusterNameList.forEach(cluster -> appAuthClusterPhyList.add(clusterPhyService.getClusterByName(cluster)));
        }

        return appAuthClusterPhyList;
    }

    /**
     * 构建用户控制台统计信息: 集群使用率
     */
    @Override
    public void buildPhyClusterStatics(ConsoleClusterPhyVO cluster) {
        try {
            Triple<Long, Long, Double> esClusterStaticInfoTriple = getESClusterStaticInfoTriple(cluster.getCluster());
            cluster.setDiskTotal(esClusterStaticInfoTriple.v1());
            cluster.setDiskUsage(esClusterStaticInfoTriple.v2());
            cluster.setDiskUsagePercent(esClusterStaticInfoTriple.v3());
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=buildPhyClusterResourceUsage||logicClusterId={}",
                    cluster.getId(), e);
        }
    }

    @Override
    public void buildClusterRole(ConsoleClusterPhyVO cluster) {
        try {
            List<RoleCluster> roleClusters = roleClusterService.getAllRoleClusterByClusterId(cluster.getId());
            List<ESRoleClusterVO> roleClusterVOS = ConvertUtil.list2List(roleClusters, ESRoleClusterVO.class);

            for (ESRoleClusterVO esRoleClusterVO : roleClusterVOS) {
                List<RoleClusterHost> roleClusterHosts = roleClusterHostService.getByRoleClusterId(esRoleClusterVO.getId());
                List<ESRoleClusterHostVO> esRoleClusterHostVOS = ConvertUtil.list2List(roleClusterHosts, ESRoleClusterHostVO.class);
                esRoleClusterVO.setEsRoleClusterHostVO(esRoleClusterHostVOS);
            }

            cluster.setEsRoleClusterVOS(roleClusterVOS);
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=buildClusterRole||logicClusterId={}", cluster.getId(), e);
        }
    }

    @Override
    public boolean updateClusterHealth(String clusterPhyName, String operator) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (null == clusterPhy) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=updateClusterHealth||clusterPhyName={}||msg=clusterPhy is empty", clusterPhyName);
            return false;
        }

        ESClusterDTO      esClusterDTO      = new ESClusterDTO();
        ClusterHealthEnum clusterHealthEnum = esClusterService.syncGetClusterHealthEnum(clusterPhyName);

        esClusterDTO.setId(clusterPhy.getId());
        esClusterDTO.setHealth(clusterHealthEnum.getCode());
        Result<Boolean> editClusterResult = clusterPhyService.editCluster(esClusterDTO, operator);
        if (editClusterResult.failed()) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=updateClusterHealth||clusterPhyName={}||errMsg={}",
                clusterPhyName, editClusterResult.getMessage());
        }

        return true;
    }

    @Override
    public Result<Boolean> checkClusterHealth(String clusterPhyName, String operator) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (null == clusterPhy) {
            return Result.buildFail();
        }

        if (ClusterHealthEnum.GREEN.getCode().equals(clusterPhy.getHealth()) ||
                ClusterHealthEnum.YELLOW.getCode().equals(clusterPhy.getHealth())) {
            return Result.buildSucc(true);
        }

        updateClusterHealth(clusterPhyName, operator);
        return Result.buildFail();
    }

    @Override
    public Result<Boolean> checkClusterIsExit(String clusterPhyName, String operator) {
        return Result.build(clusterPhyService.isClusterExists(clusterPhyName));
    }

    @Override
    public Result<Boolean> deleteClusterExit(String clusterPhyName, Integer appId, String operator) {
        if  (!appService.isSuperApp(appId)) {
            return Result.buildFail("无权限删除集群");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (null == clusterPhy) {
            return Result.buildSucc(true);
        }

        return clusterPhyManager.deleteClusterInfo(clusterPhy.getId(), operator, appId);
    }

    @Override
    public void buildBelongAppIdAndName(ConsoleClusterPhyVO consoleClusterPhyVO) {
        //获取物理集群绑定的逻辑集群
        ClusterLogic clusterLogic = getClusterLogicByClusterPhyName(consoleClusterPhyVO.getCluster());
        if(clusterLogic == null) {
            return;
        }

        Integer belongAppId = clusterLogic.getAppId();
        consoleClusterPhyVO.setBelongAppId(belongAppId);
        consoleClusterPhyVO.setBelongAppName(appService.getAppName(belongAppId));
    }

    @Override
    public Result<List<String>> getPhyClusterNameWithSameEsVersion(Integer clusterLogicType,/*用户在新建逻辑集群阶段已选择的物理集群名称*/String hasSelectedClusterNameWhenBind) {
        //获取可以绑定的物理集群名称列表
        Result<List<String>> canBeAssociatedClustersPhyNamesResult = validLogicAndReturnPhyNamesWhenBindPhy(null, clusterLogicType);
        if (canBeAssociatedClustersPhyNamesResult.failed()) {
            return Result.buildFrom(canBeAssociatedClustersPhyNamesResult);
        }

        //没有指定物理集群名称，则返回全量的匹配数据，不做版本的筛选
        if(AriusObjUtils.isNull(hasSelectedClusterNameWhenBind)) {
            return canBeAssociatedClustersPhyNamesResult;
        }

        //根据已绑定的物理集群的版本进行筛选
        return Result.buildSucc(getPhyClusterNameWithSameEsVersion(hasSelectedClusterNameWhenBind, canBeAssociatedClustersPhyNamesResult.getData()));
    }

    @Override
    public Result<List<String>> getPhyClusterNameWithSameEsVersionAfterBuildLogic(Long clusterLogicId) {
        //获取可以绑定的物理集群名称列表
        Result<List<String>> canBeAssociatedClustersPhyNamesResult = validLogicAndReturnPhyNamesWhenBindPhy(clusterLogicId, null);
        if (canBeAssociatedClustersPhyNamesResult.failed()) {
            return Result.buildFrom(canBeAssociatedClustersPhyNamesResult);
        }

        //获取逻辑集群已绑定的物理集群信息
        List<ClusterLogicRackInfo> clusterLogicRackInfos = regionRackService.listLogicClusterRacks(clusterLogicId);
        if (CollectionUtils.isEmpty(clusterLogicRackInfos)) {
            return canBeAssociatedClustersPhyNamesResult;
        }

        //根据已绑定的物理集群的版本进行筛选
        String hasSelectedPhyClusterName = clusterLogicRackInfos.get(0).getPhyClusterName();
        return Result.buildSucc(getPhyClusterNameWithSameEsVersion(hasSelectedPhyClusterName, canBeAssociatedClustersPhyNamesResult.getData()));
    }

/**************************************** private method ***************************************************/
    /**
     * 更新物理模板setting single_type为true
     * @param cluster  集群
     * @param template 物理模板
     * @return
     */
    private boolean setTemplateSettingSingleType(String cluster, String template) {
        Map<String, String> setting = new HashMap<>();
        setting.put(AdminConstant.SINGLE_TYPE_KEY, AdminConstant.DEFAULT_SINGLE_TYPE);
        try {
            return esTemplateService.syncUpsertSetting(cluster, template, setting, 3);
        } catch (ESOperateException e) {
            LOGGER.warn(
                "class=ClusterPhyManagerImpl||method=setTemplateSettingSingleType||errMsg={}||e={}||cluster={}||template={}",
                e.getMessage(), e, cluster, template);
        }

        return false;
    }

    /**
     * 新建逻辑集群和已创建逻辑集群时绑定物理集群时进行校验,并且获取可以绑定的物理集群民称列表
     * @param clusterLogicId 逻辑集群id
     * @param clusterLogicType 逻辑集群类型
     * @return 可以绑定的物理集群民称列表
     */
    Result<List<String>> validLogicAndReturnPhyNamesWhenBindPhy(Long clusterLogicId, Integer clusterLogicType) {
        if (clusterLogicId == null && clusterLogicType == null) {
            return Result.buildFail("传入的参数错误");
        }

        if (clusterLogicId != null) {
            ClusterLogic clusterLogicById = clusterLogicService.getClusterLogicById(clusterLogicId);
            if (clusterLogicById == null) {
                return Result.buildFail("选定的逻辑集群不存在");
            }
            clusterLogicType = clusterLogicById.getType();
        }

        if (!ResourceLogicTypeEnum.isExist(clusterLogicType)) {
            return Result.buildParamIllegal("逻辑集群类型非法");
        }

        Result<List<String>> canBeAssociatedClustersPhyNames = clusterContextManager.getCanBeAssociatedClustersPhys(clusterLogicType, clusterLogicId);
        if (canBeAssociatedClustersPhyNames.failed()) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=getPhyClusterNameWithSameEsVersionAfterBuildLogic||errMsg={}",
                    canBeAssociatedClustersPhyNames.getMessage());
            Result.buildFail("无法获取对应的物理集群名称列表");
        }

        return canBeAssociatedClustersPhyNames;
    }

    /**
     * 根据已经选定的物理集群筛选出版本相同的可以绑定的物理集群名称列表
     * @param hasSelectedPhyClusterName 已经选择的物理集群名称
     * @param canBeAssociatedClustersPhyNames 可以匹配的物理集群名称列表（待筛选状态）
     * @return 物理集群名称列表
     */
    private List<String> getPhyClusterNameWithSameEsVersion(String hasSelectedPhyClusterName, List<String> canBeAssociatedClustersPhyNames) {
        //获取用户已选择的物理集群的信息
        ClusterPhy hasSelectedCluster = clusterPhyService.getClusterByName(hasSelectedPhyClusterName);
        //如果指定的物理集群名称为null，则返回全量的物理集群名称列表
        if (AriusObjUtils.isNull(hasSelectedPhyClusterName)
                || AriusObjUtils.isNull(hasSelectedCluster)
                || CollectionUtils.isEmpty(canBeAssociatedClustersPhyNames)) {
            return null;
        }

        //筛选出和用户以指定的物理集群的版本号相同的物理集群名称列表
        List<String> canBeAssociatedPhyClusterNameWithSameEsVersion = Lists.newArrayList();
        for (String canBeAssociatedClustersPhyName : canBeAssociatedClustersPhyNames) {
            ClusterPhy canBeAssociatedClustersPhy = clusterPhyService.getClusterByName(canBeAssociatedClustersPhyName);
            if (!AriusObjUtils.isNull(canBeAssociatedClustersPhy)
                    && !AriusObjUtils.isNull(canBeAssociatedClustersPhy.getEsVersion())
                    && !AriusObjUtils.isNull(canBeAssociatedClustersPhy.getCluster())
                    && canBeAssociatedClustersPhy.getEsVersion().equals(hasSelectedCluster.getEsVersion())) {
                canBeAssociatedPhyClusterNameWithSameEsVersion.add(canBeAssociatedClustersPhy.getCluster());
            }
        }

        return canBeAssociatedPhyClusterNameWithSameEsVersion;
    }

    /**
     * 构建物理集群详情
     * @param phyClusters 物理集群元数据信息
     * @param currentAppId 当前登录项目
     */
    private List<ConsoleClusterPhyVO> buildConsoleClusterPhy(List<ClusterPhy> phyClusters, Integer currentAppId) {

        List<ConsoleClusterPhyVO> consoleClusterPhyVOS = ConvertUtil.list2List(phyClusters, ConsoleClusterPhyVO.class);

        consoleClusterPhyVOS.parallelStream()
            .forEach(consoleClusterPhyVO -> buildPhyCluster(consoleClusterPhyVO, currentAppId));

        Collections.sort(consoleClusterPhyVOS);

        return consoleClusterPhyVOS;
    }

    /**
     * 构建物理集群详情
     * @param consoleClusterPhyVO 物理集群元数据信息
     * @return
     */
    private void buildPhyCluster(ConsoleClusterPhyVO consoleClusterPhyVO, Integer currentAppId) {
        if (!AriusObjUtils.isNull(consoleClusterPhyVO)) {
            buildPhyClusterStatics(consoleClusterPhyVO);
            buildPhyClusterTemplateSrv(consoleClusterPhyVO);
            buildClusterRole(consoleClusterPhyVO);
            buildWithOtherInfo(consoleClusterPhyVO, currentAppId);
        }
    }

    private void buildPhyClusterTemplateSrv(ConsoleClusterPhyVO cluster) {
        try {
            Result<List<ClusterTemplateSrv>> listResult = templateSrvManager
                .getPhyClusterTemplateSrv(cluster.getCluster());
            if (null != listResult && listResult.success()) {
                cluster.setEsClusterTemplateSrvVOS(
                    ConvertUtil.list2List(listResult.getData(), ESClusterTemplateSrvVO.class));
            }
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=buildPhyClusterTemplateSrv||logicClusterId={}",
                cluster.getId(), e);
        }
    }

    /**
     * 1. 获取gateway地址
     * 2. 关联App的权限信息
     * 3. 物理集群责任人
     */
    private void buildWithOtherInfo(ConsoleClusterPhyVO cluster, Integer currentAppId) {
        cluster.setGatewayAddress(esGatewayClient.getGatewayAddress());

        if (appService.isSuperApp(currentAppId)) {
            cluster.setCurrentAppAuth(AppClusterLogicAuthEnum.ALL.getCode());
        }

        //获取物理集群绑定的逻辑集群
        ClusterLogic clusterLogic = getClusterLogicByClusterPhyName(cluster.getCluster());
        if(clusterLogic == null) {
            return;
        }

        //TODO:  公共模块依赖, 一个物理集群对应多个逻辑集群的情况该归属哪个appId
        cluster.setBelongAppId(clusterLogic.getAppId());
        cluster.setResponsible(clusterLogic.getResponsible());

        App app = appService.getAppById(clusterLogic.getAppId());
        if (!AriusObjUtils.isNull(app)) {
            cluster.setBelongAppName(app.getName());
        }

        //TODO:  公共模块依赖, auth table中 加type字段标识是逻辑集群还是物理集群
        AppClusterLogicAuthEnum logicClusterAuthEnum = appClusterLogicAuthService.getLogicClusterAuthEnum(currentAppId, clusterLogic.getId());
        cluster.setCurrentAppAuth(logicClusterAuthEnum.getCode());
    }

    /**
     * 获取物理集群所绑定的逻辑集群的信息
     */
    private ClusterLogic getClusterLogicByClusterPhyName(String phyClusterName) {
        ClusterPhyContext clusterPhyContext = clusterContextManager.getClusterPhyContext(phyClusterName);
        List<Long> clusterLogicIds = Lists.newArrayList();
        if (!AriusObjUtils.isNull(clusterPhyContext)
                && !AriusObjUtils.isNull(clusterPhyContext.getAssociatedClusterLogicIds())) {
            clusterLogicIds = Lists.newArrayList(clusterPhyContext.getAssociatedClusterLogicIds());
        }

        if (CollectionUtils.isEmpty(clusterLogicIds)) {
            return null;
        }

        //物理集群被多个逻辑集群关联, 取第一个
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(clusterLogicIds.get(0));
        if (AriusObjUtils.isNull(clusterLogic)) {
            LOGGER.warn(
                    "class=ClusterPhyManagerImpl||method=getClusterLogicByPhyClusterName||clusterName={}||msg=the associated logical cluster is empty",
                    phyClusterName);
            return null;
        }
        return clusterLogic;
    }

    private Result<Tuple<Long/*逻辑集群id*/, String/*物理集群名称*/>> doClusterJoin(ClusterJoinDTO param, String operator) throws AdminOperateException {
        Tuple<Long, String> clusterLogicIdAndClusterPhyNameTuple = new Tuple<>();

        // 1.保存物理集群信息(集群、角色、节点)
        Result<Void> saveClusterResult = saveClusterPhyInfo(param, operator);
        if (saveClusterResult.failed()) {
            throw new AdminOperateException(saveClusterResult.getMessage());
        }
        clusterLogicIdAndClusterPhyNameTuple.setV2(param.getCluster());

        //如果没有携带逻辑集群，则不操作region的绑定以及后续步骤
        if (StringUtils.isBlank(param.getLogicCluster())) {
            return Result.buildSucc(clusterLogicIdAndClusterPhyNameTuple);
        }

        // 2.创建region信息
        List<Long> regionIds = Lists.newArrayList();
        for (String racks : param.getRegionRacks()) {
            if (StringUtils.isBlank(racks)) {
                continue;
            }
            Result<Long> createPayClusterRegionResult = regionRackService.createPhyClusterRegion(param.getCluster(),
                racks, null, operator);
            if (createPayClusterRegionResult.failed()) {
                throw new AdminOperateException(createPayClusterRegionResult.getMessage());
            }

            if (createPayClusterRegionResult.success()) {
                regionIds.add(createPayClusterRegionResult.getData());
            }
        }

        // 3.保存逻辑集群信息
        Result<Long> saveClusterLogicResult = saveClusterLogic(param, operator);
        if (saveClusterLogicResult.failed()) {
            throw new AdminOperateException(saveClusterLogicResult.getMessage());
        }

        // 4.绑定Region
        Long clusterLogicId = saveClusterLogicResult.getData();
        for (Long regionId : regionIds) {
            Result<Void> bindRegionResult = regionRackService.bindRegion(regionId, clusterLogicId, null, operator);
            if (bindRegionResult.failed()) {
                throw new AdminOperateException(bindRegionResult.getMessage());
            }
        }

        clusterLogicIdAndClusterPhyNameTuple.setV1(clusterLogicId);

        return Result.buildSucc(clusterLogicIdAndClusterPhyNameTuple);
    }

    private Result<Void> saveClusterPhyInfo(ClusterJoinDTO param, String operator) {
        //保存集群信息
        ESClusterDTO    clusterDTO    =  buildClusterPhy(param, operator);
        Result<Boolean> addClusterRet =  clusterPhyService.createCluster(clusterDTO, operator);
        if (addClusterRet.failed()) { return Result.buildFrom(addClusterRet);}
        return Result.buildSucc();
    }

    private ESClusterDTO buildClusterPhy(ClusterJoinDTO param, String operator) {
        ESClusterDTO clusterDTO = ConvertUtil.obj2Obj(param, ESClusterDTO.class);

        String clientAddress = roleClusterHostService.buildESClientHttpAddressesStr(param.getRoleClusterHosts());

        clusterDTO.setDesc(param.getPhyClusterDesc());
        clusterDTO.setDataCenter(CN.getCode());
        clusterDTO.setHttpAddress(clientAddress);
        clusterDTO.setHttpWriteAddress(clientAddress);
        clusterDTO.setTemplateSrvs(DEFAULT_CLUSTER_TEMPLATE_SRVS);
        clusterDTO.setIdc(DEFAULT_CLUSTER_IDC);
        clusterDTO.setLevel(ResourceLogicLevelEnum.NORMAL.getCode());
        clusterDTO.setImageName("");
        clusterDTO.setPackageId(-1L);
        clusterDTO.setNsTree("");
        clusterDTO.setPlugIds("");
        clusterDTO.setCreator(operator);
        clusterDTO.setRunMode(RunModeEnum.READ_WRITE_SHARE.getRunMode());
        clusterDTO.setHealth(DEFAULT_CLUSTER_HEALTH);
        return clusterDTO;
    }

    private Result<Long> saveClusterLogic(ClusterJoinDTO param, String operator) {
        ESLogicClusterDTO esLogicClusterDTO = new ESLogicClusterDTO();
        esLogicClusterDTO.setAppId(param.getAppId());
        esLogicClusterDTO.setResponsible(param.getResponsible());
        esLogicClusterDTO.setName(param.getLogicCluster());
        esLogicClusterDTO.setDataCenter(CN.getCode());
        esLogicClusterDTO.setType(PRIVATE.getCode());
        esLogicClusterDTO.setHealth(DEFAULT_CLUSTER_HEALTH);

        Long dataNodeNumber = param.getRoleClusterHosts().stream().filter(hosts -> DATA_NODE.getCode() == hosts.getRole()).count();

        esLogicClusterDTO.setDataNodeNu(dataNodeNumber.intValue());
        esLogicClusterDTO.setLibraDepartmentId("");
        esLogicClusterDTO.setLibraDepartment("");
        esLogicClusterDTO.setMemo(param.getPhyClusterDesc());

        Result<Long> result = clusterLogicService.createClusterLogic(esLogicClusterDTO, operator);
        if (result.failed()) {
            return Result.buildFail("逻辑集群创建失败");
        }

        return result;
    }

    private Result<Void> validCheckAndInitForClusterJoin(ClusterJoinDTO param, String operator) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("参数为空");
        }

        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人不存在");
        }

        if (ES_HOST != ESClusterTypeEnum.valueOf(param.getType())) {
            return Result.buildParamIllegal("仅支持host类型的集群, 请确认是否为host类型");
        }

        List<ESRoleClusterHostDTO> roleClusterHosts = param.getRoleClusterHosts();
        if (CollectionUtils.isEmpty(roleClusterHosts)) {
            return Result.buildParamIllegal("集群节点信息为空");
        }

        Set<Integer> roleForNode = roleClusterHosts.stream().map(ESRoleClusterHostDTO::getRole)
            .collect(Collectors.toSet());

        if (!roleForNode.contains(MASTER_NODE.getCode())) {
            return Result.buildParamIllegal(String.format(NODE_NOT_EXISTS_TIPS, MASTER_NODE.getDesc()));
        }

        Map<Integer, List<String>> role2IpsMap = ConvertUtil.list2MapOfList(roleClusterHosts,
            ESRoleClusterHostDTO::getRole, ESRoleClusterHostDTO::getIp);

        List<String> masterIps = role2IpsMap.get(MASTER_NODE.getCode());
        if (masterIps.size() < JOIN_MASTER_NODE_MIN_NUMBER) {
            return Result.buildParamIllegal(String.format("集群%s的masternode角色节点个数要求大于等于1，且不重复", param.getCluster()));
        }

        String duplicateIpForMaster = ClusterUtils.getDuplicateIp(masterIps);
        if (!AriusObjUtils.isBlack(duplicateIpForMaster)) {
            return Result.buildParamIllegal(String.format(IP_DUPLICATE_TIPS, duplicateIpForMaster));
        }

        String duplicateIpForClient = ClusterUtils.getDuplicateIp(role2IpsMap.get(CLIENT_NODE.getCode()));
        if (!AriusObjUtils.isBlack(duplicateIpForClient)) {
            return Result.buildParamIllegal(String.format(IP_DUPLICATE_TIPS, duplicateIpForClient));
        }

        String duplicateIpForData = ClusterUtils.getDuplicateIp(role2IpsMap.get(DATA_NODE.getCode()));
        if (!AriusObjUtils.isBlack(duplicateIpForData)) {
            return Result.buildParamIllegal(String.format(IP_DUPLICATE_TIPS, duplicateIpForData));
        }

        if (clusterPhyService.isClusterExists(param.getCluster())) {
            return Result.buildParamIllegal(String.format("物理集群名称:%s已存在", param.getCluster()));
        }

        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByName(param.getLogicCluster());
        if (!AriusObjUtils.isNull(clusterLogic)) {
            return Result.buildParamIllegal(String.format("逻辑集群名称:%s已存在", param.getLogicCluster()));
        }

        String esClientHttpAddressesStr = roleClusterHostService.buildESClientHttpAddressesStr(roleClusterHosts);
        Result<Void> rackSetResult = initRackValueForClusterJoin(param, esClientHttpAddressesStr);
        if (rackSetResult.failed()) return rackSetResult;

        param.setResponsible(operator);
        return Result.buildSucc();
    }

    /**
     * 初始化rack信息
     * @param param ClusterJoinDTO
     * @param esClientHttpAddressesStr  http连接地址
     * @return
     */
    private Result<Void> initRackValueForClusterJoin(ClusterJoinDTO param, String esClientHttpAddressesStr) {
        if(CollectionUtils.isEmpty(param.getRegionRacks())) {
            Result<Set<String>> rackSetResult = esClusterService.getClusterRackByHttpAddress(esClientHttpAddressesStr);
            if (rackSetResult.failed()) {
                return Result.buildFail(rackSetResult.getMessage());
            } else {
                param.setRegionRacks(new ArrayList<>(rackSetResult.getData()));
            }
        }
        return Result.buildSucc();
    }

    private void doDeleteClusterJoin(ClusterPhy clusterPhy, String operator) throws AdminOperateException {
        ClusterPhyContext clusterPhyContext = clusterContextManager.getClusterPhyContext(clusterPhy.getCluster());
        if (null == clusterPhyContext) {
            return;
        }

        List<Long> associatedRegionIds = clusterPhyContext.getAssociatedRegionIds();
        for (Long associatedRegionId : associatedRegionIds) {
            Result<Void> unbindRegionResult = regionRackService.unbindRegion(associatedRegionId, operator);
            if (unbindRegionResult.failed()) {
                throw new AdminOperateException(String.format("解绑region(%s)失败", associatedRegionId));
            }

            Result<Void> deletePhyClusterRegionResult = regionRackService.deletePhyClusterRegion(associatedRegionId,
                operator);
            if (deletePhyClusterRegionResult.failed()) {
                throw new AdminOperateException(String.format("删除region(%s)失败", associatedRegionId));
            }
        }

        List<Long> clusterLogicIds = clusterPhyContext.getAssociatedClusterLogicIds();
        for (Long clusterLogicId : clusterLogicIds) {
            Result<Void> deleteLogicClusterResult = clusterLogicService.deleteClusterLogicById(clusterLogicId,
                operator);
            if (deleteLogicClusterResult.failed()) {
                throw new AdminOperateException(String.format("删除逻辑集群(%s)失败", clusterLogicId));
            }
        }

        Result<Boolean> deleteClusterResult = clusterPhyService.deleteClusterById(clusterPhy.getId(), operator);
        if (deleteClusterResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群(%s)失败", clusterPhy.getCluster()));
        }

        Result<Void> deleteRoleClusterResult = roleClusterService.deleteRoleClusterByClusterId(clusterPhy.getId());
        if (deleteRoleClusterResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群角色(%s)失败", clusterPhy.getCluster()));
        }

        Result<Void> deleteRoleClusterHostResult = roleClusterHostService.deleteByCluster(clusterPhy.getCluster());
        if (deleteRoleClusterHostResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群节点(%s)失败", clusterPhy.getCluster()));
        }
    }

    /**
     * 初始化物理集群配置信息
     * @return Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>中Map的String表示的是动态配置的字段，例如cluster.routing.allocation.awareness.attributes
     * Object则是对应动态配置字段的值
     */
    private Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>> initClusterDynamicConfigs() {
        Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>> esClusterPhyDynamicConfig = Maps.newHashMap();
        for (ClusterDynamicConfigsTypeEnum clusterDynamicConfigsTypeEnum : ClusterDynamicConfigsTypeEnum
            .valuesWithoutUnknown()) {
            esClusterPhyDynamicConfig.put(clusterDynamicConfigsTypeEnum, Maps.newHashMap());
        }

        return esClusterPhyDynamicConfig;
    }
    
    private Triple<Long/*diskTotal*/, Long/*diskUsage*/, Double/*diskUsagePercent*/> getESClusterStaticInfoTriple(String cluster) {
        Triple<Long, Long, Double> initTriple = buildInitTriple();
        if (!clusterPhyService.isClusterExists(cluster)) {
            LOGGER.error(
                "class=ClusterPhyManagerImpl||method=getESClusterStaticInfoTriple||clusterName={}||msg=cluster is empty",
                cluster);
            return initTriple;
        }

        if (clusterName2ESClusterStatsTripleMap.containsKey(cluster)) {
            return clusterName2ESClusterStatsTripleMap.get(cluster);
        } else {
            ESClusterStatsResponse clusterStats = esClusterService.syncGetClusterStats(cluster);
            if (null != clusterStats && null != clusterStats.getFreeFs() && null != clusterStats.getTotalFs()
                && clusterStats.getTotalFs().getBytes() > 0 && clusterStats.getFreeFs().getBytes() > 0) {
                initTriple.setV1(clusterStats.getTotalFs().getBytes());
                initTriple.setV2(clusterStats.getTotalFs().getBytes() - clusterStats.getFreeFs().getBytes());
                double diskFreePercent = clusterStats.getFreeFs().getGbFrac() / clusterStats.getTotalFs().getGbFrac();
                initTriple.setV3(1 - diskFreePercent);
            }

            clusterName2ESClusterStatsTripleMap.put(cluster, initTriple);
            return initTriple;
        }
    }

    private Triple<Long/*diskTotal*/, Long/*diskTotal*/, Double/*diskUsagePercent*/> buildInitTriple() {
        Triple<Long/*diskTotal*/, Long/*diskTotal*/, Double/*diskUsagePercent*/> triple = new Triple<>();
        triple.setV1(0L);
        triple.setV2(0L);
        triple.setV3(0d);
        return triple;
    }

    private void postProcessingForClusterJoin(ClusterJoinDTO param,
                                              Tuple<Long, String> clusterLogicIdAndClusterPhyIdTuple, String operator) {
        esOpClient.connect(param.getCluster());

        roleClusterHostService.collectClusterNodeSettings(param.getCluster());

        clusterPhyManager.updateClusterHealth(param.getCluster(), AriusUser.SYSTEM.getDesc());

        Long clusterLogicId = clusterLogicIdAndClusterPhyIdTuple.getV1();
        if (null != clusterLogicId) {
            clusterLogicManager.updateClusterLogicHealth(clusterLogicId);
        }

        operateRecordService.save(ModuleEnum.ES_CLUSTER_JOIN, OperationEnum.ADD, param.getCluster(),
            param.getPhyClusterDesc(), operator);
    }

    private void refreshClusterDistInfo() {
        List<String> clusterNameList = clusterPhyService.listAllClusters().stream().map(ClusterPhy::getCluster)
            .collect(Collectors.toList());
        for (String clusterName : clusterNameList) {
            Triple<Long, Long, Double> initTriple = buildInitTriple();
            ESClusterStatsResponse clusterStats = esClusterService.syncGetClusterStats(clusterName);
            if (null != clusterStats && null != clusterStats.getFreeFs() && null != clusterStats.getTotalFs()
                    && clusterStats.getTotalFs().getBytes() > 0 && clusterStats.getFreeFs().getBytes() > 0) {
                initTriple.setV1(clusterStats.getTotalFs().getBytes());
                initTriple.setV2(clusterStats.getTotalFs().getBytes() - clusterStats.getFreeFs().getBytes());
                double diskFreePercent = clusterStats.getFreeFs().getGbFrac() / clusterStats.getTotalFs().getGbFrac();
                initTriple.setV3(1 - diskFreePercent);
            }

            clusterName2ESClusterStatsTripleMap.put(clusterName, initTriple);
        }
    }
}

package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum.CN;
import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.CLUSTER_PHY;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum.PRIVATE;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.didichuxing.datachannel.arius.admin.biz.app.AppClusterPhyAuthManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.page.ClusterPhyPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplatePhyMappingManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.TemplatePipelineManager;
import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterPhyAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterTags;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.RunModeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterPhyAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.*;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterPhyEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusScheduleThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 *
 * @author ohushenglin_v
 * @date 2022-05-10
 */
@Component
public class ClusterPhyManagerImpl implements ClusterPhyManager {

    private static final ILog                                LOGGER                        = LogFactory
        .getLog(ClusterPhyManagerImpl.class);

    private static final String                              NODE_NOT_EXISTS_TIPS          = "集群缺少类型为%s的节点";

    private static final String                              IP_DUPLICATE_TIPS             = "集群ip:%s重复, 请重新输入";
    /**
     * Map< cluster , Triple< diskUsage , diskTotal , diskUsagePercent >>
     */
    private static final Map<String, Triple<Long, Long, Double>> CLUSTER_NAME_TO_ES_CLUSTER_STATS_TRIPLE_MAP = Maps.newConcurrentMap();

    @Autowired
    private ESTemplateService                                esTemplateService;

    @Autowired
    private ClusterPhyService                                clusterPhyService;

    @Autowired
    private ClusterLogicService                              clusterLogicService;
    
    @Autowired
    private ClusterLogicManager                              clusterLogicManager;

    @Autowired
    private ClusterRoleService clusterRoleService;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private TemplateSrvManager                               templateSrvManager;

    @Autowired
    private TemplatePhyMappingManager                        templatePhyMappingManager;

    @Autowired
    private TemplatePipelineManager                          templatePipelineManager;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private TemplatePhyManager                               templatePhyManager;

    @Autowired
    private ClusterRegionService clusterRegionService;

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
    private OperateRecordService operateRecordService;

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
        ariusScheduleThreadPool.submitScheduleAtFixedDelayTask(this::refreshClusterDistInfo,60,180);
    }

    private static final FutureUtil<Void> FUTURE_UTIL = FutureUtil.init("ClusterPhyManagerImpl",20, 40,100);

    @Override
    public boolean copyMapping(String cluster, int retryCount) {
        // 获取物理集群下的所有物理模板
        List<IndexTemplatePhy> physicals = indexTemplatePhyService.getNormalTemplateByCluster(cluster);
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
                IndexTemplate templateLogic = indexTemplateService.getLogicTemplateById(physical.getLogicId());
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
        List<IndexTemplatePhy> physicals = indexTemplatePhyService.getNormalTemplateByCluster(cluster);
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
                    indexTemplateService.getLogicTemplateWithPhysicalsById(physical.getLogicId()));
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
    @Deprecated
    public Result<Void> releaseRacks(String cluster, String racks, int retryCount) {
        if (!isClusterExists(cluster)) {
            return Result.buildNotExist("集群不存在");
        }

        Set<String> racksToRelease = Sets.newHashSet(racks.split(AdminConstant.RACK_COMMA));

        // 获取分配到要释放的rack上的物理模板
        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getNormalTemplateByClusterAndRack(cluster,
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

    @Override
    @Deprecated
    public List<ConsoleClusterPhyVO> getConsoleClusterPhys(ClusterPhyDTO param, Integer currentAppId) {

        List<ClusterPhy> esClusterPhies = clusterPhyService.listClustersByCondt(param);

        return buildConsoleClusterPhy(esClusterPhies, currentAppId);
    }

    @Override
    public List<ConsoleClusterPhyVO> getConsoleClusterPhys(ClusterPhyDTO param) {

        List<ClusterPhy> phyClusters = clusterPhyService.listClustersByCondt(param);
        List<ConsoleClusterPhyVO> consoleClusterPhys = ConvertUtil.list2List(phyClusters, ConsoleClusterPhyVO.class);

        consoleClusterPhys.parallelStream()
                .forEach(this::buildClusterRole);

        Collections.sort(consoleClusterPhys);

        return consoleClusterPhys;
    }


    @Override
    public List<ConsoleClusterPhyVO> buildClusterInfo(List<ClusterPhy> clusterPhyList, Integer appId) {
        if (CollectionUtils.isEmpty(clusterPhyList)) {
            return Lists.newArrayList();
        }

        // 获取项目对集群列表的权限信息
        List<AppClusterPhyAuth> appClusterPhyAuthList      = appClusterPhyAuthManager.getByClusterPhyListAndAppIdFromCache(appId, clusterPhyList);
        Map<String, Integer>    clusterPhyName2AuthTypeMap = ConvertUtil.list2Map(appClusterPhyAuthList, AppClusterPhyAuth::getClusterPhyName, AppClusterPhyAuth::getType);

        List<ConsoleClusterPhyVO> consoleClusterPhyVOList = ConvertUtil.list2List(clusterPhyList, ConsoleClusterPhyVO.class);

        //1. 设置单个集群权限
        consoleClusterPhyVOList.forEach(consoleClusterPhyVO -> consoleClusterPhyVO.setCurrentAppAuth(clusterPhyName2AuthTypeMap.get(consoleClusterPhyVO.getCluster())));

        //2.设置物理集群的所属项目和所属AppId
        long timeForBuildClusterAppInfo = System.currentTimeMillis();
        consoleClusterPhyVOList.forEach(consoleClusterPhyVO -> {
            FUTURE_UTIL.runnableTask(() -> {
                ClusterPhyContext clusterPhyContext = clusterContextManager.getClusterPhyContext(consoleClusterPhyVO.getCluster());
                consoleClusterPhyVO.setBelongAppIds(  null != clusterPhyContext ? clusterPhyContext.getAssociatedAppIds()   : null);
                consoleClusterPhyVO.setBelongAppNames(null != clusterPhyContext ? clusterPhyContext.getAssociatedAppNames() : null);

                // 兼容旧版本
                consoleClusterPhyVO.setBelongAppId((null != clusterPhyContext &&
                        CollectionUtils.isNotEmpty(clusterPhyContext.getAssociatedAppIds())) ?
                        clusterPhyContext.getAssociatedAppIds().get(0) : null);
                // 兼容旧版本
                consoleClusterPhyVO.setBelongAppName(null != clusterPhyContext &&
                        CollectionUtils.isNotEmpty(clusterPhyContext.getAssociatedAppNames()) ?
                        clusterPhyContext.getAssociatedAppNames().get(0) : null);
            });
        });
        FUTURE_UTIL.waitExecute();

        LOGGER.info("class=ClusterPhyManagerImpl||method=buildClusterInfo||msg=time to build clusters belongAppIds and AppName is {} ms",
                System.currentTimeMillis() - timeForBuildClusterAppInfo);

        List<Integer> clusterIds = consoleClusterPhyVOList.stream().map(ConsoleClusterPhyVO::getId).collect(Collectors.toList());
        Map<Long, List<ClusterRoleInfo>> roleListMap = clusterRoleService.getAllRoleClusterByClusterIds(clusterIds);

        //3. 设置集群基本统计信息：磁盘使用信息
        long timeForBuildClusterDiskInfo = System.currentTimeMillis();
        for (ConsoleClusterPhyVO consoleClusterPhyVO : consoleClusterPhyVOList) {
            FUTURE_UTIL.runnableTask(() -> buildClusterRole(consoleClusterPhyVO, roleListMap.get(consoleClusterPhyVO.getId().longValue())));
        }
        FUTURE_UTIL.waitExecute();
        LOGGER.info("class=ClusterPhyManagerImpl||method=buildClusterInfo||msg=consumed build cluster belongAppIds and AppName time is {} ms",
                System.currentTimeMillis() - timeForBuildClusterDiskInfo);

        return consoleClusterPhyVOList;
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
    public Result<List<ESClusterRoleHostVO>> getClusterPhyRegionInfos(Integer clusterId) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(clusterId);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail(String.format("集群[%s]不存在", clusterId));
        }
        List<ClusterRoleHost> clusterRoleHostList = clusterRoleHostService.getNodesByCluster(clusterPhy.getCluster());
        return Result.buildSucc(ConvertUtil.list2List(clusterRoleHostList, ESClusterRoleHostVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ClusterPhyVO> joinCluster(ClusterJoinDTO param, String operator) {
        try {
            
            Result<Void> checkResult = checkClusterJoin(param, operator);
            if (checkResult.failed()) {
                return Result.buildFail(checkResult.getMessage());
            }
            String esClientHttpAddressesStr = clusterRoleHostService
                .buildESClientHttpAddressesStr(param.getRoleClusterHosts());

            Result<Void> initResult = initClusterJoin(param, operator, esClientHttpAddressesStr);
            if (initResult.failed()) {
                return Result.buildFail(initResult.getMessage());
            }

            // 1.保存物理集群信息(集群、角色、节点)
            Result<ClusterPhyVO> saveClusterResult = saveClusterPhy(param, operator);
            if (saveClusterResult.failed()) {
                throw new AdminOperateException(saveClusterResult.getMessage());
            } else {
                SpringTool.publish(new ClusterPhyEvent(param.getCluster(), param.getAppId()));
                postProcessingForClusterJoin(param, operator);
            }

            return saveClusterResult;
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
    public Result<List<PluginVO>> listPlugins(String cluster) {
        return Result.buildSucc(ConvertUtil.list2List(clusterPhyService.listClusterPlugins(cluster), PluginVO.class));
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
        Map<String, Object> clusterConfigMap = new HashMap<>(16);
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
        return Result.buildSucc(clusterPhyService.getRoutingAllocationAwarenessAttributes(cluster));
    }

    @Override
    public List<String> getAppClusterPhyNames(Integer appId) {
        if(appService.isSuperApp(appId)){
            //超级appId返回所有的集群
            List<ClusterPhy> phyList = clusterPhyService.listAllClusters();
            return phyList.stream().map(ClusterPhy::getCluster).distinct().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        }
        List<Long> appAuthLogicClusters = clusterLogicService.getHasAuthClusterLogicIdsByAppId(appId);
        Set<String> names = new HashSet<>();
        for (Long logicClusterId : appAuthLogicClusters) {
            ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContextCache(logicClusterId);
            if (clusterLogicContext != null) {
                names.addAll(clusterLogicContext.getAssociatedClusterPhyNames());
            }
        }
        List<String> appClusterPhyNames = Lists.newArrayList(names);
        appClusterPhyNames.sort(Comparator.naturalOrder());
        return appClusterPhyNames;
    }

    @Override
    public Result<List<String>> getTemplateSameVersionClusterNamesByTemplateId(Integer appId, Integer templateId) {
        List<String> clusterPhyNameList = getAppClusterPhyNames(appId);
        // No permission, cut branches and return
        if (CollectionUtils.isEmpty(clusterPhyNameList)) { return Result.buildSucc();}

        IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService.getLogicTemplateWithPhysicalsById(templateId);
        if (null == logicTemplateWithPhysicals) { return  Result.buildFail(String.format("templateId[%s] is not exist", templateId));}

        IndexTemplatePhy masterPhyTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
        if (null == masterPhyTemplate) {
            return  Result.buildFail(String.format("the physicals of templateId[%s] is empty", templateId));
        }

        String cluster = masterPhyTemplate.getCluster();
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(cluster);
        if (null == clusterPhy) { return  Result.buildFail(String.format("the cluster[%s] from templateId[%s] is empty", cluster, templateId));}

        String esVersion = clusterPhy.getEsVersion();

        List<ClusterPhy> clusterPhies = clusterPhyService.listAllClusters();
        List<String> sameVersionClusterNameList = clusterPhies.stream()
                .filter(Objects::nonNull)
                .filter(r -> clusterPhyNameList.contains(r.getCluster())
                        && esVersion.equals(r.getEsVersion()))
                .map(ClusterPhy::getCluster)
                .distinct()
                .collect(Collectors.toList());

        return Result.buildSucc(sameVersionClusterNameList);
    }

    @Override
    public List<String> getAppClusterPhyNodeNames(String clusterPhyName) {
        if (null == clusterPhyName) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=getAppClusterPhyNodeNames||errMsg=集群名称为空");
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
        ClusterPhy clusterPhy  = clusterPhyService.getClusterById(clusterPhyId);
        if (null == clusterPhy) {
            return Result.buildFail(String.format("物理集群Id[%s]不存在", clusterPhyId));
        }

        try {
            List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.getNodesByCluster(clusterPhy.getCluster());
            // 该物理集群有采集到host数据才执行删除操作
            if (!CollectionUtils.isEmpty(clusterRoleHosts)) {
                Result<Void> deleteHostResult = clusterRoleHostService.deleteByCluster(clusterPhy.getCluster());
                if (deleteHostResult.failed()) {
                    throw new AdminOperateException(String.format("删除集群[%s]节点信息失败", clusterPhy.getCluster()));
                }
            }

            Result<Void> deleteRoleResult = clusterRoleService.deleteRoleClusterByClusterId(clusterPhy.getId());
            if (deleteRoleResult.failed()) {
                throw new AdminOperateException(String.format("删除集群[%s]角色信息失败", clusterPhy.getCluster()));
            }

            Result<Boolean> deleteClusterResult  = clusterPhyService.deleteClusterById(clusterPhyId, operator);
            if (deleteClusterResult.failed()) {
                throw new AdminOperateException(String.format("删除集群[%s]信息失败", clusterPhy.getCluster()));
            }

            List<ClusterRegion> clusterRegionList = clusterRegionService.listPhyClusterRegions(clusterPhy.getCluster());
            if(!AriusObjUtils.isEmptyList(clusterRegionList)) {
                // 该物理集群有Region才删除
                Result<Void> deletePhyClusterRegionResult = clusterRegionService.deleteByClusterPhy(clusterPhy.getCluster(), operator);
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
    public Result<Boolean> addCluster(ClusterPhyDTO param, String operator, Integer appId) {
        Result<Boolean> result = clusterPhyService.createCluster(param, operator);

        if (result.success()) {
            SpringTool.publish(new ClusterPhyEvent(param.getCluster(), appId));
            operateRecordService.save(ModuleEnum.CLUSTER, OperationEnum.ADD, param.getCluster(), null, operator);
        }
        return result;
    }

    @Override
    public Result<Boolean> editCluster(ClusterPhyDTO param, String operator, Integer appId) {
        return clusterPhyService.editCluster(param, operator);
    }

    @Override
    public PaginationResult<ConsoleClusterPhyVO> pageGetClusterPhys(ClusterPhyConditionDTO condition, Integer appId) {
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
        App app = appService.getAppById(appId);
        if (!appService.isAppExists(app)) {
            return Lists.newArrayList();
        }

        boolean isSuperApp = appService.isSuperApp(app);
        //超级用户对所有模板都是管理权限
        if (isSuperApp && !AppClusterPhyAuthEnum.OWN.getCode().equals(authType)) {
            return Lists.newArrayList();
        }

        if (!AppClusterPhyAuthEnum.isExitByCode(authType)) {
            return Lists.newArrayList();
        }

        switch (AppClusterPhyAuthEnum.valueOf(authType)) {
            case OWN:
                if (isSuperApp) {
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
                            .map(clusterContextManager::getClusterLogicContextCache)
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
            List<ClusterRoleInfo> clusterRoleInfos = clusterRoleService.getAllRoleClusterByClusterId(cluster.getId());

            buildClusterRole(cluster, clusterRoleInfos);
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=buildClusterRole||logicClusterId={}", cluster.getId(), e);
        }
    }

    @Override
    public void buildClusterRole(ConsoleClusterPhyVO cluster, List<ClusterRoleInfo> clusterRoleInfos) {
        try {
            List<ESClusterRoleVO> roleClusters = ConvertUtil.list2List(clusterRoleInfos, ESClusterRoleVO.class);

            List<Long> roleClusterIds = roleClusters.stream().map(ESClusterRoleVO::getId).collect( Collectors.toList());
            Map<Long, List<ClusterRoleHost>> roleIdsMap = clusterRoleHostService.getByRoleClusterIds(roleClusterIds);

            for (ESClusterRoleVO esClusterRoleVO : roleClusters) {
                List<ClusterRoleHost> clusterRoleHosts = roleIdsMap.get(esClusterRoleVO.getId());
                List<ESClusterRoleHostVO> esClusterRoleHosts = ConvertUtil.list2List(clusterRoleHosts, ESClusterRoleHostVO.class);
                esClusterRoleVO.setEsClusterRoleHostVO(esClusterRoleHosts);
            }

            cluster.setEsClusterRoleVOS(roleClusters);
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

        ClusterPhyDTO esClusterDTO      = new ClusterPhyDTO();
        ClusterHealthEnum clusterHealthEnum = esClusterService.syncGetClusterHealthEnum(clusterPhyName);

        esClusterDTO.setId(clusterPhy.getId());
        esClusterDTO.setHealth(clusterHealthEnum.getCode());
        Result<Boolean> editClusterResult = clusterPhyService.editCluster(esClusterDTO, operator);
        if (editClusterResult.failed()) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=updateClusterHealth||clusterPhyName={}||errMsg={}",
                clusterPhyName, editClusterResult.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean updateClusterInfo(String cluster, String operator) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(cluster);
        if (null == clusterPhy) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=updateClusterInfo||clusterPhyName={}||msg=clusterPhy is empty", cluster);
            return false;
        }

        ESClusterStatsResponse clusterStats = esClusterService.syncGetClusterStats(cluster);
        long totalFsBytes      = clusterStats.getTotalFs().getBytes();
        long usageFsBytes      = clusterStats.getTotalFs().getBytes() - clusterStats.getFreeFs().getBytes();

        double diskFreePercent = clusterStats.getFreeFs().getGbFrac() / clusterStats.getTotalFs().getGbFrac();
        diskFreePercent = CommonUtils.formatDouble(1 - diskFreePercent, 5);

        ClusterPhyDTO esClusterDTO = new ClusterPhyDTO();
        esClusterDTO.setId(clusterPhy.getId());
        esClusterDTO.setDiskTotal(totalFsBytes);
        esClusterDTO.setDiskUsage(usageFsBytes);
        esClusterDTO.setDiskUsagePercent(diskFreePercent);
        Result<Boolean> editClusterResult = clusterPhyService.editCluster(esClusterDTO, operator);
        if (editClusterResult.failed()) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=updateClusterInfo||clusterPhyName={}||errMsg={}",
                    cluster, editClusterResult.getMessage());
            return false;
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
        return Result.buildSucc();
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

        return deleteClusterInfo(clusterPhy.getId(), operator, appId);
    }

    @Override
    public void buildBelongAppIdsAndNames(ConsoleClusterPhyVO consoleClusterPhyVO) {
        ClusterPhyContext clusterPhyContext = clusterContextManager.getClusterPhyContextCache(consoleClusterPhyVO.getCluster());
        consoleClusterPhyVO.setBelongAppIds(  null != clusterPhyContext ? clusterPhyContext.getAssociatedAppIds()   : null);
        consoleClusterPhyVO.setBelongAppNames(null != clusterPhyContext ? clusterPhyContext.getAssociatedAppNames() : null);

        // 兼容旧版本
        consoleClusterPhyVO.setBelongAppId((null != clusterPhyContext &&
                CollectionUtils.isNotEmpty(clusterPhyContext.getAssociatedAppIds())) ?
                clusterPhyContext.getAssociatedAppIds().get(0) : null);
        // 兼容旧版本
        consoleClusterPhyVO.setBelongAppName(null != clusterPhyContext &&
                CollectionUtils.isNotEmpty(clusterPhyContext.getAssociatedAppNames()) ?
                clusterPhyContext.getAssociatedAppNames().get(0) : null);
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
        List<ClusterLogicRackInfo> clusterLogicRackInfos = clusterRegionService.listLogicClusterRacks(clusterLogicId);
        if (CollectionUtils.isEmpty(clusterLogicRackInfos)) {
            return canBeAssociatedClustersPhyNamesResult;
        }

        //根据已绑定的物理集群的版本进行筛选
        String hasSelectedPhyClusterName = clusterLogicRackInfos.get(0).getPhyClusterName();
        return Result.buildSucc(getPhyClusterNameWithSameEsVersion(hasSelectedPhyClusterName, canBeAssociatedClustersPhyNamesResult.getData()));
    }

    @Override
    public Result<Boolean> checkTemplateServiceWhenJoin(ClusterJoinDTO clusterJoinDTO, String strId, String operator) {
        if (AriusObjUtils.isNull(clusterJoinDTO)) {
            return Result.buildFail("接入集群不存在");
        }

        //从指定接入物理集群中获取可以使用的httpAddress
        String httpAddresses = buildClusterReadAndWriteAddressWhenJoin(clusterJoinDTO);
        if (StringUtils.isBlank(httpAddresses)) {
            return Result.buildFail("接入集群中可连接信息为空");
        }

        return templateSrvManager.checkTemplateSrvWhenJoin(httpAddresses, clusterJoinDTO.getPassword(), strId);
    }

    /**
     * 根据物理集群名称来获取满足设置磁盘存储大小的rack列表
     *
     * @param clusterPhyName   物理集群名称
     * @param templateSize     模板设置的磁盘存储大小
     * @param clusterLogicName 逻辑集群名称
     * @return 满足条件的rack列表
     */
    @Override
    @Deprecated
    public Result<Set<String>> getValidRacksListByTemplateSize(String clusterPhyName, String clusterLogicName, String templateSize) {
        //将传入的数据大小转化为字节数的单位,获取逻辑集群信息
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByName(clusterLogicName);
        if (AriusObjUtils.isNull(clusterLogic)) {
            return Result.buildFail("对应的逻辑集群不存在");
        }

        //获取逻辑集群关联到物理集群的region资源信息
        List<ClusterRegion> logicBindRegions = clusterRegionService.listRegionsByLogicAndPhyCluster(clusterLogic.getId(), clusterPhyName);
        if (CollectionUtils.isEmpty(logicBindRegions)) {
            return Result.buildFail("无法获取逻辑集群对应的region信息");
        }

        Set<String> canCreateTemplateRegionLists = logicBindRegions
                .stream()
                .map(ClusterRegion::getRacks)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(canCreateTemplateRegionLists)) {
            return Result.buildFail("没有能设置指定模板数据大小的region");
        }

        //返回按照磁盘大小倒序输出的序列
        return Result.buildSucc(canCreateTemplateRegionLists);
    }

    @Override
    public Result<ClusterPhyVO> updateClusterGateway(ClusterPhyDTO param, String operator, Integer appId) {
        ClusterPhyDTO clusterPhyDTO = new ClusterPhyDTO();
        clusterPhyDTO.setId(param.getId());
        clusterPhyDTO.setGatewayUrl(param.getGatewayUrl());
        ClusterPhy oldCluster = clusterPhyService.getClusterById(param.getId());
        Result<Boolean> result = clusterPhyService.editCluster(clusterPhyDTO, operator);
        if (result.failed() || !result.getData()) {
            return Result.buildFail("编辑gateway失败！");
        }
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(param.getId());
        //todo 这里需要记录操作记录
        return Result.buildSucc(ConvertUtil.obj2Obj(clusterPhy, ClusterPhyVO.class));
    }


    /**************************************** private method ***************************************************/
    /**
     * 更新物理模板setting single_type为true
     * @param cluster  集群
     * @param template 物理模板
     * @return
     */
    private boolean setTemplateSettingSingleType(String cluster, String template) {
        Map<String, String> setting = new HashMap<>(2);
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
     * 根据接入集群信息获取可以连通的es地址
     * @param clusterJoinDTO 接入物理集群
     * @return 有效的es地址
     */
    private String buildClusterReadAndWriteAddressWhenJoin(ClusterJoinDTO clusterJoinDTO) {
        // 获取集群原有的client-node和master-node的地址和端口号
        List<ESClusterRoleHostDTO> clusterRoleHosts = clusterJoinDTO.getRoleClusterHosts();
        if (CollectionUtils.isEmpty(clusterRoleHosts)) {
            return null;
        }

        //设置接入集群中的master和client地址信息
        List<String> clientHttpAddresses = Lists.newArrayList();
        List<String> masterHttpAddresses = Lists.newArrayList();
        for (ESClusterRoleHostDTO roleClusterHost : clusterRoleHosts) {
            if (roleClusterHost.getRole().equals(CLIENT_NODE.getCode())) {
                clientHttpAddresses.add(roleClusterHost.getIp() + ":" + roleClusterHost.getPort());
            }
            if (roleClusterHost.getRole().equals(MASTER_NODE.getCode())) {
                masterHttpAddresses.add(roleClusterHost.getIp() + ":" + roleClusterHost.getPort());
            }
        }

        // 如果client节点信息不为空，则使用client节点的ip地址, 否则使用master节点信息
        if (!CollectionUtils.isEmpty(clientHttpAddresses)) {
            return ListUtils.strList2String(clientHttpAddresses);
        } else {
            return ListUtils.strList2String(masterHttpAddresses);
        }
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

        if (!ClusterResourceTypeEnum.isExist(clusterLogicType)) {
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

        List<ConsoleClusterPhyVO> consoleClusterPhys = ConvertUtil.list2List(phyClusters, ConsoleClusterPhyVO.class);

        consoleClusterPhys.parallelStream()
            .forEach(consoleClusterPhyVO -> buildPhyCluster(consoleClusterPhyVO, currentAppId));

        Collections.sort(consoleClusterPhys);

        return consoleClusterPhys;
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

        if (appService.isSuperApp(currentAppId)) {
            cluster.setCurrentAppAuth(AppClusterLogicAuthEnum.ALL.getCode());
        }

        //获取物理集群绑定的逻辑集群
        ClusterLogic clusterLogic = getClusterLogicByClusterPhyName(cluster.getCluster());
        if(clusterLogic == null) {
            return;
        }

        //TODO:  公共模块依赖, 一个物理集群对应多个逻辑集群的情况该归属哪个appId
        cluster.setBelongAppIds(Lists.newArrayList(clusterLogic.getAppId()));
        cluster.setResponsible(clusterLogic.getResponsible());

        App app = appService.getAppById(clusterLogic.getAppId());
        if (!AriusObjUtils.isNull(app)) {
            cluster.setBelongAppNames(Lists.newArrayList(app.getName()));
        }

        //TODO:  公共模块依赖, auth table中 加type字段标识是逻辑集群还是物理集群
        AppClusterLogicAuthEnum logicClusterAuthEnum = appClusterLogicAuthService.getLogicClusterAuthEnum(currentAppId, clusterLogic.getId());
        cluster.setCurrentAppAuth(logicClusterAuthEnum.getCode());

        if (appService.isSuperApp(currentAppId)) {
            cluster.setCurrentAppAuth(AppClusterLogicAuthEnum.ALL.getCode());
        }
    }

    /**
     * 获取物理集群所绑定的逻辑集群的信息
     */
    private ClusterLogic getClusterLogicByClusterPhyName(String phyClusterName) {
        ClusterPhyContext clusterPhyContext = clusterContextManager.getClusterPhyContext(phyClusterName);
        List<Long> clusterLogicIds = Lists.newArrayList();
        if (!AriusObjUtils.isNull(clusterPhyContext)
                && !AriusObjUtils.isNull(clusterPhyContext.getAssociatedClusterLogicIds())) {
            clusterLogicIds = clusterPhyContext.getAssociatedClusterLogicIds();
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


    private Result<ClusterPhyVO> saveClusterPhy(ClusterJoinDTO param, String operator) {
        //保存集群信息
        ClusterPhyDTO clusterDTO    =  buildClusterPhy(param, operator);
        Result<Boolean> addClusterRet =  clusterPhyService.createCluster(clusterDTO, operator);
        if (addClusterRet.failed()) { return Result.buildFrom(addClusterRet);}
        return Result.buildSucc(ConvertUtil.obj2Obj(clusterDTO, ClusterPhyVO.class));
    }

    private ClusterPhyDTO buildClusterPhy(ClusterJoinDTO param, String operator) {
        ClusterPhyDTO clusterDTO = ConvertUtil.obj2Obj(param, ClusterPhyDTO.class);

        String clientAddress = clusterRoleHostService.buildESClientHttpAddressesStr(param.getRoleClusterHosts());

        clusterDTO.setDesc(param.getPhyClusterDesc());
        clusterDTO.setDataCenter(param.getDataCenter());
        clusterDTO.setHttpAddress(clientAddress);
        clusterDTO.setHttpWriteAddress(clientAddress);
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
    /**
     * 集群接入参数校验
     *
     * @param param    参数
     * @param operator 操作人
     * @return {@link Result}<{@link Void}>
     */
    private Result<Void> checkClusterJoin(ClusterJoinDTO param, String operator) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("参数为空");
        }
        ClusterTags clusterTags = ConvertUtil.str2ObjByJson(param.getTags(), ClusterTags.class);

        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人不存在");
        }

        if (!ESClusterTypeEnum.validCode(param.getType())) {
            return Result.buildParamIllegal("非支持的集群类型");
        }

        if (!ClusterResourceTypeEnum.isExist(param.getResourceType())) {
            return Result.buildParamIllegal("非支持的集群资源类型");
        }

        if (ESClusterCreateSourceEnum.ES_IMPORT != ESClusterCreateSourceEnum.valueOf(clusterTags.getCreateSource())) {
            return Result.buildParamIllegal("非集群接入来源");
        }

        if (!ESClusterImportRuleEnum.validCode(param.getImportRule())) {
            return Result.buildParamIllegal("非支持的接入规则");
        }

        return checkClusterNodes(param);
    }

    private Result<Void> checkClusterNodes(ClusterJoinDTO param) {
        List<ESClusterRoleHostDTO> roleClusterHosts = param.getRoleClusterHosts();
        if (CollectionUtils.isEmpty(roleClusterHosts)) {
            return Result.buildParamIllegal("集群节点信息为空");
        }

        // 对于接入集群的节点端口进行校验
        Set<String> wrongPortSet = roleClusterHosts.stream()
                .map(ESClusterRoleHostDTO::getPort)
                .filter(this::wrongPortDetect)
                .collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(wrongPortSet)) {
            return Result.buildParamIllegal("接入集群中端口号存在异常" + wrongPortSet);
        }

        if (ESClusterImportRuleEnum.FULL_IMPORT.getCode() == param.getImportRule()) {
            Set<Integer> roleForNode = roleClusterHosts.stream().map(ESClusterRoleHostDTO::getRole)
                .collect(Collectors.toSet());

            if (!roleForNode.contains(MASTER_NODE.getCode())) {
                return Result.buildParamIllegal(String.format(NODE_NOT_EXISTS_TIPS, MASTER_NODE.getDesc()));
            }

            Map<Integer, List<String>> role2IpsMap = ConvertUtil.list2MapOfList(roleClusterHosts,
                ESClusterRoleHostDTO::getRole, ESClusterRoleHostDTO::getIp);

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
        } else {

            List<String> ips = roleClusterHosts.stream().map(ESClusterRoleHostDTO::getIp).collect(Collectors.toList());
            if (ips.size() < JOIN_MASTER_NODE_MIN_NUMBER) {
                return Result.buildParamIllegal(String.format("集群%s的节点个数要求大于等于1，且不重复", param.getCluster()));
            }

            String duplicateIpForMaster = ClusterUtils.getDuplicateIp(ips);
            if (!AriusObjUtils.isBlack(duplicateIpForMaster)) {
                return Result.buildParamIllegal(String.format(IP_DUPLICATE_TIPS, duplicateIpForMaster));
            }
        }

        if (clusterPhyService.isClusterExists(param.getCluster())) {
            return Result.buildParamIllegal(String.format("物理集群名称:%s已存在", param.getCluster()));
        }

        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByName(param.getLogicCluster());
        if (!AriusObjUtils.isNull(clusterLogic)) {
            return Result.buildParamIllegal(String.format("逻辑集群名称:%s已存在", param.getLogicCluster()));
        }

        String esClientHttpAddressesStr = clusterRoleHostService.buildESClientHttpAddressesStr(roleClusterHosts);

        //密码验证
        Result<Void> passwdResult = checkClusterWithoutPasswd(param, esClientHttpAddressesStr);
        if (passwdResult.failed()) {
            return passwdResult;
        }
        //同集群验证
        Result<Void> sameClusterResult = checkSameCluster(param.getPassword(), clusterRoleHostService.buildESAllRoleHttpAddressesList(roleClusterHosts));
        if (sameClusterResult.failed()) {
            return Result.buildParamIllegal("禁止同时接入超过两个不同集群节点");
        }
        return Result.buildSucc();
    }

    private Result<Void> initClusterJoin(ClusterJoinDTO param, String operator, String esClientHttpAddressesStr) {
        //获取设置es版本
        Result<Void> esVersionSetResult = initESVersionForClusterJoin(param, esClientHttpAddressesStr);
        if (esVersionSetResult.failed()) {
            return esVersionSetResult;
        }

        param.setResponsible(operator);
        return Result.buildSucc();
    }

    /**
     * 检测「未设置密码的集群」接入时是否携带账户信息
     */
    private Result<Void> checkClusterWithoutPasswd(ClusterJoinDTO param, String esClientHttpAddressesStr) {
        ClusterConnectionStatus status = esClusterService.checkClusterPassword(esClientHttpAddressesStr, null);
        if (ClusterConnectionStatus.DISCONNECTED == status) {
            return Result.buildParamIllegal("集群离线未能连通");
        }

        if (!Strings.isNullOrEmpty(param.getPassword())) {
            if (ClusterConnectionStatus.NORMAL == status) {
                return Result.buildParamIllegal("未设置密码的集群，请勿输入账户信息");
            }
            status = esClusterService.checkClusterPassword(esClientHttpAddressesStr, param.getPassword());
            if (ClusterConnectionStatus.UNAUTHORIZED == status) {
                return Result.buildParamIllegal("集群的账户信息错误");
            }
        } else {
            if (ClusterConnectionStatus.UNAUTHORIZED == status) {
                return Result.buildParamIllegal("集群设置有密码，请输入账户信息");
            }
        }
        return Result.buildSucc();
    }

    private Result<Void> checkSameCluster(String passwd, List<String> esClientHttpAddressesList) {
        return esClusterService.checkSameCluster(passwd, esClientHttpAddressesList);
    }

    /**
     * 初始化集群版本
     * @param param
     * @param esClientHttpAddressesStr
     * @return
     */
    private Result<Void> initESVersionForClusterJoin(ClusterJoinDTO param, String esClientHttpAddressesStr) {
        String esVersion = esClusterService.synGetESVersionByHttpAddress(esClientHttpAddressesStr, param.getPassword());
        if (Strings.isNullOrEmpty(esVersion)) {
            return Result.buildParamIllegal(String.format("%s无法获取es版本", esClientHttpAddressesStr));
        }
        param.setEsVersion(esVersion);
        return Result.buildSucc();
    }

    private void doDeleteClusterJoin(ClusterPhy clusterPhy, String operator) throws AdminOperateException {
        ClusterPhyContext clusterPhyContext = clusterContextManager.getClusterPhyContext(clusterPhy.getCluster());
        if (null == clusterPhyContext) {
            return;
        }

        List<Long> associatedRegionIds = clusterPhyContext.getAssociatedRegionIds();
        for (Long associatedRegionId : associatedRegionIds) {
            Result<Void> unbindRegionResult = clusterRegionService.unbindRegion(associatedRegionId, null, operator);
            if (unbindRegionResult.failed()) {
                throw new AdminOperateException(String.format("解绑region(%s)失败", associatedRegionId));
            }

            Result<Void> deletePhyClusterRegionResult = clusterRegionService.deletePhyClusterRegion(associatedRegionId,
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

        Result<Void> deleteRoleClusterResult = clusterRoleService.deleteRoleClusterByClusterId(clusterPhy.getId());
        if (deleteRoleClusterResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群角色(%s)失败", clusterPhy.getCluster()));
        }

        Result<Void> deleteRoleClusterHostResult = clusterRoleHostService.deleteByCluster(clusterPhy.getCluster());
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

        return getClusterStatsTriple(cluster, initTriple);
    }

    private Triple<Long, Long, Double> getClusterStatsTriple(String cluster, Triple<Long, Long, Double> initTriple) {
        if (CLUSTER_NAME_TO_ES_CLUSTER_STATS_TRIPLE_MAP.containsKey(cluster)) {
            return CLUSTER_NAME_TO_ES_CLUSTER_STATS_TRIPLE_MAP.get(cluster);
        } else {
            ESClusterStatsResponse clusterStats = esClusterService.syncGetClusterStats(cluster);
            if (null != clusterStats && null != clusterStats.getFreeFs() && null != clusterStats.getTotalFs()
                    && clusterStats.getTotalFs().getBytes() > 0 && clusterStats.getFreeFs().getBytes() > 0) {
                initTriple.setV1(clusterStats.getTotalFs().getBytes());
                initTriple.setV2(clusterStats.getTotalFs().getBytes() - clusterStats.getFreeFs().getBytes());
                double diskFreePercent = clusterStats.getFreeFs().getGbFrac() / clusterStats.getTotalFs().getGbFrac();
                initTriple.setV3(1 - diskFreePercent);
            }

            CLUSTER_NAME_TO_ES_CLUSTER_STATS_TRIPLE_MAP.put(cluster, initTriple);
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

    private void postProcessingForClusterJoin(ClusterJoinDTO param, String operator) {
        esOpClient.connect(param.getCluster());

        if (ESClusterImportRuleEnum.AUTO_IMPORT == ESClusterImportRuleEnum.valueOf(param.getImportRule())) {
            clusterRoleHostService.collectClusterNodeSettings(param.getCluster());
        } else if (ESClusterImportRuleEnum.FULL_IMPORT == ESClusterImportRuleEnum.valueOf(param.getImportRule())) {
            //1.先持久化用户输入的节点信息
            clusterRoleHostService.saveClusterNodeSettings(param);
            //2.直接拉es 更新节点信息，去除因为定时任务触发导致的更新延时
            clusterRoleHostService.collectClusterNodeSettings(param.getCluster());
        }

        updateClusterHealth(param.getCluster(), AriusUser.SYSTEM.getDesc());

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

            CLUSTER_NAME_TO_ES_CLUSTER_STATS_TRIPLE_MAP.put(clusterName, initTriple);
        }
    }

    /**
     * 对于异常的端口号的检测
     * @param port 端口号
     * @return 校验结果
     */
    private boolean wrongPortDetect(String port) {
        try {
            int portValue = Integer.parseInt(port);
            return portValue < AdminConstant.MIN_BIND_PORT_VALUE || portValue > AdminConstant.MAX_BIND_PORT_VALUE;
        } catch (NumberFormatException e) {
            LOGGER.error(
                    "class=ClusterPhyManagerImpl||method=wrongPortDetect||port={}||msg=Integer format error",
                    port);
            return false;
        }
    }
}

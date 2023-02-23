package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.CLUSTER_PHY;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.page.ClusterPhyPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplatePhyMappingManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.PipelineManager;
import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterTag;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting.ESClusterGetSettingsAllResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPhyPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.plugin.PluginInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.RunModeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterCreateSourceEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterImportRuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ResourceLogicLevelEnum;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionEditByHostEvent;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterPhyEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusScheduleThreadPool;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleThree;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.plugin.PluginInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.core.service.template.pipeline.ESPipelineService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didiglobal.knowframework.elasticsearch.client.request.ingest.Pipeline;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.template.TemplateConfig;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.didiglobal.logi.op.manager.application.ComponentService;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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

    private static final ILog                                    LOGGER                                      = LogFactory
        .getLog(ClusterPhyManagerImpl.class);

    private static final String                                  NODE_NOT_EXISTS_TIPS                        = "集群缺少类型为%s的节点";

    private static final String                                  IP_DUPLICATE_TIPS                           = "集群ip:%s重复, 请重新输入";
    /**
     * Map< cluster , Triple< diskUsage , diskTotal , diskUsagePercent >>
     */
    private static final Map<String, Triple<Long, Long, Double>> CLUSTER_NAME_TO_ES_CLUSTER_STATS_TRIPLE_MAP = Maps
        .newConcurrentMap();
    public static final String                                   SEPARATOR_CHARS                             = ",";
    private static final String COLD             = "cold";

    

    @Autowired
    private ESGatewayClient                                      esGatewayClient;

    @Autowired
    private ESTemplateService                                    esTemplateService;

    @Autowired
    private ClusterPhyService                                    clusterPhyService;

    @Autowired
    private ClusterLogicService                                  clusterLogicService;

    @Autowired
    private ClusterRoleService                                   clusterRoleService;

    @Autowired
    private ClusterRoleHostService                               clusterRoleHostService;

    @Autowired
    private IndexTemplatePhyService                              indexTemplatePhyService;

    @Autowired
    private TemplatePhyMappingManager                            templatePhyMappingManager;

    @Autowired
    private PipelineManager                                      templatePipelineManager;

    @Autowired
    private IndexTemplateService                                 indexTemplateService;

    @Autowired
    private TemplatePhyManager                                   templatePhyManager;

    @Autowired
    private ClusterRegionService                                 clusterRegionService;

    

    @Autowired
    private ProjectService                                       projectService;

    @Autowired
    private OperateRecordService                                 operateRecordService;

    @Autowired
    private ESClusterNodeService                                 esClusterNodeService;

    @Autowired
    private ESClusterService                                     esClusterService;

    @Autowired
    private HandleFactory                                        handleFactory;

    @Autowired
    private AriusScheduleThreadPool                              ariusScheduleThreadPool;

    @Autowired
    private ESOpClient                                           esOpClient;

    @Autowired
    private GatewayClusterService gatewayClusterService;

    @Autowired
    private GatewayNodeService gatewayNodeService;
    @Autowired
    private RoleTool           roleTool;
    @Autowired
    private ComponentService         componentService;
    @Autowired
    private PluginInfoService        pluginInfoService;

    @Autowired
    private ESPipelineService esPipelineService;
    
    private static final FutureUtil<Void>         FUTURE_UTIL               = FutureUtil.init(
            "ClusterPhyManagerImpl", 20, 40, 100);
    private static final Cache</*clusterPhy*/String, TupleThree</*dcdrExist*/Boolean,/*pipelineExist*/ Boolean,/*existColdRegion*/ Boolean>> CLUSTER_PHY_DCDR_PIPELINE   = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(10000).build();
    private static final Cache</*clusterPhy*/String, ClusterConnectionStatusWithTemplateEnum> CLUSTER_PHY_CONNECTION_ENUM = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(10000).build();
    @PostConstruct
    private void init() {
        ariusScheduleThreadPool.submitScheduleAtFixedDelayTask(this::refreshClusterDistInfo, 60, 180);
        ariusScheduleThreadPool.submitScheduleAtFixedDelayTask(this::refreshClusterPhyInfoWithCache, 60, 10 * 60L);
    
    }
    
    /**
     * 每45分钟全量更新一遍集群
     */
    private synchronized void refreshClusterPhyInfoWithCache() {
    
        for (String clusterName : clusterPhyService.listClusterNames()) {
            try {
                CLUSTER_PHY_DCDR_PIPELINE.put(clusterName, getDCDRAndPipelineTupleByClusterPhy(clusterName));
            } catch (ESOperateException e) {
                LOGGER.error("class=ClusterPhyManagerImpl||method=refreshClusterPhyInfoWithCache||clusterName={}||errMsg=fail to getDCDRAndPipelineTupleByClusterPhy",
                        clusterName);
            }
            CLUSTER_PHY_CONNECTION_ENUM.put(clusterName, getClusterConnectionStatus(clusterName));
        }
    }

  
    
    /**
     * @param clusterPhy
     */
    @Override
    public TupleThree</*dcdrExist*/Boolean,/*pipelineExist*/ Boolean,/*existColdRegion*/ Boolean> getDCDRAndPipelineAndColdRegionTupleByClusterPhyWithCache(
            String clusterPhy) {
        try {
           return CLUSTER_PHY_DCDR_PIPELINE.get(
                clusterPhy,()->getDCDRAndPipelineTupleByClusterPhy(clusterPhy));
        }catch (Exception e){
            return Tuples.of(false, false, false);
        }
    }
    
    @Override
    public ClusterConnectionStatusWithTemplateEnum getClusterConnectionStatusWithCache(String clusterPhy) {
        try {
            return CLUSTER_PHY_CONNECTION_ENUM.get(clusterPhy, () -> getClusterConnectionStatus(clusterPhy));
        } catch (Exception e) {
            return ClusterConnectionStatusWithTemplateEnum.DISCONNECTED;
        }
    }
    
   
    
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
    public void syncTemplateMetaData(String cluster, int retryCount) throws ESOperateException {
        // 获取物理集群下的所有物理模板
        List<IndexTemplatePhy> physicals = indexTemplatePhyService.getNormalTemplateByCluster(cluster);
        if (CollectionUtils.isEmpty(physicals)) {
            LOGGER.info(
                "class=ESClusterPhyServiceImpl||method=syncTemplateMetaData||cluster={}||msg=syncTemplateMetaData no template",
                cluster);
            return;
        }

        // 从ES中获取到集群的全量pipeline
        Map<String/*pipelineId*/, Pipeline> pipelineMap = esPipelineService.getClusterPipelines(cluster);
        // 从ES中获取到集群全量模版配置信息
        Map<String/*templateName*/, TemplateConfig> templateConfigMap = esTemplateService
                .syncGetAllTemplates(Collections.singletonList(cluster));

        // 遍历物理模板
        for (IndexTemplatePhy physical : physicals) {
            try {
                // 同步模板元数据到ES集群（修改ES集群中的模板）
                templatePhyManager.syncMeta(physical.getId(), retryCount, templateConfigMap);
                // 同步最新元数据到ES集群pipeline
                templatePipelineManager.syncPipeline(physical.getLogicId(), pipelineMap);
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
    public List<ClusterPhyVO> listClusterPhys(ClusterPhyDTO param) {
        List<ClusterPhy> phyClusters = clusterPhyService.listClustersByCondt(param);
        return buildClusterInfo(phyClusters);
    }

    @Override
    public List<ClusterPhyVO> buildClusterInfo(List<ClusterPhy> clusterPhyList) {
        if (CollectionUtils.isEmpty(clusterPhyList)) {
            return Lists.newArrayList();
        }

        List<ClusterPhyVO> clusterPhyVOList = ConvertUtil.list2List(clusterPhyList, ClusterPhyVO.class);

        List<Integer> clusterIds = clusterPhyVOList.stream().map(ClusterPhyVO::getId).collect(Collectors.toList());
        Map<Long, List<ClusterRoleInfo>> roleListMap = clusterRoleService.getAllRoleClusterByClusterIds(clusterIds);
        //3. 设置集群基本统计信息：磁盘使用信息
        long timeForBuildClusterDiskInfo = System.currentTimeMillis();
        for (ClusterPhyVO clusterPhyVO : clusterPhyVOList) {
            FUTURE_UTIL.runnableTask(
                            () -> buildClusterRole(clusterPhyVO, roleListMap.get(clusterPhyVO.getId().longValue())));
        }
        buildClusterPhyWithLogicAndRegion(clusterPhyVOList);
        FUTURE_UTIL.waitExecute();
        LOGGER.info(
            "class=ClusterPhyManagerImpl||method=buildClusterInfo||msg=consumed build cluster belongProjectIds and ProjectName time is {} ms",
            System.currentTimeMillis() - timeForBuildClusterDiskInfo);

        return clusterPhyVOList;
    }

    private long buildClusterPhyWithLogicAndRegion(List<ClusterPhyVO> clusterPhyVOList) {
        List<ClusterRegion> regions = clusterRegionService.listRegionByPhyClusterNames(
            clusterPhyVOList.stream().map(ClusterPhyVO::getCluster).distinct().collect(Collectors.toList()));
        Map<String, Set<Long>> phyCluster2logicClusterIds = Maps.newHashMap();
        final List<ProjectBriefVO> projectBriefList = projectService.getProjectBriefList();
        final Map<Integer, String> projectId2ProjectNameMap = ConvertUtil.list2Map(projectBriefList, ProjectBriefVO::getId,
                ProjectBriefVO::getProjectName);
        Map<Long,  List<ClusterLogicVO>> logicClusterId2LogicClusterList = Maps.newHashMap();
        Map<Long, ClusterRegionVO> logicClusterId2Region = Maps.newHashMap();
        List<Long> logicIds = Lists.newArrayList();
        regions.stream()
            .filter(region -> StringUtils.isNotBlank(region.getPhyClusterName())
                              && StringUtils.isNotBlank(region.getLogicClusterIds())
                              && !AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID.equals(region.getLogicClusterIds()))
            .forEach(region -> {
                String idStr = region.getLogicClusterIds();
                List<Long> list = Lists.newArrayList();
                for (String id : StringUtils.split(idStr, SEPARATOR_CHARS)) {
                    if (StringUtils.isNumeric(id)) {
                        list.add(Long.valueOf(id));
                    }
                }
                Set<Long> ids = phyCluster2logicClusterIds.getOrDefault(region.getPhyClusterName(), Sets.newHashSet());
                ids.addAll(list);
                phyCluster2logicClusterIds.put(region.getPhyClusterName(), ids);
                list.forEach(id -> logicClusterId2Region.put(id, ConvertUtil.obj2Obj(region, ClusterRegionVO.class, regionVO -> regionVO.setClusterName(region.getPhyClusterName()))));
                logicIds.addAll(list);
            });
        if (CollectionUtils.isNotEmpty(logicIds)) {
            List<ClusterLogic> clusterLogicList = clusterLogicService.getClusterLogicListByIds(logicIds);
    
            logicClusterId2LogicClusterList = ConvertUtil.list2MapOfList(clusterLogicList, ClusterLogic::getId,
                    clusterLogic -> ConvertUtil.obj2Obj(clusterLogic, ClusterLogicVO.class,
                            clusterLogicVO -> clusterLogicVO.setProjectName(
                                    projectId2ProjectNameMap.get(clusterLogicVO.getProjectId()))));
        }
        
        //3. 设置集群基本统计信息：磁盘使用信息
        long timeForBuildClusterDiskInfo = System.currentTimeMillis();
        for (ClusterPhyVO clusterPhyVO : clusterPhyVOList) {
            Set<Long> set = phyCluster2logicClusterIds.getOrDefault(clusterPhyVO.getCluster(), Sets.newHashSet());
            Map<Long, List<ClusterLogicVO>> finalLogicClusterId2Vo = logicClusterId2LogicClusterList;
            FUTURE_UTIL.runnableTask(()-> {
                set.forEach(id -> {
                    List<ClusterLogicVO> clusterLogicVOList = finalLogicClusterId2Vo.get(id);
                    if (CollectionUtils.isNotEmpty(clusterLogicVOList)) {
                        List<String> projectNames = clusterLogicVOList.stream().map(ClusterLogicVO::getProjectName).collect(Collectors.toList());
                        ClusterLogicVOWithProjects clusterLogicVOWithProjects = ConvertUtil.obj2Obj(clusterLogicVOList.get(0),
                                ClusterLogicVOWithProjects.class, cp -> cp.setProjectNameList(projectNames));
                        clusterPhyVO.addLogicCluster(clusterLogicVOWithProjects, logicClusterId2Region.get(id));
                    }
        
                });
                Optional.ofNullable(clusterPhyVO.getLogicClusterAndRegionList())
                        .map(logicClusterAndRegionList -> logicClusterAndRegionList.stream().map(Tuple::getV1).map(ClusterLogicVOWithProjects::getName).distinct().collect(Collectors.toList()))
                        .ifPresent(clusterPhyVO::setBindLogicCluster);
            });
        }
        FUTURE_UTIL.waitExecute();
        return timeForBuildClusterDiskInfo;
    }

    @Override
    public ClusterPhyVO getClusterPhyOverview(Integer clusterId, Integer currentProjectId) {
        // 获取基本信息
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(clusterId);
        if (clusterPhy == null) {
            return new ClusterPhyVO();
        }

        ClusterPhyVO clusterPhyVO = ConvertUtil.obj2Obj(clusterPhy, ClusterPhyVO.class);
        // 构建overView信息
        buildPhyCluster(clusterPhyVO);
        return clusterPhyVO;
    }

    @Override
    public Result<List<String>> listCanBeAssociatedRegionOfClustersPhys(Integer clusterLogicType, Long clusterLogicId) {
        if (!ClusterResourceTypeEnum.isExist(clusterLogicType)) {
            return Result.buildParamIllegal("集群资源类型非法");
        }
        List<String> clusters = Lists.newArrayList();
        ClusterLogic clusterLogic =
                clusterLogicService.getClusterLogicByIdThatNotContainsProjectId(clusterLogicId );
        if (clusterLogic == null) {
            return Result.buildFail("选定的逻辑集群不存在");
        }

        ClusterRegion logicClusterRegions = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
        if (null != logicClusterRegions) {
            return Result.buildSucc(clusters);
        }
        return listCanBeAssociatedClustersPhys(clusterLogicType);
    }

    @Override
    public Result<List<String>> listCanBeAssociatedClustersPhys(Integer clusterLogicType) {
        if (!ClusterResourceTypeEnum.isExist(clusterLogicType)) {
            return Result.buildParamIllegal("集群资源类型非法");
        }

        List<String> clusters = Lists.newArrayList();
        ClusterPhyDTO clusterPhyDTO = new ClusterPhyDTO();
        clusterPhyDTO.setResourceType(clusterLogicType);
        List<ClusterPhy> list = clusterPhyService.listClustersByCondt(clusterPhyDTO);

        if (PUBLIC.getCode() == clusterLogicType) {
            //共享
            clusters = list.stream().map(ClusterPhy::getCluster).collect(Collectors.toList());
        } else if (EXCLUSIVE.getCode() == clusterLogicType) {
            //独享，需要查询是否有未绑定的region和节点
            clusters = list.stream().filter(cluster -> {
                List<ClusterRegion> regions = clusterRegionService.listPhyClusterRegions(cluster.getCluster());
                if (regions.stream().anyMatch(region -> !clusterRegionService.isRegionBound(region))) {
                    return true;
                }
                List<ClusterRoleHost> roleHostList = clusterRoleHostService
                    .getByRoleAndClusterId(Long.valueOf(cluster.getId()), DATA_NODE.getDesc());
                return roleHostList.stream().anyMatch(node -> node.getRegionId() == -1);
            }).map(ClusterPhy::getCluster).collect(Collectors.toList());
        } else if (PRIVATE.getCode() == clusterLogicType) {
            //独立，未绑定逻辑集群
            clusters = list.stream().filter(cluster -> {
                Set<Long> logicIds = clusterRegionService.getLogicClusterIdByPhyClusterId(cluster.getId());
                return CollectionUtils.isEmpty(logicIds);
            }).map(ClusterPhy::getCluster).collect(Collectors.toList());
        }

        return Result.buildSucc(clusters);
    }

    private final Consumer<ESClusterRoleHostDTO> roleClusterHostsTrimHostnameAndPort = esClusterRoleHostDTO -> {
        esClusterRoleHostDTO.setCluster(StringUtils.trim(esClusterRoleHostDTO.getCluster()));
        esClusterRoleHostDTO.setHostname(StringUtils.trim(esClusterRoleHostDTO.getHostname()));
        esClusterRoleHostDTO.setIp(StringUtils.trim(esClusterRoleHostDTO.getIp()));
        esClusterRoleHostDTO.setPort(StringUtils.trim(esClusterRoleHostDTO.getPort()));

    };

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ClusterPhyVO> joinCluster(ClusterJoinDTO param, String operator, Integer projectId) {
        if (param.getProjectId() == null) {
            param.setProjectId(projectId);
        }
        //这里其实是需要一个内置trim 用来保证传输进行的roleClusterHosts是正确的
        param.getRoleClusterHosts().forEach(roleClusterHostsTrimHostnameAndPort);
        Result<Void> checkResult = checkClusterJoin(param, operator);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }
        String esClientHttpAddressesStr = clusterRoleHostService
            .buildESClientHttpAddressesStr(param.getRoleClusterHosts());
        for (ESClusterRoleHostDTO roleClusterHost : param.getRoleClusterHosts()) {
            if (roleClusterHost.getRegionId() == null) {
                roleClusterHost.setRegionId(-1);
            }
        }
        Result<Void> initResult = initClusterJoin(param, esClientHttpAddressesStr);
        if (initResult.failed()) {
            return Result.buildFail(initResult.getMessage());
        }

        try {

            // 1.保存物理集群信息(集群、角色、节点)
            Result<ClusterPhyVO> saveClusterResult = saveClusterPhy(param, operator);

            if (saveClusterResult.failed()) {
                throw new AdminOperateException(saveClusterResult.getMessage());
            } else {
    
                postProcessingForClusterJoin(param);
                SpringTool.publish(new ClusterPhyEvent(param.getCluster(), operator));
                 operateRecordService.saveOperateRecordWithManualTrigger(saveClusterResult.getData().getCluster(), operator,
                        AuthConstant.SUPER_PROJECT_ID, saveClusterResult.getData().getId(),
                        OperateTypeEnum.PHYSICAL_CLUSTER_JOIN);
            }

            return saveClusterResult;
        } catch (AdminOperateException | ElasticsearchTimeoutException e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=clusterJoin||clusterPhy={}||es operation errMsg={}",
                param.getCluster(), e);
            // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("接入失败, 请重新尝试接入集群,多次重试不成功,请联系管理员");
        } catch (NullPointerException e) {
            LOGGER.error(
                "class=ClusterPhyManagerImpl||method=clusterJoin||clusterPhy={}||join cluster operation null point exception errMsg={}",
                param.getCluster(), e);
            // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("接入集群发生致命错误,请联系管理员");
        } catch (Exception e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=clusterJoin||clusterPhy={}||errMsg={}",
                param.getCluster(), e);
            // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("操作失败,请联系管理员");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteClusterJoin(Integer clusterId, String operator, Integer projectId) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(clusterId);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildParamIllegal("物理集群不存在");
        }

        try {
            doDeleteClusterJoin(clusterPhy, operator, projectId);
        } catch (AdminOperateException e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=deleteClusterJoin||errMsg={}||e={}||clusterId={}",
                e.getMessage(), e, clusterId);
            // 这里显示回滚处理特殊异常场景
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail(e.getMessage());
        }
         operateRecordService.saveOperateRecordWithManualTrigger(clusterPhy.getCluster(), operator, projectId, clusterId,
                OperateTypeEnum.PHYSICAL_CLUSTER_OFFLINE);
    
        return Result.buildSucc();
    }

    @Override
    public Result<List<PluginVO>> listPlugins(String cluster) {
        return Result.buildSucc(ConvertUtil.list2List(clusterPhyService.listClusterPlugins(cluster), PluginVO.class));
    }

    @Override
    public Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> getPhyClusterDynamicConfigs(String cluster) {
        if (!isClusterExists(cluster)) {
            return Result.buildFail(String.format("集群[%s]不存在", cluster));
        }

        ESClusterGetSettingsAllResponse clusterSetting = null;
        try {
            clusterSetting = esClusterService.syncGetClusterSetting(cluster);
        } catch (Exception e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=getPhyClusterDynamicConfigs||clusterName={}", cluster, e);
            return Result.buildFail(String.format("获取集群setting异常，请确认是否集群[%s]是否正常", cluster));
        }
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
    public Result<Boolean> updatePhyClusterDynamicConfig(ClusterSettingDTO param, String operator, Integer projectId) {
        final Result<Void> resultCheck = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (resultCheck.failed()) {
            return Result.buildFail(resultCheck.getMessage());
        }
        final Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> beforeChangeConfigs = getPhyClusterDynamicConfigs(
            param.getClusterName());
        if (beforeChangeConfigs.failed()) {
            return Result.buildFail(beforeChangeConfigs.getMessage());
        }
        String changeKey = param.getKey();
        if (beforeChangeConfigs.getData().values() == null) {
            return Result.buildFail("获取要更新的集群配置项的信息失败");
        }
        Object beforeValue = beforeChangeConfigs.getData().values().stream()
            .filter(
                clusterDynamicConfigsTypeEnumMapValues -> clusterDynamicConfigsTypeEnumMapValues.get(changeKey) != null)
            .map(clusterDynamicConfigsTypeEnumMapValues -> clusterDynamicConfigsTypeEnumMapValues.get(changeKey))
            .findFirst().orElse("");
        Object changeValue = param.getValue();
        final ClusterPhy clusterByName = clusterPhyService.getClusterByName(param.getClusterName());
        final Result<Boolean> result = clusterPhyService.updatePhyClusterDynamicConfig(param);
        if (result.success()) {
             operateRecordService.saveOperateRecordWithManualTrigger(String.format("%s:%s->%s", changeKey, beforeValue, changeValue), operator,
                    AuthConstant.SUPER_PROJECT_ID, clusterByName.getId(),
                    OperateTypeEnum.PHYSICAL_CLUSTER_DYNAMIC_CONF_CHANGE);
        }
        return result;
    }

    @Override
    public Result<Set<String>> getRoutingAllocationAwarenessAttributes(String cluster) {
        return Result.buildSucc(clusterPhyService.getRoutingAllocationAwarenessAttributes(cluster));
    }

    @Override
    public List<String> listClusterPhyNameByProjectId(Integer projectId) {
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            //超级projectId返回所有的集群
            List<ClusterPhy> phyList = clusterPhyService.listAllClusters();
            return phyList.stream().map(ClusterPhy::getCluster).distinct().sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        }
        // 非超级管理员，获取拥有的逻辑集群对应的物理集群列表
        List<ClusterLogic> clusterLogicList = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
        //项目下的有管理权限逻辑集群会关联多个物理集群
        List<ClusterRegion> regions = clusterRegionService.getClusterRegionsByLogicIds(
            clusterLogicList.stream().map(ClusterLogic::getId).collect(Collectors.toList()));
        return regions.stream().map(ClusterRegion::getPhyClusterName).distinct().sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());
    }
    
    /**
     * @param clusterPhy
     * @return
     */
    @Override
    public Result<ClusterPhy> getClusterByName(String clusterPhy) {
        return Result.buildSucc(clusterPhyService.getClusterByName(clusterPhy));
    }
    
    /**
     * @param cluster
     * @param remoteCluster
     * @return
     */
    @Override
    public boolean ensureDCDRRemoteCluster(String cluster, String remoteCluster) throws ESOperateException {
        return clusterPhyService.ensureDCDRRemoteCluster(cluster,remoteCluster);
    }
    
    @Override
    public Result<List<String>> listClusterPhyNameByResourceType(Integer clusterResourceType, Integer projectId) {

        if (null != clusterResourceType && !ClusterResourceTypeEnum.isExist(clusterResourceType)) {
            return Result.buildParamIllegal("集群资源类型非法");
        }
        List<String> clusters;
        List<ClusterPhy> clusterPhyList;
        if (null != clusterResourceType) {
            ClusterPhyDTO clusterPhyDTO = new ClusterPhyDTO();
            clusterPhyDTO.setResourceType(clusterResourceType);
            clusterPhyList = clusterPhyService.listClustersByCondt(clusterPhyDTO);

        } else {
            clusterPhyList = clusterPhyService.listAllClusters();
        }
        Set<String> clusterNameSet = ConvertUtil.list2Set(clusterPhyList, ClusterPhy::getCluster);
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            clusters = clusterPhyList.stream().map(ClusterPhy::getCluster).distinct().sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        } else {
            List<ClusterLogic> clusterLogicList = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
            //项目下的有管理权限逻辑集群会关联多个物理集群
            List<ClusterRegion> regions = clusterRegionService.getClusterRegionsByLogicIds(
                clusterLogicList.stream().map(ClusterLogic::getId).collect(Collectors.toList()));
            clusters = regions.stream().map(ClusterRegion::getPhyClusterName).distinct()
                .filter(clusterNameSet::contains).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        }
        return Result.buildSucc(clusters);
    }
    
    @Override
    public Result<List<String>> getTemplateSameVersionClusterNamesByTemplateId(Integer projectId, Integer templateId) {
        List<String> clusterPhyNameList = listClusterPhyNameByProjectId(projectId);
        // No permission, cut branches and return
        if (CollectionUtils.isEmpty(clusterPhyNameList)) {
            return Result.buildSucc();
        }

        IndexTemplateWithPhyTemplates logicTemplateWithPhysicals = indexTemplateService
            .getLogicTemplateWithPhysicalsById(templateId);
        if (null == logicTemplateWithPhysicals) {
            return Result.buildFail(String.format("templateId[%s] is not exist", templateId));
        }

        IndexTemplatePhy masterPhyTemplate = logicTemplateWithPhysicals.getMasterPhyTemplate();
        if (null == masterPhyTemplate) {
            return Result.buildFail(String.format("the physicals of templateId[%s] is empty", templateId));
        }

        String cluster = masterPhyTemplate.getCluster();
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(cluster);
        if (null == clusterPhy) {
            return Result.buildFail(String.format("the cluster[%s] from templateId[%s] is empty", cluster, templateId));
        }
        
        String esVersion = clusterPhy.getEsVersion();

        List<ClusterPhy> clusterPhies = clusterPhyService.listAllClusters();

        Predicate<ClusterPhy> matchingSameVersionESVersionPredicate =
                cp -> ESVersionUtil.compareBigVersionConsistency(esVersion,cp.getEsVersion());
        List<String> sameVersionClusterNameList = clusterPhies.stream().filter(Objects::nonNull)
                .filter(r-> !ClusterHealthEnum.UNKNOWN.getCode().equals(r.getHealth()))
            .filter(r -> clusterPhyNameList.contains(r.getCluster()))
            .filter(rCluster -> !StringUtils.equals(logicTemplateWithPhysicals.getMasterPhyTemplate().getCluster(),
                rCluster.getCluster()))
            .filter(matchingSameVersionESVersionPredicate).map(ClusterPhy::getCluster)
                .distinct()
            .collect(Collectors.toList());

        return Result.buildSucc(sameVersionClusterNameList);
    }
    
    /**
     * @param projectId
     * @param templateId
     * @return
     */
    @Override
    public Result<List<String>> getTemplateSameVersionClusterNamesByTemplateIdExistDCDR(Integer projectId,
                                                                                        Integer templateId) {
        final Result<List<String>> clusterPhyListRes = getTemplateSameVersionClusterNamesByTemplateId(projectId,
                templateId);
        if (clusterPhyListRes.failed()) {
            return clusterPhyListRes;
        }
        final List<String> existDCDRPluginClusterPhyList = clusterPhyListRes.getData().stream()
                .filter(clusterPhy -> Boolean.TRUE.equals(
                        getDCDRAndPipelineAndColdRegionTupleByClusterPhyWithCache(clusterPhy).v1))
                .collect(Collectors.toList());
        return Result.buildSucc(existDCDRPluginClusterPhyList);
    }
    
    @Override
    public List<String> listClusterPhyNodeName(String clusterPhyName) {
        if (null == clusterPhyName) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=getAppClusterPhyNodeNames||errMsg=集群名称为空");
            return Lists.newArrayList();
        }
        return esClusterNodeService.syncGetNodeNames(clusterPhyName);
    }

    @Override
    public List<String> listNodeNameByProjectId(Integer projectId) {
        List<String> appAuthNodeNames = Lists.newCopyOnWriteArrayList();

        List<String> appClusterPhyNames = listClusterPhyNameByProjectId(projectId);
        appClusterPhyNames
            .forEach(clusterPhyName -> appAuthNodeNames.addAll(esClusterNodeService.syncGetNodeNames(clusterPhyName)));

        return appAuthNodeNames;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteCluster(Integer clusterPhyId, String operator, Integer projectId) {
        if (!roleTool.isAdmin(operator) || !AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return Result.buildFail("当前登录人或项目没有权限进行该操作！");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterById(clusterPhyId);
        if (null == clusterPhy) {
            return Result.buildFail(String.format("物理集群Id[%s]不存在", clusterPhyId));
        }

        Set<Long> clusterLogicIdList = clusterRegionService.getLogicClusterIdByPhyClusterId(clusterPhyId);
        if (CollectionUtils.isNotEmpty(clusterLogicIdList)) {
            List<ClusterLogic> clusterLogicList = clusterLogicService
                .getClusterLogicListByIds(Lists.newArrayList(clusterLogicIdList));
            return Result.buildFail(String.format("物理集群[%s]和逻辑集群[%s]关联", clusterPhy.getCluster(),
                ConvertUtil.list2String(Lists.newArrayList(clusterLogicList), ",", ClusterLogic::getName)));
        }

        List<String> templatePhyNameList = indexTemplatePhyService.getNormalTemplateByCluster(clusterPhy.getCluster())
            .stream().map(IndexTemplatePhy::getName).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(templatePhyNameList)) {
            return Result.buildFail(String.format("物理集群[%s]中已经存在模板[%s]", clusterPhy.getCluster(),
                ListUtils.strList2String(templatePhyNameList)));
        }

        Result<Boolean> deleteClusterResult = deleteClusterInner(clusterPhyId, projectId);
        if (deleteClusterResult.failed()) {
            return Result.buildFrom(deleteClusterResult);
        }


        SpringTool.publish(new ClusterPhyEvent(clusterPhy.getCluster(), operator));
         operateRecordService.saveOperateRecordWithManualTrigger(clusterPhy.getCluster(), operator,
                AuthConstant.SUPER_PROJECT_ID, clusterPhyId, OperateTypeEnum.PHYSICAL_CLUSTER_OFFLINE);
        return Result.buildSucc(true);
    }

    @Override
    public Result<Boolean> addCluster(ClusterPhyDTO param, String operator, Integer projectId) {
        Result<Boolean> result = clusterPhyService.createCluster(param);

        if (result.success()) {
            SpringTool.publish(new ClusterPhyEvent(param.getCluster(), operator));
             operateRecordService.saveOperateRecordWithManualTrigger(param.getCluster(), operator, projectId, param.getId(),
                    OperateTypeEnum.PHYSICAL_CLUSTER_NEW);
        }
        return result;
    }

    @Override
    public Result<Boolean> editCluster(ClusterPhyDTO param, String operator) {
        final ClusterPhy oldClusterPhy = clusterPhyService.getClusterById(param.getId());
        final Result<Boolean> result = clusterPhyService.editCluster(param, operator);
        if (result.success()) {

            if (!StringUtils.equals(oldClusterPhy.getDesc(), param.getDesc())) {
                 operateRecordService.saveOperateRecordWithManualTrigger(
                        String.format("%s, 修改集群描述:%s-->%s", oldClusterPhy.getCluster(), oldClusterPhy.getDesc(),
                                param.getDesc()), operator, AuthConstant.SUPER_PROJECT_ID, param.getId(),
                        OperateTypeEnum.PHYSICAL_CLUSTER_INFO_MODIFY);
            }

        }
        return result;
    }

    @Override
    public PaginationResult<ClusterPhyVO> pageGetClusterPhys(ClusterPhyConditionDTO condition,
                                                             Integer projectId) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(CLUSTER_PHY.getPageSearchType());
        if (baseHandle instanceof ClusterPhyPageSearchHandle) {
            ClusterPhyPageSearchHandle pageSearchHandle = (ClusterPhyPageSearchHandle) baseHandle;
            return pageSearchHandle.doPage(condition, projectId);
        }

        LOGGER.warn(
            "class=ClusterPhyManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the ClusterPhyPageSearchHandle");

        return PaginationResult.buildFail("分页获取物理集群信息失败");
    }

    @Override
    public Result<List<String>> listClusterPhyNameBySuperApp(Integer projectId) {
        List<String> names = Lists.newArrayList();
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            names.addAll(clusterPhyService.listClusterNames());
        } else {
            return Result.buildFail("非超级项目，不能查看物理集群列表");
        }
        if (names.size()==0){
            return Result.buildFail("超级项目无集群信息，请前往集群管理-->物理集群，进行新建集群或者接入集群。");
        }
        return Result.buildSucc(names);
    }

    /**
     * 构建用户控制台统计信息: 集群使用率
     */
    @Override
    public void buildPhyClusterStatics(ClusterPhyVO cluster) {
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
    public void buildClusterRole(ClusterPhyVO cluster) {
        try {
            List<ClusterRoleInfo> clusterRoleInfos = clusterRoleService.getAllRoleClusterByClusterId(cluster.getId());

            buildClusterRole(cluster, clusterRoleInfos);
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=buildClusterRole||logicClusterId={}", cluster.getId(), e);
        }
    }

    @Override
    public void buildClusterRole(ClusterPhyVO cluster, List<ClusterRoleInfo> clusterRoleInfos) {
        try {
            List<ESClusterRoleVO> roleClusters = ConvertUtil.list2List(clusterRoleInfos, ESClusterRoleVO.class);

            List<Long> roleClusterIds = roleClusters.stream().map(ESClusterRoleVO::getId).collect(Collectors.toList());
            Map<Long, List<ClusterRoleHost>> roleIdsMap = clusterRoleHostService.getByRoleClusterIds(roleClusterIds);

            for (ESClusterRoleVO esClusterRoleVO : roleClusters) {
                List<ClusterRoleHost> clusterRoleHosts = roleIdsMap.get(esClusterRoleVO.getId());
                List<ESClusterRoleHostVO> esClusterRoleHosts = ConvertUtil.list2List(clusterRoleHosts,
                    ESClusterRoleHostVO.class);
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
            LOGGER.warn(
                "class=ClusterPhyManagerImpl||method=updateClusterHealth||clusterPhyName={}||msg=clusterPhy is empty",
                clusterPhyName);
            return false;
        }

        ClusterPhyDTO esClusterDTO = new ClusterPhyDTO();
        ClusterHealthEnum clusterHealthEnum = null;
        try {
            clusterHealthEnum = esClusterService.syncGetClusterHealthEnum(clusterPhyName);
        } catch (ESOperateException e) {
            LOGGER.error(
                    "class=ClusterPhyManagerImpl||method=updateClusterHealth||clusterPhyName={}||msg=fail to get clusterHealthEnum",
                    clusterPhyName);
            return false;
        }

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
            LOGGER.warn(
                "class=ClusterPhyManagerImpl||method=updateClusterInfo||clusterPhyName={}||msg=clusterPhy is empty",
                cluster);
            return false;
        }

        ESClusterStatsResponse clusterStats = esClusterService.syncGetClusterStats(cluster);
        long totalFsBytes = clusterStats.getTotalFs().getBytes();
        long usageFsBytes = clusterStats.getTotalFs().getBytes() - clusterStats.getFreeFs().getBytes();

        double diskFreePercent = 0d;
        double clusterTotalFs = clusterStats.getTotalFs().getGbFrac();
        if(clusterTotalFs > 0){
            diskFreePercent = clusterStats.getFreeFs().getGbFrac() / clusterTotalFs;
            diskFreePercent = CommonUtils.formatDouble(1 - diskFreePercent, 5);
        }

        ClusterPhyDTO esClusterDTO = new ClusterPhyDTO();
        esClusterDTO.setId(clusterPhy.getId());
        esClusterDTO.setDiskTotal(totalFsBytes);
        esClusterDTO.setDiskUsage(usageFsBytes);
        esClusterDTO.setDiskUsagePercent(diskFreePercent);
        Result<Boolean> editClusterResult = clusterPhyService.editCluster(esClusterDTO, operator);
        if (editClusterResult.failed()) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=updateClusterInfo||clusterPhyName={}||errMsg={}", cluster,
                editClusterResult.getMessage());
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

        if (ClusterHealthEnum.GREEN.getCode().equals(clusterPhy.getHealth())
            || ClusterHealthEnum.YELLOW.getCode().equals(clusterPhy.getHealth())) {
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
    public Result<Boolean> deleteClusterExit(String clusterPhyName, Integer projectId, String operator) {
        if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return Result.buildFail("无权限删除集群");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (null == clusterPhy) {
            return Result.buildSucc(true);
        }

        return deleteClusterInner(clusterPhy.getId(), projectId);
    }

    @Override
    public Result<List<String>> getPhyClusterNameWithSameEsVersion(Integer clusterLogicType,
                                                                   /*用户在新建逻辑集群阶段已选择的物理集群名称*/String hasSelectedClusterNameWhenBind) {
        //获取可以绑定的物理集群名称列表
        Result<List<String>> canBeAssociatedClustersPhyNamesResult = validLogicAndReturnPhyNamesWhenBindPhy(null,
            clusterLogicType);
        if (canBeAssociatedClustersPhyNamesResult.failed()) {
            return Result.buildFrom(canBeAssociatedClustersPhyNamesResult);
        }

        //没有指定物理集群名称，则返回全量的匹配数据，不做版本的筛选
        if (AriusObjUtils.isNull(hasSelectedClusterNameWhenBind)) {
            return canBeAssociatedClustersPhyNamesResult;
        }

        //根据已绑定的物理集群的版本进行筛选
        return Result.buildSucc(getPhyClusterNameWithSameEsVersion(hasSelectedClusterNameWhenBind,
            canBeAssociatedClustersPhyNamesResult.getData()));
    }

    @Override
    public Result<List<String>> getPhyClusterNameWithSameEsVersionAfterBuildLogic(Long clusterLogicId) {
        //获取可以绑定的物理集群名称列表
        Result<List<String>> canBeAssociatedClustersPhyNamesResult = validLogicAndReturnPhyNamesWhenBindPhy(
            clusterLogicId, null);
        if (canBeAssociatedClustersPhyNamesResult.failed()) {
            return Result.buildFrom(canBeAssociatedClustersPhyNamesResult);
        }

        //获取逻辑集群已绑定的物理集群信息
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogicId);
        if (null == clusterRegion) {
            return canBeAssociatedClustersPhyNamesResult;
        }

        //根据已绑定的物理集群的版本进行筛选
        return Result.buildSucc(getPhyClusterNameWithSameEsVersion(clusterRegion.getPhyClusterName(),
            canBeAssociatedClustersPhyNamesResult.getData()));
    }

    @Override
    public Result<ClusterPhyVO> updateClusterGateway(ClusterPhyDTO param, String operator) {
        ClusterPhyDTO clusterPhyDTO = new ClusterPhyDTO();
        clusterPhyDTO.setId(param.getId());
        clusterPhyDTO.setGatewayUrl(param.getGatewayUrl());
        ClusterPhy oldCluster = clusterPhyService.getClusterById(param.getId());
        Result<Boolean> result = clusterPhyService.editCluster(clusterPhyDTO, operator);
        if (result.failed()) {
            return Result.buildFail("编辑gateway失败！");
        }
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(param.getId());
         operateRecordService.saveOperateRecordWithManualTrigger(String.format("%s, 绑定 gateway 集群 gateway_cluster:%s", oldCluster.getCluster(),
                        param.getGatewayUrl()), operator, AuthConstant.SUPER_PROJECT_ID, param.getId(),
                OperateTypeEnum.PHYSICAL_CLUSTER_GATEWAY_CHANGE);
        return Result.buildSucc(ConvertUtil.obj2Obj(clusterPhy, ClusterPhyVO.class));
    }
    
   
    
    @Override
    public List<ClusterRoleInfo> listClusterRolesByClusterId(Integer clusterId) {
        return clusterRoleService.getAllRoleClusterByClusterId(clusterId);
    }

    @Override
    public  List<ClusterRoleHost> listClusterRoleHostByCluster(String cluster) {
        return clusterRoleHostService.getNodesByCluster(cluster);
    }
    
    /**
     * 它返回满足条件的总数。
     *
     * @param condition 查询的条件。
     * @return 长
     */
    @Override
    public Long fuzzyClusterPhyHitByCondition(ClusterPhyConditionDTO condition) {
        return clusterPhyService.fuzzyClusterPhyHitByCondition(condition);
    }
    
    /**
     * 按条件获取集群物理信息
     *
     * @param condition 查询的条件。
     * @return 列表<ClusterPhy>
     */
    @Override
    public List<ClusterPhy> pagingGetClusterPhyByCondition(ClusterPhyConditionDTO condition) {
        return  clusterPhyService.pagingGetClusterPhyByCondition(condition);
    }

    /**
     * 批量更新物理集群的动态配置项
     * @param param        要更新的配置项
     * @param operator
     * @param projectId
     * @return
     */
    @Override
    public Result<Boolean> batchUpdateClusterDynamicConfig(MultiClusterSettingDTO param, String operator,
                                                           Integer projectId) throws ESOperateException {
        final Result<Void> projectCheck = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (projectCheck.failed()) {
            return Result.buildFail(projectCheck.getMessage());
        }
        Result<Boolean> result = checkClusterExistAndConfigType(param);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }

        boolean updateFail = false;
        StringBuilder updateFailClusters = new StringBuilder();
        // 对每个集群进行更新配置操作
        for (String cluster : param.getClusterNameList()) {
            Map<String, Object> persistentConfig = Maps.newHashMap();
            String changeKey = param.getKey();
            Object changeValue = param.getValue();
            persistentConfig.put(changeKey, changeValue);
            boolean succ = esClusterService.syncPutPersistentConfig(cluster, persistentConfig);
            if(!succ){
                updateFail = true;
                updateFailClusters.append(cluster).append(",");
            }else {
                // 记录操作
                final Result<Map<ClusterDynamicConfigsTypeEnum, Map<String, Object>>> beforeChangeConfigs = getPhyClusterDynamicConfigs(cluster);
                if (beforeChangeConfigs.failed() || beforeChangeConfigs.getData().values() == null){
                    updateFail = true;
                    updateFailClusters.append(cluster).append(",");
                } else {

                    Object beforeValue = beforeChangeConfigs.getData().values().stream()
                            .filter(clusterDynamicConfigsTypeEnumMapValues -> clusterDynamicConfigsTypeEnumMapValues.containsKey(changeKey))
                            .map(clusterDynamicConfigsTypeEnumMapValues -> clusterDynamicConfigsTypeEnumMapValues.get(changeKey))
                            .filter(Objects::nonNull)
                            .findFirst().orElse("");

                    final ClusterPhy clusterByName = clusterPhyService.getClusterByName(cluster);
                    operateRecordService.saveOperateRecordWithManualTrigger(String.format("%s:%s->%s", changeKey, beforeValue, changeValue),
                            operator, AuthConstant.SUPER_PROJECT_ID, clusterByName.getId(),
                            OperateTypeEnum.PHYSICAL_CLUSTER_DYNAMIC_CONF_CHANGE);
                }
            }
        }
        if(updateFail){
            return Result.buildFail(updateFailClusters.deleteCharAt(updateFailClusters.length()-1) + " 集群更新动态配置失败");
        }

        return Result.buildSucc();
    }

    @Override
    public Result<Boolean> verifyNameUniqueness(String clusterName) {
        	return Result.build(clusterPhyService.isClusterExists(clusterName) ||
					componentService.queryComponentByName(clusterName).isSuccess());
    }

    @Override
    public Result<Void> updateVersionWithECM(Integer componentId, String version) {
        if (!clusterPhyService.hasClusterRelationComponentId(componentId)){
            return Result.buildFail("版本更新失败,无法匹配到具有组件ID【%s】的集群信息");
        }
        // 无需写入到操作记录，工单操作中生成操作记录即可
        return Result.build(clusterPhyService.updateVersion(componentId,version));
    }

    @Override
    public Result<String> getNameByComponentId(Integer componentId) {
        ClusterPhyPO clusterPhy = clusterPhyService.getOneByComponentId(componentId);
        if (Objects.isNull(clusterPhy)) {
            return Result.buildFail("无法匹配到具有组件 ID【%s】的集群信息");
        }
        return Result.buildSucc(clusterPhy.getCluster());
    }

    @Override
    public Result<ClusterPhyVO> getOneByComponentId(Integer componentId) {
        ClusterPhyPO clusterPhy = clusterPhyService.getOneByComponentId(componentId);
        if (Objects.isNull(clusterPhy)) {
            return Result.buildFail("无法匹配到具有组件 ID【%s】的集群信息");
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(clusterPhy,ClusterPhyVO.class));
    }

    @Override
    public Result<Void> checkShrinkNodesWhetherToReadWrite(List<ESClusterRoleHostDTO> nodes,
        String clusterName) {
        final Optional<ClusterPhy> clusterPhyOptional = clusterPhyService.listClustersByNames(
            Collections.singletonList(clusterName)).stream().findFirst();
        if (!clusterPhyOptional.isPresent()) {
            return Result.buildFail("缩容的节点未匹配到物理集群信息");
        }
        final Set<String> shrinks = nodes.stream().map(i -> String.format("%s:%s", i.getIp(), i
            .getPort())).collect(Collectors.toSet());
        final String httpAddress = clusterPhyOptional.get().getHttpAddress();
        final List<String> httpAddressList = Arrays.stream(StringUtils.split(httpAddress, ","))
            .collect(Collectors.toList());
        final String httpWriteAddress = clusterPhyOptional.get().getHttpWriteAddress();
        final List<String> httpWriteAddresList = Arrays.stream(StringUtils.split(httpWriteAddress,
                ","))
            .collect(Collectors.toList());
        //如果全部包含则不允许缩容，否则将影响读写功能
        if (shrinks.containsAll(httpAddressList) || shrinks.containsAll(httpWriteAddresList)) {
            return Result.buildFail(
                "默认连接的配置组不可以全部缩容完，否则集群信息无法监控，将影响集群读写功能，请通过页面编辑功能将读写地址的信息进行变更");
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Void> shrinkNodesUpdateToReadWrite(List<ESClusterRoleHostDTO> nodes,
        String clusterName) {
        final Optional<ClusterPhy> clusterPhyOptional = clusterPhyService.listClustersByNames(
            Collections.singletonList(clusterName)).stream().findFirst();
        if (!clusterPhyOptional.isPresent()) {
            return Result.buildFail("缩容的节点未匹配到物理集群信息");
        }
        final Set<String> shrinks = nodes.stream().map(i -> String.format("%s:%s", i.getIp(), i
            .getPort())).collect(Collectors.toSet());
        final String httpAddress = Arrays.stream(
                StringUtils.split(clusterPhyOptional.get().getHttpAddress(), ","))
            .filter(i -> !shrinks.contains(i))
            .collect(Collectors.joining(","));
        final String httpWriteAddress = Arrays.stream(
                StringUtils.split(clusterPhyOptional.get().getHttpWriteAddress(),
                    ","))
            .filter(i -> !shrinks.contains(i))
            .collect(Collectors.joining(","));
        final ClusterPhyDTO clusterPhyDTO = ConvertUtil.obj2Obj(clusterPhyOptional.get(),
            ClusterPhyDTO.class);
        clusterPhyDTO.setHttpAddress(httpAddress);
        clusterPhyDTO.setHttpWriteAddress(httpWriteAddress);
        final Result<Boolean> booleanResult = clusterPhyService.editCluster(clusterPhyDTO,
            AriusUser.SYSTEM.getDesc());
        return Result.buildFrom(booleanResult);
    }

    @Override
    public Result<ClusterPhyVO> getOneById(Integer clusterId) {
        return Result.buildSucc(ConvertUtil.obj2Obj(clusterPhyService.getClusterById(clusterId),
            ClusterPhyVO.class));
    }

    @Override
    public Result<Void> checkShrinkNodesContainsBindRegion(List<ESClusterRoleHostDTO> nodes,
        String clusterName) {
        //1.1 获取所有节点信息
        List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.listNodesByClusters(
            Collections.singletonList(clusterName));
        if (CollectionUtils.isEmpty(clusterRoleHosts)) {
            return Result.buildFail("节点信息不存在，无法进行缩容");
        }
        //2. 先排除元数据 NULL 或者 -1 的 region
        final List<ClusterRoleHost> existsRegionClusterRoleHostList =
            clusterRoleHosts.stream().filter(i -> !Objects.equals(-1, i.getRegionId()))
                .collect(Collectors.toList());
        // 如果没有，则证明没有绑定 region，可以正常缩容
        if (existsRegionClusterRoleHostList.isEmpty()) {
            return Result.buildSucc();
        }
        //3 获得出此次缩容的ip
        final List<String> ipLists = nodes.stream().map(ESClusterRoleHostDTO::getIp).distinct()
            .collect(Collectors.toList());
        //3.1 从过滤出的region列表中获取ip
        final List<String> regionIpList = existsRegionClusterRoleHostList.stream()
            .map(ClusterRoleHost::getIp).distinct().collect(
                Collectors.toList());
         //3过滤出region列表
        final List<String> filterRegionIp = ipLists.stream().filter(regionIpList::contains)
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterRegionIp)){
            return Result.buildSucc();
        }
        final Map<String, List<ESClusterRoleHostDTO>> ip2DTOListMap = ConvertUtil.list2MapOfList(
            nodes, ESClusterRoleHostDTO::getIp, i -> i);
        final Map<String, List<ClusterRoleHost>> ip2HostListMap = ConvertUtil.list2MapOfList(
            existsRegionClusterRoleHostList, ClusterRoleHost::getIp, i -> i);
        final Map<Integer, List<ClusterRoleHost>> regionId2HostsMap = ConvertUtil.list2MapOfList(
            existsRegionClusterRoleHostList, ClusterRoleHost::getRegionId, i -> i);
        //4. 只需要确定 ip：port 维度在缩容完成后，还会有节点支持 region 即可
        for (Entry<String, List<ESClusterRoleHostDTO>> ip2DTOEntry : ip2DTOListMap.entrySet()) {
            final String ip = ip2DTOEntry.getKey();
            final List<String> ipPorts = ip2DTOEntry.getValue().stream()
                .map(i -> String.format("%s:%s", i.getIp(), i.getPort()))
                .collect(Collectors.toList());
            final List<ClusterRoleHost> hosts = ip2HostListMap.get(ip);
            if (CollectionUtils.isEmpty(hosts)) {
                continue;
            }
            final long count = hosts.stream()
                .filter(i -> regionId2HostsMap.containsKey(i.getRegionId()))
                .map(i -> regionId2HostsMap.get(i.getRegionId()))
                .flatMap(Collection::stream)
                .map(i -> String.format("%s:%s", i.getIp(), i.getPort()))
                .distinct()
                // 进行剔除需要下线的 region，进行取反
                .filter(i -> !ipPorts.contains(i))
                .count();
            //5. 取反完成后，如果节点数量是 0，那么说明 region 绑定的节点全量落在了缩容列表中，此时禁止缩容
            if (count == 0) {
                return Result.buildFail("region 至少需要一个节点进行绑定，当前节点不支持缩容");
            }
        }
        return Result.buildSucc();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createWithECM(ClusterCreateDTO createDTO, String operator) {
        List<ESClusterRoleHostDTO> roleClusterHosts = createDTO.getRoleClusterHosts();
        // 这里其实是需要一个内置 trim 用来保证传输进行的 roleClusterHosts 是正确的
        roleClusterHosts.forEach(roleClusterHostsTrimHostnameAndPort);

        // 保存全量节点信息到 DB
        try {
            // 保存集群信息
            ClusterPhyDTO clusterDTO = buildPhyClusters(createDTO, operator);
            Result<Boolean> addClusterRet = clusterPhyService.createCluster(clusterDTO);
            if (addClusterRet.failed()) {
                // 这里必须显示事务回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return Result.buildFrom(addClusterRet);
            }
        } catch (Exception e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=createWithECM||clusterPhy={}||errMsg={}",
                         createDTO.getCluster(), e);
            // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("操作失败, 请联系管理员");
        }
        // 触发采集
        try {
            clusterRoleHostService.collectClusterNodeSettings(createDTO.getCluster(),
                createDTO.getComponentId());
        } catch (AdminTaskException ignored) {
        }
        return Result.buildSucc();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> shrinkNodesWithEcm(List<ESClusterRoleHostDTO> nodes, String clusterName) {
        //1 获取所有节点信息
        List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.listNodesByClusters(
            Collections.singletonList(clusterName));
        if (CollectionUtils.isEmpty(clusterRoleHosts)){
            return Result.buildSucc();
        }
        final Map<String, List<Long>> ipPort2IdsMaps = ConvertUtil.list2MapOfList(clusterRoleHosts,
            i -> String.format("%s:%s", i.getIp(),
                i.getPort()), ClusterRoleHost::getId);
        final List<Integer> removeIds = nodes.stream()
            .map(i -> String.format("%s:%s", i.getIp(), i.getPort())).distinct()
            .filter(ipPort2IdsMaps::containsKey)
            .map(ipPort2IdsMaps::get)
            .flatMap(Collection::stream)
            .map(Long::intValue)
            .collect(Collectors.toList());
        final Map<Long, Integer> id2RegionIdMap = ConvertUtil.list2Map(clusterRoleHosts,
            ClusterRoleHost::getId,
            ClusterRoleHost::getRegionId);

        // 获取需要触发缩容的 region
        final List<Long> regionIdList = removeIds.stream().map(Integer::longValue)
            .filter(id2RegionIdMap::containsKey)
            .map(id2RegionIdMap::get)
            .map(Integer::longValue)
            .distinct().collect(Collectors.toList());
        final boolean deleteByIds = clusterRoleHostService.deleteByIds(removeIds);
        if (deleteByIds) {
            // 触发采集
            try {
                clusterRoleHostService.collectClusterNodeSettings(clusterName);
                // 触发缩容事件
                SpringTool.publish(new RegionEditByHostEvent(this, regionIdList));
                return Result.buildSucc();
            } catch (AdminTaskException e) {
                return Result.buildFail(e.getMessage());
            }
        }
        return Result.buildFail("缩容失败");
    }

    @Override
    public Result<Void> expandNodesWithECM(List<ESClusterRoleHostDTO> nodes, String clusterName) {
        //扩容后触发采集任务进行节点采集刷新
        try {
            final ClusterPhy cluster = clusterPhyService.getClusterByName(clusterName);
            clusterRoleHostService.collectClusterNodeSettings(clusterName,
                cluster.getComponentId());
            return Result.buildSucc();
        } catch (AdminTaskException e) {
            return Result.buildFail(e.getMessage());
        }
    }

    @Override
    public Result<Integer> getIdByComponentId(Integer dependComponentId) {
        ClusterPhyPO clusterPhyPO = clusterPhyService.getOneByComponentId(dependComponentId);
        if (Objects.isNull(clusterPhyPO)){
            return Result.buildFail(String.format("组件ID【%s】未匹配到对应的集群信息",dependComponentId));
        }
        return Result.buildSucc(clusterPhyPO.getId());
    }

    @Override
    public Result<Void> checkCompleteUnbindResources(ClusterPhy clusterPhy) {
        // 1. set region
        List<ClusterRegion> regions = clusterRegionService.listPhyClusterRegions(clusterPhy.getCluster());
        if (CollectionUtils.isEmpty(regions)) {
            return Result.buildSucc();
        }
        //必须先全部把逻辑集群下线才是对的
        if (regions.stream().allMatch(i-> StringUtils.equals("-1", i.getLogicClusterIds()))){
            return Result.buildFail("当前下线的集群存在绑定的逻辑集群，无法下线");
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Void> offlineWithECM(Integer id, String creator, Integer projectId) {
        // 再次确认当前信息未被中途绑定
        ClusterPhy cluster = clusterPhyService.getClusterById(id);
        if (Objects.isNull(cluster)) {
            return Result.buildSucc();
        }
        Result<Void> result = checkCompleteUnbindResources(cluster);
        if (result.failed()) {
            return result;
        }
        try {
            doDeleteClusterJoin(cluster, creator, projectId);
        } catch (AdminOperateException e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=offlineWithECM||errMsg={}||e={}||clusterId={}",
                         e.getMessage(), e, id);
            // 这里显示回滚处理特殊异常场景
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail(e.getMessage());
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Void> bindGatewayCluster(Integer clusterPhyId, List<Integer> bindGatewayClusterIds, String operator, Integer projectId) {
        ClusterPhy cluster = clusterPhyService.getClusterById(clusterPhyId);
        Set<Integer> gatewayIdSets=Sets.newHashSet();
        gatewayIdSets.addAll(bindGatewayClusterIds);
        cluster.setGatewayIds(StringUtils.join(gatewayIdSets,","));
        clusterPhyService.editCluster(ConvertUtil.obj2Obj(cluster,ClusterPhyDTO.class),operator);
        return Result.buildSucc();
    }



    /**************************************** private method ***************************************************/

    private Result<Boolean> checkClusterExistAndConfigType(MultiClusterSettingDTO param) {
        // check集群是否都存在
        for (String cluster : param.getClusterNameList()) {
            boolean clusterExist = clusterPhyService.isClusterExists(cluster);
            if(!clusterExist) {
                return Result.buildFail(cluster + "集群不存在");
            }
        }

        // check配置项是否合规
        ClusterDynamicConfigsEnum clusterSettingEnum = ClusterDynamicConfigsEnum.valueCodeOfName(param.getKey());
        if (clusterSettingEnum.equals(ClusterDynamicConfigsEnum.UNKNOWN)) {
            return Result.buildFail("传入的字段类型未知");
        }
        if (!clusterSettingEnum.getCheckFun().apply(String.valueOf(param.getValue())).booleanValue()) {
            return Result.buildFail("传入的字段参数格式有误");
        }

        return Result.buildSucc();
    }

    public Result<Boolean> deleteClusterInner(Integer clusterPhyId, Integer projectId) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(clusterPhyId);
        if (null == clusterPhy) {
            return Result.buildFail(String.format("物理集群Id[%s]不存在", clusterPhyId));
        }
        try {
            List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.getNodesByCluster(clusterPhy.getCluster());
            // 该物理集群有采集到host数据才执行删除操作
            if (!CollectionUtils.isEmpty(clusterRoleHosts)) {
                Result<Void> deleteHostResult = clusterRoleHostService.deleteByCluster(clusterPhy.getCluster(),
                    projectId);
                if (deleteHostResult.failed()) {
                    throw new AdminOperateException(String.format("删除集群[%s]节点信息失败", clusterPhy.getCluster()));
                }
            }

            Result<Void> deleteRoleResult = clusterRoleService.deleteRoleClusterByClusterId(clusterPhy.getId(),
                projectId);
            if (deleteRoleResult.failed()) {
                throw new AdminOperateException(String.format("删除集群[%s]角色信息失败", clusterPhy.getCluster()));
            }

            List<ClusterRegion> clusterRegionList = clusterRegionService.listPhyClusterRegions(clusterPhy.getCluster());
            if (!AriusObjUtils.isEmptyList(clusterRegionList)) {
                // 该物理集群有Region才删除
                Result<Void> deletePhyClusterRegionResult = clusterRegionService
                    .deleteByClusterPhy(clusterPhy.getCluster());
                if (deletePhyClusterRegionResult.failed()) {
                    throw new AdminOperateException(String.format("删除集群[%s]Region新失败", clusterPhy.getCluster()));
                }
            }

            Result<Boolean> deleteClusterResult = clusterPhyService.deleteClusterById(clusterPhyId, projectId);
            if (deleteClusterResult.failed()) {
                throw new AdminOperateException(String.format("删除集群[%s]信息失败", clusterPhy.getCluster()));
            }
            if (Objects.nonNull(clusterPhy.getComponentId()) && clusterPhy.getComponentId() > 0) {
                offlineCluster(clusterPhy);
            }
        } catch (AdminOperateException e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=deleteClusterInfo||clusterName={}||errMsg={}||e={}",
                clusterPhy.getCluster(), e.getMessage(), e);
            // 这里显示回滚处理特殊异常场景
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("删除物理集群失败");
        }
        return Result.buildSucc(true);
    }

    /**
     * 下线集群
     *
     * @param clusterPhy 集群
     * @throws AdminOperateException 管理操作Exception
     */
    public void offlineCluster(ClusterPhy clusterPhy)
        throws AdminOperateException {
        // 下线对应的插件
        Set<Integer> deleteComponentIds=Sets.newHashSet();
        final List<PluginInfoPO> pluginInfoPOS = pluginInfoService.listByClusterId(
            clusterPhy.getId(), PluginClusterTypeEnum.ES);
        if (CollectionUtils.isNotEmpty(pluginInfoPOS)) {
            final List<Long> pluginIds = pluginInfoPOS.stream().map(PluginInfoPO::getId)
                .collect(Collectors.toList());
            final boolean deleteByIds = pluginInfoService.deleteByIds(pluginIds);
            pluginInfoPOS.stream()
                .map(PluginInfoPO::getComponentId).distinct()
                .forEach(deleteComponentIds::add);
            if (!deleteByIds) {
                throw new AdminOperateException("集群关联插件下线失败");
            }
        }
        final com.didiglobal.logi.op.manager.domain.component.entity.Component component = componentService.queryComponentById(
            clusterPhy.getComponentId()).getData();
        if (Objects.nonNull(component)) {
            if (StringUtils.isNotBlank(component.getContainComponentIds())) {
                for (String componentId : StringUtils.split(component.getContainComponentIds(),
                    ",")) {
                    if (StringUtils.isNumeric(componentId)) {
                        // 添加
                      deleteComponentIds.add(Integer.parseInt(componentId));
                    }
                }
            }
            deleteComponentIds.add(clusterPhy.getComponentId());
        }
         com.didiglobal.logi.op.manager.infrastructure.common.Result<Boolean> offLineComponentRes =
             componentService.deleteComponents(
               Lists.newArrayList(deleteComponentIds));
            //下线最终集群
            if (Objects.equals(offLineComponentRes.getData(),Boolean.FALSE)) {
                throw new AdminOperateException(
                    String.format("删除集群 [%s] 信息失败", offLineComponentRes.getMessage()));
            }

    }

    /**
     * 更新物理模板setting single_type为true
     * @param cluster  集群
     * @param template 物理模板
     * @return
     */
    private boolean setTemplateSettingSingleType(String cluster, String template) {
        Map<String, String> setting = new HashMap<>(2);
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
        Result<List<String>> canBeAssociatedClustersPhyNames = Result.buildSucc(Lists.newArrayList());
        if (clusterLogicId != null) {
            ClusterLogic clusterLogicById =
                    clusterLogicService.listClusterLogicByIdThatProjectIdStrConvertProjectIdList(clusterLogicId).stream().findFirst().orElse(null);
            if (clusterLogicById == null) {
                return Result.buildFail("选定的逻辑集群不存在");
            }
            clusterLogicType = clusterLogicById.getType();
            canBeAssociatedClustersPhyNames = listCanBeAssociatedRegionOfClustersPhys(clusterLogicType, clusterLogicId);
        } else {
            canBeAssociatedClustersPhyNames = listCanBeAssociatedClustersPhys(clusterLogicType);
        }

        if (!ClusterResourceTypeEnum.isExist(clusterLogicType)) {
            return Result.buildParamIllegal("逻辑集群类型非法");
        }

        if (canBeAssociatedClustersPhyNames.failed()) {
            LOGGER.warn(
                "class=ClusterPhyManagerImpl||method=getPhyClusterNameWithSameEsVersionAfterBuildLogic||errMsg={}",
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
    private List<String> getPhyClusterNameWithSameEsVersion(String hasSelectedPhyClusterName,
                                                            List<String> canBeAssociatedClustersPhyNames) {
        //获取用户已选择的物理集群的信息
        ClusterPhy hasSelectedCluster = clusterPhyService.getClusterByName(hasSelectedPhyClusterName);
        //如果指定的物理集群名称为null，则返回全量的物理集群名称列表
        if (AriusObjUtils.isNull(hasSelectedPhyClusterName) || AriusObjUtils.isNull(hasSelectedCluster)
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
     * @param clusterPhyVO 物理集群元数据信息
     * @return
     */
    private void buildPhyCluster(ClusterPhyVO clusterPhyVO) {
        if (!AriusObjUtils.isNull(clusterPhyVO)) {
            Set<String> gatewayUrlLis = Sets.newHashSet();
            if (StringUtils.isNotBlank(clusterPhyVO.getGatewayIds())){
                String[] gatewayIds = clusterPhyVO.getGatewayIds().split(",");
                for (int i = 0; i < gatewayIds.length; i++) {
                    if (StringUtils.isNumeric(gatewayIds[i])) {
                        GatewayClusterPO gatewayClusterPO = gatewayClusterService.getOneById(
                            Integer.valueOf(gatewayIds[i]));
                        if (StringUtils.isNotBlank(gatewayClusterPO.getProxyAddress())) {
                            gatewayUrlLis.add(gatewayClusterPO.getProxyAddress());
                        } else {
                            List<GatewayClusterNodePO> gatewayClusterNodePOS = gatewayNodeService.listByClusterName(
                                gatewayClusterPO.getClusterName());
                            for (GatewayClusterNodePO node : gatewayClusterNodePOS) {
                                gatewayUrlLis.add(node.getHostName() + ":" + node.getPort());
                            }
                        }
                    }
                }
            }
            clusterPhyVO.setGatewayUrl(String.join(",",gatewayUrlLis));
            buildPhyClusterStatics(clusterPhyVO);
            buildClusterRole(clusterPhyVO);
            buildClusterPhyWithLogicAndRegion(Collections.singletonList(clusterPhyVO));
        }
    }

    private Result<ClusterPhyVO> saveClusterPhy(ClusterJoinDTO param, String operator) {
        //保存集群信息
        ClusterPhyDTO clusterDTO = buildPhyClusters(param, operator);
        Result<Boolean> addClusterRet = clusterPhyService.createCluster(clusterDTO);
        if (addClusterRet.failed()) {
            return Result.buildFrom(addClusterRet);
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(clusterDTO, ClusterPhyVO.class));
    }

    private ClusterPhyDTO buildPhyClusters(ClusterJoinDTO param, String operator) {
        ClusterPhyDTO clusterDTO = ConvertUtil.obj2Obj(param, ClusterPhyDTO.class);
        String clientAddress = "";
        if (StringUtils.isNotBlank(param.getProxyAddress())){
            clientAddress = param.getProxyAddress();
        }else {
            clientAddress = clusterRoleHostService.buildESClientHttpAddressesStr(param.getRoleClusterHosts());
        }

        clusterDTO.setDesc(param.getPhyClusterDesc());
        if (StringUtils.isBlank(clusterDTO.getDataCenter())) {
            clusterDTO.setDataCenter(DataCenterEnum.CN.getCode());
        }
        if (null == clusterDTO.getType()) {
            clusterDTO.setType(ESClusterTypeEnum.ES_HOST.getCode());
        }
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
        clusterDTO.setEcmAccess(Boolean.FALSE);
        clusterDTO.setComponentId(-1);
        return clusterDTO;
    }

    private ClusterPhyDTO buildPhyClusters(ClusterCreateDTO param, String operator) {
        ClusterPhyDTO clusterDTO = ConvertUtil.obj2Obj(param, ClusterPhyDTO.class);

        String clientAddress = "";
        if (StringUtils.isNotBlank(param.getProxyAddress())){
            clientAddress = param.getProxyAddress();
        }else {
            clientAddress = clusterRoleHostService.buildESClientHttpAddressesStr(param.getRoleClusterHosts());
        }

        clusterDTO.setDesc(param.getPhyClusterDesc());
        if (StringUtils.isBlank(clusterDTO.getDataCenter())) {
            clusterDTO.setDataCenter(DataCenterEnum.CN.getCode());
        }
        if (null == clusterDTO.getType()) {
            clusterDTO.setType(ESClusterTypeEnum.ES_HOST.getCode());
        }
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
        ClusterTag clusterTag = ConvertUtil.str2ObjByJson(param.getTags(), ClusterTag.class);

        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人不存在");
        }

        if (!ESClusterTypeEnum.validCode(param.getType())) {
            return Result.buildParamIllegal("非支持的集群类型");
        }

        if (!ClusterResourceTypeEnum.isExist(param.getResourceType())) {
            return Result.buildParamIllegal("非支持的集群资源类型");
        }

        if (ESClusterCreateSourceEnum.ES_IMPORT != ESClusterCreateSourceEnum.valueOf(clusterTag.getCreateSource())) {
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
        Set<String> wrongPortSet = roleClusterHosts.stream().map(ESClusterRoleHostDTO::getPort)
            .filter(this::wrongPortDetect).collect(Collectors.toSet());
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

            List<String> ips = roleClusterHosts.stream().map(ESClusterRoleHostDTO::getIp)
                .filter(StringUtils::isNotBlank).collect(Collectors.toList());
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

        String esClientHttpAddressesStr = clusterRoleHostService.buildESClientHttpAddressesStr(roleClusterHosts);

        // 密码验证
        Result<Void> passwdResult = checkClusterWithoutPasswd(param, esClientHttpAddressesStr);
        if (passwdResult.failed()) {
            return passwdResult;
        }
        // 同集群验证
        Result<Void> sameClusterResult = checkSameCluster(param.getPassword(),
            clusterRoleHostService.buildESAllRoleHttpAddressesList(roleClusterHosts));
        if (sameClusterResult.failed()) {
            return Result.buildParamIllegal("禁止同时接入超过两个不同集群节点");
        }

        // 校验 是否接入同一集群
        Result<Void> checkSameClientOrMasterClusterRet = checkSameESClientHttpAddresses(esClientHttpAddressesStr);
        if (checkSameClientOrMasterClusterRet.failed()) {
            return Result.buildFrom(checkSameClientOrMasterClusterRet);
        }

        return Result.buildSucc();
    }

    /**
     * 检查ESClientHttpAddresses是否已经存在
     * @param esClientHttpAddressesStr
     * @return
     */
    private Result<Void> checkSameESClientHttpAddresses(String esClientHttpAddressesStr) {
        List<ClusterPhy> clusterPhies = clusterPhyService.listAllClusters();
        if (CollectionUtils.isEmpty(clusterPhies)) {
            return Result.buildSucc();
        }

        // 过滤出目前平台存在的ES集群链接ip:port
        List<String> existClusterHttpAddress = Lists.newArrayList();
        List<String> clusterHttpAddressList = clusterPhies.stream().map(ClusterPhy::getHttpAddress)
            .collect(Collectors.toList());
        for (String clusterHttpAddress : clusterHttpAddressList) {
            for (String httpAddress : ListUtils.string2StrList(clusterHttpAddress)) {
                if (!existClusterHttpAddress.contains(httpAddress.trim())) {
                    existClusterHttpAddress.add(httpAddress.trim());
                }
            }
        }

        List<String> esClientHttpAddressesFromJoin = ListUtils.string2StrList(esClientHttpAddressesStr);
        for (String esClientHttpAddressFromJoin : esClientHttpAddressesFromJoin) {
            if (existClusterHttpAddress.contains(esClientHttpAddressFromJoin.trim())) {
                return Result.buildFail(String.format("平台已经存在相同的集群，连接信息为[%s], 不允许重复接入", esClientHttpAddressFromJoin));
            }
        }

        return Result.buildSucc();
    }

    private Result<Void> initClusterJoin(ClusterJoinDTO param, String esClientHttpAddressesStr) {
        //获取设置es版本
        Result<Void> esVersionSetResult = initESVersionForClusterJoin(param, esClientHttpAddressesStr);
        if (esVersionSetResult.failed()) {
            return esVersionSetResult;
        }

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

    private void doDeleteClusterJoin(ClusterPhy clusterPhy, String operator,
                                     Integer projectId) throws AdminOperateException {
        
           // 1. set region
        List<ClusterRegion> regions = clusterRegionService.listPhyClusterRegions(clusterPhy.getCluster());
        if (CollectionUtils.isEmpty(regions)) {
            return;
        }
    
        List<Long> associatedRegionIds = regions.stream().map(ClusterRegion::getId).collect(Collectors.toList());
        for (Long associatedRegionId : associatedRegionIds) {
            final ClusterRegion region = clusterRegionService.getRegionById(associatedRegionId);
            Result<Void> unbindRegionResult = clusterRegionService.unbindRegion(associatedRegionId, null, operator);
            if (unbindRegionResult.failed()) {
                throw new AdminOperateException(String.format("解绑region(%s)失败", associatedRegionId));
            } else {
                
                //解绑region
                 operateRecordService.saveOperateRecordWithManualTrigger(String.format("解绑 region：%s", region.getName()), operator, projectId,
                        clusterPhy.getId(), OperateTypeEnum.PHYSICAL_CLUSTER_REGION_CHANGE);
            }
            Result<Void> deletePhyClusterRegionResult = clusterRegionService.deletePhyClusterRegion(associatedRegionId,
                operator);
            if (deletePhyClusterRegionResult.failed()) {
                throw new AdminOperateException(String.format("删除region(%s)失败", associatedRegionId));
            } else {
                
                //删除region
                 operateRecordService.saveOperateRecordWithManualTrigger(String.format("删除 region：%s", region.getName()), operator, projectId,
                        clusterPhy.getId(), OperateTypeEnum.PHYSICAL_CLUSTER_REGION_CHANGE);
            }
        }

        List<Long> clusterLogicIds =regions.stream()
                .filter(clusterRegion -> Objects.nonNull(clusterRegion.getLogicClusterIds()))
                .map(clusterRegion -> ListUtils.string2LongList(clusterRegion.getLogicClusterIds()))
                .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
                .filter(logicId -> Objects.equals(logicId,
                        Long.parseLong(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID))).distinct()
                .collect(Collectors.toList());
        for (Long clusterLogicId : clusterLogicIds) {
            final ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByIdThatNotContainsProjectId(clusterLogicId);
            if (Objects.isNull(clusterLogic)){
                continue;
            }
            Result<Void> deleteLogicClusterResult = clusterLogicService.deleteClusterLogicById(clusterLogicId, operator,
                projectId);
            if (deleteLogicClusterResult.failed()) {
                throw new AdminOperateException(String.format("删除逻辑集群(%s)失败", clusterLogicId));
            } else {
                //删除逻辑集群
                 operateRecordService.saveOperateRecordWithManualTrigger(clusterLogic.getName(), operator, projectId,
                        clusterPhy.getId(), OperateTypeEnum.MY_CLUSTER_OFFLINE);
            }
        }

        Result<Boolean> deleteClusterResult = clusterPhyService.deleteClusterById(clusterPhy.getId(), projectId);
        if (deleteClusterResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群(%s)失败", clusterPhy.getCluster()));
        } else {
            //删除物理集群
             operateRecordService.saveOperateRecordWithManualTrigger(clusterPhy.getCluster(), operator, projectId,
                    clusterPhy.getId(), OperateTypeEnum.PHYSICAL_CLUSTER_OFFLINE);
        }

        Result<Void> deleteRoleClusterResult = clusterRoleService.deleteRoleClusterByClusterId(clusterPhy.getId(),
            projectId);
        if (deleteRoleClusterResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群角色(%s)失败", clusterPhy.getCluster()));
        } else {
            //删除物理集群角色
              operateRecordService.saveOperateRecordWithManualTrigger(String.format("cluster:[%s]删除物理集群角色;[%d]", clusterPhy.getCluster(), clusterPhy.getId()), operator,
                    projectId, clusterPhy.getId(), OperateTypeEnum.PHYSICAL_CLUSTER_OFFLINE);
        }

        Result<Void> deleteRoleClusterHostResult = clusterRoleHostService.deleteByCluster(clusterPhy.getCluster(),
            projectId);
        if (deleteRoleClusterHostResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群节点(%s)失败", clusterPhy.getCluster()));
        } else {
            if (Objects.nonNull(clusterPhy.getComponentId()) && clusterPhy.getComponentId() > 0) {
                offlineCluster(clusterPhy);
            }
            //删除物理集群角色
             operateRecordService.saveOperateRecordWithManualTrigger(String.format("cluster:[%s] 删除物理集群节点", clusterPhy.getCluster()), operator,
                    projectId, clusterPhy.getId(), OperateTypeEnum.PHYSICAL_CLUSTER_OFFLINE);
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
            refreshClusterStats(cluster, initTriple);
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

    private void postProcessingForClusterJoin(ClusterJoinDTO param) throws AdminTaskException {
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

    }

    private void refreshClusterDistInfo() {
        List<String> clusterNameList = clusterPhyService.listAllClusters().stream().map(ClusterPhy::getCluster)
            .collect(Collectors.toList());
        for (String clusterName : clusterNameList) {
            Triple<Long, Long, Double> initTriple = buildInitTriple();
            refreshClusterStats(clusterName, initTriple);
        }
    }

    private void refreshClusterStats(String clusterName, Triple<Long, Long, Double> initTriple) {
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
            LOGGER.error("class=ClusterPhyManagerImpl||method=wrongPortDetect||port={}||msg=Integer format error",
                port);
            return true;
        }
    }
    
    private TupleThree</*dcdrExist*/Boolean,/*pipelineExist*/ Boolean,/*existColdRegion*/ Boolean> getDCDRAndPipelineTupleByClusterPhy(
            String clusterPhy) throws ESOperateException {
        TupleTwo<Boolean, Boolean> tupleTwo = esClusterNodeService.existDCDRAndPipelineModule(clusterPhy);
        return Tuples.of(tupleTwo.v1,tupleTwo.v2,CollectionUtils.isNotEmpty(getColdRegionByPhyCluster(clusterPhy)));
    }
    
    private ClusterConnectionStatusWithTemplateEnum getClusterConnectionStatus(String clusterPhy) {
        return esClusterService.isConnectionStatus(clusterPhy)
                ? ClusterConnectionStatusWithTemplateEnum.NORMAL
                : ClusterConnectionStatusWithTemplateEnum.DISCONNECTED;
    }
    
   
    
     public List<ClusterRegion> getColdRegionByPhyCluster(String phyCluster) {
        List<ClusterRegion> clusterRegions = clusterRegionService.listPhyClusterRegions(phyCluster);
        //冷region是不会保存在逻辑集群侧的，所以这里关联的region肯定是大于1的，如果是小于1，那么是一定不会具备的
        if (clusterRegions.size()<=1){
            return Collections.emptyList();
        }
          return clusterRegions.stream().filter(coldTruePreByClusterRegion).collect(Collectors.toList());
    }
     private final static Predicate<ClusterRegion> coldTruePreByClusterRegion = clusterRegion -> {
        if (StringUtils.isBlank(clusterRegion.getConfig())) {
            return Boolean.FALSE;
        }
        try {
            return JSON.parseObject(clusterRegion.getConfig()).getBoolean(COLD);
        
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    
    };
}
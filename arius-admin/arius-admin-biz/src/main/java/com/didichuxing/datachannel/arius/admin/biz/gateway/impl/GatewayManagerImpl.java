package com.didichuxing.datachannel.arius.admin.biz.gateway.impl;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.TemplateLogicAliasManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Alias;
import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.gateway.GatewayClusterNode;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateAlias;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.GatewayESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplatePhysicalDeployVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.GatewaySqlConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicAliasService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.DslStatisticsService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * @author didi
 */
@Component
public class GatewayManagerImpl implements GatewayManager {

    private static final ILog               LOGGER                 = LogFactory.getLog(GatewayManagerImpl.class);

    private static final int                TIMEOUT                = 10 * 60 * 1000;

    @Autowired
    private ESUserService                   esUserService;

    @Autowired
    private ProjectService                  projectService;

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    @Autowired
    private IndexTemplateService            indexTemplateService;

    @Autowired
    private IndexTemplatePhyService         indexTemplatePhyService;

    @Autowired
    private TemplateLogicAliasManager       templateLogicAliasManager;

    @Autowired
    private GatewayService                  gatewayService;

    @Autowired
    private ClusterPhyService clusterPhyService;

    @Autowired
    private DslStatisticsService            dslStatisticsService;


    @Autowired
    private TemplateLogicAliasService       templateLogicAliasService;

    @Autowired
    private ProjectConfigService            projectConfigService;

    @Autowired
    private GatewayClusterService gatewayClusterService;

    private final Cache<String, Object>     projectESUserListCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100).build();

    @Override
    public Result<Void> heartbeat(GatewayHeartbeat heartbeat) {
        return gatewayService.heartbeat(heartbeat);
    }

    @Override
    public Result<Integer> heartbeat(String clusterName) {
        return gatewayService.aliveCount(clusterName, TIMEOUT);
    }

    @Override
    public Result<List<GatewayClusterNodeVO>> getGatewayAliveNode(String clusterName) {
        return Result.buildSucc(
            ConvertUtil.list2List(gatewayService.getAliveNode(clusterName, TIMEOUT), GatewayClusterNodeVO.class));
    }

    @Override
    public Result<List<String>> getGatewayAliveNodeNames(String clusterName) {
        List<GatewayClusterNode> aliveNodes = gatewayService.getAliveNode(clusterName, TIMEOUT);
        List<String> list = Lists.newArrayList();
        if (aliveNodes != null && !aliveNodes.isEmpty()) {
            list = aliveNodes.stream().map(GatewayClusterNode::getHostName).collect(Collectors.toList());
        }
        return Result.buildSucc(list);
    }



    @Override
    public Result<List<GatewayESUserVO>> listESUserByProject(List<String> gatewayClusterName) {

        // 查询出所有的应用
        List<ESUser> esUsers = listESUserWithCache();
        final Map<Integer/*projectId*/, /*es user*/List<Integer>> projectIdEsUsersMap = esUsers.stream().collect(
            Collectors.groupingBy(ESUser::getProjectId, Collectors.mapping(ESUser::getId, Collectors.toList())));
        final Map<Integer/*projectId*/, String/*projectName*/> projectId2ProjectNameMap = listProjectWithCache();
        Map<Integer/*projectId*/, ProjectConfig> projectId2ProjectConfigMap = listProjectConfigWithCache();

        // 查询出所有的权限
        Map<Integer/*projectId*/, Collection<ProjectTemplateAuth>> projectId2ProjectTemplateAuthsMap = projectLogicTemplateAuthService.getAllProjectTemplateAuthsWithCache();
        Map<Integer/*es user*/, Collection<ProjectTemplateAuth>> esUser2ProjectTemplateAuthsMap = Maps.newHashMap();
        Map<Integer/*es user*/, String/*projectName*/> esUser2ProjectNameMap = Maps.newHashMap();
        Map<Integer/*es user*/, ProjectConfig> esUser2ESUserConfigMap = Maps.newHashMap();
        //转换
        for (Entry<Integer, List<Integer>> projectIdESUsersEntry : projectIdEsUsersMap.entrySet()) {
            final Integer projectId = projectIdESUsersEntry.getKey();
            for (Integer esuser : projectIdESUsersEntry.getValue()) {
                Optional.ofNullable(projectId2ProjectTemplateAuthsMap.get(projectId)).ifPresent(
                    projectTemplateAuths -> esUser2ProjectTemplateAuthsMap.put(esuser, projectTemplateAuths));
                Optional.ofNullable(projectId2ProjectNameMap.get(projectId))
                    .ifPresent(projectName -> esUser2ProjectNameMap.put(esuser, projectName));
                Optional.ofNullable(projectId2ProjectConfigMap.get(projectId))
                    .ifPresent(projectConfig -> esUser2ESUserConfigMap.put(esuser, projectConfig));
            }
        }
        Map<Integer/*logicId*/, List<String>> aliasMap = templateLogicAliasService.listAliasMapWithCache();
        //如果不传gateway集群名称，默认返回全量app元信息
        //如果传递，返回绑定了gateway的app元信息
        List<GatewayESUserVO> appVOList;
        if (CollectionUtils.isEmpty(gatewayClusterName)) {
            appVOList = getGatewayESUserVOS(esUsers, esUser2ProjectTemplateAuthsMap, esUser2ProjectNameMap, esUser2ESUserConfigMap, aliasMap);
        } else {
            appVOList = getESUserVOSBindedGateway(gatewayClusterName, esUsers, esUser2ProjectTemplateAuthsMap, esUser2ProjectNameMap, esUser2ESUserConfigMap, aliasMap);
        }
        return Result.buildSucc(appVOList);
    }

    @Override
    public Result<Map<String, GatewayTemplatePhysicalVO>> getTemplateMap(String cluster, List<String> gatewayClusterName) {
        if(!CollectionUtils.isEmpty(gatewayClusterName)){
            List<String> phyClusterNamesByGatewayClusterName = getPhyClusterNamesByGatewayClusterName(gatewayClusterName);
            if (!phyClusterNamesByGatewayClusterName.contains(cluster)) {
                return Result.buildSucc(Maps.newHashMap());
            }
        }

        List<IndexTemplatePhy> indexTemplatePhysicalInfos = indexTemplatePhyService.getNormalTemplateByCluster(cluster);

        if (CollectionUtils.isEmpty(indexTemplatePhysicalInfos)) {
            return Result.buildSucc(Maps.newHashMap());
        }

        Map<Integer, IndexTemplate> templateId2IndexTemplateLogicMap = indexTemplateService.getAllLogicTemplatesMap();

        List<IndexTemplateAlias> aliases = templateLogicAliasManager.listAlias();
        Multimap<Integer, IndexTemplateAlias> logicId2IndexTemplateAliasMultiMap = ConvertUtil.list2MulMap(aliases,
            IndexTemplateAlias::getLogicId);

        Map<String, GatewayTemplatePhysicalVO> result = Maps.newHashMap();
        for (IndexTemplatePhy templatePhysical : indexTemplatePhysicalInfos) {
            try {
                GatewayTemplatePhysicalVO templatePhysicalVO = ConvertUtil.obj2Obj(templatePhysical,
                    GatewayTemplatePhysicalVO.class);

                templatePhysicalVO
                    .setDataCenter(templateId2IndexTemplateLogicMap.get(templatePhysical.getLogicId()).getDataCenter());

                Collection<IndexTemplateAlias> indexTemplateAliases = logicId2IndexTemplateAliasMultiMap
                    .get(templatePhysical.getLogicId());
                if (CollectionUtils.isEmpty(indexTemplateAliases)) {
                    templatePhysicalVO.setAliases(Lists.newArrayList());
                } else {
                    templatePhysicalVO
                        .setAliases(ConvertUtil.list2List(Lists.newArrayList(indexTemplateAliases), Alias.class));
                }

                result.put(templatePhysicalVO.getExpression(), templatePhysicalVO);
            } catch (Exception e) {
                LOGGER.warn("class=GatewayManagerImpl||method=getTemplateMap||cluster={}||errMsg={}", cluster,
                    e.getMessage(), e);
            }
        }

        return Result.buildSucc(result);
    }

    @Override
    public Result<Map<String, GatewayTemplateDeployInfoVO>> listDeployInfo(List<String> gatewayClusterName) {
        List<IndexTemplateWithPhyTemplates> logicWithPhysicals;
        //如果传gateway集群名称，过滤出绑定了gateway的逻辑模板。如果不传默认返回全量模板信息
        if(CollectionUtils.isEmpty(gatewayClusterName)){
             logicWithPhysicals = indexTemplateService.listTemplateWithPhysical();
        }else{
            List<String> phyClusterNames = getPhyClusterNamesByGatewayClusterName(gatewayClusterName);
            logicWithPhysicals = getFilterLogicTemplatesBindedGateway(phyClusterNames);
        }
        List<IndexTemplateAlias> logicWithAliases = templateLogicAliasManager.listAlias(logicWithPhysicals);
        Multimap<Integer, IndexTemplateAlias> logicId2IndexTemplateAliasMultiMap = ConvertUtil
            .list2MulMap(logicWithAliases, IndexTemplateAlias::getLogicId);

        Map<String, GatewayTemplateDeployInfoVO> result = Maps.newHashMap();
        for (IndexTemplateWithPhyTemplates logicWithPhysical : logicWithPhysicals) {
            if (logicWithPhysical.hasPhysicals()) {
                try {
                    GatewayTemplateDeployInfoVO gatewayTemplateDeployInfoVO = buildGatewayTemplateDeployInfoVO(
                        logicWithPhysical, logicId2IndexTemplateAliasMultiMap);

                    if (null != gatewayTemplateDeployInfoVO) {
                        result.put(logicWithPhysical.getName(), gatewayTemplateDeployInfoVO);
                    }
                } catch (Exception e) {
                    LOGGER.warn(
                        "class=GatewayManagerImpl||method=listDeployInfo||||templateName={}||errMsg={}",
                         logicWithPhysical.getName(), e.getMessage(), e);
                }
            }
        }

        return Result.buildSucc(result);
    }

    @Override
    public Result<ScrollDslTemplateResponse> scrollSearchDslTemplate(ScrollDslTemplateRequest request) {
        return dslStatisticsService.scrollSearchDslTemplate(request);
    }

    @Override
    public Result<Boolean> addAlias(IndexTemplateAliasDTO indexTemplateAliasDTO) {
        return templateLogicAliasService.addAlias(indexTemplateAliasDTO);
    }

    @Override
    public Result<Boolean> delAlias(IndexTemplateAliasDTO indexTemplateAliasDTO) {
        return templateLogicAliasService.delAlias(indexTemplateAliasDTO);
    }

    @Override
    public Result<String> sqlExplain(String sql, String phyClusterName, Integer projectId) {
        if (projectId == null || !esUserService.checkDefaultESUserByProject(projectId)) {
            return Result.buildParamIllegal("对应的projectId字段非法");
        }
        if (!clusterPhyService.isClusterExists(phyClusterName)) {
            return Result.buildNotExist("集群不存在");
        }
        final ESUser esUser = esUserService.getDefaultESUserByProject(projectId);

        return gatewayService.sqlOperate(sql, phyClusterName, esUser, GatewaySqlConstant.SQL_EXPLAIN);
    }

    @Override
    public Result<String> directSqlSearch(String sql, String phyClusterName, Integer projectId) {
        if (projectId == null || !esUserService.checkDefaultESUserByProject(projectId)) {
            return Result.buildParamIllegal("对应的projectId字段非法");
        }
        if (!clusterPhyService.isClusterExists(phyClusterName)) {
            return Result.buildNotExist("集群不存在");
        }
        final ESUser esUser = esUserService.getDefaultESUserByProject(projectId);
        return gatewayService.sqlOperate(sql, phyClusterName, esUser, GatewaySqlConstant.SQL_SEARCH);
    }

    /**************************************** private method *************************************************/
    private GatewayTemplatePhysicalDeployVO buildPhysicalDeployVO(IndexTemplatePhy physical) {
        if (physical == null) {
            return null;
        }

        GatewayTemplatePhysicalDeployVO deployVO = new GatewayTemplatePhysicalDeployVO();
        deployVO.setTemplateName(physical.getName());
        deployVO.setCluster(physical.getCluster());
        deployVO.setRack(physical.getRack());
        deployVO.setShardNum(physical.getShard());
        deployVO.setGroupId(physical.getGroupId());
        deployVO.setDefaultWriterFlags(physical.fetchDefaultWriterFlags());

        if (StringUtils.isNotBlank(physical.getConfig())) {
            IndexTemplatePhysicalConfig config = JSON.parseObject(physical.getConfig(),
                IndexTemplatePhysicalConfig.class);
            deployVO.setTopic(config.getKafkaTopic());
            deployVO.setAccessProjects(config.getAccessProjects());
            deployVO.setMappingIndexNameEnable(config.getMappingIndexNameEnable());
            deployVO.setTypeIndexMapping(config.getTypeIndexMapping());
        }

        return deployVO;
    }

    private GatewayESUserVO buildESUserVO(ESUser esUser,
                                          Map<Integer/*es user*/, Collection<ProjectTemplateAuth>> esUser2ProjectTemplateAuthsMap,
                                          Map<Integer, ProjectConfig> esUser2ESUserConfigMap,
                                          Map<Integer, IndexTemplate> templateId2IndexTemplateLogicMap,
                                          String defaultReadPermissionIndexes, Map<Integer, List<String>> aliasMap) {

        GatewayESUserVO gatewayESUserVO = ConvertUtil.obj2Obj(esUser, GatewayESUserVO.class);

        if (StringUtils.isBlank(gatewayESUserVO.getDataCenter())) {
            gatewayESUserVO.setDataCenter("cn");
        }

        if (StringUtils.isNotBlank(esUser.getIp())) {
            gatewayESUserVO.setIp(Lists.newArrayList(esUser.getIp().split(",")));
        } else {
            gatewayESUserVO.setIp(Lists.newArrayList());
        }

        gatewayESUserVO.setIsRoot(esUser.getIsRoot());
        if (AdminConstant.YES.equals(esUser.getIsRoot())) {
            gatewayESUserVO.setIndexExp(Lists.newArrayList("*"));
            gatewayESUserVO.setWIndexExp(Lists.newArrayList("*"));
        } else {
            List<String> readPermissionIndexExpressions = new ArrayList<>();
            List<String> writePermissionIndexExpressions = new ArrayList<>();

            if (StringUtils.isNotBlank(esUser.getIndexExp())) {
                readPermissionIndexExpressions.addAll(Lists.newArrayList(esUser.getIndexExp().split(",")));
            }
            readPermissionIndexExpressions.addAll(Arrays.asList(defaultReadPermissionIndexes.split(",")));
            //判断key 是否属于该项目下的es user
            if (esUser2ProjectTemplateAuthsMap.containsKey(esUser.getId())) {
                //获取该项目下的es user
                Collection<ProjectTemplateAuth> templateAuthCollection = esUser2ProjectTemplateAuthsMap
                    .get(esUser.getId());
                if (!templateAuthCollection.isEmpty()) {

                    fetchPermissionIndexExpressions(esUser.getId(), templateAuthCollection,
                        templateId2IndexTemplateLogicMap, aliasMap, readPermissionIndexExpressions,
                        writePermissionIndexExpressions);
                } else {
                    LOGGER.warn("class=GatewayManagerImpl||method=buildAppVO||esUser={}||msg=esUser has no permission.",
                        esUser.getId());
                }
            }

            gatewayESUserVO.setIndexExp(readPermissionIndexExpressions);
            gatewayESUserVO.setWIndexExp(writePermissionIndexExpressions);
        }

        ProjectConfig config = esUser2ESUserConfigMap.get(esUser.getId());
        if (config != null) {
            gatewayESUserVO.setDslAnalyzeEnable(config.getDslAnalyzeEnable());
            gatewayESUserVO.setAggrAnalyzeEnable(config.getAggrAnalyzeEnable());
            gatewayESUserVO.setAnalyzeResponseEnable(config.getAnalyzeResponseEnable());
        } else {
            LOGGER.warn("class=GatewayManagerImpl||method=buildAppVO||esUser={}||msg=esUser config is not exists.",
                esUser.getId());
        }

        return gatewayESUserVO;
    }

    /**
     * 获取当前项目所有读写权限索引列表
     * @param esUserId es user
     * @param projectTemplateAuthCollection project模板权限集合
     * @param templateId2IndexTemplateLogicMap  模板Id跟模板详情映射
     * @param indexExpressions  当前esUser读权限列表
     * @param writeExpressions  当前esUser写权限列表
     */
    private void fetchPermissionIndexExpressions(Integer esUserId,
                                                 Collection<ProjectTemplateAuth> projectTemplateAuthCollection,
                                                 Map<Integer, IndexTemplate> templateId2IndexTemplateLogicMap,
                                                 Map<Integer, List<String>> aliasMap, List<String> indexExpressions,
                                                 List<String> writeExpressions) {
        if (CollectionUtils.isNotEmpty(projectTemplateAuthCollection)) {
            projectTemplateAuthCollection.stream().forEach(auth -> {
                List<String> alias = aliasMap.getOrDefault(auth.getTemplateId(), new ArrayList<>(0));
                String expression;
                try {
                    expression = templateId2IndexTemplateLogicMap.get(auth.getTemplateId()).getExpression();
                } catch (Exception e) {
                    LOGGER.warn(
                        "class=GatewayManagerImpl||method=fetchPermissionIndexExpressions||projectId={}||templateId={}||msg=template not exists.",
                        esUserId, auth.getTemplateId());
                    return;
                }
                indexExpressions.add(expression);
                indexExpressions.addAll(alias);
                if (ProjectTemplateAuthEnum.OWN.getCode().equals(auth.getType())
                    || ProjectTemplateAuthEnum.RW.getCode().equals(auth.getType())) {
                    writeExpressions.add(expression);
                    writeExpressions.addAll(alias);
                }
            });
        }
    }

    private GatewayTemplateDeployInfoVO buildGatewayTemplateDeployInfoVO(IndexTemplateWithPhyTemplates logicWithPhysical,
                                                                         Multimap<Integer, IndexTemplateAlias> logicId2IndexTemplateAliasMultiMap) {
        if (null == logicWithPhysical || null == logicWithPhysical.getMasterPhyTemplate()) {
            return null;
        }

        GatewayTemplateVO baseInfo = ConvertUtil.obj2Obj(logicWithPhysical, GatewayTemplateVO.class);
        baseInfo.setDeployStatus(TemplateUtils.genDeployStatus(logicWithPhysical));
        baseInfo.setAliases(logicId2IndexTemplateAliasMultiMap.get(logicWithPhysical.getId()).stream()
            .map(IndexTemplateAlias::getName).collect(Collectors.toList()));
        baseInfo.setVersion(logicWithPhysical.getMasterPhyTemplate().getVersion());
        
        GatewayTemplatePhysicalDeployVO masterInfo = genMasterInfo(logicWithPhysical);
        List<GatewayTemplatePhysicalDeployVO> slaveInfos = genSlaveInfos(logicWithPhysical);

        GatewayTemplateDeployInfoVO deployInfoVO = new GatewayTemplateDeployInfoVO();
        deployInfoVO.setBaseInfo(baseInfo);
        deployInfoVO.setMasterInfo(masterInfo);
        deployInfoVO.setSlaveInfos(slaveInfos);
        
        if (!TemplateServiceEnum.strContainsSrv(logicWithPhysical.getOpenSrv(), TemplateServiceEnum.TEMPLATE_PIPELINE)) {
            deployInfoVO.getBaseInfo().setIngestPipeline("");
        }

        return deployInfoVO;
    }

    private GatewayTemplatePhysicalDeployVO genMasterInfo(IndexTemplateWithPhyTemplates logicWithPhysical) {
        return buildPhysicalDeployVO(logicWithPhysical.getMasterPhyTemplate());
    }

    private List<GatewayTemplatePhysicalDeployVO> genSlaveInfos(IndexTemplateWithPhyTemplates logicWithPhysical) {
        List<GatewayTemplatePhysicalDeployVO> slavesInfos = Lists.newArrayList();
        for (IndexTemplatePhy physical : logicWithPhysical.getPhysicals()) {
            if (physical.getRole().equals(TemplateDeployRoleEnum.MASTER.getCode())) {
                continue;
            }
            slavesInfos.add(buildPhysicalDeployVO(physical));
        }
        return slavesInfos;
    }
    
        private List<ESUser> listESUsers() {
        final List<Integer> projectIds = projectService.getProjectBriefList().stream().map(ProjectBriefVO::getId)
            .collect(Collectors.toList());
        return esUserService.listESUsers(projectIds);
    }

    private Map<Integer, String> listProject() {
        return projectService.getProjectBriefList().stream()
            .collect(Collectors.toMap(ProjectBriefVO::getId, ProjectBriefVO::getProjectName));
    }

    private Map<Integer, String> listProjectWithCache() {
        try {
            return (Map<Integer, String>) projectESUserListCache.get("listProject", this::listProject);
        } catch (ExecutionException e) {
            return listProject();
        }
    }

    private List<ESUser> listESUserWithCache() {
        try {
            return (List<ESUser>) projectESUserListCache.get("listESUsers", this::listESUsers);
        } catch (ExecutionException e) {
            return listESUsers();
        }
    }

    private Map<Integer/*projectId*/, ProjectConfig> listProjectConfig() {

        return projectConfigService.projectId2ProjectConfigMap();
    }

    private Map<Integer/*projectId*/, ProjectConfig> listProjectConfigWithCache() {
        try {
            return (Map<Integer/*projectId*/, ProjectConfig>) projectESUserListCache.get("listProjectConfig",
                this::listProjectConfig);
        } catch (ExecutionException e) {
            return listProjectConfig();
        }
    }

    /**
     * 获取全量app元信息
     *
     * @param esUsers
     * @param esUser2ProjectTemplateAuthsMap
     * @param esUser2ProjectNameMap
     * @param esUser2ESUserConfigMap
     * @param aliasMap
     * @return
     */
    private List<GatewayESUserVO> getGatewayESUserVOS(List<ESUser> esUsers, Map<Integer, Collection<ProjectTemplateAuth>> esUser2ProjectTemplateAuthsMap, Map<Integer, String> esUser2ProjectNameMap, Map<Integer, ProjectConfig> esUser2ESUserConfigMap, Map<Integer, List<String>> aliasMap) {
        //获取带有物理模板的逻辑模板
        Map<Integer/*id*/, IndexTemplate> templateId2IndexTemplateLogicMap = indexTemplateService
                .getAllLogicTemplatesMapWithCache();

        List<GatewayESUserVO> appVOList = esUsers.parallelStream().map(user -> {
            try {
                final GatewayESUserVO gatewayESUserVO = buildESUserVO(user, esUser2ProjectTemplateAuthsMap,
                        esUser2ESUserConfigMap, templateId2IndexTemplateLogicMap, "", aliasMap);
                final Integer esUser = gatewayESUserVO.getId();
                if (esUser2ProjectNameMap.containsKey(esUser)) {
                    gatewayESUserVO.setName(esUser2ProjectNameMap.get(esUser));
                }
                return gatewayESUserVO;
            } catch (Exception e) {
                LOGGER.warn("class=GatewayManagerImpl||method=getGatewayESUserVOS||errMsg={}||stackTrace={}", e.getMessage(),
                        JSON.toJSONString(e.getStackTrace()), e);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return appVOList;
    }

    /**
     * 获取绑定了gateway的app元信息
     *
     * @param gatewayClusterName
     * @param esUsers
     * @param esUser2ProjectTemplateAuthsMap
     * @param esUser2ProjectNameMap
     * @param esUser2ESUserConfigMap
     * @param aliasMap
     * @return
     */
    private List<GatewayESUserVO> getESUserVOSBindedGateway(List<String> gatewayClusterName, List<ESUser> esUsers, Map<Integer, Collection<ProjectTemplateAuth>> esUser2ProjectTemplateAuthsMap, Map<Integer, String> esUser2ProjectNameMap, Map<Integer, ProjectConfig> esUser2ESUserConfigMap, Map<Integer, List<String>> aliasMap) {
        List<IndexTemplate> filterBindedLogicTemplates = filterLogicTemplatesBindedGatewayByGatewayClusterName(gatewayClusterName);
        Map<Integer, IndexTemplate> templateId2IndexTemplateLogicMap = ConvertUtil.list2Map(filterBindedLogicTemplates, IndexTemplate::getId, index -> index);
        List<Integer> filterProjectIdsBindedGateway = filterBindedLogicTemplates.stream().map(IndexTemplate::getProjectId).distinct().collect(Collectors.toList());

        List<GatewayESUserVO> appVOList = esUsers.parallelStream().filter(esUser ->
                filterProjectIdsBindedGateway.contains(esUser.getProjectId())).map(user -> {
            try {
                final GatewayESUserVO gatewayESUserVO = buildESUserVO(user, esUser2ProjectTemplateAuthsMap,
                        esUser2ESUserConfigMap, templateId2IndexTemplateLogicMap, "", aliasMap);
                final Integer esUser = gatewayESUserVO.getId();
                if (esUser2ProjectNameMap.containsKey(esUser)) {
                    gatewayESUserVO.setName(esUser2ProjectNameMap.get(esUser));
                }
                return gatewayESUserVO;
            } catch (Exception e) {
                LOGGER.warn("class=GatewayManagerImpl||method=getESUserVOSBindedGateway||errMsg={}||stackTrace={}", e.getMessage(),
                        JSON.toJSONString(e.getStackTrace()), e);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return appVOList;
    }

    /**
     * 通过gateway集群名称过滤出绑定了gateway的逻辑模板
     *
     * @param gatewayClusterName
     * @return
     */
    private List<IndexTemplate> filterLogicTemplatesBindedGatewayByGatewayClusterName(List<String> gatewayClusterName) {
        List<String> phyClusterNames = getPhyClusterNamesByGatewayClusterName(gatewayClusterName);
        List<IndexTemplateWithPhyTemplates> filterLogicTemplatesWithPhyBindedGateway = getFilterLogicTemplatesBindedGateway(phyClusterNames);
        List<IndexTemplate> filterLogicTemplatesBindedGateway = ConvertUtil.list2List(filterLogicTemplatesWithPhyBindedGateway, IndexTemplate.class);
        return filterLogicTemplatesBindedGateway;
    }

    /**
     * 过滤出绑定了gateway的逻辑模板
     *
     * @param phyClusterNames
     * @return
     */
    private List<IndexTemplateWithPhyTemplates> getFilterLogicTemplatesBindedGateway(List<String> phyClusterNames) {
        List<IndexTemplateWithPhyTemplates> indexTemplateWithPhyTemplates = indexTemplateService.listAllLogicTemplateWithPhysicals();

        List<IndexTemplateWithPhyTemplates> filterBindedLogicTemplatesWithPhy = indexTemplateWithPhyTemplates.stream()
                .filter(logicTemplate -> logicTemplate.getPhysicals().stream()
                        .anyMatch(indexTemplatePhy -> phyClusterNames.contains(indexTemplatePhy.getCluster())))
                .collect(Collectors.toList());
        return filterBindedLogicTemplatesWithPhy;
    }

    /**
     * 通过网关集群获取物理集群
     *
     * @param gatewayClusterName
     * @return
     */
    private List<String> getPhyClusterNamesByGatewayClusterName(List<String> gatewayClusterName) {
        List<Integer> gatewayClusterIds = gatewayClusterService.listByNames(gatewayClusterName).stream().map(GatewayClusterPO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(gatewayClusterIds)) {
            return Lists.newArrayList();
        }
        List<ClusterPhy> clusterPhies = clusterPhyService.listAllClusters();
        List<ClusterPhy> listClustersByGatewayIds = clusterPhies.stream().filter(clusterPhy -> {
            List<Integer> gatewayIds = Lists.newArrayList(StringUtils.split(Optional.ofNullable(clusterPhy.getGatewayIds()).orElse(""), ",")).stream().map(Integer::valueOf).collect(Collectors.toList());
            return gatewayIds.stream().anyMatch(gatewayId->gatewayClusterIds.contains(gatewayId));
        }).collect(Collectors.toList());
        List<String> phyClusterNames = listClustersByGatewayIds.stream().map(ClusterPhy::getCluster).collect(Collectors.toList());
        return phyClusterNames;
    }

}
package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.DEFAULT_CLUSTER_HEALTH;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD_BIND_MULTIPLE_PROJECT;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.LogicResourceConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Plugin;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterNodeSepc;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterLogicPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.LogicClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author d06679
 * @date 2019/3/25
 */
@Service
public class ClusterLogicServiceImpl implements ClusterLogicService {

    private static final ILog              LOGGER = LogFactory.getLog(ClusterLogicServiceImpl.class);

    @Autowired
    private LogicClusterDAO                logicClusterDAO;
    @Autowired
    private IndexTemplateDAO indexTemplateDAO;

    @Autowired
    private ProjectClusterLogicAuthService logicClusterAuthService;

    @Autowired
    private ProjectService                 projectService;


    @Autowired
    private ESPluginService                esPluginService;

    @Autowired
    private ClusterPhyService              clusterPhyService;

    @Autowired
    private ESMachineNormsService          esMachineNormsService;

    @Autowired
    private ClusterRegionService           clusterRegionService;

    @Autowired
    private ClusterRoleHostService         clusterRoleHostService;

    /**
     * 条件查询逻辑集群
     *
     * @param param 条件
     * @return 逻辑集群列表
     */
    @Override
    public List<ClusterLogic> listClusterLogics(ESLogicClusterDTO param) {
        return logicClusterDAO.listByCondition(ConvertUtil.obj2Obj(param, ClusterLogicPO.class)).stream()
                .map(this::clusterLogicPoProjectIdStrConvertClusterLogic).flatMap(Collection::stream)
                .filter(clusterLogic -> filterClusterLogicByProjectId(clusterLogic, param.getProjectId()))
                .collect(Collectors.toList());
        
    }

    /**
     * 获取所有逻辑集群
     *
     * @return 逻辑集群列表
     */
    @Override
    public List<ClusterLogic> listAllClusterLogics() {
        return logicClusterDAO.listAll()
                .stream()
                .map(this::clusterLogicPoProjectIdStrConvertClusterLogic)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 删除逻辑集群
     *
     * @param logicClusterId 资源id
     * @param operator       操作人
     * @param deleteProjectId     项目id
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteClusterLogicById(Long logicClusterId, String operator,
                                               Integer deleteProjectId) throws AdminOperateException {
        ClusterLogicPO logicCluster = logicClusterDAO.getById(logicClusterId);
        if (logicCluster == null) {
            return Result.buildNotExist("逻辑集群不存在");
        }
        if ( hasLogicClusterWithTemplates(logicClusterId)) {
            return Result.build(ResultType.IN_USE_ERROR.getCode(), "逻辑集群使用中");
        }
        boolean succeed = false;
        if (StringUtils.contains(logicCluster.getProjectId(), ",")) {
            final ClusterLogicPO clusterLogicPO = new ClusterLogicPO();
            clusterLogicPO.setId(logicClusterId);
            final List<Integer> projectIds = str2ListProjectIds(logicCluster);
            projectIds.remove(deleteProjectId);
            clusterLogicPO.setProjectId(ConvertUtil.list2String(projectIds, ","));
            succeed = (logicClusterDAO.update(clusterLogicPO) == 1);
        } else {
            succeed = (logicClusterDAO.delete(logicClusterId) == 1);
        }
        if (!succeed) {
            throw new AdminOperateException("删除逻辑集群失败");
        }

        return Result.buildSucc();
    }

    @Override
    public boolean hasLogicClusterWithTemplates(Long logicClusterId) {
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(logicClusterId);
        if (null == clusterRegion) {
            return false;
        }
        return CollectionUtils.isNotEmpty(
                indexTemplateDAO.listByResourceIds(Collections.singletonList(logicClusterId)));
    }
    
    /**
     * > 加入集群
     *
     * @param param 加入集群逻辑的参数对象。
     * @return 加入集群逻辑的结果。
     */
    @Override
    public Result<Long> joinClusterLogic(ESLogicClusterDTO param) {
        Result<Void> checkResult = validateClusterLogicParams(param, ADD_BIND_MULTIPLE_PROJECT, param.getProjectId());
        if (checkResult.failed()) {
            LOGGER.warn("class=ClusterLogicServiceImpl||method=joinClusterLogic||msg={}", checkResult.getMessage());
            return Result.buildFrom(checkResult);
        }
        ClusterLogicPO clusterLogicPO = logicClusterDAO.getById(param.getId());
        final List<Integer> projectIds = str2ListProjectIds(clusterLogicPO);
        projectIds.add(param.getProjectId());
        final String projectIdStr = ConvertUtil.list2String(projectIds.stream().distinct().collect(Collectors.toList()),
                ",");
        clusterLogicPO.setProjectId(projectIdStr);
        return Result.build(logicClusterDAO.update(clusterLogicPO) == 1, clusterLogicPO.getId());
    }
    
    /**
     * 新建逻辑集群
     *
     * @param param    参数
     * @return result
     */
    @Override
    public Result<Long> createClusterLogic(ESLogicClusterDTO param) {
        Result<Void>  checkResult = validateClusterLogicParams(param, ADD, param.getProjectId());
        if (checkResult.failed()) {
            LOGGER.warn("class=ClusterLogicServiceImpl||method=createClusterLogic||msg={}", checkResult.getMessage());
            return Result.buildFrom(checkResult);
        }
    
        initLogicCluster(param);
    
        ClusterLogicPO logicPO = ConvertUtil.obj2Obj(param, ClusterLogicPO.class,
                po -> po.setProjectId(param.getProjectId().toString()));
        boolean succeed = logicClusterDAO.insert(logicPO) == 1;
        return Result.build(succeed, logicPO.getId());
    
    }

    /**
     * 验证逻辑集群是否合法
     *
     * @param param     参数
     * @param operation 操作
     * @param projectId 项目id
     * @return result
     */
    @Override
    public Result<Void> validateClusterLogicParams(ESLogicClusterDTO param, OperationEnum operation,
                                                   Integer projectId) {
        return checkLogicClusterParams(param, operation, projectId);
    }

    @Override
    public Result<Void> editClusterLogic(ESLogicClusterDTO param, String operator, Integer projectId) {
        Result<Void> checkResult = validateClusterLogicParams(param, EDIT, projectId);
        if (checkResult.failed()) {
            LOGGER.warn("class=ClusterLogicServiceImpl||method=editResource||msg={}", checkResult.getMessage());
            return checkResult;
        }

        return editClusterLogicNotCheck(param, operator);
    }

    @Override
    public Result<Void> editClusterLogicNotCheck(ESLogicClusterDTO param, String operator) {
        ClusterLogicPO paramPO = ConvertUtil.obj2Obj(param, ClusterLogicPO.class);
        if (Objects.nonNull(paramPO.getProjectId())) {
            final ClusterLogicPO clusterLogicPO = logicClusterDAO.getById(param.getId());
            final List<Integer> listProjectIds = str2ListProjectIds(clusterLogicPO);
            listProjectIds.add(param.getProjectId());
            final String projectIdStr = ConvertUtil.list2String(
                    listProjectIds.stream().distinct().collect(Collectors.toList()), ",");
            paramPO.setProjectId(projectIdStr);
        }
        
    
        boolean succ = (1 == logicClusterDAO.update(paramPO));

        return Result.build(succ);
    }
    
    /**
     * 获取集群逻辑通过id那不包含项目id
     *
     * @param logicClusterId 逻辑集群id
     * @return {@code ClusterLogic}
     */
    @Override
    public ClusterLogic getClusterLogicByIdThatNotContainsProjectId(Long logicClusterId) {
        return ConvertUtil.obj2Obj(logicClusterDAO.getById(logicClusterId),ClusterLogic.class);
    }
    
    /**
     * @param logicClusterId
     * @return
     */
    @Override
    public boolean existClusterLogicById(Long logicClusterId) {
        return Objects.nonNull(logicClusterDAO.getById(logicClusterId));
    }
    
    @Override
    public ClusterLogic getClusterLogicByIdAndProjectId(Long logicClusterId, Integer projectId) {
        return clusterLogicPoProjectIdStrConvertClusterLogic(logicClusterDAO.getById(logicClusterId)).stream()
                .filter(clusterLogic -> filterClusterLogicByProjectId(clusterLogic, projectId)).findFirst()
                .orElse(null);
       
    }
    
    /**
     * @param logicClusterId
     * @return
     */
    @Override
    public List<ClusterLogic> listClusterLogicByIdThatProjectIdStrConvertProjectIdList(Long logicClusterId) {
        return clusterLogicPoProjectIdStrConvertClusterLogic(logicClusterDAO.getById(logicClusterId));
    }
    
    @Override
    public ClusterLogic getClusterLogicByNameAndProjectId(String logicClusterName, Integer projectId) {
        return clusterLogicPoProjectIdStrConvertClusterLogic(logicClusterDAO.getByName(logicClusterName)).stream()
                .filter(clusterLogic -> filterClusterLogicByProjectId(clusterLogic, projectId)).findFirst()
                .orElse(null);
    }
    
    /**
     * @param logicClusterName
     * @return
     */
    @Override
    public ClusterLogic getClusterLogicByNameThatNotContainsProjectId(String logicClusterName) {
        return ConvertUtil.obj2Obj(logicClusterDAO.getByName(logicClusterName), ClusterLogic.class);
    }
    
    /**
     * @param logicClusterName
     * @return
     */
    @Override
    public List<ClusterLogic> listClusterLogicByNameThatProjectIdStrConvertProjectIdList(String logicClusterName) {
         return clusterLogicPoProjectIdStrConvertClusterLogic(logicClusterDAO.getByName(logicClusterName));
    }
    
    /**
     * 查询指定逻辑集群的配置
     *
     * @param logicClusterId 逻辑集群id
     * @return 逻辑集群 不存在返回null
     */
    @Override
    public LogicResourceConfig getClusterLogicConfigById(Long logicClusterId) {
        ClusterLogic clusterLogic =ConvertUtil.obj2Obj(logicClusterDAO.getById(logicClusterId), ClusterLogic.class);
        if (clusterLogic == null) {
            return null;
        }
        return genClusterLogicConfig(clusterLogic.getConfigJson());
    }

    /**
     * 查询指定project所创建的逻辑集群
     *
     * @param projectId project
     * @return list
     */
    @Override
    public List<ClusterLogic> getOwnedClusterLogicListByProjectId(Integer projectId) {
        return logicClusterDAO.listByProjectId(projectId).stream()
                .map(this::clusterLogicPoProjectIdStrConvertClusterLogic).flatMap(Collection::stream)
                .filter(clusterLogic -> filterClusterLogicByProjectId(clusterLogic,projectId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getHasAuthClusterLogicIdsByProjectId(Integer projectId) {
        if (projectId == null) {
            LOGGER.error(
                "class=ClusterLogicServiceImpl||method=getHasAuthClusterLogicsByProjectId||errMsg=获取有权限逻辑集群时projectId为null");
            return new ArrayList<>();
        }

        // 获取有权限的逻辑集群id
        return logicClusterAuthService.getAllLogicClusterAuths(projectId).stream()
            .map(ProjectClusterLogicAuth::getLogicClusterId).distinct().collect(Collectors.toList());
    }

    /**
     * 查询指定app有权限的逻辑集群（包括申请权限）
     *
     * @param projectId APP ID
     * @return 逻辑集群列表
     */
    @Override
    public List<ClusterLogic> getHasAuthClusterLogicsByProjectId(Integer projectId) {
        if (projectId == null) {
            LOGGER.error(
                "class=ClusterLogicServiceImpl||method=getHasAuthClusterLogicsByProjectId||errMsg=获取有权限逻辑集群时projectId为null");
            return new ArrayList<>();
        }

        // 获取有权限的逻辑集群id
        Set<Long> hasAuthLogicClusterIds = logicClusterAuthService.getAllLogicClusterAuths(projectId).stream()
            .map(ProjectClusterLogicAuth::getLogicClusterId).collect(Collectors.toSet());

        // 批量获取有权限的集群
        List<ClusterLogicPO> hasAuthLogicClusters = !hasAuthLogicClusterIds.isEmpty()
            ? logicClusterDAO.listByIds(hasAuthLogicClusterIds)
            : new ArrayList<>();

        // 获取作为owner的集群, 这里权限管控逻辑参看 getClusterLogicByProjectIdAndAuthType
        List<ClusterLogicPO> ownedLogicClusters;
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            ownedLogicClusters = logicClusterDAO.listAll();
        } else {
            ownedLogicClusters = logicClusterDAO.listByProjectId(projectId);
        }

        // 综合
        for (ClusterLogicPO ownedLogicCluster : ownedLogicClusters) {
            if (!hasAuthLogicClusterIds.contains(ownedLogicCluster.getId())) {
                hasAuthLogicClusters.add(ownedLogicCluster);
            }
        }
        return hasAuthLogicClusters.stream().map(this::clusterLogicPoProjectIdStrConvertClusterLogic)
                .flatMap(Collection::stream)
                .filter(clusterLogic -> filterClusterLogicByProjectId(clusterLogic,projectId))
                
                .collect(Collectors.toList());
    }

    @Override
    public Boolean isClusterLogicExists(Long resourceId) {
        return null != logicClusterDAO.getById(resourceId);
    }

    /**
     * 根据配置字符创获取配置，填充默认值
     *
     * @param configJson json
     * @return config
     */
    @Override
    public LogicResourceConfig genClusterLogicConfig(String configJson) {
        if (StringUtils.isBlank(configJson)) {
            return new LogicResourceConfig();
        }
        return JSON.parseObject(configJson, LogicResourceConfig.class);
    }

    @Override
    public Set<RoleClusterNodeSepc> getLogicDataNodeSepc(Long logicClusterId) {
        List<ClusterRoleInfo> clusterRoleInfos = getClusterLogicRole(logicClusterId);

        Set<RoleClusterNodeSepc> esRoleClusterDataNodeSepcs = new HashSet<>();

        if (CollectionUtils.isNotEmpty(clusterRoleInfos)) {
            for (ClusterRoleInfo clusterRoleInfo : clusterRoleInfos) {
                if (DATA_NODE.getDesc().equals(clusterRoleInfo.getRole())) {
                    RoleClusterNodeSepc roleClusterNodeSepc = new RoleClusterNodeSepc();
                    roleClusterNodeSepc.setRole(DATA_NODE.getDesc());
                    roleClusterNodeSepc.setSpec(clusterRoleInfo.getMachineSpec());

                    esRoleClusterDataNodeSepcs.add(roleClusterNodeSepc);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(esRoleClusterDataNodeSepcs)) {
            return esRoleClusterDataNodeSepcs;
        }

        List<ESMachineNormsPO> esMachineNormsPOS = esMachineNormsService.listMachineNorms();
        for (ESMachineNormsPO esMachineNormsPO : esMachineNormsPOS) {
            esRoleClusterDataNodeSepcs.add(ConvertUtil.obj2Obj(esMachineNormsPO, RoleClusterNodeSepc.class));
        }

        return esRoleClusterDataNodeSepcs;
    }

    @Override
    public List<ClusterRoleInfo> getClusterLogicRole(Long logicClusterId) {
        List<ClusterRoleInfo> clusterRoleInfos = new ArrayList<>();

        try {
            ClusterLogicPO clusterLogicPO = logicClusterDAO.getById(logicClusterId);

            List<String> phyClusterNames = clusterRegionService.listPhysicClusterNames(logicClusterId);
            if (CollectionUtils.isEmpty(phyClusterNames)) {
                return new ArrayList<>();
            }

            //拿第一个物理集群的client、master信息，因为只有Arius维护的大公共共享集群才会有一个逻辑集群映射成多个物理集群
            ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyClusterNames.get(0));
            if (null == clusterPhy) {
                return new ArrayList<>();
            }

            List<ClusterRoleInfo> esRolePhyClusters = clusterPhy.getClusterRoleInfos();
            List<ClusterRoleHost> esRolePhyClusterHosts = clusterPhy.getClusterRoleHosts();

            for (ClusterRoleInfo clusterRoleInfo : esRolePhyClusters) {

                List<ClusterRoleHost> clusterRoleHosts = new ArrayList<>();

                //如果是datanode节点，那么使用逻辑集群申请的节点个数和阶段规格配置
                if (DATA_NODE.getDesc().equals(clusterRoleInfo.getRoleClusterName())) {
                    setLogicClusterInfo(logicClusterId, clusterLogicPO, clusterRoleInfo, clusterRoleHosts);
                } else {
                    setPhyClusterInfo(esRolePhyClusterHosts, clusterRoleInfo, clusterRoleHosts);
                }

                clusterRoleInfo.setClusterRoleHosts(clusterRoleHosts);
                clusterRoleInfo.setPodNumber(clusterRoleHosts.size());
                clusterRoleInfos.add(clusterRoleInfo);
            }
        } catch (Exception e) {
            LOGGER.warn("class=ClusterLogicServiceImpl||method=acquireLogicClusterRole||logicClusterId={}",
                logicClusterId, e);
        }

        return clusterRoleInfos;
    }

    @Override
    public List<Plugin> getClusterLogicPlugins(Long logicClusterId) {
        List<String> clusterNameList = clusterRegionService.listPhysicClusterNames(logicClusterId);
        if (AriusObjUtils.isEmptyList(clusterNameList)) {
            return new ArrayList<>();
        }

        //逻辑集群对应的物理集群插件一致 取其中一个物理集群
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterNameList.get(0));
        List<PluginPO> pluginPOList = esPluginService.listClusterAndDefaultESPlugin(clusterPhy.getId().toString());

        if (AriusObjUtils.isEmptyList(pluginPOList)) {
            return new ArrayList<>();
        }

        List<ClusterPhy> clusterPhyList = clusterPhyService.listAllClusters();
        Map<String, ClusterPhy> name2ClusterPhyMap = ConvertUtil.list2Map(clusterPhyList, ClusterPhy::getCluster);

        Map<Long, Plugin> pluginMap = new HashMap<>(0);
        for (PluginPO pluginPO : pluginPOList) {
            Plugin logicalPlugin = ConvertUtil.obj2Obj(pluginPO, Plugin.class);
            logicalPlugin.setInstalled(Boolean.FALSE);
            pluginMap.put(pluginPO.getId(), logicalPlugin);
        }

        for (String clusterName : clusterNameList) {
            ClusterPhy cluster = name2ClusterPhyMap.get(clusterName);
            if (AriusObjUtils.isNull(cluster)) {
                continue;
            }
            List<Long> pluginIds = parsePluginIds(cluster.getPlugIds());
            for (Long pluginId : pluginIds) {
                Plugin logicalPlugin = pluginMap.get(pluginId);
                if (AriusObjUtils.isNull(logicalPlugin)) {
                    continue;
                }
                logicalPlugin.setInstalled(true);
            }
        }

        return new ArrayList<>(pluginMap.values());
    }



    @Override
    public Result<Long> addPlugin(Long logicClusterId, PluginDTO pluginDTO,
                                  String operator) throws NotFindSubclassException {

        if (null != logicClusterId) {
            List<Integer> clusterIdList = clusterRegionService.listPhysicClusterId(logicClusterId);
            if (AriusObjUtils.isEmptyList(clusterIdList)) {
                return Result.buildFail("对应物理集群不存在");
            }

            String clusterIds = ListUtils.intList2String(clusterIdList);
            pluginDTO.setPhysicClusterId(clusterIds);
        }
        return esPluginService.addESPlugin(pluginDTO);
    }

    @Override
    public Result<Void> transferClusterLogic(Long clusterLogicId, Integer targetProjectId, String submitor) {

        ESLogicClusterDTO esLogicClusterDTO = new ESLogicClusterDTO();
        esLogicClusterDTO.setId(clusterLogicId);
        esLogicClusterDTO.setProjectId(targetProjectId);
        return editClusterLogicNotCheck(esLogicClusterDTO, submitor);
    }

    @Override
    public List<ClusterLogic> pagingGetClusterLogicByCondition(ClusterLogicConditionDTO param) {
        List<ClusterLogicPO> clusters = Lists.newArrayList();
        try {
            clusters = logicClusterDAO.pagingByCondition(param);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("class=ClusterPhyServiceImpl||method=pagingGetClusterPhyByCondition||msg={}", e.getMessage(),
                e);
        }
      return clusters.stream().map(this::clusterLogicPoProjectIdStrConvertClusterLogic).flatMap(Collection::stream)
              .filter(clusterLogic -> filterClusterLogicByProjectId(clusterLogic,param.getProjectId()))
              .collect(Collectors.toList());
    }

    @Override
    public Long fuzzyClusterLogicHitByCondition(ClusterLogicConditionDTO param) {
        return logicClusterDAO.getTotalHitByCondition(param);
    }

    @Override
    public List<ClusterLogic> getClusterLogicListByIds(List<Long> clusterLogicIdList) {
        return logicClusterDAO.listByIds(new HashSet<>(clusterLogicIdList)).stream()
                .map(this::clusterLogicPoProjectIdStrConvertClusterLogic).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClusterLogic> listClusterLogicByProjectIdAndName(Integer projectId, String clusterName) {
        return logicClusterDAO.listByNameAndProjectId(clusterName, projectId).stream()
                .map(this::clusterLogicPoProjectIdStrConvertClusterLogic)
                .flatMap(Collection::stream)
                .filter(clusterLogic -> filterClusterLogicByProjectId(clusterLogic,projectId))
                .collect(Collectors.toList());
    }
    
    /**
     * @param level
     * @return
     */
    @Override
    public List<ClusterLogic> listLogicClustersByLevelThatProjectIdStrConvertProjectIdList(Integer level) {
         return logicClusterDAO.listByLevel(level)
                 .stream()
                 .map(this::clusterLogicPoProjectIdStrConvertClusterLogic)
                .flatMap(Collection::stream)
                 .collect(Collectors.toList());
    }
    /***************************************** private method ****************************************************/
    /**
     * Check逻辑集群参数
     *
     * @param param     逻辑集群
     * @param operation 操作类型
     * @param projectId 项目id
     * @return  Result
     */
    private Result<Void> checkLogicClusterParams(ESLogicClusterDTO param, OperationEnum operation, Integer projectId) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("逻辑集群信息为空");
        }
        if (ADD_BIND_MULTIPLE_PROJECT.equals(operation)) {
            if (!existClusterLogicById(param.getId())) {
                return Result.buildFail("逻辑集群不存在");
            }
            return Result.buildSucc();
        }
        Result<Void> isIllegalResult = isIllegal(param);
        if (isIllegalResult.failed()) {
            return isIllegalResult;
        }

        if (ADD.equals(operation)) {
            Result<Void> isFieldNullResult = addVoidResult(param);
            if (isFieldNullResult != null) {
                return isFieldNullResult;
            }
        } else if (EDIT.equals(operation)) {
            Result<Void> editVoidResult = editVoidResult(param, projectId);
            if (editVoidResult != null) {
                return editVoidResult;
            }
        }

        return Result.buildSucc();
    }

    @Nullable
    private Result<Void> addVoidResult(ESLogicClusterDTO param) {
        Result<Void> isFieldNullResult = isFieldNull(param);
        if (isFieldNullResult.failed()) {
            return isFieldNullResult;
        }
        //逻辑集群绑定多个项目
        ClusterLogicPO logicPO = logicClusterDAO.getByName(param.getName());
        if (!AriusObjUtils.isNull(logicPO)) {
            return Result.buildDuplicate("逻辑集群重复");
        }
        return null;
    }

    @Nullable
    private Result<Void> editVoidResult(ESLogicClusterDTO param, Integer projectId) {
        if (AriusObjUtils.isNull(param.getId())) {
            return Result.buildParamIllegal("逻辑集群ID为空");
        }

        ClusterLogicPO oldPO = logicClusterDAO.getById(param.getId());
        if (oldPO == null) {
            return Result.buildNotExist("逻辑集群不存在");
        }
        //当param中projectid存在
        if (Objects.nonNull(param.getProjectId())) {
            final boolean failed = str2ListProjectIds(oldPO).stream()
                    .map(pid -> ProjectUtils.checkProjectCorrectly(a -> a, pid, param.getProjectId()))
                
                    .allMatch(Result::failed);
            if (failed) {
                return Result.buildFail("当前项目不属于超级项目或者持有该操作的项目");
            }
        
        } else {
            //校验路径
            final boolean failed = str2ListProjectIds(oldPO).stream()
                    .map(pid -> ProjectUtils.checkProjectCorrectly(a -> a, pid, projectId))
                
                    .allMatch(Result::failed);
        
            if (failed) {
                return Result.buildFail("当前项目不属于超级项目或者持有该操作的项目");
            }
        }
        return null;
    }
    
    @NotNull
    private static List<Integer> str2ListProjectIds(ClusterLogicPO oldPO) {
        return Arrays.stream(StringUtils.split(oldPO.getProjectId(), ","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
    
    private Result<Void> isFieldNull(ESLogicClusterDTO param) {
        if (AriusObjUtils.isNull(param.getName())) {
            return Result.buildParamIllegal("集群名字为空");
        }
        if (AriusObjUtils.isNull(param.getType())) {
            return Result.buildParamIllegal("类型为空");
        }
        if (AriusObjUtils.isNull(param.getProjectId())) {
            return Result.buildParamIllegal("应用ID为空");
        }
        return Result.buildSucc();
    }

    private Result<Void> isIllegal(ESLogicClusterDTO param) {
        ClusterResourceTypeEnum typeEnum = ClusterResourceTypeEnum.valueOf(param.getType());
        if (ClusterResourceTypeEnum.UNKNOWN.equals(typeEnum)) {
            return Result.buildParamIllegal("新建逻辑集群提交内容中集群类型非法");
        }

        if (param.getProjectId() != null && !projectService.checkProjectExist(param.getProjectId())) {
            return Result.buildParamIllegal("应用ID非法");
        }
        return Result.buildSucc();
    }

    /**
     * 解析插件ID列表
     *
     * @param pluginIdsStr 插件ID格式化字符串
     * @return List<Long>
     */
    private List<Long> parsePluginIds(String pluginIdsStr) {
        List<Long> pluginIds = new ArrayList<>();
        if (StringUtils.isNotBlank(pluginIdsStr)) {
            String[] arr = StringUtils.split(pluginIdsStr, ",");
            for (String s : arr) {
                pluginIds.add(Long.parseLong(s));
            }
        }
        return pluginIds;
    }

    private void initLogicCluster(ESLogicClusterDTO param) {

        if (AriusObjUtils.isNull(param.getConfigJson())) {
            param.setConfigJson("");
        }

        if (!AriusObjUtils.isNull(param.getDataNodeNum())) {
            param.setQuota((double) param.getDataNodeNum());
        }

        if (AriusObjUtils.isNull(param.getDataCenter())) {
            param.setDataCenter(EnvUtil.getDC().getCode());
        }

        if (AriusObjUtils.isNull(param.getLevel())) {
            param.setLevel(1);
        }

        if (AriusObjUtils.isNull(param.getMemo())) {
            param.setMemo("");
        }

        if (AriusObjUtils.isNull(param.getQuota())) {
            param.setQuota(0.00);
        }

        if (null == param.getHealth()) {
            param.setHealth(DEFAULT_CLUSTER_HEALTH);
        }
    }

    private void setPhyClusterInfo(List<ClusterRoleHost> esRolePhyClusterHosts, ClusterRoleInfo clusterRoleInfo,
                                   List<ClusterRoleHost> clusterRoleHosts) {
        for (ClusterRoleHost clusterRoleHost : esRolePhyClusterHosts) {
            if (clusterRoleHost.getRoleClusterId().longValue() == clusterRoleInfo.getId().longValue()) {
                clusterRoleHosts.add(ConvertUtil.obj2Obj(clusterRoleHost, ClusterRoleHost.class));
            }
        }
    }

    private void setLogicClusterInfo(Long logicClusterId, ClusterLogicPO clusterLogicPO,
                                     ClusterRoleInfo clusterRoleInfo, List<ClusterRoleHost> clusterRoleHosts) {
        clusterRoleInfo.setPodNumber(clusterLogicPO.getDataNodeNum());
        clusterRoleInfo.setMachineSpec(clusterLogicPO.getDataNodeSpec());

        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(logicClusterId);
        if (null == clusterRegion) {
            return;
        }

        Result<List<ClusterRoleHost>> ret = clusterRoleHostService.listByRegionId(clusterRegion.getId().intValue());
        if (ret.failed() || CollectionUtils.isEmpty(ret.getData())) {
            LOGGER.error("class=ClusterLogicServiceImpl||method=setLogicClusterInfo||errMsg={}", ret.getMessage());
            return;
        }

        for (ClusterRoleHost clusterHost : ret.getData()) {
            ClusterRoleHost clusterRoleHost = new ClusterRoleHost();
            clusterRoleHost.setHostname(clusterHost.getHostname());
            clusterRoleHost.setRole(DATA_NODE.getCode());

            clusterRoleHosts.add(clusterRoleHost);
        }
    }
    
    /**
     * 逻辑集群po中的projectId转换为int类型的ClusterLogic
     *
     * @param clusterLogicPO 集群逻辑po
     * @return {@code List<ClusterLogic>}
     */
    public List<ClusterLogic> clusterLogicPoProjectIdStrConvertClusterLogic(ClusterLogicPO clusterLogicPO) {
        if (clusterLogicPO==null){
            return Collections.emptyList();
        }
        if (!StringUtils.contains(clusterLogicPO.getProjectId(), ",")) {
            return Collections.singletonList(ConvertUtil.obj2Obj(clusterLogicPO, ClusterLogic.class,
                    clusterLogic -> clusterLogic.setProjectId(Integer.parseInt(clusterLogicPO.getProjectId()))
            
            ));
        } else {
            return str2ListProjectIds(clusterLogicPO).stream()
                    .map(projectId -> ConvertUtil.obj2Obj(clusterLogicPO, ClusterLogic.class,
                            clusterLogic -> clusterLogic.setProjectId(projectId))).distinct()
                    .collect(Collectors.toList());
        }
    }
    private boolean filterClusterLogicByProjectId(ClusterLogic clusterLogic, Integer projectId){
        return Objects.isNull(projectId) || Objects.equals(clusterLogic.getProjectId(), projectId);
    }
}
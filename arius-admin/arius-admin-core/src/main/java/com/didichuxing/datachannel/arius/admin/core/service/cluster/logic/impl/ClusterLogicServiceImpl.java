package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.DEFAULT_CLUSTER_HEALTH;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static java.util.stream.Collectors.toList;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.LogicResourceConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Plugin;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterNodeSepc;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterLogicDiskUsedInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterLogicPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPhyPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESClusterRoleHostPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsNodeInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESClusterRoleHostDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.LogicClusterDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author d06679
 * @date 2019/3/25
 */
@Service
public class ClusterLogicServiceImpl implements ClusterLogicService {

    private static final ILog          LOGGER = LogFactory.getLog(ClusterLogicServiceImpl.class);

    @Autowired
    private LogicClusterDAO            logicClusterDAO;

    @Autowired
    private ProjectClusterLogicAuthService logicClusterAuthService;

    @Autowired
    private ProjectService projectService;
    @Autowired
    private RoleTool roleTool;



    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private ESPluginService            esPluginService;

    @Autowired
    private ClusterPhyService          clusterPhyService;

    @Autowired
    private ESMachineNormsService      esMachineNormsService;

    @Autowired
    private ClusterRegionService       clusterRegionService;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private AriusStatsNodeInfoESDAO ariusStatsNodeInfoESDAO;

    @Autowired
    private ESClusterRoleHostDAO clusterRoleHostDAO ;

    /**
     * 条件查询逻辑集群
     *
     * @param param 条件
     * @return 逻辑集群列表
     */
    @Override
    public List<ClusterLogic> listClusterLogics(ESLogicClusterDTO param) {
        return ConvertUtil.list2List(
            logicClusterDAO.listByCondition(ConvertUtil.obj2Obj(param, ClusterLogicPO.class)),
                ClusterLogic.class);
    }

    /**
     * 获取所有逻辑集群
     *
     * @return 逻辑集群列表
     */
    @Override
    public List<ClusterLogic> listAllClusterLogics() {
        return ConvertUtil.list2List(logicClusterDAO.listAll(), ClusterLogic.class);
    }

    /**
     * 删除逻辑集群
     *
     * @param logicClusterId 资源id
     * @param operator       操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteClusterLogicById(Long logicClusterId, String operator) throws AdminOperateException {
        ClusterLogicPO logicCluster = logicClusterDAO.getById(logicClusterId);
        if (logicCluster == null) {
            return Result.buildNotExist("逻辑集群不存在");
        }

        if (hasLogicClusterWithTemplates(logicClusterId)) {
            return Result.build(ResultType.IN_USE_ERROR.getCode(), "逻辑集群使用中");
        }

        boolean succeed = (logicClusterDAO.delete(logicClusterId) > 0);
        if (!succeed) {
            throw new AdminOperateException("删除逻辑集群失败");
        }

        return Result.buildSucc();
    }

    @Override
    public Boolean hasLogicClusterWithTemplates(Long logicClusterId) {
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(logicClusterId);
        if (null == clusterRegion) { return false;}

        Result<List<IndexTemplatePhy>> ret = indexTemplatePhyService.listByRegionId(clusterRegion.getId().intValue());
        return ret.success() && CollectionUtils.isNotEmpty(ret.getData());
    }

    /**
     * 新建逻辑集群
     *
     * @param param    参数
     * @return result
     */
    @Override
    public Result<Long> createClusterLogic(ESLogicClusterDTO param) {
        Result<Void> checkResult = validateClusterLogicParams(param, ADD);
        if (checkResult.failed()) {
            LOGGER.warn("class=ClusterLogicServiceImpl||method=createClusterLogic||msg={}", checkResult.getMessage());
            return Result.buildFrom(checkResult);
        }

        initLogicCluster(param);

        ClusterLogicPO logicPO = ConvertUtil.obj2Obj(param, ClusterLogicPO.class);
        boolean succeed = logicClusterDAO.insert(logicPO) == 1;
        return Result.build(succeed, logicPO.getId());
    }

    /**
     * 验证逻辑集群是否合法
     *
     * @param param     参数
     * @param operation 操作
     * @return result
     */
    @Override
    public Result<Void> validateClusterLogicParams(ESLogicClusterDTO param, OperationEnum operation) {
        return checkLogicClusterParams(param, operation);
    }

    @Override
    public Result<Void> editClusterLogic(ESLogicClusterDTO param, String operator) {
        Result<Void> checkResult = validateClusterLogicParams(param, EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=ClusterLogicServiceImpl||method=editResource||msg={}", checkResult.getMessage());
            return checkResult;
        }

        return editClusterLogicNotCheck(param, operator);
    }

    @Override
    public Result<Void> editClusterLogicNotCheck(ESLogicClusterDTO param, String operator) {
        ClusterLogicPO paramPO = ConvertUtil.obj2Obj(param, ClusterLogicPO.class);
        boolean succ = (1 == logicClusterDAO.update(paramPO));

        return Result.build(succ);
    }

    @Override
    public ClusterLogic getClusterLogicById(Long logicClusterId) {
        return ConvertUtil.obj2Obj(logicClusterDAO.getById(logicClusterId), ClusterLogic.class);
    }

    @Override
    public ClusterLogic getClusterLogicByName(String logicClusterName) {
        return ConvertUtil.obj2Obj(logicClusterDAO.getByName(logicClusterName), ClusterLogic.class);
    }

    /**
     * 查询指定逻辑集群的配置
     *
     * @param logicClusterId 逻辑集群id
     * @return 逻辑集群 不存在返回null
     */
    @Override
    public LogicResourceConfig getClusterLogicConfigById(Long logicClusterId) {
        ClusterLogic clusterLogic = getClusterLogicById(logicClusterId);
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
        return ConvertUtil.list2List(logicClusterDAO.listByProjectId(projectId), ClusterLogic.class);
    }

    @Override
    public List<Long> getHasAuthClusterLogicIdsByProjectId(Integer projectId){
        if (projectId == null) {
            LOGGER.error(
                    "class=ClusterLogicServiceImpl||method=getHasAuthClusterLogicsByProjectId||errMsg=获取有权限逻辑集群时projectId为null");
            return new ArrayList<>();
        }

        // 获取有权限的逻辑集群id
        Set<Long> hasAuthLogicClusterIds = logicClusterAuthService.getAllLogicClusterAuths(projectId).stream()
                .map(ProjectClusterLogicAuth::getLogicClusterId).collect(Collectors.toSet());

        return new ArrayList<>(hasAuthLogicClusterIds);
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

        return ConvertUtil.list2List(hasAuthLogicClusters, ClusterLogic.class);
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
    public Result<Long> addPlugin(Long logicClusterId, PluginDTO pluginDTO, String operator) {

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
    public Result<Void> transferClusterLogic(Long clusterLogicId, Integer targetProjectId, String targetResponsible,
                                             String submitor) {

        ESLogicClusterDTO esLogicClusterDTO = new ESLogicClusterDTO();
        esLogicClusterDTO.setId(clusterLogicId);
        esLogicClusterDTO.setProjectId(targetProjectId);
        esLogicClusterDTO.setResponsible(targetResponsible);
        return editClusterLogicNotCheck(esLogicClusterDTO, submitor);
    }

    @Override
    public List<ClusterLogic> pagingGetClusterLogicByCondition(ClusterLogicConditionDTO param) {
        String sortTerm = null == param.getSortTerm() ? SortConstant.ID : param.getSortTerm();
        String sortType = param.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
        param.setSortTerm(sortTerm);
        param.setSortType(sortType);
        param.setFrom((param.getPage() - 1) * param.getSize());
        List<ClusterLogicPO> clusters = Lists.newArrayList();
        try {
            clusters = logicClusterDAO.pagingByCondition(param);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("class=ClusterPhyServiceImpl||method=pagingGetClusterPhyByCondition||msg={}", e.getMessage(), e);
        }
        return ConvertUtil.list2List(clusters, ClusterLogic.class);
    }

    @Override
    public Long fuzzyClusterLogicHitByCondition(ClusterLogicConditionDTO param) {
        return logicClusterDAO.getTotalHitByCondition(ConvertUtil.obj2Obj(param, ClusterLogicPO.class));
    }

    @Override
    public List<ClusterLogic> getClusterLogicListByIds(List<Long> clusterLogicIdList) {
        return ConvertUtil.list2List(logicClusterDAO.listByIds(new HashSet<>(clusterLogicIdList)), ClusterLogic.class);
    }

    @Override
    public ClusterLogicDiskUsedInfoPO getDiskInfo(Long id) {
        ClusterRegion clusterRegion =clusterRegionService.getRegionByLogicClusterId(id);
        List<ESClusterRoleHostPO> esClusterRoleHostPOS = clusterRoleHostDAO.listByRegionId(Math.toIntExact(clusterRegion.getId()));
        //节点名称列表
        List<String> nodeList = esClusterRoleHostPOS.stream().map(ESClusterRoleHostPO::getNodeSet).collect(toList());
        String clusterName =clusterRegion.getPhyClusterName();
        return ariusStatsNodeInfoESDAO.getClusterLogicDiskUsedInfo(clusterName, nodeList);
    }

    /***************************************** private method ****************************************************/
    /**
     * Check逻辑集群参数
     *
     * @param param     逻辑集群
     * @param operation 操作类型
     * @return
     */
    private Result<Void> checkLogicClusterParams(ESLogicClusterDTO param, OperationEnum operation) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("逻辑集群信息为空");
        }

        Result<Void> isIllegalResult = isIllegal(param);
        if (isIllegalResult.failed()) {
            return isIllegalResult;
        }

        if (ADD.equals(operation)) {
            Result<Void> isFieldNullResult = isFieldNull(param);
            if (isFieldNullResult.failed()) {
                return isFieldNullResult;
            }

            ClusterLogicPO logicPO = logicClusterDAO.getByName(param.getName());
            if (!AriusObjUtils.isNull(logicPO)) {
                return Result.buildDuplicate("逻辑集群重复");
            }
        } else if (EDIT.equals(operation)) {
            if (AriusObjUtils.isNull(param.getId())) {
                return Result.buildParamIllegal("逻辑集群ID为空");
            }

            ClusterLogicPO oldPO = logicClusterDAO.getById(param.getId());
            if (oldPO == null) {
                return Result.buildNotExist("逻辑集群不存在");
            }
        }

        return Result.buildSucc();
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

        if (AriusObjUtils.isNull(param.getResponsible())) {
            return Result.buildParamIllegal("责任人为空");
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
        final ProjectVO projectVO = projectService.getProjectDetailByProjectId(param.getProjectId());
        if (!(roleTool.isAdmin(param.getResponsible())||CommonUtils.isUserNameBelongProjectMember(param.getResponsible(), projectVO)
            || CommonUtils.isUserNameBelongProjectResponsible(param.getResponsible(), projectVO))) {
            return Result.buildParamIllegal("责任人非法");
        }
        return Result.buildSucc();
    }

    /**
     * 解析插件ID列表
     *
     * @param pluginIdsStr 插件ID格式化字符串
     * @return
     */
    private List<Long> parsePluginIds(String pluginIdsStr) {
        List<Long> pluginIds = new ArrayList<>();
        if (StringUtils.isNotBlank(pluginIdsStr)) {
            String[] arr = StringUtils.split(pluginIdsStr, ",");
            for (int i = 0; i < arr.length; ++i) {
                pluginIds.add(Long.parseLong(arr[i]));
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

    private void setLogicClusterInfo(Long logicClusterId, ClusterLogicPO clusterLogicPO, ClusterRoleInfo clusterRoleInfo,
                                     List<ClusterRoleHost> clusterRoleHosts) {
        clusterRoleInfo.setPodNumber(clusterLogicPO.getDataNodeNu());
        clusterRoleInfo.setMachineSpec(clusterLogicPO.getDataNodeSpec());

        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(logicClusterId);
        if (null == clusterRegion) { return;}

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
}
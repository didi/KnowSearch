package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Plugin;
import com.didichuxing.datachannel.arius.admin.common.bean.common.LogicResourceConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHostInfo;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicWithRack;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterNodeSepc;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterLogicPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.ResponsibleConvertTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.employee.EmployeeService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.LogicClusterDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.DEFAULT_CLUSTER_HEALTH;

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
    private AppClusterLogicAuthService logicClusterAuthService;

    @Autowired
    private RegionRackService          rackService;

    @Autowired
    private AppService                 appService;

    @Autowired
    private EmployeeService            employeeService;

    @Autowired
    private ResponsibleConvertTool     responsibleConvertTool;

    @Autowired
    private TemplatePhyService         templatePhyService;

    @Autowired
    private ESPluginService            esPluginService;

    @Autowired
    private ClusterPhyService          clusterPhyService;

    @Autowired
    private ClusterLogicNodeService    clusterLogicNodeService;

    @Autowired
    private ESMachineNormsService      esMachineNormsService;

    @Autowired
    private RegionRackService          regionRackService;

    /**
     * 条件查询逻辑集群
     *
     * @param param 条件
     * @return 逻辑集群列表
     */
    @Override
    public List<ClusterLogic> listClusterLogics(ESLogicClusterDTO param) {
        return responsibleConvertTool.list2List(
            logicClusterDAO.listByCondition(responsibleConvertTool.obj2Obj(param, ClusterLogicPO.class)),
            ClusterLogic.class);
    }

    /**
     * 获取所有逻辑集群
     *
     * @return 逻辑集群列表
     */
    @Override
    public List<ClusterLogic> listAllClusterLogics() {
        return responsibleConvertTool.list2List(logicClusterDAO.listAll(), ClusterLogic.class);
    }

    /**
     * 获取所有资源
     *
     * @return
     */
    @Override
    public List<ClusterLogicWithRack> listAllClusterLogicsWithRackInfo() {

        // 所有逻辑集群rack信息
        List<ClusterLogicRackInfo> allLogicClusterRackInfos = regionRackService.listAllLogicClusterRacks();

        // 逻辑集群ID到逻辑集群rack信息的Multimap
        Multimap<Long, ClusterLogicRackInfo> logicClusterId2RackInfoMap = ArrayListMultimap.create();
        for (ClusterLogicRackInfo param : allLogicClusterRackInfos) {
            List<Long> logicClusterIds = ListUtils.string2LongList(param.getLogicClusterIds());
            logicClusterIds.forEach(logicClusterId -> logicClusterId2RackInfoMap.put(logicClusterId, param));
        }

        List<ClusterLogicPO> logicClusters = logicClusterDAO.listAll();
        List<ClusterLogicWithRack> logicClustersWithRackInfo = Lists.newArrayList();
        for (ClusterLogicPO logicCluster : logicClusters) {
            ClusterLogicWithRack logicClusterWithRackInfo = responsibleConvertTool.obj2Obj(logicCluster,
                ClusterLogicWithRack.class);
            logicClusterWithRackInfo.setItems(logicClusterId2RackInfoMap.get(logicCluster.getId()));
            logicClustersWithRackInfo.add(logicClusterWithRackInfo);
        }

        return logicClustersWithRackInfo;
    }

    /**
     * 获取逻辑集群，返回结果里包含逻辑集群拥有的rack信息
     *
     * @return
     */
    @Override
    public ClusterLogicWithRack getClusterLogicWithRackInfoById(Long logicClusterId) {
        // 所有逻辑集群rack信息
        List<ClusterLogicRackInfo> allLogicClusterRackInfos = regionRackService.listAllLogicClusterRacks();

        // 逻辑集群ID到逻辑集群rack信息的Multimap
        Multimap<Long, ClusterLogicRackInfo> logicClusterId2RackInfoMap = ArrayListMultimap.create();
        for (ClusterLogicRackInfo param : allLogicClusterRackInfos) {
            List<Long> logicClusterIds = ListUtils.string2LongList(param.getLogicClusterIds());
            logicClusterIds.forEach(logicId -> logicClusterId2RackInfoMap.put(logicId, param));
        }

        ClusterLogicPO clusterLogicPO = logicClusterDAO.getById(logicClusterId);

        ClusterLogicWithRack logicClusterWithRackInfo = responsibleConvertTool.obj2Obj(clusterLogicPO,
            ClusterLogicWithRack.class);
        logicClusterWithRackInfo.setItems(logicClusterId2RackInfoMap.get(clusterLogicPO.getId()));

        return logicClusterWithRackInfo;
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

        List<ClusterLogicRackInfo> racks = rackService.listLogicClusterRacks(logicClusterId);
        if (CollectionUtils.isEmpty(racks)) {
            LOGGER.info("class=ClusterLogicServiceImpl||method=delResource||resourceId={}||msg=delResource no items!",
                logicClusterId);
        } else {
            LOGGER.info(
                "class=ClusterLogicServiceImpl||method=delResource||resourceId={}||itemSize={}||msg=delResource has items!",
                logicClusterId, racks.size());

            for (ClusterLogicRackInfo item : racks) {
                rackService.deleteRackById(item.getId());
            }
        }

        boolean succeed = (logicClusterDAO.delete(logicClusterId) > 0);
        if (!succeed) {
            throw new AdminOperateException("删除逻辑集群失败");
        }

        return Result.buildSucc();
    }

    @Override
    public Boolean hasLogicClusterWithTemplates(Long logicClusterId) {
        List<ClusterLogicRackInfo> clusterLogicRackInfos = regionRackService.listLogicClusterRacks(logicClusterId);
        if (CollectionUtils.isEmpty(clusterLogicRackInfos)) {
            return false;
        }

        // 获取逻辑逻辑集群内的所有rack, 按着cluster分组
        Multimap<String, String> clusterRackMultiMap = ConvertUtil.list2MulMap(clusterLogicRackInfos,
            ClusterLogicRackInfo::getPhyClusterName, ClusterLogicRackInfo::getRack);

        for (Map.Entry<String, Collection<String>> entry : clusterRackMultiMap.asMap().entrySet()) {
            String cluster = entry.getKey();
            Collection<String> racks = entry.getValue();

            List<IndexTemplatePhy> templatePhysicals = templatePhyService.getNormalTemplateByCluster(cluster);
            for (IndexTemplatePhy physical : templatePhysicals) {
                if (RackUtils.hasIntersect(physical.getRack(), racks)) {
                    return true;
                }
            }
        }

        return false;
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

        ClusterLogicPO logicPO = responsibleConvertTool.obj2Obj(param, ClusterLogicPO.class);
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
        ClusterLogicPO paramPO = responsibleConvertTool.obj2Obj(param, ClusterLogicPO.class);
        boolean succ = (1 == logicClusterDAO.update(paramPO));

        return Result.build(succ);
    }

    @Override
    public ClusterLogic getClusterLogicById(Long logicClusterId) {
        return responsibleConvertTool.obj2Obj(logicClusterDAO.getById(logicClusterId), ClusterLogic.class);
    }

    @Override
    public ClusterLogic getClusterLogicByName(String logicClusterName) {
        return responsibleConvertTool.obj2Obj(logicClusterDAO.getByName(logicClusterName), ClusterLogic.class);
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
     * 查询指定app所创建的逻辑集群
     *
     * @param appId APPID
     * @return list
     */
    @Override
    public List<ClusterLogic> getOwnedClusterLogicListByAppId(Integer appId) {
        return responsibleConvertTool.list2List(logicClusterDAO.listByAppId(appId), ClusterLogic.class);
    }

    @Override
    public List<Long> getHasAuthClusterLogicIdsByAppId(Integer appId){
        if (appId == null) {
            LOGGER.error(
                    "class=ClusterLogicServiceImpl||method=getHasAuthClusterLogicsByAppId||errMsg=获取有权限逻辑集群时appId为null");
            return new ArrayList<>();
        }

        // 获取有权限的逻辑集群id
        Set<Long> hasAuthLogicClusterIds = logicClusterAuthService.getAllLogicClusterAuths(appId).stream()
                .map(AppClusterLogicAuth::getLogicClusterId).collect(Collectors.toSet());

        return new ArrayList<>(hasAuthLogicClusterIds);
    }

    /**
     * 查询指定app有权限的逻辑集群（包括申请权限）
     *
     * @param appId APP ID
     * @return 逻辑集群列表
     */
    @Override
    public List<ClusterLogic> getHasAuthClusterLogicsByAppId(Integer appId) {
        if (appId == null) {
            LOGGER.error(
                "class=ClusterLogicServiceImpl||method=getHasAuthClusterLogicsByAppId||errMsg=获取有权限逻辑集群时appId为null");
            return new ArrayList<>();
        }

        // 获取有权限的逻辑集群id
        Set<Long> hasAuthLogicClusterIds = logicClusterAuthService.getAllLogicClusterAuths(appId).stream()
            .map(AppClusterLogicAuth::getLogicClusterId).collect(Collectors.toSet());

        // 批量获取有权限的集群
        List<ClusterLogicPO> hasAuthLogicClusters = !hasAuthLogicClusterIds.isEmpty()
            ? logicClusterDAO.listByIds(hasAuthLogicClusterIds)
            : new ArrayList<>();

        // 获取作为owner的集群, 这里权限管控逻辑参看 getClusterLogicByAppIdAndAuthType
        List<ClusterLogicPO> ownedLogicClusters;
        if (appService.isSuperApp(appId)) {
            ownedLogicClusters = logicClusterDAO.listAll();
        } else {
            ownedLogicClusters = logicClusterDAO.listByAppId(appId);
        }

        // 综合
        for (ClusterLogicPO ownedLogicCluster : ownedLogicClusters) {
            if (!hasAuthLogicClusterIds.contains(ownedLogicCluster.getId())) {
                hasAuthLogicClusters.add(ownedLogicCluster);
            }
        }

        return responsibleConvertTool.list2List(hasAuthLogicClusters, ClusterLogic.class);
    }

    @Override
    public Boolean isClusterLogicExists(Long resourceId) {
        return null != logicClusterDAO.getById(resourceId);
    }

    /**
     * 获取rack匹配到的逻辑集群
     *
     * @param cluster 集群
     * @param racks   rack
     * @return count
     */
    @Override
    public ClusterLogic getClusterLogicByRack(String cluster, String racks) {

        List<ClusterLogicRackInfo> logicClusterRackInfos = regionRackService.listAssignedRacksByClusterName(cluster);

        if (CollectionUtils.isEmpty(logicClusterRackInfos)) {
            return null;
        }

        // 获取逻辑逻辑集群内的所有rack, 按着resourceId分组
        Multimap<Long, String> logicClusterId2RackMultiMap = ArrayListMultimap.create();
        for (ClusterLogicRackInfo param : logicClusterRackInfos) {
            List<Long> logicClusterIds = ListUtils.string2LongList(param.getLogicClusterIds());
            logicClusterIds.forEach(logicClusterId -> logicClusterId2RackMultiMap.put(logicClusterId, param.getRack()));
        }

        // 遍历逻辑集群，获取与给定的racks有交集的逻辑集群
        for (Map.Entry<Long, Collection<String>> entry : logicClusterId2RackMultiMap.asMap().entrySet()) {
            if (RackUtils.hasIntersect(racks, entry.getValue())) {
                return getClusterLogicById(entry.getKey());
            }
        }

        return null;
    }

    /**
     * 根据责任人查询
     *
     * @param responsibleId 责任人id
     * @return list
     */
    @Override
    public List<ClusterLogic> getLogicClusterByOwnerId(Long responsibleId) {
        return responsibleConvertTool.list2List(logicClusterDAO.listByResponsible(String.valueOf(responsibleId)),
            ClusterLogic.class);
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
        List<RoleCluster> roleClusters = getClusterLogicRole(logicClusterId);

        Set<RoleClusterNodeSepc> esRoleClusterDataNodeSepcs = new HashSet<>();

        if (CollectionUtils.isNotEmpty(roleClusters)) {
            for (RoleCluster roleCluster : roleClusters) {
                if (DATA_NODE.getDesc().equals(roleCluster.getRole())) {
                    RoleClusterNodeSepc roleClusterNodeSepc = new RoleClusterNodeSepc();
                    roleClusterNodeSepc.setRole(DATA_NODE.getDesc());
                    roleClusterNodeSepc.setSpec(roleCluster.getMachineSpec());

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
    public List<RoleCluster> getClusterLogicRole(Long logicClusterId) {
        List<RoleCluster> roleClusters = new ArrayList<>();

        try {
            ClusterLogicPO clusterLogicPO = logicClusterDAO.getById(logicClusterId);

            List<String> phyClusterNames = rackService.listPhysicClusterNames(logicClusterId);
            if (CollectionUtils.isEmpty(phyClusterNames)) {
                return new ArrayList<>();
            }

            //拿第一个物理集群的client、master信息，因为只有Arius维护的大公共共享集群才会有一个逻辑集群映射成多个物理集群
            ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyClusterNames.get(0));
            if (null == clusterPhy) {
                return new ArrayList<>();
            }

            List<RoleCluster> esRolePhyClusters = clusterPhy.getRoleClusters();
            List<ClusterRoleHostInfo> esRolePhyClusterHosts = clusterPhy.getClusterRoleHostInfos();

            for (RoleCluster roleCluster : esRolePhyClusters) {

                List<ClusterRoleHostInfo> clusterRoleHostInfos = new ArrayList<>();

                //如果是datanode节点，那么使用逻辑集群申请的节点个数和阶段规格配置
                if (DATA_NODE.getDesc().equals(roleCluster.getRoleClusterName())) {
                    setLogicClusterService(logicClusterId, clusterLogicPO, roleCluster, clusterRoleHostInfos);
                } else {
                    setPhyClusterService(esRolePhyClusterHosts, roleCluster, clusterRoleHostInfos);
                }

                roleCluster.setClusterRoleHostInfos(clusterRoleHostInfos);
                roleCluster.setPodNumber(clusterRoleHostInfos.size());
                roleClusters.add(roleCluster);
            }
        } catch (Exception e) {
            LOGGER.warn("class=ClusterLogicServiceImpl||method=acquireLogicClusterRole||logicClusterId={}",
                logicClusterId, e);
        }

        return roleClusters;
    }

    @Override
    public List<Plugin> getClusterLogicPlugins(Long logicClusterId) {
        List<String> clusterNameList = rackService.listPhysicClusterNames(logicClusterId);
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
            List<Integer> clusterIdList = rackService.listPhysicClusterId(logicClusterId);
            if (AriusObjUtils.isEmptyList(clusterIdList)) {
                return Result.buildFail("对应物理集群不存在");
            }

            String clusterIds = ListUtils.intList2String(clusterIdList);
            pluginDTO.setPhysicClusterId(clusterIds);
        }
        return esPluginService.addESPlugin(pluginDTO);
    }

    @Override
    public Result<Void> transferClusterLogic(Long clusterLogicId, Integer targetAppId, String targetResponsible,
                                             String submitor) {

        ESLogicClusterDTO esLogicClusterDTO = new ESLogicClusterDTO();
        esLogicClusterDTO.setId(clusterLogicId);
        esLogicClusterDTO.setAppId(targetAppId);
        esLogicClusterDTO.setResponsible(targetResponsible);
        return editClusterLogicNotCheck(esLogicClusterDTO, submitor);
    }

    @Override
    public List<ClusterLogic> pagingGetClusterLogicByCondition(ClusterLogicConditionDTO param) {
        String sortTerm = null == param.getSortTerm() ? SortConstant.ID : param.getSortTerm();
        String sortType = param.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;

        return ConvertUtil.list2List(logicClusterDAO.pagingByCondition(param.getName(), param.getAppId(),
                        param.getType(), param.getHealth(), (param.getPage() - 1) * param.getSize(), param.getSize(), sortTerm, sortType),
            ClusterLogic.class);
    }

    @Override
    public Long fuzzyClusterLogicHitByCondition(ClusterLogicConditionDTO param) {
        return logicClusterDAO.getTotalHitByCondition(ConvertUtil.obj2Obj(param, ClusterLogicPO.class));
    }

    @Override
    public List<ClusterLogic> getClusterLogicListByIds(List<Long> clusterLogicIdList) {
        return ConvertUtil.list2List(logicClusterDAO.listByIds(new HashSet<>(clusterLogicIdList)), ClusterLogic.class);
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
        if (AriusObjUtils.isNull(param.getAppId())) {
            return Result.buildParamIllegal("应用ID为空");
        }

        if (AriusObjUtils.isNull(param.getResponsible())) {
            return Result.buildParamIllegal("责任人为空");
        }
        return Result.buildSucc();
    }

    private Result<Void> isIllegal(ESLogicClusterDTO param) {
        ResourceLogicTypeEnum typeEnum = ResourceLogicTypeEnum.valueOf(param.getType());
        if (ResourceLogicTypeEnum.UNKNOWN.equals(typeEnum)) {
            return Result.buildParamIllegal("新建逻辑集群提交内容中集群类型非法");
        }

        if (param.getAppId() != null && !appService.isAppExists(param.getAppId())) {
            return Result.buildParamIllegal("应用ID非法");
        }

        for (String responsible : ListUtils.string2StrList(param.getResponsible())) {
            if (employeeService.checkUsers(responsible).failed()) {
                return Result.buildParamIllegal("责任人非法");
            }
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

        if (AriusObjUtils.isNull(param.getLibraDepartment())) {
            param.setLibraDepartment("");
        }

        if (AriusObjUtils.isNull(param.getLibraDepartmentId())) {
            param.setLibraDepartmentId("");
        }

        if (AriusObjUtils.isNull(param.getConfigJson())) {
            param.setConfigJson("");
        }

        if (!AriusObjUtils.isNull(param.getDataNodeNu())) {
            param.setQuota((double) param.getDataNodeNu());
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

    private void setPhyClusterService(List<ClusterRoleHostInfo> esRolePhyClusterHosts, RoleCluster roleCluster,
                                      List<ClusterRoleHostInfo> clusterRoleHostInfos) {
        for (ClusterRoleHostInfo clusterRoleHostInfo : esRolePhyClusterHosts) {
            if (clusterRoleHostInfo.getRoleClusterId().longValue() == roleCluster.getId().longValue()) {
                clusterRoleHostInfos.add(ConvertUtil.obj2Obj(clusterRoleHostInfo, ClusterRoleHostInfo.class));
            }
        }
    }

    private void setLogicClusterService(Long logicClusterId, ClusterLogicPO clusterLogicPO, RoleCluster roleCluster,
                                        List<ClusterRoleHostInfo> clusterRoleHostInfos) {
        roleCluster.setPodNumber(clusterLogicPO.getDataNodeNu());
        roleCluster.setMachineSpec(clusterLogicPO.getDataNodeSpec());

        List<ClusterRoleHostInfo> clusterRoleHostInfoList = clusterLogicNodeService.getLogicClusterNodes(logicClusterId);

        for (ClusterRoleHostInfo clusterHost : clusterRoleHostInfoList) {
            ClusterRoleHostInfo clusterRoleHostInfo = new ClusterRoleHostInfo();
            clusterRoleHostInfo.setHostname(clusterHost.getHostname());
            clusterRoleHostInfo.setRole(DATA_NODE.getCode());

            clusterRoleHostInfos.add(clusterRoleHostInfo);
        }
    }
}

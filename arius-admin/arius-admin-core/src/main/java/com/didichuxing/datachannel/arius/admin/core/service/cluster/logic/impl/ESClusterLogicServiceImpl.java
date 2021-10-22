package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.impl;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.RESOURCE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.DELETE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ESPlugin;
import com.didichuxing.datachannel.arius.admin.client.bean.common.LogicResourceConfig;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppLogicClusterAuthDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPluginDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicWithRack;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterNodeSepc;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.LogicClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterLogicEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.RackUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didichuxing.datachannel.arius.admin.core.component.ResponsibleConvertTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicClusterAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.employee.EmployeeService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.ClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.LogicClusterDAO;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * @author d06679
 * @date 2019/3/25
 */
@Service
public class ESClusterLogicServiceImpl implements ESClusterLogicService {

    private static final ILog          LOGGER = LogFactory.getLog(ESClusterLogicServiceImpl.class);

    @Autowired
    private LogicClusterDAO            logicClusterDAO;

    @Autowired
    private AppLogicClusterAuthService logicClusterAuthService;

    @Autowired
    private ESRegionRackService        rackService;

    @Autowired
    private OperateRecordService       operateRecordService;

    @Autowired
    private AppService                 appService;

    @Autowired
    private EmployeeService            employeeService;

    @Autowired
    private ResponsibleConvertTool     responsibleConvertTool;

    @Autowired
    private TemplatePhyService         templatePhyService;

    @Autowired
    private ClusterDAO                 clusterDAO;

    @Autowired
    private ESPluginService            esPluginService;

    @Autowired
    private ESClusterPhyService        esClusterPhyService;

    @Autowired
    private ESClusterLogicNodeService  esClusterLogicNodeService;

    @Autowired
    private ESMachineNormsService      esMachineNormsService;

    @Autowired
    private ESRegionRackService        esRegionRackService;

    /**
     * 数据质量校验
     *
     * @return true/false
     */
    @Override
    public boolean checkAllLogicClustersMeta() {
        return true;
    }

    /**
     * 条件查询逻辑集群
     *
     * @param param 条件
     * @return 逻辑集群列表
     */
    @Override
    public List<ESClusterLogic> listLogicClusters(ESLogicClusterDTO param) {
        return responsibleConvertTool.list2List(
            logicClusterDAO.listByCondition(responsibleConvertTool.obj2Obj(param, LogicClusterPO.class)),
            ESClusterLogic.class);
    }

    /**
     * 获取所有逻辑集群
     *
     * @return 逻辑集群列表
     */
    @Override
    public List<ESClusterLogic> listAllLogicClusters() {
        return responsibleConvertTool.list2List(logicClusterDAO.listAll(), ESClusterLogic.class);
    }

    /**
     * 获取所有资源
     * @return
     */
    @Override
    public List<ESClusterLogicWithRack> listAllLogicClustersWithRackInfo() {

        // 所有逻辑集群rack信息
        List<ESClusterLogicRackInfo> allLogicClusterRackInfos = esRegionRackService.listAllLogicClusterRacks();

        // 逻辑集群ID到逻辑集群rack信息的Multimap
        Multimap<Long, ESClusterLogicRackInfo> logicClusterId2RackInfoMap = ConvertUtil
            .list2MulMap(allLogicClusterRackInfos, ESClusterLogicRackInfo::getLogicClusterId);

        List<LogicClusterPO> logicClusters = logicClusterDAO.listAll();
        List<ESClusterLogicWithRack> logicClustersWithRackInfo = Lists.newArrayList();
        for (LogicClusterPO logicCluster : logicClusters) {
            ESClusterLogicWithRack logicClusterWithRackInfo = responsibleConvertTool.obj2Obj(logicCluster,
                ESClusterLogicWithRack.class);
            logicClusterWithRackInfo.setItems(logicClusterId2RackInfoMap.get(logicCluster.getId()));
            logicClustersWithRackInfo.add(logicClusterWithRackInfo);
        }

        return logicClustersWithRackInfo;
    }

    /**
     * 获取逻辑集群，返回结果里包含逻辑集群拥有的rack信息
     * @return
     */
    @Override
    public ESClusterLogicWithRack getLogicClusterWithRackInfoById(Long logicClusterId){
        // 所有逻辑集群rack信息
        List<ESClusterLogicRackInfo> allLogicClusterRackInfos = esRegionRackService.listAllLogicClusterRacks();

        // 逻辑集群ID到逻辑集群rack信息的Multimap
        Multimap<Long, ESClusterLogicRackInfo> logicClusterId2RackInfoMap = ConvertUtil
                .list2MulMap(allLogicClusterRackInfos, ESClusterLogicRackInfo::getLogicClusterId);

        LogicClusterPO logicClusterPO = logicClusterDAO.getById(logicClusterId);

        ESClusterLogicWithRack logicClusterWithRackInfo = responsibleConvertTool.obj2Obj(logicClusterPO,
                ESClusterLogicWithRack.class);
        logicClusterWithRackInfo.setItems(logicClusterId2RackInfoMap.get(logicClusterPO.getId()));

        return logicClusterWithRackInfo;
    }

    /**
     * 删除逻辑集群
     *
     * @param logicClusterId 资源id
     * @param operator   操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteLogicClusterById(Long logicClusterId, String operator) throws AdminOperateException {
        LogicClusterPO logicCluster = logicClusterDAO.getById(logicClusterId);
        if (logicCluster == null) {
            return Result.buildNotExist("逻辑集群不存在");
        }

        if (hasTemplate(logicClusterId)) {
            return Result.build(ResultType.IN_USE_ERROR.getCode(), "逻辑集群使用中");
        }

        List<ESClusterLogicRackInfo> racks = rackService.listLogicClusterRacks(logicClusterId);
        if (CollectionUtils.isEmpty(racks)) {
            LOGGER.info("class=ESClusterLogicServiceImpl||method=delResource||resourceId={}||msg=delResource no items!",
                logicClusterId);
        } else {
            LOGGER.info(
                "class=ESClusterLogicServiceImpl||method=delResource||resourceId={}||itemSize={}||msg=delResource has items!",
                logicClusterId, racks.size());

            for (ESClusterLogicRackInfo item : racks) {
                rackService.deleteRackById(item.getId());
            }
        }

        boolean succeed = (logicClusterDAO.delete(logicClusterId) > 0);
        if (!succeed) {
            throw new AdminOperateException("删除逻辑集群失败");
        }

        //集群上下文数量变化, 需要更新集群上下文
        SpringTool.publish(new ClusterLogicEvent(this));

        operateRecordService.save(RESOURCE, DELETE, logicClusterId, "", operator);
        return Result.buildSucc();
    }

    /**
     * 新建逻辑集群
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Override
    public Result<Long> createLogicCluster(ESLogicClusterDTO param, String operator) {
        Result checkResult = validateLogicClusterParams(param, ADD);
        if (checkResult.failed()) {
            LOGGER.warn("class=ESClusterLogicServiceImpl||method=addResource||msg={}", checkResult.getMessage());
            return Result.buildFrom(checkResult);
        }

        initLogicCluster(param);

        LogicClusterPO logicPO = responsibleConvertTool.obj2Obj(param, LogicClusterPO.class);
        boolean succeed = logicClusterDAO.insert(logicPO) == 1;

        if (succeed) {
            operateRecordService.save(RESOURCE, ADD, logicPO.getId(), "创建逻辑集群", operator);

            //集群上下文数量变化, 需要更新集群上下文
            SpringTool.publish(new ClusterLogicEvent(this));
        }

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
    public Result validateLogicClusterParams(ESLogicClusterDTO param, OperationEnum operation) {
        return checkLogicClusterParams(param, operation);
    }

    @Override
    public Result editLogicCluster(ESLogicClusterDTO param, String operator) {
        Result checkResult = validateLogicClusterParams(param, EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=ESClusterLogicServiceImpl||method=editResource||msg={}", checkResult.getMessage());
            return checkResult;
        }

        return editClusterLogicNotCheck(param, operator);
    }

    @Override
    public Result editClusterLogicNotCheck(ESLogicClusterDTO param, String operator) {
        LogicClusterPO oldPO = logicClusterDAO.getById(param.getId());

        LogicClusterPO paramPO = responsibleConvertTool.obj2Obj(param, LogicClusterPO.class);
        boolean succ = (1 == logicClusterDAO.update(paramPO));

        if (succ) {
            operateRecordService.save(RESOURCE, EDIT, param.getId(), AriusObjUtils
                            .findChanged(oldPO, paramPO),
                operator);
        }

        return Result.build(succ);
    }

    @Override
    public ESClusterLogic getLogicClusterById(Long logicClusterId) {
        return responsibleConvertTool.obj2Obj(logicClusterDAO.getById(logicClusterId), ESClusterLogic.class);
    }

    @Override
    public ESClusterLogic getLogicClusterByName(String logicClusterName) {
        return responsibleConvertTool.obj2Obj(logicClusterDAO.getByName(logicClusterName), ESClusterLogic.class);
    }

    /**
     * 查询指定逻辑集群的配置
     *
     * @param logicClusterId 逻辑集群id
     * @return 逻辑集群 不存在返回null
     */
    @Override
    public LogicResourceConfig getLogicClusterConfigById(Long logicClusterId) {
        ESClusterLogic esClusterLogic = getLogicClusterById(logicClusterId);
        if (esClusterLogic == null) {
            return null;
        }
        return genLogicClusterConfig(esClusterLogic.getConfigJson());
    }

    /**
     * 查询指定app所创建的逻辑集群
     *
     * @param appId APPID
     * @return list
     */
    @Override
    public List<ESClusterLogic> getOwnedLogicClustersByAppId(Integer appId) {
        return responsibleConvertTool.list2List(logicClusterDAO.listByAppId(appId), ESClusterLogic.class);
    }

    /**
     * 查询指定app有权限的逻辑集群（包括申请权限）
     * @param appId APP ID
     * @return 逻辑集群列表
     */
    @Override
    public List<ESClusterLogic> getHasAuthLogicClustersByAppId(Integer appId) {
        if (appId == null) {
            LOGGER.error("获取有权限逻辑集群时appId为null");
            return new ArrayList<>();
        }

        // 获取有权限的逻辑集群id
        Set<Long> hasAuthLogicClusterIds = logicClusterAuthService.getLogicClusterAuths(appId).stream()
            .map(AppLogicClusterAuthDTO::getLogicClusterId).collect(Collectors.toSet());

        // 批量获取有权限的集群
        List<LogicClusterPO> hasAuthLogicClusters = hasAuthLogicClusterIds.size() > 0
            ? logicClusterDAO.listByIds(hasAuthLogicClusterIds)
            : new ArrayList<>();

        // 获取作为owner的集群
        List<LogicClusterPO> ownedLogicClusters = logicClusterDAO.listByAppId(appId);

        // 综合
        for (LogicClusterPO ownedLogicCluster : ownedLogicClusters) {
            if (!hasAuthLogicClusterIds.contains(ownedLogicCluster.getId())) {
                hasAuthLogicClusters.add(ownedLogicCluster);
            }
        }

        return responsibleConvertTool.list2List(hasAuthLogicClusters, ESClusterLogic.class);
    }

    @Override
    public Boolean isLogicClusterExists(Long resourceId) {
        return null != logicClusterDAO.getById(resourceId);
    }

    /**
     * 获取rack匹配到的逻辑集群
     *
     * @param cluster 集群
     * @param racks    rack
     * @return count
     */
    @Override
    public ESClusterLogic getLogicClusterByRack(String cluster, String racks) {

        List<ESClusterLogicRackInfo> logicClusterRackInfos = esRegionRackService
            .listAssignedRacksByClusterName(cluster);

        if (CollectionUtils.isEmpty(logicClusterRackInfos)) {
            return null;
        }

        // 获取逻辑逻辑集群内的所有rack, 按着resourceId分组
        Multimap<Long, String> logicClusterId2RackMultiMap = ConvertUtil.list2MulMap(logicClusterRackInfos,
            ESClusterLogicRackInfo::getLogicClusterId, ESClusterLogicRackInfo::getRack);

        // 遍历逻辑集群，获取与给定的racks有交集的逻辑集群
        for (Map.Entry<Long, Collection<String>> entry : logicClusterId2RackMultiMap.asMap().entrySet()) {
            if (RackUtils.hasIntersect(racks, entry.getValue())) {
                return getLogicClusterById(entry.getKey());
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
    public List<ESClusterLogic> getLogicClusterByOwnerId(Long responsibleId) {
        return responsibleConvertTool.list2List(logicClusterDAO.listByResponsible(String.valueOf(responsibleId)),
            ESClusterLogic.class);
    }

    /**
     * 根据配置字符创获取配置，填充默认值
     *
     * @param configJson json
     * @return config
     */
    @Override
    public LogicResourceConfig genLogicClusterConfig(String configJson) {
        if (StringUtils.isBlank(configJson)) {
            return new LogicResourceConfig();
        }
        return JSON.parseObject(configJson, LogicResourceConfig.class);
    }

    @Override
    public Set<ESRoleClusterNodeSepc> getLogicDataNodeSepc(Long logicClusterId) {
        List<ESRoleCluster> esRoleClusters = getLogicClusterRole(logicClusterId);

        Set<ESRoleClusterNodeSepc> esRoleClusterDataNodeSepcs = new HashSet<>();

        if (CollectionUtils.isNotEmpty(esRoleClusters)) {
            for (ESRoleCluster esRoleCluster : esRoleClusters) {
                if (DATA_NODE.getDesc().equals(esRoleCluster.getRole())) {
                    ESRoleClusterNodeSepc esRoleClusterNodeSepc = new ESRoleClusterNodeSepc();
                    esRoleClusterNodeSepc.setRole(DATA_NODE.getDesc());
                    esRoleClusterNodeSepc.setSpec(esRoleCluster.getMachineSpec());

                    esRoleClusterDataNodeSepcs.add(esRoleClusterNodeSepc);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(esRoleClusterDataNodeSepcs)) {
            return esRoleClusterDataNodeSepcs;
        }

        List<ESMachineNormsPO> esMachineNormsPOS = esMachineNormsService.listMachineNorms();
        for (ESMachineNormsPO esMachineNormsPO : esMachineNormsPOS) {
            esRoleClusterDataNodeSepcs.add(ConvertUtil.obj2Obj(esMachineNormsPO, ESRoleClusterNodeSepc.class));
        }

        return esRoleClusterDataNodeSepcs;
    }

    @Override
    public List<ESRoleCluster> getLogicClusterRole(Long logicClusterId) {
        List<ESRoleCluster> esRoleClusters = new ArrayList<>();

        try {
            LogicClusterPO logicClusterPO = logicClusterDAO.getById(logicClusterId);

            List<String> phyClusterNames = rackService.listPhysicClusterNames(logicClusterId);
            if (CollectionUtils.isEmpty(phyClusterNames)) {
                return new ArrayList<>();
            }

            //拿第一个物理集群的client、master信息，因为只有Arius维护的大公共共享集群才会有一个逻辑集群映射成多个物理集群
            ESClusterPhy esClusterPhy = esClusterPhyService.getClusterByName(phyClusterNames.get(0));
            if (null == esClusterPhy) {
                return new ArrayList<>();
            }

            List<ESRoleCluster> esRolePhyClusters = esClusterPhy.getRoleClusters();
            List<ESRoleClusterHost> esRolePhyClusterHosts = esClusterPhy.getRoleClusterHosts();

            for (ESRoleCluster esRolePhyCluster : esRolePhyClusters) {
                ESRoleCluster esRoleCluster = ConvertUtil.obj2Obj(esRolePhyCluster, ESRoleCluster.class);

                List<ESRoleClusterHost> esRoleClusterHosts = new ArrayList<>();

                //如果是datanode节点，那么使用逻辑集群申请的节点个数和阶段规格配置
                if (DATA_NODE.getDesc().equals(esRoleCluster.getRoleClusterName())) {
                    esRoleCluster.setPodNumber(logicClusterPO.getDataNodeNu());
                    esRoleCluster.setMachineSpec(logicClusterPO.getDataNodeSpec());

                    List<ESRoleClusterHost> esRoleClusterHostList = esClusterLogicNodeService
                        .getLogicClusterNodes(logicClusterId);

                    for (ESRoleClusterHost clusterHost : esRoleClusterHostList) {
                        ESRoleClusterHost esRoleClusterHost = new ESRoleClusterHost();
                        esRoleClusterHost.setHostname(clusterHost.getHostname());
                        esRoleClusterHost.setRole(DATA_NODE.getCode());

                        esRoleClusterHosts.add(esRoleClusterHost);
                    }
                } else {
                    for (ESRoleClusterHost roleClusterHost : esRolePhyClusterHosts) {
                        if (roleClusterHost.getRoleClusterId().longValue() == esRoleCluster.getId().longValue()) {
                            esRoleClusterHosts.add(ConvertUtil.obj2Obj(roleClusterHost, ESRoleClusterHost.class));
                        }
                    }
                }

                esRoleCluster.setEsRoleClusterHosts(esRoleClusterHosts);
                esRoleCluster.setPodNumber(esRoleClusterHosts.size());
                esRoleClusters.add(esRoleCluster);
            }
        } catch (Exception e) {
            LOGGER.warn("class=ESClusterLogicServiceImpl||method=acquireLogicClusterRole||logicClusterId={}",
                logicClusterId, e);
        }

        return esRoleClusters;
    }

    @Override
    public List<ESPlugin> getLogicClusterPlugins(Long logicClusterId) {
        List<String> clusterNameList = rackService.listPhysicClusterNames(logicClusterId);
        if (ValidateUtils.isEmptyList(clusterNameList)) {
            return new ArrayList<>();
        }

        //逻辑集群对应的物理集群插件一致 取其中一个物理集群
        ESClusterPhy clusterPhy = esClusterPhyService.getClusterByName(clusterNameList.get(0));
        List<ESPluginPO> pluginPOList = esPluginService.listClusterAndDefaultESPlugin(clusterPhy.getId().toString());

        if (ValidateUtils.isEmptyList(pluginPOList)) {
            return new ArrayList<>();
        }

        Map<String, ClusterPO> clusterPOMap = clusterDAO.listAll().stream()
            .collect(Collectors.toMap(ClusterPO::getCluster, Function.identity(), (key1, key2) -> key2));

        Map<Long, ESPlugin> pluginMap = new HashMap<>(0);
        for (ESPluginPO esPluginPO : pluginPOList) {
            ESPlugin logicalPlugin = ConvertUtil.obj2Obj(esPluginPO, ESPlugin.class);
            logicalPlugin.setInstalled(Boolean.FALSE);
            pluginMap.put(esPluginPO.getId(), logicalPlugin);
        }

        for (String clusterName : clusterNameList) {
            ClusterPO clusterPO = clusterPOMap.get(clusterName);
            if (ValidateUtils.isNull(clusterPO)) {
                continue;
            }
            List<Long> pluginIds = parsePluginIds(clusterPO.getPlugIds());
            for (Long pluginId : pluginIds) {
                ESPlugin logicalPlugin = pluginMap.get(pluginId);
                if (ValidateUtils.isNull(logicalPlugin)) {
                    continue;
                }
                logicalPlugin.setInstalled(true);
            }
        }

        return new ArrayList<>(pluginMap.values());
    }

    @Override
    public Result addPlugin(Long logicClusterId, ESPluginDTO esPluginDTO, String operator) {

        if (null != logicClusterId) {
            List<Integer> clusterIdList = rackService.listPhysicClusterId(logicClusterId);
            if (ValidateUtils.isEmptyList(clusterIdList)) {
                return Result.buildFail("对应物理集群不存在");
            }

            String clusterIds = ListUtils.intList2String(clusterIdList);
            esPluginDTO.setPhysicClusterId(clusterIds);
        }
        return esPluginService.addESPlugin(esPluginDTO);
    }

    @Override
    public Result transferClusterLogic(Long clusterLogicId, Integer targetAppId, String targetResponsible, String submitor) {

        ESLogicClusterDTO esLogicClusterDTO = new ESLogicClusterDTO();
        esLogicClusterDTO.setId(clusterLogicId);
        esLogicClusterDTO.setAppId(targetAppId);
        esLogicClusterDTO.setResponsible(targetResponsible);
        return editClusterLogicNotCheck(esLogicClusterDTO, submitor);
    }

    /***************************************** private method ****************************************************/
    /**
     * Check逻辑集群参数
     *
     * @param param     逻辑集群
     * @param operation 操作类型
     * @return
     */
    private Result checkLogicClusterParams(ESLogicClusterDTO param, OperationEnum operation) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("逻辑集群信息为空");
        }

        if (param.getType() != null) {
            ResourceLogicTypeEnum typeEnum = ResourceLogicTypeEnum.valueOf(param.getType());
            if (ResourceLogicTypeEnum.UNKNOWN.equals(typeEnum)) {
                return Result.buildParamIllegal("类型非法");
            }
        }

        if (param.getAppId() != null) {
            if (!appService.isAppExists(param.getAppId())) {
                return Result.buildParamIllegal("应用ID非法");
            }
        }

        for (String responsible : ListUtils.string2StrList(param.getResponsible())) {
            if (employeeService.checkUsers(responsible, null).failed()) {
                return Result.buildParamIllegal("责任人非法");
            }
        }

        if (ADD.equals(operation)) {
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
            if (AriusObjUtils.isNull(param.getMemo())) {
                return Result.buildParamIllegal("备注为空");
            }

            LogicClusterPO logicPO = logicClusterDAO.getByName(param.getName());
            if (!AriusObjUtils.isNull(logicPO)) {
                return Result.buildDuplicate("逻辑集群重复");
            }
        } else if (EDIT.equals(operation)) {
            if (AriusObjUtils.isNull(param.getId())) {
                return Result.buildParamIllegal("逻辑集群ID为空");
            }

            LogicClusterPO oldPO = logicClusterDAO.getById(param.getId());
            if (oldPO == null) {
                return Result.buildNotExist("逻辑集群不存在");
            }
        }

        return Result.buildSucc();
    }

    /**
     * 判断逻辑集群是否有模板
     *
     * @param logicClusterId 逻辑集群Id
     * @return
     */
    private boolean hasTemplate(Long logicClusterId) {
        List<ESClusterLogicRackInfo> esClusterLogicRackInfos = esRegionRackService
            .listLogicClusterRacks(logicClusterId);
        if (CollectionUtils.isEmpty(esClusterLogicRackInfos)) {
            return false;
        }

        // 获取逻辑逻辑集群内的所有rack, 按着cluster分组
        Multimap<String, String> clusterRackMultiMap = ConvertUtil.list2MulMap(esClusterLogicRackInfos,
            ESClusterLogicRackInfo::getPhyClusterName, ESClusterLogicRackInfo::getRack);

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
    }
}

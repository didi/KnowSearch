package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ESPlugin;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhyDiscover;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterPhyEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.ClusterDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.model.type.ESVersion;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.COLD_RACK_PREFER;

/**
 * @author d06679
 * @date 2019/3/20
 */
@Service
public class ESClusterPhyServiceImpl implements ESClusterPhyService {

    private static final ILog        LOGGER = LogFactory.getLog(ESClusterPhyServiceImpl.class);

    @Value("${es.client.cluster.port}")
    private String                   esClusterClientPort;

    @Autowired
    private ClusterDAO               clusterDAO;

    @Autowired
    private ESClusterService         esClusterService;

    @Autowired
    private ESRegionRackService      esRegionRackService;

    @Autowired
    private ESPluginService          esPluginService;

    @Autowired
    private ESRoleClusterService     esRoleClusterService;

    @Autowired
    private ESRoleClusterHostService esRoleClusterHostService;

    @Autowired
    private OperateRecordService     operateRecordService;

    /**
     * 条件查询
     * @param params 条件
     * @return 集群列表
     */
    @Override
    public List<ESClusterPhy> listClustersByCondt(ESClusterDTO params) {
        List<ClusterPO> clusterPOs = clusterDAO.listByCondition(ConvertUtil.obj2Obj(params, ClusterPO.class));

        if (CollectionUtils.isEmpty(clusterPOs)) {
            return Lists.newArrayList();
        }

        return ConvertUtil.list2List(clusterPOs, ESClusterPhy.class);
    }

    /**
     * 删除集群
     *
     * @param clusterId 集群id
     * @param operator  操作人
     * @return 成功 true 失败 false
     * <p>
     * NotExistException
     * 集群不存在
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteClusterById(Integer clusterId, String operator) {
        ClusterPO clusterPO = clusterDAO.getById(clusterId);
        if (clusterPO == null) {
            return Result.buildNotExist("集群不存在");
        }

        List<ESClusterLogicRackInfo> logicClusterRacks = esRegionRackService
            .listAssignedRacksByClusterName(clusterPO.getCluster());

        if (CollectionUtils.isNotEmpty(logicClusterRacks)) {
            return Result.buildParamIllegal("集群region已配置给逻辑集群，无法删除");
        }

        boolean succ = clusterDAO.delete(clusterId) == 1;
        if(succ) {
            operateRecordService.save(ModuleEnum.CLUSTER, OperationEnum.DELETE, clusterPO.getCluster(), null, operator);

            //集群上下文数量发生变化, 需要更新集群上下文
            SpringTool.publish(new ClusterPhyEvent(this));
        }
        
        return Result.build(succ);
    }

    /**
     * 新建集群
     * @param param    集群信息
     * @param operator 操作人
     * @return 成功 true 失败 false
     * <p>
     * DuplicateException
     * 集群已经存在(用名字校验)
     * IllegalArgumentException
     * 参数不合理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createCluster(ESClusterDTO param, String operator) {
        Result checkResult = checkClusterParam(param, OperationEnum.ADD);
        if (checkResult.failed()) {
            LOGGER.warn("class=ESClusterPhyServiceImpl||method=addCluster||msg={}", checkResult.getMessage());
            return checkResult;
        }

        initClusterParam(param);

        ClusterPO clusterPO = ConvertUtil.obj2Obj(param, ClusterPO.class);
        boolean succ = (1 == clusterDAO.insert(clusterPO));
        if (succ) {
            param.setId(clusterPO.getId());

            operateRecordService.save(ModuleEnum.CLUSTER, OperationEnum.ADD, param.getCluster(), param.getDesc(),
                operator);
            
            //集群上下文数量变化, 需要更新集群上下文
            SpringTool.publish(new ClusterPhyEvent(this));
        }
        return Result.build(succ);
    }

    /**
     * 编辑集群
     * @param param    集群信息
     * @param operator 操作人
     * @return 成功 true 失败 false
     * <p>
     * IllegalArgumentException
     * 参数不合理
     * NotExistException
     * 集群不存在
     */
    @Override
    public Result editCluster(ESClusterDTO param, String operator) {
        Result checkResult = checkClusterParam(param, OperationEnum.EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=ESClusterPhyServiceImpl||method=editCluster||msg={}", checkResult.getMessage());
            return checkResult;
        }

        boolean succ = (1 == clusterDAO.update(ConvertUtil.obj2Obj(param, ClusterPO.class)));
        return Result.build(succ);
    }

    /**
     * 根据集群名字查询集群
     * @param clusterName 集群名字
     * @return 集群
     */
    @Override
    public ESClusterPhy getClusterByName(String clusterName) {
        // 获取物理集群
        ClusterPO clusterPO = clusterDAO.getByName(clusterName);
        if (null == clusterPO) {
            return null;
        }

        // 转换物理集群对象
        ESClusterPhy esClusterPhy = ConvertUtil.obj2Obj(clusterPO, ESClusterPhy.class);

        // 添加角色、机器信息
        List<ESRoleCluster> esRoleClusters = esRoleClusterService.getAllRoleClusterByClusterId(esClusterPhy.getId());
        if (CollectionUtils.isNotEmpty(esRoleClusters)) {
            // 角色信息
            esClusterPhy.setRoleClusters(esRoleClusters);

            // 机器信息
            List<ESRoleClusterHost> roleClusterHosts = new ArrayList<>();
            for (ESRoleCluster esRoleCluster : esRoleClusters) {
                List<ESRoleClusterHost> esRoleClusterHosts = esRoleClusterHostService
                    .getByRoleClusterId(esRoleCluster.getId());
                roleClusterHosts.addAll(esRoleClusterHosts);
            }

            esClusterPhy.setRoleClusterHosts(roleClusterHosts);
        }

        return esClusterPhy;
    }

    @Override
    public List<ESClusterPhy> listAllClusters() {
        return ConvertUtil.list2List(clusterDAO.listAll(), ESClusterPhy.class);
    }

    /**
     * 集群是否存在
     * @param clusterName 集群名字
     * @return true 存在
     */
    @Override
    public boolean isClusterExists(String clusterName) {
        return clusterDAO.getByName(clusterName) != null;
    }

    /**
     * 集群是否存在
     * @param clusterName 集群名字
     * @return true 存在
     */
    @Override
    public boolean isHighVersionCluster(String clusterName) {
        ESClusterPhy cluster = getClusterByName(clusterName);
        if (cluster == null) {
            return false;
        }
        ESVersion esVersion = ESVersion.valueBy(cluster.getEsVersion());
        return ESVersion.ES651.equals(esVersion) || ESVersion.ES760.equals(esVersion);
    }

    /**
     * rack是否存在
     * @param cluster 集群名字
     * @param racks   rack名字
     * @return true 存在
     */
    @Override
    public boolean isRacksExists(String cluster, String racks) {
        Set<String> rackSet = getClusterRacks(cluster);
        if (CollectionUtils.isEmpty(rackSet)) {
            LOGGER.warn("class=ESClusterPhyServiceImpl||method=rackExist||cluster={}||msg=can not get rack set!",
                cluster);
            return false;
        }

        for (String r : racks.split(AdminConstant.RACK_COMMA)) {
            if (!rackSet.contains(r)) {
                LOGGER.warn(
                    "class=ESClusterPhyServiceImpl||method=rackExist||cluster={}||rack={}||msg=can not get rack!",
                    cluster, r);
                return false;
            }
        }

        return true;
    }

    /**
     * 获取集群全部的rack
     * @param cluster cluster
     * @return set
     */
    @Override
    public Set<String> getClusterRacks(String cluster) {
        List<ESRoleClusterHost> nodes = esRoleClusterHostService.getNodesByCluster(cluster);
        if (CollectionUtils.isEmpty(nodes)) {
            return Sets.newHashSet();
        }

        Set<String> rackSet = new HashSet<>();
        // 只有datanode才有rack
        for (ESRoleClusterHost esRoleClusterHost : nodes) {
            if (ESClusterNodeRoleEnum.DATA_NODE.getCode() == esRoleClusterHost.getRole()) {
                rackSet.add(esRoleClusterHost.getRack());
            }
        }

        return rackSet;
    }

    @Override
    public Set<String> listHotRacks(String cluster) {
        // 冷存的rack以c开头，排除冷存即为热存
        return getClusterRacks(cluster).stream().filter(rack -> !rack.toLowerCase().startsWith(COLD_RACK_PREFER))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> listColdRacks(String cluster) {
        // 冷存的rack以c开头
        return getClusterRacks(cluster).stream().filter(rack -> rack.toLowerCase().startsWith(COLD_RACK_PREFER))
            .collect(Collectors.toSet());
    }

    /**
     * 根据集群名称解析获取对应的插件列表
     * @param cluster 集群名称
     * @return
     */
    @Override
    public List<ESPlugin> listClusterPlugins(String cluster) {

        ClusterPO clusterPhy = clusterDAO.getByName(cluster);
        List<ESPluginPO> pluginPOList = esPluginService.listClusterAndDefaultESPlugin(clusterPhy.getId().toString());

        if (ValidateUtils.isEmptyList(pluginPOList)) {
            return new ArrayList<>();
        }

        Map<String, ClusterPO> clusterPOMap = clusterDAO.listAll().stream()
            .collect(Collectors.toMap(ClusterPO::getCluster, Function.identity(), (key1, key2) -> key2));

        Map<Long, ESPlugin> pluginMap = new HashMap<>(0);
        for (ESPluginPO esPluginPO : pluginPOList) {
            ESPlugin phyPlugin = ConvertUtil.obj2Obj(esPluginPO, ESPlugin.class);
            phyPlugin.setInstalled(Boolean.FALSE);
            pluginMap.put(esPluginPO.getId(), phyPlugin);
        }

        ClusterPO clusterPO = clusterPOMap.get(clusterPhy.getCluster());
        if (ValidateUtils.isNull(clusterPO)) {
            return new ArrayList<>();
        }
        List<Long> pluginIds = parsePluginIds(clusterPO.getPlugIds());
        //pluginIds = Arrays.stream(clusterPO.getPlugIds().split(",")).map(Long::parseLong).collect(Collectors.toList());
        for (Long pluginId : pluginIds) {
            ESPlugin phyPlugin = pluginMap.get(pluginId);
            if (ValidateUtils.isNull(phyPlugin)) {
                continue;
            }
            phyPlugin.setInstalled(true);
        }

        return new ArrayList<>(pluginMap.values());
    }

    /**
     * 获取物理集群各种纬度监控数据以及日志数据
     * @param cluster 集群名称
     * @return
     */
    @Override
    public List<ESClusterPhyDiscover> getClusterDiscovers(String cluster) {
        // TODO: implement it.
        return new ArrayList<>();
    }

    /**
     * 查询指定集群
     * @param clusterId 集群id
     * @return 集群  不存在返回null
     */
    @Override
    public ESClusterPhy getClusterById(Integer clusterId) {
        return ConvertUtil.obj2Obj(clusterDAO.getById(clusterId), ESClusterPhy.class);
    }

    /**
     * 获取写节点的个数
     * @param cluster 集群
     * @return count
     */
    @Override
    public int getWriteClientCount(String cluster) {
        ClusterPO clusterPO = clusterDAO.getByName(cluster);

        if (StringUtils.isBlank(clusterPO.getHttpWriteAddress())) {
            return 1;
        }

        return clusterPO.getHttpWriteAddress().split(",").length;
    }

    /**
     * 确保集群配置了DCDR的远端集群地址，如果没有配置尝试配置
     * @param cluster       集群
     * @param remoteCluster 远端集群
     * @return
     */
    @Override
    public boolean ensureDcdrRemoteCluster(String cluster, String remoteCluster) throws ESOperateException {

        ESClusterPhy esClusterPhy = getClusterByName(cluster);
        if (esClusterPhy == null) {
            return false;
        }

        ESClusterPhy remoteESClusterPhy = getClusterByName(remoteCluster);
        if (remoteESClusterPhy == null) {
            return false;
        }

        if (esClusterService.settingExist(cluster,
            String.format(ESOperateContant.REMOTE_CLUSTER_FORMAT, remoteCluster))) {
            return true;
        }

        return esClusterService.syncPutRemoteCluster(cluster, remoteCluster,
            genTcpAddr(remoteESClusterPhy.getHttpWriteAddress(), 9300), 3);
    }

    @Override
    public List<ESRoleCluster> listPhysicClusterRoles(Integer clusterId) {
        return esRoleClusterService.getAllRoleClusterByClusterId(clusterId);
    }

    @Override
    public ClusterStatusEnum getEsStatus(String phyClusterName) {
        return esClusterService.getClusterStatus(phyClusterName);
    }

    /**************************************** private method ***************************************************/
    private List<String> genTcpAddr(String httpAddress, int tcpPort) {
        try {
            String[] httpAddrArr = httpAddress.split(",");
            List<String> result = Lists.newArrayList();
            for (String httpAddr : httpAddrArr) {
                result.add(httpAddr.split(":")[0] + ":" + tcpPort);
            }
            return result;
        } catch (Exception e) {
            LOGGER.warn("method=genTcpAddr||httpAddress={}||errMsg={}", httpAddress, e.getMessage(), e);
        }

        return Lists.newArrayList();
    }

    private Result checkClusterParam(ESClusterDTO param, OperationEnum operation) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("集群信息为空");
        }

        if (OperationEnum.ADD.equals(operation)) {
            if (AriusObjUtils.isNull(param.getCluster())) {
                return Result.buildParamIllegal("集群名称为空");
            }
            if (AriusObjUtils.isNull(param.getDesc())) {
                return Result.buildParamIllegal("集群描述为空");
            }
            if (AriusObjUtils.isNull(param.getHttpAddress())) {
                return Result.buildParamIllegal("集群HTTP地址为空");
            }
            if (AriusObjUtils.isNull(param.getType())) {
                return Result.buildParamIllegal("集群类型为空");
            }
            if (AriusObjUtils.isNull(param.getDataCenter())) {
                return Result.buildParamIllegal("数据中心为空");
            }
            if (AriusObjUtils.isNull(param.getIdc())) {
                return Result.buildParamIllegal("机房信息为空");
            }
            if (AriusObjUtils.isNull(param.getEsVersion())) {
                return Result.buildParamIllegal("es版本为空");
            }
            if (AriusObjUtils.isNull(param.getTemplateSrvs())) {
                return Result.buildParamIllegal("集群的索引服务id列表为空");
            }

            if (param.getCluster() != null) {
                ClusterPO clusterPO = clusterDAO.getByName(param.getCluster());
                if (clusterPO != null && clusterPO.getId().equals(param.getId())) {
                    return Result.buildDuplicate("集群重复");
                }
            }
        } else if (OperationEnum.EDIT.equals(operation)) {
            if (AriusObjUtils.isNull(param.getId())) {
                return Result.buildParamIllegal("集群ID为空");
            }

            ClusterPO oldClusterPO = clusterDAO.getById(param.getId());
            if (oldClusterPO == null) {
                return Result.buildNotExist("集群不存在");
            }
        }

        if (param.getType() != null && ESClusterTypeEnum.UNKNOWN == ESClusterTypeEnum.valueOf(param.getType())) {
            return Result.buildParamIllegal("集群类型非法");
        }

        if (param.getDataCenter() != null && !DataCenterEnum.validate(param.getDataCenter())) {
            return Result.buildParamIllegal("数据中心非法");
        }

        if (param.getEsVersion() != null && ESVersion.valueBy(param.getEsVersion()) == null) {
            return Result.buildParamIllegal("es版本号非法");
        }

        return Result.buildSucc();
    }

    private void initClusterParam(ESClusterDTO param) {
        if (param.getWriteAddress() == null) {
            param.setWriteAddress("");
        }

        if (param.getReadAddress() == null) {
            param.setReadAddress("");
        }

        if (param.getHttpWriteAddress() == null) {
            param.setHttpWriteAddress("");
        }

        if (param.getPassword() == null) {
            param.setPassword("");
        }

        if(param.getImageName() == null) {
            param.setImageName("");
        }

        if(param.getLevel() == null) {
            param.setLevel(1);
        }

        if(param.getPlugIds() == null) {
            param.setPlugIds("");
        }

        if(param.getCreator() == null) {
            param.setCreator("");
        }

        if(param.getNsTree() == null) {
            param.setNsTree("");
        }
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
}

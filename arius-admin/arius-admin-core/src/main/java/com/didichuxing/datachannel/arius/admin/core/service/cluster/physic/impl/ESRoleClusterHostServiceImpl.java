package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.impl;

import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeStatusEnum.ONLINE;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.ES_ROLE_DATA;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.ES_ROLE_MASTER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESRoleClusterHostPO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.Getter;
import com.didichuxing.datachannel.arius.admin.common.util.RackUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterNodeDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESRoleClusterHostDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.model.node.NodeAttributes;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * ES集群表对应各角色主机列表 服务实现类
 * @author didi
 * @since 2020-08-24
 */
@Service
public class ESRoleClusterHostServiceImpl implements ESRoleClusterHostService {

    private static final ILog    LOGGER = LogFactory.getLog(ESRoleClusterHostServiceImpl.class);

    @Autowired
    private ESRoleClusterHostDAO roleClusterHostDAO;

    @Autowired
    private ESRoleClusterService esRoleClusterService;

    @Autowired
    private ESClusterNodeDAO     esClusterNodeDAO;

    @Override
    public List<ESRoleClusterHost> queryNodeByCondt(ESRoleClusterHostDTO condt) {
        List<ESRoleClusterHostPO> pos = roleClusterHostDAO
            .listByCondition(ConvertUtil.obj2Obj(condt, ESRoleClusterHostPO.class));
        return ConvertUtil.list2List(pos, ESRoleClusterHost.class);
    }

    @Override
    public List<ESRoleClusterHost> getNodesByCluster(String cluster) {
        List<ESRoleClusterHostPO> pos = roleClusterHostDAO.listByCluster(cluster);
        return ConvertUtil.list2List(pos, ESRoleClusterHost.class);
    }

    @Override
    public List<ESRoleClusterHost> getDataNodesByCluster(String cluster) {
        List<ESRoleClusterHost> nodesByCluster = getNodesByCluster(cluster);
        return nodesByCluster.stream().filter(r -> DATA_NODE.getCode() == r.getRole()).collect(Collectors.toList());
    }

    @Override
    public List<ESRoleClusterHost> getOnlineNodesByCluster(String cluster) {
        List<ESRoleClusterHost> roleClusterHosts = getNodesByCluster(cluster);
        if (CollectionUtils.isEmpty(roleClusterHosts)) {
            return Lists.newArrayList();
        }
        return roleClusterHosts.stream().filter(esClusterNode -> ONLINE.getCode() == esClusterNode.getStatus())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result editNodeStatus(ESRoleClusterHostDTO param, String operator) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("节点信息为空");
        }
        if (AriusObjUtils.isNull(param.getId())) {
            return Result.buildParamIllegal("节点ID为空");
        }
        if (AriusObjUtils.isNull(param.getStatus())) {
            return Result.buildParamIllegal("节点状态为空");
        }
        if (ESClusterNodeStatusEnum.UNKNOWN != ESClusterNodeStatusEnum.valueOf(param.getStatus())) {
            return Result.buildParamIllegal("节点状态非法");
        }

        ESRoleClusterHostPO hostPo = roleClusterHostDAO.getById(param.getId());
        if (hostPo == null) {
            return Result.buildNotExist("节点不存在");
        }

        ESRoleClusterHostPO clusterHostPO = new ESRoleClusterHostPO();
        clusterHostPO.setId(param.getId());
        clusterHostPO.setStatus(param.getStatus());

        return Result.build(1 == (roleClusterHostDAO.update(clusterHostPO)));
    }

    @Override
    public Result editNode(ESRoleClusterHostDTO param) {
        Result checkResult = checkNodeParam(param, OperationEnum.EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=ESRoleClusterHostServiceImpl||method=editNode|msg={}", checkResult.getMessage());
            return checkResult;
        }
        return Result.build(1 == roleClusterHostDAO.update(ConvertUtil.obj2Obj(param, ESRoleClusterHostPO.class)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collectClusterNodeSettings(String cluster) throws AdminTaskException {

        // 从ES引擎获取节点信息
        Set<ESRoleClusterHostPO> nodesFromEs = getClusterHostByCluster(cluster);
        if (CollectionUtils.isEmpty(nodesFromEs)) {
            throw new AdminTaskException("无法获取集群节点配置");
        }

        // 从数据库获取已有的节点信息
        List<ESRoleClusterHostPO> nodesFromDB = roleClusterHostDAO.listByCluster(cluster);
        LOGGER.info(
            "class=ESRoleClusterHostServiceImpl||method=collectClusterNodeSettings||cluster={}||esSize={}||dbSize={}",
            cluster, nodesFromEs.size(), nodesFromDB.size());

        Map<String, ESRoleClusterHostPO> nodePOFromDbMap = Maps.newHashMap();
        for (ESRoleClusterHostPO node : nodesFromDB) {
            nodePOFromDbMap.put(node.getIp(), node);
        }

        // 需要添加的节点
        List<ESRoleClusterHostPO> shouldAdd = Lists.newArrayList();
        // 需要修改的节点
        List<ESRoleClusterHostPO> shouldEdit = Lists.newArrayList();

        for (ESRoleClusterHostPO nodePO : nodesFromEs) {
            if (nodePOFromDbMap.containsKey(nodePO.getIp())) {
                nodePO.setId(nodePOFromDbMap.get(nodePO.getIp()).getId());
                LOGGER.info(
                    "class=ESRoleClusterHostServiceImpl||method=collectClusterNodeSettings||ip={}||id={}||msg=node has exist!",
                    nodePO.getIp(), nodePO.getId());
                shouldEdit.add(nodePO);
            } else {
                LOGGER.info(
                    "class=ESRoleClusterHostServiceImpl||method=collectClusterNodeSettings||ip={}||msg=node is new!",
                    nodePO.getIp());
                shouldAdd.add(nodePO);
            }
        }

        roleClusterHostDAO.offlineByCluster(cluster);

        if (!addNodeBatch(shouldAdd)) {
            throw new AdminTaskException("保存新增节点失败");
        }
        if (!editNodeBatch(shouldEdit)) {
            throw new AdminTaskException("修改节点信息失败");
        }

        return true;
    }

    @Override
    public int getIndicesCount(String cluster, String racks) {
        String nodes = getHostNodeNames(cluster, racks);
        return esClusterNodeDAO.getIndicesCount(cluster, nodes);
    }

    @Override
    public List<ESRoleClusterHost> listOnlineNode() {
        List<ESRoleClusterHostPO> pos = roleClusterHostDAO.listOnlineNode();
        return ConvertUtil.list2List(pos, ESRoleClusterHost.class);
    }

    @Override
    public Result<Long> save(ESRoleClusterHost roleClusterHost) {
        ESRoleClusterHostPO esRoleClusterHostPO = ConvertUtil.obj2Obj(roleClusterHost, ESRoleClusterHostPO.class);
        boolean succ = (1 == roleClusterHostDAO.insert(esRoleClusterHostPO));
        return Result.build(succ, esRoleClusterHostPO.getId());
    }

    @Override
    public ESRoleClusterHost getById(Long id) {
        return ConvertUtil.obj2Obj(roleClusterHostDAO.getById(id), ESRoleClusterHost.class);
    }

    @Override
    public List<ESRoleClusterHost> getByRoleClusterId(Long roleClusterId) {
        List<ESRoleClusterHostPO> roleClusterPOS = roleClusterHostDAO.listByRoleClusterId(roleClusterId);

        if (null == roleClusterPOS) {
            return Arrays.asList();
        }
        return ConvertUtil.list2List(roleClusterPOS, ESRoleClusterHost.class);
    }

    @Override
    public List<String> getHostNamesByRoleAndClusterId(Long clusterId, String role) {
        List<String> allHostNames = roleClusterHostDAO.listHostNamesByRoleAndclusterId(clusterId, role);

        if (CollectionUtils.isEmpty(allHostNames)) {
            return Arrays.asList();
        }
        return allHostNames;
    }

    @Override
    public List<ESRoleClusterHost> getByRoleAndClusterId(Long clusterId, String role) {
        return ConvertUtil.list2List(roleClusterHostDAO.listByRoleAndClusterId(clusterId, role),
            ESRoleClusterHost.class);
    }

    @Override
    public Result deleteByCluster(String cluster) {
        boolean success = (roleClusterHostDAO.deleteByCluster(cluster) > 0);
        if (!success) {
            return Result.buildFail("failed to delete the clusterHost");
        }
        return Result.buildSucc("success to delete the clusterHost");
    }

    @Override
    public List<ESRoleClusterHost> listAllNode() {
        List<ESRoleClusterHostPO> pos = roleClusterHostDAO.listAll();
        return ConvertUtil.list2List(pos, ESRoleClusterHost.class);
    }

    @Override
    public List<ESRoleClusterHost> listRacksNodes(String clusterName, String racks) {
        List<ESRoleClusterHost> nodes = new ArrayList<>();
        if (StringUtils.isAnyBlank(clusterName, racks)) {
            return nodes;
        }

        for (String rack : RackUtils.racks2List(racks)) {
            ESRoleClusterHostDTO queryClusterNodeRequest = new ESRoleClusterHostDTO();
            queryClusterNodeRequest.setCluster(clusterName);
            queryClusterNodeRequest.setRack(rack);
            nodes.addAll(queryNodeByCondt(queryClusterNodeRequest));
        }

        return nodes;
    }

    @Override
    public Result setHostValid(ESRoleClusterHost deleteHost) {
        boolean success = 1 == roleClusterHostDAO.updateHostValid(deleteHost);
        if (success) {
            return Result.buildSucc();
        }
        return Result.buildFail();
    }

    @Override
    public ESRoleClusterHost getDeleteHostByHostNameAnRoleId(String hostname, Long roleId) {
        return ConvertUtil.obj2Obj(roleClusterHostDAO.getDeleteHostByHostNameAnRoleId(hostname, roleId),
            ESRoleClusterHost.class);
    }

    @Override
    public ESRoleClusterHost getByHostName(String hostName) {
        return ConvertUtil.obj2Obj(roleClusterHostDAO.getByHostName(hostName), ESRoleClusterHost.class);
    }

    @Override
    public Result deleteById(Long id) {
        boolean success = (1 == roleClusterHostDAO.delete(id));
        if (!success) {
            return Result.buildFail("failed to delete the clusterHost");
        }
        return Result.buildSucc("success to delete the clusterHost");
    }

    /***************************************** private method ****************************************************/
    private Result checkNodeParam(ESRoleClusterHostDTO param, OperationEnum operation) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("节点信息为空");
        }

        if (OperationEnum.ADD.equals(operation)) {
            if (AriusObjUtils.isNull(param.getHostname())) {
                return Result.buildParamIllegal("节点名字为空");
            }
            if (AriusObjUtils.isNull(param.getCluster())) {
                return Result.buildParamIllegal("集群为空");
            }
            if (AriusObjUtils.isNull(param.getIp())) {
                return Result.buildParamIllegal("IP地址为空");
            }
            if (AriusObjUtils.isNullStr(param.getPort())) {
                return Result.buildParamIllegal("节点端口为空");
            }
            if (AriusObjUtils.isNull(param.getRole())) {
                return Result.buildParamIllegal("节点角色为空");
            }
            if (AriusObjUtils.isNull(param.getStatus())) {
                return Result.buildParamIllegal("节点状态为空");
            }
            if (AriusObjUtils.isNullStr(param.getRack())) {
                return Result.buildParamIllegal("节点rack为空");
            }
            if (AriusObjUtils.isNullStr(param.getNodeSet())) {
                return Result.buildParamIllegal("节点set为空");
            }
        } else if (OperationEnum.EDIT.equals(operation)) {
            ESRoleClusterHostPO oldClusterHostPo = roleClusterHostDAO.getById(param.getId());
            if (oldClusterHostPo == null) {
                return Result.buildNotExist("节点不存在");
            }
        }

        if (param.getRole() != null) {
            if (ESClusterNodeRoleEnum.UNKNOWN == ESClusterNodeRoleEnum.valueOf(param.getRole())) {
                return Result.buildParamIllegal("节点角色非法");
            }
        }

        if (param.getStatus() != null) {
            if (ESClusterNodeStatusEnum.UNKNOWN == ESClusterNodeStatusEnum.valueOf(param.getStatus())) {
                return Result.buildParamIllegal("节点状态非法");
            }
        }

        return Result.buildSucc();
    }

    private boolean addNodeBatch(List<ESRoleClusterHostPO> nodePOs) {
        return CollectionUtils.isEmpty(nodePOs) || nodePOs.size() == roleClusterHostDAO.insertBatch(nodePOs);
    }

    private boolean editNodeBatch(List<ESRoleClusterHostPO> nodePOs) {
        if (CollectionUtils.isEmpty(nodePOs)) {
            return true;
        }

        boolean succ = true;
        for (ESRoleClusterHostPO nodePO : nodePOs) {
            succ &= editNode(ConvertUtil.obj2Obj(nodePO, ESRoleClusterHostDTO.class)).success();
        }

        return succ;
    }

    private String getHostNodeNames(String cluster, String racks) {
        List<ESRoleClusterHostPO> rackNodePOs = Lists.newArrayList();
        for (String rack : racks.split(",")) {
            List<ESRoleClusterHostPO> nodePOS = roleClusterHostDAO.listByClusterAndRack(cluster, rack);
            rackNodePOs.addAll(nodePOS);
        }
        return rackNodePOs.stream().map(ESRoleClusterHostPO::getHostname).collect(Collectors.joining(","));
    }

    /**
     * 获取集群内节点配置
     * @param cluster 集群名称
     * @return EsClusterNodePO列表
     */
    private Set<ESRoleClusterHostPO> getClusterHostByCluster(String cluster) {
        Set<ESRoleClusterHostPO> nodePOSet = Sets.newHashSet();

        // 从ES集群获取节点设置，key-节点ID(ES集群生成的唯一ID)，value-节点设置
        Map<String, ClusterNodeSettings> nodeSettingsMap = esClusterNodeDAO.getSettingsByCluster(cluster);
        if (nodeSettingsMap == null || nodeSettingsMap.isEmpty()) {
            return nodePOSet;
        }

        // 节点设置列表
        List<ClusterNodeSettings> nodeSettingsInCluster = Lists.newArrayList(nodeSettingsMap.values());

        // 节点名到节点设置的map, 注意一台机器上部署多个节点，节点名可能一样，因此为MultiMap
        Multimap<String, ClusterNodeSettings> name2ClusterNodeSettingsMultiMap = ConvertUtil
            .list2MulMap(nodeSettingsInCluster, ClusterNodeSettings::getName);

        for (Map.Entry<String, Collection<ClusterNodeSettings>> entry : name2ClusterNodeSettingsMultiMap.asMap()
            .entrySet()) {

            // 节点名
            String nodeName = entry.getKey();
            if (StringUtils.isBlank(nodeName)) {
                continue;
            }

            // 节点设置
            List<ClusterNodeSettings> nodeSettingsList = entry.getValue().stream().filter(Objects::nonNull)
                .collect(Collectors.toList());

            // 构造节点记录数据
            List<ESRoleClusterHostPO> roleClusterHostPOs = buildEsClusterHostPO(nodeSettingsList, cluster);

            // 设置节点所属的角色记录ID
            setRoleClusterId(roleClusterHostPOs, cluster);

            nodePOSet.addAll(roleClusterHostPOs);
        }

        return nodePOSet;
    }

    private List<ESRoleClusterHostPO> buildEsClusterHostPO(List<ClusterNodeSettings> nodeSettingsList, String cluster) {
        List<ESRoleClusterHostPO> roleClusterHostPOS = new ArrayList<>(nodeSettingsList.size());

        for (ClusterNodeSettings nodeSettings : nodeSettingsList) {
            ESRoleClusterHostPO nodePO = new ESRoleClusterHostPO();
            nodePO.setHostname(nodeSettings.getName());
            // todo：ecm同步节点状态需要修改，不能依赖host表已有记录
            nodePO.setIp(Getter.withDefault(nodeSettings.getIp(), ""));
            nodePO.setCluster(cluster);
            nodePO.setPort("");
            nodePO.setStatus(ESClusterNodeStatusEnum.ONLINE.getCode());
            nodePO.setRack(getRackFromNodeSettings(nodeSettings));
            nodePO.setNodeSet("");
            nodePO.setRole(getRoleFromNodeSettings(nodeSettings));
            roleClusterHostPOS.add(nodePO);
        }

        return roleClusterHostPOS;
    }

    /**
     * 设置节点所属的角色记录ID
     * @param roleClusterHostPOs 节点记录
     * @param cluster            物理集群名
     * @return
     */
    private void setRoleClusterId(List<ESRoleClusterHostPO> roleClusterHostPOs, String cluster) {
        for (ESRoleClusterHostPO roleClusterHostPO : roleClusterHostPOs) {
            ESClusterNodeRoleEnum role = ESClusterNodeRoleEnum.valueOf(roleClusterHostPO.getRole());
            ESRoleCluster roleCluster = esRoleClusterService.createRoleClusterIfNotExist(cluster, role.getDesc());
            roleClusterHostPO.setRoleClusterId(roleCluster.getId());
        }
    }

    /**
     * 从节点设置获取节点rack
     * @param nodeSettings 节点设置
     * @return
     */
    private String getRackFromNodeSettings(ClusterNodeSettings nodeSettings) {
        try {
            if (nodeSettings.getAttributes() == null) {
                return "";
            }

            return Getter.withDefault(nodeSettings.getAttributes().getRack(), "");
        } catch (Exception e) {
            LOGGER.warn("method=getRack||mg=analyze rack fail||nodeSettings={}", JSON.toJSON(nodeSettings));
        }
        return "";
    }

    /**
     * 从节点设置获取节点角色，注意一个节点可能会有多种角色，取角色的优先级为：data->master->client
     * @param settings 节点设置
     * @return
     */
    private Integer getRoleFromNodeSettings(ClusterNodeSettings settings) {
        // 高版本
        JSONArray roles = settings.getRoles();
        if (roles != null && roles.size() > 0) {
            if (roles.contains(ES_ROLE_DATA)) {
                return DATA_NODE.getCode();
            }
            if (roles.contains(ES_ROLE_MASTER)) {
                return ESClusterNodeRoleEnum.MASTER_NODE.getCode();
            }
        }

        // 低版本
        NodeAttributes attributes = settings.getAttributes();
        if (attributes != null && attributes.isData()) {
            return DATA_NODE.getCode();
        }

        if (attributes != null && attributes.isMaster()) {
            return ESClusterNodeRoleEnum.MASTER_NODE.getCode();
        }

        return ESClusterNodeRoleEnum.CLIENT_NODE.getCode();
    }

}

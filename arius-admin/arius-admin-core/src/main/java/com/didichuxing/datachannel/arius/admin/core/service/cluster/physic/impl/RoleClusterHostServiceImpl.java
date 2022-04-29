package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum.OFFLINE;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum.ONLINE;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;
import com.didiglobal.logi.elasticsearch.client.response.model.http.HttpInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESRoleClusterHostPO;

import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESRoleClusterHostDAO;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * ES集群表对应各角色主机列表 服务实现类
 * @author didi
 * @since 2020-08-24
 */
@Service
public class RoleClusterHostServiceImpl implements RoleClusterHostService {

    private static final ILog    LOGGER = LogFactory.getLog(RoleClusterHostServiceImpl.class);

    @Autowired
    private ESRoleClusterHostDAO roleClusterHostDAO;

    @Autowired
    private RoleClusterService   roleClusterService;

    @Autowired
    private ESClusterNodeService esClusterNodeService;

    @Autowired
    private ESClusterService     esClusterService;

    @Override
    public List<RoleClusterHost> queryNodeByCondt(ESRoleClusterHostDTO condt) {
        List<ESRoleClusterHostPO> pos = roleClusterHostDAO
            .listByCondition(ConvertUtil.obj2Obj(condt, ESRoleClusterHostPO.class));
        return ConvertUtil.list2List(pos, RoleClusterHost.class);
    }

    @Override
    public List<RoleClusterHost> getNodesByCluster(String cluster) {
        List<ESRoleClusterHostPO> pos = roleClusterHostDAO.listByCluster(cluster);
        return ConvertUtil.list2List(pos, RoleClusterHost.class);
    }

    @Override
    public List<RoleClusterHost> getOnlineNodesByCluster(String cluster) {
        List<RoleClusterHost> roleClusterHosts = getNodesByCluster(cluster);
        if (CollectionUtils.isEmpty(roleClusterHosts)) {
            return Lists.newArrayList();
        }
        return roleClusterHosts.stream().filter(esClusterNode -> ONLINE.getCode() == esClusterNode.getStatus())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> editNodeStatus(ESRoleClusterHostDTO param, String operator) {
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
    public Result<Void> editNode(ESRoleClusterHostDTO param) {
        Result<Void> checkResult = checkNodeParam(param, OperationEnum.EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=RoleClusterHostServiceImpl||method=editNode|msg={}", checkResult.getMessage());
            return checkResult;
        }
        return Result.build(1 == roleClusterHostDAO.update(ConvertUtil.obj2Obj(param, ESRoleClusterHostPO.class)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collectClusterNodeSettings(String cluster) throws AdminTaskException{
        // get node information from ES engine
        List<ESRoleClusterHostPO> nodesFromEs = getClusterHostFromEsAndCreateRoleClusterIfNotExist(cluster);
        if (CollectionUtils.isEmpty(nodesFromEs)) {
            roleClusterHostDAO.offlineByCluster(cluster);
            LOGGER.warn(
                "class=RoleClusterHostServiceImpl||method=collectClusterNodeSettings||clusterPhyName={}||errMag=fail to collect cluster node settings",
                cluster);
            return false;
        }

        // get node info from db
        Map<String/*roleClusterId@esNodeName*/, ESRoleClusterHostPO> nodePOFromDbMap = getNodeInfoFromDbMap(cluster);
        List<ESRoleClusterHostPO> shouldAdd                            = Lists.newArrayList();
        List<ESRoleClusterHostPO> shouldEdit                           = Lists.newArrayList();

        for (ESRoleClusterHostPO nodePO : nodesFromEs) {
            if (nodePOFromDbMap.containsKey(nodePO.getKey())) {
                nodePO.setId(nodePOFromDbMap.get(nodePO.getKey()).getId());
                LOGGER.info(
                    "class=RoleClusterHostServiceImpl||method=collectClusterNodeSettings||nodeName={}||id={}||msg=node has exist!",
                    nodePO.getNodeSet(), nodePO.getId());
                shouldEdit.add(nodePO);
            } else {
                LOGGER.info(
                    "class=RoleClusterHostServiceImpl||method=collectClusterNodeSettings||ip={}||msg=node is new!",
                    nodePO.getIp());
                shouldAdd.add(nodePO);
            }
        }

        return addAndEditNodes(cluster, shouldAdd, shouldEdit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveClusterNodeSettings(ClusterJoinDTO param) throws AdminTaskException {
        if (null == param.getRoleClusterHosts()) {
            return false;
        }
        List<ESRoleClusterHostPO> nodePOList = new ArrayList<>();
        for (ESRoleClusterHostDTO node: param.getRoleClusterHosts()) {
            ESRoleClusterHostPO roleClusterHostPO = buildEsClusterHostPO(node);
            setRoleClusterId(roleClusterHostPO, param.getCluster());
            roleClusterHostPO.setStatus(OFFLINE.getCode());
            nodePOList.add(roleClusterHostPO);
        }
        return addAndEditNodes(param.getCluster(), nodePOList, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createClusterNodeSettings(List<ESClusterRoleHost> param, String phyClusterName) throws AdminTaskException {
        if (CollectionUtils.isEmpty(param) || StringUtils.isBlank(phyClusterName)) {
            return false;
        }

        List<ESRoleClusterHostPO> nodePOList = new ArrayList<>();
        param.stream().filter(esClusterRoleHost -> !AriusObjUtils.isNull(esClusterRoleHost.getHostname()))
                .forEach(esClusterRoleHost -> nodePOList.add(buildEsClusterHostPOFromEcmTaskOrder(esClusterRoleHost, phyClusterName)));

        return addAndEditNodes(phyClusterName, nodePOList, null);
    }

    private ESRoleClusterHostPO buildEsClusterHostPOFromEcmTaskOrder(ESClusterRoleHost esClusterRoleHost, String phyClusterName) {
        ESRoleClusterHostPO roleClusterHostPO = ConvertUtil.obj2Obj(esClusterRoleHost, ESRoleClusterHostPO.class);
        roleClusterHostPO.setHostname(roleClusterHostPO.getIp());
        roleClusterHostPO.setCluster(phyClusterName);
        roleClusterHostPO.setRole(ESClusterNodeRoleEnum.getByDesc(esClusterRoleHost.getRole()).getCode());
        roleClusterHostPO.setStatus(2);
        roleClusterHostPO.setRack("");
        roleClusterHostPO.setNodeSet("");
        setRoleClusterId(roleClusterHostPO, phyClusterName);
        return roleClusterHostPO;
    }

    @Override
    public int getIndicesCount(String cluster, String racks) {
        String nodes = getHostNodeNames(cluster, racks);
        return esClusterNodeService.syncGetIndicesCount(cluster, nodes);
    }

    @Override
    public List<RoleClusterHost> listOnlineNode() {
        List<ESRoleClusterHostPO> pos = roleClusterHostDAO.listOnlineNode();
        return ConvertUtil.list2List(pos, RoleClusterHost.class);
    }

    @Override
    public Result<Long> save(RoleClusterHost roleClusterHost) {
        ESRoleClusterHostPO esRoleClusterHostPO = ConvertUtil.obj2Obj(roleClusterHost, ESRoleClusterHostPO.class);
        boolean succ = (1 == roleClusterHostDAO.insert(esRoleClusterHostPO));
        return Result.build(succ, esRoleClusterHostPO.getId());
    }

    @Override
    public RoleClusterHost getById(Long id) {
        return ConvertUtil.obj2Obj(roleClusterHostDAO.getById(id), RoleClusterHost.class);
    }

    @Override
    public List<RoleClusterHost> getByRoleClusterId(Long roleClusterId) {
        List<ESRoleClusterHostPO> roleClusterPOS = roleClusterHostDAO.listByRoleClusterId(roleClusterId);

        if (null == roleClusterPOS) {
            return Lists.newArrayList();
        }
        return ConvertUtil.list2List(roleClusterPOS, RoleClusterHost.class);
    }

    @Override
    public Map<Long,List<RoleClusterHost>> getByRoleClusterIds(List<Long> roleClusterIds) {
        if(CollectionUtils.isEmpty(roleClusterIds)){
            return new HashMap<>();
        }
        List<ESRoleClusterHostPO> roleClusterPOS = roleClusterHostDAO.listByRoleClusterIds(roleClusterIds);
        Map<Long, List<RoleClusterHost>> ret = new HashMap<>();
        if (CollectionUtils.isNotEmpty(roleClusterPOS)) {
            List<RoleClusterHost> list = ConvertUtil.list2List(roleClusterPOS, RoleClusterHost.class);
            ret = list.stream().collect(Collectors.groupingBy(RoleClusterHost::getRoleClusterId));
        }
        return ret;
    }


    @Override
    public List<String> getHostNamesByRoleAndClusterId(Long clusterId, String role) {
        List<RoleClusterHost> roleClusterHosts = getByRoleAndClusterId(clusterId, role);
        return roleClusterHosts.stream().map(RoleClusterHost::getHostname).collect(Collectors.toList());
    }

    @Override
    public List<RoleClusterHost> getByRoleAndClusterId(Long clusterId, String role) {
        RoleCluster roleCluster = roleClusterService.getByClusterIdAndRole(clusterId, role);
        if(null == roleCluster) {
            return Arrays.asList();
        }
        return ConvertUtil.list2List(roleClusterHostDAO.listByRoleClusterId(roleCluster.getId()),
                RoleClusterHost.class);
    }

    @Override
    public Result<Void> deleteByCluster(String cluster) {
        boolean success = (roleClusterHostDAO.deleteByCluster(cluster) > 0);
        if (!success) {
            return Result.buildFail("failed to delete the clusterHost");
        }
        return Result.buildSuccWithMsg("success to delete the clusterHost");
    }

    @Override
    public List<RoleClusterHost> listAllNode() {
        List<ESRoleClusterHostPO> pos = roleClusterHostDAO.listAll();
        return ConvertUtil.list2List(pos, RoleClusterHost.class);
    }

    @Override
    public List<RoleClusterHost> listRacksNodes(String clusterName, String racks) {
        List<RoleClusterHost> nodes = new ArrayList<>();
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
    public Result<Void> setHostValid(RoleClusterHost deleteHost) {
        boolean success = 1 == roleClusterHostDAO.updateHostValid(deleteHost);
        if (success) {
            return Result.buildSucc();
        }
        return Result.buildFail();
    }

    @Override
    public RoleClusterHost getDeleteHostByHostNameAnRoleId(String hostname, Long roleId) {
        return ConvertUtil.obj2Obj(roleClusterHostDAO.getDeleteHostByHostNameAnRoleId(hostname, roleId),
            RoleClusterHost.class);
    }

    @Override
    public RoleClusterHost getByHostName(String hostName) {
        return ConvertUtil.obj2Obj(roleClusterHostDAO.getByHostName(hostName), RoleClusterHost.class);
    }

    @Override
    public Result<Void> deleteById(Long id) {
        boolean success = (1 == roleClusterHostDAO.delete(id));
        if (!success) {
            return Result.buildFail("failed to delete the clusterHost");
        }
        return Result.buildSuccWithMsg("success to delete the clusterHost");
    }

    @Override
    public Result deleteByHostNameAndRoleId(List<String> hostnames, Long roleId) {
        boolean success = (hostnames.size() == roleClusterHostDAO.deleteByHostNameAndRoleId(hostnames, roleId));
        if (!success) {
            LOGGER.error(
                    "class=RoleClusterHostServiceImpl||method=deleteByHostNameAndRoleId||hostname={}||roleId={}"
                            + "msg=failed to delete roleClusterHost",
                    hostnames, roleId);
            return Result.buildFail("failed to delete the clusterHost");
        }
        return Result.buildSuccWithMsg("success to delete the clusterHost");
    }

    @Override
    public String buildESClientHttpAddressesStr(List<ESRoleClusterHostDTO> roleClusterHosts) {
        return ListUtils.strList2String(buildESClientMasterHttpAddressesList(roleClusterHosts));
    }

    @Override
    public List<String> buildESClientMasterHttpAddressesList(List<ESRoleClusterHostDTO> roleClusterHosts){
        List<String> clientAddressesList = buildESRoleNodeHttpAddressesList(roleClusterHosts, CLIENT_NODE.getCode());
        List<String> masterAddressesList = buildESRoleNodeHttpAddressesList(roleClusterHosts, MASTER_NODE.getCode());
        return Stream.of(clientAddressesList, masterAddressesList).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public List<String> buildESAllRoleHttpAddressesList(List<ESRoleClusterHostDTO> roleClusterHosts){
        List<String> dataAddressesList = buildESRoleNodeHttpAddressesList(roleClusterHosts, DATA_NODE.getCode());
        return Stream.of(dataAddressesList, buildESClientMasterHttpAddressesList(roleClusterHosts)).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public int getPodNumberByRoleId(Long roleId) {
        return roleClusterHostDAO.getPodNumberByRoleId(roleId);
    }

    /***************************************** private method ****************************************************/
    private Result<Void> checkNodeParam(ESRoleClusterHostDTO param, OperationEnum operation) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("节点信息为空");
        }

        if (OperationEnum.ADD.equals(operation)) {
            Result<Void> isNullResult = isNull(param);
            if (isNullResult.failed()){return isNullResult;}
        } else if (OperationEnum.EDIT.equals(operation)) {
            ESRoleClusterHostPO oldClusterHostPo = roleClusterHostDAO.getById(param.getId());
            if (oldClusterHostPo == null) {
                return Result.buildNotExist("节点不存在");
            }
        }

        Result<Void> isIllegalResult = isIllegal(param);
        if (isIllegalResult.failed()){return isIllegalResult;}

        return Result.buildSucc();
    }

    private Result<Void> isIllegal(ESRoleClusterHostDTO param) {
        if (param.getRole() != null
            && ESClusterNodeRoleEnum.UNKNOWN == ESClusterNodeRoleEnum.valueOf(param.getRole())) {
            return Result.buildParamIllegal("节点角色非法");
        }

        if (param.getStatus() != null
            && ESClusterNodeStatusEnum.UNKNOWN == ESClusterNodeStatusEnum.valueOf(param.getStatus())) {
            return Result.buildParamIllegal("节点状态非法");
        }
        return Result.buildSucc();
    }

    private Result<Void> isNull(ESRoleClusterHostDTO param) {
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
        return Result.buildSucc();
    }

    private boolean addNodeBatch(List<ESRoleClusterHostPO> nodePOs) {
        // 数据修改的行数成功数
        int count = 0;
        for (ESRoleClusterHostPO param : nodePOs) {
            if (null != roleClusterHostDAO.getDeleteHostByHostNameAnRoleId(param.getHostname(), param.getRoleClusterId())) {
                count += roleClusterHostDAO.restoreByHostNameAndRoleId(param.getHostname(), param.getRoleClusterId());
                continue;
            }
            count += roleClusterHostDAO.insert(param);
        }
        return CollectionUtils.isEmpty(nodePOs) || nodePOs.size() == count;
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
    private List<ESRoleClusterHostPO> getClusterHostFromEsAndCreateRoleClusterIfNotExist(String cluster) {
        List<ESRoleClusterHostPO> nodePOList = Lists.newArrayList();

        // 从ES集群中获取初始的节点信息列表
        List<ClusterNodeInfo> clusterNodeInfos = buildAllClusterNodeInfoFromES(cluster);


        // 根据集群节点角色信息构建入DB的host列表信息
        for (ClusterNodeInfo nodeInfoListFromArius : clusterNodeInfos) {
            // 构造节点记录数据
            ESRoleClusterHostPO roleClusterHostPO = buildEsClusterHostPO(nodeInfoListFromArius, cluster);
            // 节点所属的角色记录如果不存在，则去设置
            setRoleClusterId(roleClusterHostPO, cluster);

            nodePOList.add(roleClusterHostPO);
        }

        return nodePOList;
    }

    /**
     * 从es中获取全部的集群节点信息，尤其包括节点角色的初始化操作
     * @param cluster 集群名称
     * @return
     */
    private List<ClusterNodeInfo> buildAllClusterNodeInfoFromES(String cluster) {
        List<ClusterNodeInfo> clusterNodeInfoListFromES = Lists.newArrayList();

        // 从ES集群获取节点全量的信息
        Map<String, ClusterNodeInfo> clusterNodeInfoMap = esClusterService.syncGetAllSettingsByCluster(cluster);
        // 获取高低版本通用的节点角色信息
        Map<String, ClusterNodeSettings> clusterNodeSettingsMap = esClusterService.syncGetPartOfSettingsByCluster(cluster);

        if (MapUtils.isEmpty(clusterNodeSettingsMap) || MapUtils.isEmpty(clusterNodeInfoMap)) {
            return clusterNodeInfoListFromES;
        }

        // 构建原生集群节点信息列表
        for (Map.Entry</*UUID*/String, ClusterNodeInfo> entry : clusterNodeInfoMap.entrySet()) {
            // 为纳管2.3.3版本的角色信息，需要从_nodes/settings中获取
            ClusterNodeInfo clusterNodeInfo = entry.getValue();
            // 根据节点的UUID获取对应的全量的角色信息
            ClusterNodeSettings clusterNodeSettings = clusterNodeSettingsMap.get(entry.getKey());
            // 重新设置clusterNodeInfo的roles列表
            clusterNodeInfo.setRoles(buildRolesInfoFromSettings(clusterNodeSettings));
            // 构建节点角色的多角色信息
            buildMultiRoleListForESNode(clusterNodeInfoListFromES, clusterNodeInfo);
        }

        return CollectionUtils.isEmpty(clusterNodeInfoListFromES) ? (List<ClusterNodeInfo>) clusterNodeInfoMap.values() : clusterNodeInfoListFromES;
    }

    /**
     * 构建节点的多角色信息列表
     * @param clusterNodeInfoListFromES 节点信息列表
     * @param clusterNodeInfo 节点信息对象
     */
    private void buildMultiRoleListForESNode(List<ClusterNodeInfo> clusterNodeInfoListFromES, ClusterNodeInfo clusterNodeInfo) {
        // 根据当前角色列表添加节点角色信息
        List<String> roles = clusterNodeInfo.getRoles();
        if (notHasRoleOfMasterAndData(roles)) {
            //该节点既不含有data角色也不含有master角色，则添加client角色
            roles.add(ES_ROLE_CLIENT);
        }

        //根据es获取的节点信息构建节点角色信息
        addNodeRoleInfoFromES(clusterNodeInfoListFromES, clusterNodeInfo, roles);
    }

    private List<String> buildRolesInfoFromSettings(ClusterNodeSettings clusterNodeSetting) {
        List<String> roles = JSONArray.parseArray(JSON.toJSONString(clusterNodeSetting.getRoles()), String.class);
        if (CollectionUtils.isEmpty(roles)) {
            JSONObject roleNode = clusterNodeSetting.getSettings().getJSONObject("node");
            List<String> roleInfo = Lists.newArrayList();
            if (AriusObjUtils.isNull(roleNode)) {
                roleInfo.add(ES_ROLE_DATA);
                roleInfo.add(ES_ROLE_MASTER);
            } else {
                for (String role : ESClusterNodeRoleEnum.nodeRoleList()) {
                    if (role.equalsIgnoreCase(ES_ROLE_CLIENT)) {
                        continue;
                    }
                    if (!roleNode.containsKey(role) || roleNode.getBoolean(role)) {
                        roleInfo.add(role);
                    }
                }
            }


            return roleInfo;
        }

        return roles;
    }

    private void addNodeRoleInfoFromES(List<ClusterNodeInfo> clusterNodeInfoListFromArius,
                                       ClusterNodeInfo clusterNodeInfo, List<String> roles) {
        for (String role : roles) {
            //对于节点的角色进行过滤，平台只兼容data,master和client三种角色类型
            if (!ESClusterNodeRoleEnum.getByDesc(role + "node").equals(UNKNOWN)) {
                ClusterNodeInfo nodeInfo = ConvertUtil.obj2Obj(clusterNodeInfo, ClusterNodeInfo.class);
                nodeInfo.setRoles(Lists.newArrayList(role));
                clusterNodeInfoListFromArius.add(nodeInfo);
            }
        }
    }

    private boolean hasRoleOfMasterAndData(List<String> roles) {
        return roles.contains(ES_ROLE_DATA) && roles.contains(ES_ROLE_MASTER);
    }

    private boolean notHasRoleOfMasterAndData(List<String> roles) {
        return !roles.contains(ES_ROLE_DATA) && !roles.contains(ES_ROLE_MASTER);
    }

    private ESRoleClusterHostPO buildEsClusterHostPO(ClusterNodeInfo clusterNodeInfo, String cluster) {
        ESRoleClusterHostPO nodePO = new ESRoleClusterHostPO();
        nodePO.setCluster(cluster);

        nodePO.setIp(Getter.withDefault(clusterNodeInfo.getIp(), ""));
        nodePO.setHostname(Getter.withDefault(clusterNodeInfo.getHost(), ""));
        nodePO.setNodeSet(Getter.withDefault(clusterNodeInfo.getName(), ""));

        HttpInfo httpInfo = clusterNodeInfo.getHttpInfo();
        if (null != httpInfo && null != httpInfo.getPublishAddress()) {
            String[] split = httpInfo.getPublishAddress().split(":");
            nodePO.setPort(split[split.length - 1]);
        }else {
            nodePO.setPort("");
        }

        nodePO.setStatus(ONLINE.getCode());
        nodePO.setRack(getRackFromNodeSettings(clusterNodeInfo));
        nodePO.setRole(getRoleFromNodeSettings(clusterNodeInfo));

        return nodePO;
    }

    private ESRoleClusterHostPO buildEsClusterHostPO(ESRoleClusterHostDTO nodeDTO) {
        ESRoleClusterHostPO nodePO = ConvertUtil.obj2Obj(nodeDTO, ESRoleClusterHostPO.class);
        nodePO.setHostname(Getter.withDefault(nodeDTO.getIp(), ""));
        return nodePO;
    }

    /**
     * 设置节点所属的角色记录ID
     * @param roleClusterHostPO  节点记录
     * @param cluster            物理集群名
     * @return
     */
    private void setRoleClusterId(ESRoleClusterHostPO roleClusterHostPO, String cluster) {
            ESClusterNodeRoleEnum role = ESClusterNodeRoleEnum.valueOf(roleClusterHostPO.getRole());
            RoleCluster roleCluster = roleClusterService.createRoleClusterIfNotExist(cluster, role.getDesc());
            roleClusterHostPO.setRoleClusterId(roleCluster.getId());
    }

    /**
     * 从节点设置获取节点rack
     * @param clusterNodeInfo 节点设置
     * @return
     */
    private String getRackFromNodeSettings(ClusterNodeInfo clusterNodeInfo) {
        String defaultRackValue = "";
        try {
            if (clusterNodeInfo.getRoles().contains(ES_ROLE_DATA)) {
                if (clusterNodeInfo.getAttributes() == null) {
                    defaultRackValue = "*";
                } else {
                    defaultRackValue = Getter.withDefault(clusterNodeInfo.getAttributes().get(RACK), "*");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("class=RoleClusterHostServiceImpl||method=getRack||mg=analyze rack fail||nodeSettings={}", JSON.toJSON(clusterNodeInfo));
        }
        return defaultRackValue;
    }

    private Map<String/*roleClusterId@esNodeName*/ , ESRoleClusterHostPO> getNodeInfoFromDbMap(String cluster) {
        List<ESRoleClusterHostPO> nodesFromDB = roleClusterHostDAO.listByCluster(cluster);

        LOGGER.info("class=RoleClusterHostServiceImpl||method=getNodeInfoFromDbMap||cluster={}||dbSize={}", cluster,
                nodesFromDB.size());

        return ConvertUtil.list2Map(nodesFromDB, ESRoleClusterHostPO::getKey);
    }

    /**
     * 从节点设置获取节点角色，注意一个节点可能会有多种角色，取角色的优先级为：data->master->client
     * @param clusterNodeInfo 节点设置
     * @return
     */
    private Integer getRoleFromNodeSettings(ClusterNodeInfo clusterNodeInfo) {
        // 高版本
        List<String> roles = clusterNodeInfo.getRoles();
        if (roles != null && !roles.isEmpty()) {
            if (roles.contains(ES_ROLE_DATA)) {
                return DATA_NODE.getCode();
            }
            if (roles.contains(ES_ROLE_MASTER)) {
                return MASTER_NODE.getCode();
            }
        }

        return CLIENT_NODE.getCode();
    }

    private boolean updateRolePod(List<ESRoleClusterHostPO> shouldAdd, String cluster) {
        if (CollectionUtils.isEmpty(shouldAdd)) {
            return true;
        }

        Map<Integer, List<ESRoleClusterHostPO>> role2ESRoleClusterHostPOListMap = ConvertUtil.list2MapOfList(shouldAdd,
            ESRoleClusterHostPO::getRole, esRoleClusterHostPO -> esRoleClusterHostPO);

        AtomicBoolean flag = new AtomicBoolean(true);
        role2ESRoleClusterHostPOListMap.forEach((role, roleClusterHostPOList) -> {
            RoleCluster roleCluster = roleClusterService.getByClusterNameAndRole(cluster, valueOf(role).getDesc());
            roleCluster.setPodNumber(roleClusterHostDAO.getPodNumberByRoleId(roleCluster.getId()));
            Result<Void> result = roleClusterService.updatePodByClusterIdAndRole(roleCluster);
            if (result.failed()) {
                flag.set(false);
            }
        });

        return flag.get();
    }

    private boolean addAndEditNodes(String cluster, List<ESRoleClusterHostPO> shouldAdd, List<ESRoleClusterHostPO> shouldEdit) {
        roleClusterHostDAO.offlineByCluster(cluster);
        boolean flag = addNodeBatch(shouldAdd);
        if (flag) {
            if (!updateRolePod(shouldAdd, cluster)) {
                throw new AdminTaskException("更新新增节点数量失败");
            }
        }else {
            LOGGER.error(
                    "class=RoleClusterHostServiceImpl||method=collectClusterNodeSettings||clusterPhyName={}||addNode={}"
                            + "||errMag=fail to add cluster node to arius",
                    cluster, shouldAdd);
        }
        if (!editNodeBatch(shouldEdit)) {
            LOGGER.error(
                    "class=RoleClusterHostServiceImpl||method=collectClusterNodeSettings||clusterPhyName={}||addNode={}"
                            + "||errMag=fail to edit cluster node to arius",
                    cluster, shouldEdit);
        }
        return true;
    }

    private List<String> buildESRoleNodeHttpAddressesList(List<ESRoleClusterHostDTO> roleClusterHosts, Integer roleCode){
        Map<Integer, List<ESRoleClusterHostDTO>> role2ESRoleClusterHostDTOMap = ConvertUtil.list2MapOfList(
                roleClusterHosts, ESRoleClusterHostDTO::getRole, esRoleClusterHostDTO -> esRoleClusterHostDTO);

        List<String> httpAddressesList = Lists.newArrayList();
        List<ESRoleClusterHostDTO> esRoleClusterHostDTOS = role2ESRoleClusterHostDTOMap.get(roleCode);
        if (null != esRoleClusterHostDTOS) {
            esRoleClusterHostDTOS.forEach(host -> httpAddressesList.add(host.getIp() + ":" + host.getPort()));
        }
        return httpAddressesList;
    }
}

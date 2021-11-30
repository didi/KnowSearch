package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.impl;

import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.*;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeStatusEnum.ONLINE;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.*;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didiglobal.logi.elasticsearch.client.response.model.http.HttpInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESRoleClusterHostPO;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.Getter;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.RackUtils;
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
        return Result.buildSucc("success to delete the clusterHost");
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
        return Result.buildSucc("success to delete the clusterHost");
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
        return Result.buildSucc("success to delete the clusterHost");
    }

    @Override
    public String buildESClientHttpAddressesStr(List<ESRoleClusterHostDTO> roleClusterHosts) {
        Map<Integer, List<ESRoleClusterHostDTO>> role2ESRoleClusterHostDTOMap = ConvertUtil.list2MapOfList(
                roleClusterHosts, ESRoleClusterHostDTO::getRole, esRoleClusterHostDTO -> esRoleClusterHostDTO);

        List<String> httpAddressesList = Lists.newArrayList();
        List<ESRoleClusterHostDTO> esRoleClusterHostDTOSForClient = role2ESRoleClusterHostDTOMap.get(CLIENT_NODE.getCode());
        if (CollectionUtils.isNotEmpty(esRoleClusterHostDTOSForClient)) {
            esRoleClusterHostDTOSForClient.forEach(host -> httpAddressesList.add(host.getIp() + ":" + host.getPort()));
        }else {
            List<ESRoleClusterHostDTO> esRoleClusterHostDTOSForMaster = role2ESRoleClusterHostDTOMap.get(MASTER_NODE.getCode());
            esRoleClusterHostDTOSForMaster.forEach(host -> httpAddressesList.add(host.getIp() + ":" + host.getPort()));
        }

        return ListUtils.strList2String(httpAddressesList);
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
        // 从ES集群获取节点配置信息
        Map<String/*uuid*/, ClusterNodeInfo> nodeSettingsMap = esClusterService.syncGetAllSettingsByCluster(cluster);
        if (nodeSettingsMap == null || nodeSettingsMap.isEmpty()) {
            return nodePOList;
        }

        //原生集群节点信息
        List<ClusterNodeInfo> clusterNodeInfoListFromES = Lists.newArrayList(nodeSettingsMap.values());
        //兼容单个ES节点拥有多个角色场景
        List<ClusterNodeInfo> clusterNodeInfoListFromArius = buildWithMultipleRole(clusterNodeInfoListFromES);
        for (ClusterNodeInfo nodeInfoListFromArius : clusterNodeInfoListFromArius) {
            // 构造节点记录数据
            ESRoleClusterHostPO roleClusterHostPO = buildEsClusterHostPO(nodeInfoListFromArius, cluster);
            // 节点所属的角色记录如果不存在，则去设置
            setRoleClusterId(roleClusterHostPO, cluster);

            nodePOList.add(roleClusterHostPO);
        }

        return nodePOList;
    }

    /**
     * 兼容单个ES节点拥有多个角色场景
     * @param clusterNodeInfoListFromES
     * @return
     */
    private List<ClusterNodeInfo> buildWithMultipleRole(List<ClusterNodeInfo> clusterNodeInfoListFromES) {
        List<ClusterNodeInfo> clusterNodeInfoListFromArius = Lists.newArrayList();
        for (ClusterNodeInfo clusterNodeInfo : clusterNodeInfoListFromES) {
            List<String> roles = clusterNodeInfo.getRoles();
            if (hasRoleOfMasterAndData(roles)) {
                //添加client角色
                roles.add(ES_ROLE_CLIENT);
                for (String role : roles) {
                    if (!ESClusterNodeRoleEnum.getByDesc(role + "node").equals(UNKNOWN)) {
                        ClusterNodeInfo nodeInfo = ConvertUtil.obj2Obj(clusterNodeInfo, ClusterNodeInfo.class);
                        nodeInfo.setRoles(Lists.newArrayList(role));
                        clusterNodeInfoListFromArius.add(nodeInfo);
                    }
                }
            }
        }
        
        if (CollectionUtils.isEmpty(clusterNodeInfoListFromArius)) {
            return clusterNodeInfoListFromES;
        }

        return  clusterNodeInfoListFromArius;
    }

    private boolean hasRoleOfMasterAndData(List<String> roles) {
        return roles.contains(ES_ROLE_DATA) && roles.contains(ES_ROLE_MASTER); 
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
            if (roles.contains(ES_ROLE_ML)) {
                return ML_NODE.getCode();
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
}

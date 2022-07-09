package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.CLIENT_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.MASTER_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.UNKNOWN;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.valueOf;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum.OFFLINE;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum.ONLINE;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_ROLE_CLIENT;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_ROLE_DATA;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_ROLE_MASTER;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESClusterRoleHostPO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.Getter;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESClusterRoleHostDAO;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodessetting.ClusterNodeSettings;
import com.didiglobal.logi.elasticsearch.client.response.model.http.HttpInfo;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ES集群表对应各角色主机列表 服务实现类
 * @author chengxiang
 * @date 2022/5/9
 */
@Service
public class ClusterRoleHostServiceImpl implements ClusterRoleHostService {

    private static final ILog    LOGGER = LogFactory.getLog(ClusterRoleHostServiceImpl.class);

    @Autowired
    private ESClusterRoleHostDAO clusterRoleHostDAO;

    @Autowired
    private ClusterRoleService clusterRoleService;

    @Autowired
    private ESClusterNodeService esClusterNodeService;

    @Autowired
    private ESClusterService     esClusterService;

    @Override
    public List<ClusterRoleHost> queryNodeByCondt(ESClusterRoleHostDTO condt) {
        List<ESClusterRoleHostPO> pos = clusterRoleHostDAO
            .listByCondition(ConvertUtil.obj2Obj(condt, ESClusterRoleHostPO.class));
        return ConvertUtil.list2List(pos, ClusterRoleHost.class);
    }

    @Override
    public List<ClusterRoleHost> getNodesByCluster(String cluster) {
        List<ESClusterRoleHostPO> pos = clusterRoleHostDAO.listByCluster(cluster);
        return ConvertUtil.list2List(pos, ClusterRoleHost.class);
    }

    @Override
    public List<ClusterRoleHost> getOnlineNodesByCluster(String cluster) {
        List<ClusterRoleHost> clusterRoleHosts = getNodesByCluster(cluster);
        if (CollectionUtils.isEmpty(clusterRoleHosts)) {
            return Lists.newArrayList();
        }
        return clusterRoleHosts.stream().filter(esClusterNode -> ONLINE.getCode() == esClusterNode.getStatus())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> editNodeStatus(ESClusterRoleHostDTO param, String operator) {
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

        ESClusterRoleHostPO hostPo = clusterRoleHostDAO.getById(param.getId());
        if (hostPo == null) {
            return Result.buildNotExist("节点不存在");
        }

        ESClusterRoleHostPO clusterHostPO = new ESClusterRoleHostPO();
        clusterHostPO.setId(param.getId());
        clusterHostPO.setStatus(param.getStatus());

        return Result.build(1 == (clusterRoleHostDAO.update(clusterHostPO)));
    }

    @Override
    public Result<Void> editNode(ESClusterRoleHostDTO param) {
        Result<Void> checkResult = checkNodeParam(param, OperationEnum.EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=ClusterRoleHostServiceImpl||method=editNode|msg={}", checkResult.getMessage());
            return checkResult;
        }
        return Result.build(1 == clusterRoleHostDAO.update(ConvertUtil.obj2Obj(param, ESClusterRoleHostPO.class)));
    }

    @Override
    public boolean editNodeRegionId(List<Integer> nodeIds, Integer regionId) throws AdminOperateException{
        if (CollectionUtils.isEmpty(nodeIds)) { throw new AdminOperateException("节点不存在", ResultType.ILLEGAL_PARAMS);}
        if (null == regionId) { throw new AdminOperateException("regionId为空", ResultType.ILLEGAL_PARAMS);}

        return clusterRoleHostDAO.updateRegionId(nodeIds, regionId) >= 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collectClusterNodeSettings(String cluster) throws AdminTaskException{
        // get node information from ES engine
        List<ESClusterRoleHostPO> nodesFromEs = getClusterHostFromEsAndCreateRoleClusterIfNotExist(cluster);
        if (CollectionUtils.isEmpty(nodesFromEs)) {
            clusterRoleHostDAO.offlineByCluster(cluster);
            LOGGER.warn(
                "class=RoleClusterHostServiceImpl||method=collectClusterNodeSettings||clusterPhyName={}||errMag=fail to collect cluster node settings",
                cluster);
            return false;
        }

        // get node info from db
        Map<String/*roleClusterId@esNodeName*/, ESClusterRoleHostPO> nodePOFromDbMap = getNodeInfoFromDbMap(cluster);
        List<ESClusterRoleHostPO> shouldAdd                            = Lists.newArrayList();
        List<ESClusterRoleHostPO> shouldEdit                           = Lists.newArrayList();

        for (ESClusterRoleHostPO nodePO : nodesFromEs) {
            if (nodePOFromDbMap.containsKey(nodePO.getKey())) {
                nodePO.setId(nodePOFromDbMap.get(nodePO.getKey()).getId());
                nodePO.setRegionId(nodePOFromDbMap.get(nodePO.getKey()).getRegionId());
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
        List<ESClusterRoleHostPO> nodePOList = new ArrayList<>();
        for (ESClusterRoleHostDTO node: param.getRoleClusterHosts()) {
            ESClusterRoleHostPO roleClusterHostPO = buildEsClusterHostPO(node);
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

        List<ESClusterRoleHostPO> nodePOList = new ArrayList<>();
        param.stream().filter(esClusterRoleHost -> !AriusObjUtils.isNull(esClusterRoleHost.getHostname()))
                .forEach(esClusterRoleHost -> nodePOList.add(buildEsClusterHostPOFromEcmTaskOrder(esClusterRoleHost, phyClusterName)));

        return addAndEditNodes(phyClusterName, nodePOList, null);
    }

    private ESClusterRoleHostPO buildEsClusterHostPOFromEcmTaskOrder(ESClusterRoleHost esClusterRoleHost, String phyClusterName) {
        ESClusterRoleHostPO roleClusterHostPO = ConvertUtil.obj2Obj(esClusterRoleHost, ESClusterRoleHostPO.class);
        roleClusterHostPO.setHostname(roleClusterHostPO.getIp());
        roleClusterHostPO.setCluster(phyClusterName);
        roleClusterHostPO.setRole(ESClusterNodeRoleEnum.getByDesc(esClusterRoleHost.getRole()).getCode());
        roleClusterHostPO.setStatus(2);
        roleClusterHostPO.setNodeSet("");
        setRoleClusterId(roleClusterHostPO, phyClusterName);
        return roleClusterHostPO;
    }

    @Override
    public List<ClusterRoleHost> listOnlineNode() {
        List<ESClusterRoleHostPO> pos = clusterRoleHostDAO.listOnlineNode();
        return ConvertUtil.list2List(pos, ClusterRoleHost.class);
    }

    @Override
    public Result<Long> save(ClusterRoleHost clusterRoleHost) {
        ESClusterRoleHostPO esClusterRoleHostPO = ConvertUtil.obj2Obj(clusterRoleHost, ESClusterRoleHostPO.class);
        boolean succ = (1 == clusterRoleHostDAO.insert(esClusterRoleHostPO));
        return Result.build(succ, esClusterRoleHostPO.getId());
    }

    @Override
    public ClusterRoleHost getById(Long id) {
        return ConvertUtil.obj2Obj(clusterRoleHostDAO.getById(id), ClusterRoleHost.class);
    }

    @Override
    public List<ClusterRoleHost> getByRoleClusterId(Long roleClusterId) {
        List<ESClusterRoleHostPO> roleClusterPOS = clusterRoleHostDAO.listByRoleClusterId(roleClusterId);

        if (null == roleClusterPOS) {
            return Lists.newArrayList();
        }
        return ConvertUtil.list2List(roleClusterPOS, ClusterRoleHost.class);
    }

    @Override
    public List<ClusterRoleHost> getByClusterAndNodeSets(String cluster, List<String> nodeSets) {
        List<ESClusterRoleHostPO> roleHostPOS = clusterRoleHostDAO.listByClusterAndNodeSets(cluster, nodeSets);

        if (null == roleHostPOS) {
            return Lists.newArrayList();
        }

        return ConvertUtil.list2List(roleHostPOS, ClusterRoleHost.class);
    }

    @Override
    public Map<Long,List<ClusterRoleHost>> getByRoleClusterIds(List<Long> roleClusterIds) {
        if(CollectionUtils.isEmpty(roleClusterIds)){
            return new HashMap<>();
        }
        List<ESClusterRoleHostPO> roleClusterPOS = clusterRoleHostDAO.listByRoleClusterIds(roleClusterIds);
        Map<Long, List<ClusterRoleHost>> ret = new HashMap<>();
        if (CollectionUtils.isNotEmpty(roleClusterPOS)) {
            List<ClusterRoleHost> list = ConvertUtil.list2List(roleClusterPOS, ClusterRoleHost.class);
            ret = list.stream().collect(Collectors.groupingBy(ClusterRoleHost::getRoleClusterId));
        }
        return ret;
    }


    @Override
    public List<String> getHostNamesByRoleAndClusterId(Long clusterId, String role) {
        List<ClusterRoleHost> clusterRoleHosts = getByRoleAndClusterId(clusterId, role);
        return clusterRoleHosts.stream().map(ClusterRoleHost::getHostname).collect(Collectors.toList());
    }

    @Override
    public List<ClusterRoleHost> getByRoleAndClusterId(Long clusterId, String role) {
        ClusterRoleInfo clusterRoleInfo = clusterRoleService.getByClusterIdAndRole(clusterId, role);
        if(null == clusterRoleInfo) {
            return Arrays.asList();
        }
        return ConvertUtil.list2List(clusterRoleHostDAO.listByRoleClusterId(clusterRoleInfo.getId()),
                ClusterRoleHost.class);
    }

    @Override
    public Result<Void> deleteByCluster(String cluster, Integer projectId) {
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (result.failed()) {
            return result;
        }
        boolean success = (clusterRoleHostDAO.deleteByCluster(cluster) > 0);
        if (!success) {
            return Result.buildFail("failed to delete the clusterHost");
        }
        return Result.buildSuccWithMsg("success to delete the clusterHost");
    }

    @Override
    public List<ClusterRoleHost> listAllNode() {
        List<ESClusterRoleHostPO> pos = clusterRoleHostDAO.listAll();
        return ConvertUtil.list2List(pos, ClusterRoleHost.class);
    }

    @Override
    public List<ClusterRoleHost> listAllNodeByRole(Integer roleCode) {
        return ConvertUtil.list2List(clusterRoleHostDAO.listAllByRoleCode(roleCode), ClusterRoleHost.class);
    }

    @Override
    public Result<Void> setHostValid(ClusterRoleHost deleteHost) {
        boolean success = 1 == clusterRoleHostDAO.updateHostValid(deleteHost);
        if (success) {
            return Result.buildSucc();
        }
        return Result.buildFail();
    }

    @Override
    public ClusterRoleHost getDeleteHostByHostNameAnRoleId(String hostname, Long roleId) {
        return ConvertUtil.obj2Obj(clusterRoleHostDAO.getDeleteHostByHostNameAnRoleId(hostname, roleId),
            ClusterRoleHost.class);
    }

    @Override
    public ClusterRoleHost getByHostName(String hostName) {
        return ConvertUtil.obj2Obj(clusterRoleHostDAO.getByHostName(hostName), ClusterRoleHost.class);
    }

    @Override
    public Result<Void> deleteById(Long id) {
        boolean success = (1 == clusterRoleHostDAO.delete(id));
        if (!success) {
            return Result.buildFail("failed to delete the clusterHost");
        }
        return Result.buildSuccWithMsg("success to delete the clusterHost");
    }

    @Override
    public Result deleteByHostNameAndRoleId(List<String> hostnames, Long roleId) {
        boolean success = (hostnames.size() == clusterRoleHostDAO.deleteByHostNameAndRoleId(hostnames, roleId));
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
    public String buildESClientHttpAddressesStr(List<ESClusterRoleHostDTO> roleClusterHosts) {
        return ListUtils.strList2String(buildESClientMasterHttpAddressesList(roleClusterHosts));
    }

    @Override
    public List<String> buildESClientMasterHttpAddressesList(List<ESClusterRoleHostDTO> roleClusterHosts){
        List<String> clientAddressesList = buildESRoleNodeHttpAddressesList(roleClusterHosts, CLIENT_NODE.getCode());
        List<String> masterAddressesList = buildESRoleNodeHttpAddressesList(roleClusterHosts, MASTER_NODE.getCode());
        return Stream.of(clientAddressesList, masterAddressesList).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public List<String> buildESAllRoleHttpAddressesList(List<ESClusterRoleHostDTO> roleClusterHosts){
        List<String> dataAddressesList = buildESRoleNodeHttpAddressesList(roleClusterHosts, DATA_NODE.getCode());
        return Stream.of(dataAddressesList, buildESClientMasterHttpAddressesList(roleClusterHosts)).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public int getPodNumberByRoleId(Long roleId) {
        return clusterRoleHostDAO.getPodNumberByRoleId(roleId);
    }

    @Override
    public Result<List<ClusterRoleHost>> listByRegionId(Integer regionId) {
        List<ESClusterRoleHostPO> esClusterRoleHostPOS;
        try {
            esClusterRoleHostPOS = clusterRoleHostDAO.listByRegionId(regionId);
        } catch (Exception e) {
            LOGGER.error("class=ClusterRoleHostServiceImpl||method=listByRegionId||errMsg={}", e.getMessage(), e);
            return Result.buildFail(String.format("根据regionId[%d]获取节点信息失败", regionId));
        }
        return Result.buildSucc(ConvertUtil.list2List(esClusterRoleHostPOS, ClusterRoleHost.class));
    }

    /***************************************** private method ****************************************************/
    private Result<Void> checkNodeParam(ESClusterRoleHostDTO param, OperationEnum operation) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("节点信息为空");
        }

        if (OperationEnum.ADD.equals(operation)) {
            Result<Void> isNullResult = isNull(param);
            if (isNullResult.failed()){return isNullResult;}
        } else if (OperationEnum.EDIT.equals(operation)) {
            ESClusterRoleHostPO oldClusterHostPo = clusterRoleHostDAO.getById(param.getId());
            if (oldClusterHostPo == null) {
                return Result.buildNotExist("节点不存在");
            }
        }

        Result<Void> isIllegalResult = isIllegal(param);
        if (isIllegalResult.failed()){return isIllegalResult;}

        return Result.buildSucc();
    }

    private Result<Void> isIllegal(ESClusterRoleHostDTO param) {
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

    private Result<Void> isNull(ESClusterRoleHostDTO param) {
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
        if (AriusObjUtils.isNullStr(param.getNodeSet())) {
            return Result.buildParamIllegal("节点set为空");
        }
        return Result.buildSucc();
    }

    private boolean addNodeBatch(List<ESClusterRoleHostPO> nodePOs) {
        // 数据修改的行数成功数
        int count = 0;
        for (ESClusterRoleHostPO param : nodePOs) {
            if (null != clusterRoleHostDAO.getDeleteHostByHostNameAnRoleId(param.getHostname(), param.getRoleClusterId())) {
                count += clusterRoleHostDAO.restoreByHostNameAndRoleId(param.getHostname(), param.getRoleClusterId());
                continue;
            }
            count += clusterRoleHostDAO.insert(param);
        }
        return CollectionUtils.isEmpty(nodePOs) || nodePOs.size() == count;
    }


    private boolean editNodeBatch(List<ESClusterRoleHostPO> nodePOs) {
        if (CollectionUtils.isEmpty(nodePOs)) {
            return true;
        }

        boolean succ = true;
        for (ESClusterRoleHostPO nodePO : nodePOs) {
            succ &= editNode(ConvertUtil.obj2Obj(nodePO, ESClusterRoleHostDTO.class)).success();
        }

        return succ;
    }

    /**
     * 获取集群内节点配置
     * @param cluster 集群名称
     * @return EsClusterNodePO列表
     */
    private List<ESClusterRoleHostPO> getClusterHostFromEsAndCreateRoleClusterIfNotExist(String cluster) {
        List<ESClusterRoleHostPO> nodePOList = Lists.newArrayList();

        // 从ES集群中获取初始的节点信息列表
        List<ClusterNodeInfo> clusterNodeInfos = buildAllClusterNodeInfoFromES(cluster);


        // 根据集群节点角色信息构建入DB的host列表信息
        for (ClusterNodeInfo nodeInfoListFromArius : clusterNodeInfos) {
            // 构造节点记录数据
            ESClusterRoleHostPO roleClusterHostPO = buildEsClusterHostPO(nodeInfoListFromArius, cluster);
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
        if (MapUtils.isEmpty(clusterNodeInfoMap)) {
            return Collections.emptyList();
        }
        // 获取高低版本通用的节点角色信息
        Map<String, ClusterNodeSettings> clusterNodeSettingsMap = esClusterService.syncGetPartOfSettingsByCluster(
                cluster);
        if (MapUtils.isEmpty(clusterNodeSettingsMap)) {
            return Collections.emptyList();
        }

        if (MapUtils.isEmpty(clusterNodeSettingsMap) || MapUtils.isEmpty(clusterNodeInfoMap)) {
            return clusterNodeInfoListFromES;
        }

        // 构建原生集群节点信息列表
        for (Map.Entry</*UUID*/String, ClusterNodeInfo> entry : clusterNodeInfoMap.entrySet()) {
             if (!clusterNodeSettingsMap.containsKey(entry.getKey())) {
                continue;
            }
            // 为纳管2.3.3版本的角色信息，需要从_nodes/settings中获取
            ClusterNodeInfo clusterNodeInfo = entry.getValue();
            // 根据节点的UUID获取对应的全量的角色信息
            
            ClusterNodeSettings clusterNodeSettings = clusterNodeSettingsMap.get(entry.getKey());
            // 重新设置clusterNodeInfo的roles列表
            clusterNodeInfo.setRoles(buildRolesInfoFromSettings(clusterNodeSettings));
            // 构建节点角色的多角色信息
            buildMultiRoleListForESNode(clusterNodeInfoListFromES, clusterNodeInfo);
        }

        return CollectionUtils.isEmpty(clusterNodeInfoListFromES) ? Lists.newArrayList(clusterNodeInfoMap.values())  : clusterNodeInfoListFromES;
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

    private boolean notHasRoleOfMasterAndData(List<String> roles) {
        return !roles.contains(ES_ROLE_DATA) && !roles.contains(ES_ROLE_MASTER);
    }

    private ESClusterRoleHostPO buildEsClusterHostPO(ClusterNodeInfo clusterNodeInfo, String cluster) {
        ESClusterRoleHostPO nodePO = new ESClusterRoleHostPO();
        nodePO.setCluster(cluster);

        nodePO.setIp(Getter.withDefault(clusterNodeInfo.getIp(), ""));
        nodePO.setHostname(Getter.withDefault(clusterNodeInfo.getHost(), ""));
        nodePO.setNodeSet(Getter.withDefault(clusterNodeInfo.getName(), ""));
        nodePO.setAttributes(ConvertUtil.map2String(clusterNodeInfo.getAttributes()));

        HttpInfo httpInfo = clusterNodeInfo.getHttpInfo();
        if (null != httpInfo && null != httpInfo.getPublishAddress()) {
            String[] split = httpInfo.getPublishAddress().split(":");
            nodePO.setPort(split[split.length - 1]);
        }else {
            nodePO.setPort("");
        }

        nodePO.setStatus(ONLINE.getCode());
        nodePO.setRegionId(-1);
        nodePO.setRole(getRoleFromNodeSettings(clusterNodeInfo));

        return nodePO;
    }

    private ESClusterRoleHostPO buildEsClusterHostPO(ESClusterRoleHostDTO nodeDTO) {
        ESClusterRoleHostPO nodePO = ConvertUtil.obj2Obj(nodeDTO, ESClusterRoleHostPO.class);
        nodePO.setHostname(Getter.withDefault(nodeDTO.getIp(), ""));
        return nodePO;
    }

    /**
     * 设置节点所属的角色记录ID
     * @param roleClusterHostPO  节点记录
     * @param cluster            物理集群名
     * @return
     */
    private void setRoleClusterId(ESClusterRoleHostPO roleClusterHostPO, String cluster) {
            ESClusterNodeRoleEnum role = ESClusterNodeRoleEnum.valueOf(roleClusterHostPO.getRole());
            ClusterRoleInfo clusterRoleInfo = clusterRoleService.createRoleClusterIfNotExist(cluster, role.getDesc());
            roleClusterHostPO.setRoleClusterId(clusterRoleInfo.getId());
    }

    private Map<String/*roleClusterId@esNodeName*/ , ESClusterRoleHostPO> getNodeInfoFromDbMap(String cluster) {
        List<ESClusterRoleHostPO> nodesFromDB = clusterRoleHostDAO.listByCluster(cluster);

        LOGGER.info("class=RoleClusterHostServiceImpl||method=getNodeInfoFromDbMap||cluster={}||dbSize={}", cluster,
                nodesFromDB.size());

        return ConvertUtil.list2Map(nodesFromDB, ESClusterRoleHostPO::getKey);
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

    private boolean updateRolePod(List<ESClusterRoleHostPO> shouldAdd, String cluster) {
        if (CollectionUtils.isEmpty(shouldAdd)) {
            return true;
        }

        Map<Integer, List<ESClusterRoleHostPO>> role2ESRoleClusterHostPOListMap = ConvertUtil.list2MapOfList(shouldAdd,
            ESClusterRoleHostPO::getRole, esClusterRoleHostPO -> esClusterRoleHostPO);

        AtomicBoolean flag = new AtomicBoolean(true);
        role2ESRoleClusterHostPOListMap.forEach((role, roleClusterHostPOList) -> {
            ClusterRoleInfo clusterRoleInfo = clusterRoleService.getByClusterNameAndRole(cluster, valueOf(role).getDesc());
            clusterRoleInfo.setPodNumber(clusterRoleHostDAO.getPodNumberByRoleId(clusterRoleInfo.getId()));
            Result<Void> result = clusterRoleService.updatePodByClusterIdAndRole(clusterRoleInfo);
            if (result.failed()) {
                flag.set(false);
            }
        });

        return flag.get();
    }

    private boolean addAndEditNodes(String cluster, List<ESClusterRoleHostPO> shouldAdd, List<ESClusterRoleHostPO> shouldEdit) throws AdminTaskException {
        clusterRoleHostDAO.offlineByCluster(cluster);
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

    private List<String> buildESRoleNodeHttpAddressesList(List<ESClusterRoleHostDTO> roleClusterHosts, Integer roleCode){
        Map<Integer, List<ESClusterRoleHostDTO>> role2ESRoleClusterHostDTOMap = ConvertUtil.list2MapOfList(
                roleClusterHosts, ESClusterRoleHostDTO::getRole, esRoleClusterHostDTO -> esRoleClusterHostDTO);

        List<String> httpAddressesList = Lists.newArrayList();
        List<ESClusterRoleHostDTO> esClusterRoleHostDTOS = role2ESRoleClusterHostDTOMap.get(roleCode);
        if (null != esClusterRoleHostDTOS) {
            esClusterRoleHostDTOS.forEach(host -> httpAddressesList.add(host.getIp() + ":" + host.getPort()));
        }
        return httpAddressesList;
    }
}
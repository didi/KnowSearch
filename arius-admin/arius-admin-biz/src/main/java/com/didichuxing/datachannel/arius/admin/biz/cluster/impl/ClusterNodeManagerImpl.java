package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType.FAIL;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterNodeInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostWithRegionInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionEditByAttributeEvent;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionEditByHostEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author ohushenglin_v
 * @date 2022-05-30
 */
@Component
public class ClusterNodeManagerImpl implements ClusterNodeManager {

    private static final ILog      LOGGER = LogFactory.getLog(ClusterNodeManager.class);

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private ClusterRegionService   clusterRegionService;

    @Autowired
    private ClusterPhyService      clusterPhyService;

    @Autowired
    private ESClusterNodeService   esClusterNodeService;

    @Autowired
    private OperateRecordService   operateRecordService;

    @Autowired
    private ClusterLogicService    clusterLogicService;
    @Autowired
    private ProjectService projectService;

    private final static String HOST = "host";

    @Override
    @Deprecated
    public Result<List<ESClusterRoleHostWithRegionInfoVO>> listDivide2ClusterNodeInfo(Long clusterId) {
        List<ClusterRoleHost> clusterRoleHostList = null;
        try {
            clusterRoleHostList = clusterRoleHostService.getByRoleAndClusterId(clusterId, DATA_NODE.getDesc());
        } catch (Exception e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=listDivide2ClusterNodeInfo||clusterId={}||errMsg={}",
                clusterId, e.getMessage(), e);
        }
        List<ESClusterRoleHostWithRegionInfoVO> esClusterRoleHostWithRegionInfoVOS = ConvertUtil
            .list2List(clusterRoleHostList, ESClusterRoleHostWithRegionInfoVO.class);

        // 根据regionId获取region名称
        List<Integer> regionIdList = esClusterRoleHostWithRegionInfoVOS.stream()
            .map(ESClusterRoleHostWithRegionInfoVO::getRegionId).distinct().collect(Collectors.toList());

        if (CollectionUtils.isEmpty(regionIdList)) {
            return Result.buildSucc(esClusterRoleHostWithRegionInfoVOS);
        }

        Map<Integer, String> regionId2RegionNameMap = Maps.newHashMap();
        for (Integer regionId : regionIdList) {
            ClusterRegion clusterRegion = clusterRegionService.getRegionById(regionId.longValue());
            if (null == clusterRegion) {
                continue;
            }

            regionId2RegionNameMap.put(regionId, clusterRegion.getName());
        }

        for (ESClusterRoleHostWithRegionInfoVO clusterRoleHostWithRegionInfoVO : esClusterRoleHostWithRegionInfoVOS) {
            clusterRoleHostWithRegionInfoVO
                .setRegionName(regionId2RegionNameMap.get(clusterRoleHostWithRegionInfoVO.getRegionId()));
        }
        return Result.buildSucc(esClusterRoleHostWithRegionInfoVOS);
    }

    @Override
    public Result<List<ESClusterRoleHostWithRegionInfoVO>> listDivide2ClusterNodeInfoWithDivideType(Long clusterId, String divideType) {
        List<ClusterRoleHost> clusterRoleHostList = null;
        try {
            clusterRoleHostList = clusterRoleHostService.getByRoleAndClusterId(clusterId, DATA_NODE.getDesc());
        } catch (Exception e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=listDivide2ClusterNodeInfo||clusterId={}||errMsg={}",
                    clusterId, e.getMessage(), e);
        }
        List<ESClusterRoleHostWithRegionInfoVO> esClusterRoleHostWithRegionInfoVOS = ConvertUtil
                .list2List(clusterRoleHostList, ESClusterRoleHostWithRegionInfoVO.class);

        // 根据regionId获取region名称
        List<Integer> regionIdList = esClusterRoleHostWithRegionInfoVOS.stream()
                .map(ESClusterRoleHostWithRegionInfoVO::getRegionId).distinct().collect(Collectors.toList());

        if (CollectionUtils.isEmpty(regionIdList)) {
            return Result.buildSucc(esClusterRoleHostWithRegionInfoVOS);
        }

        Map<Integer, String> regionId2RegionNameMap = Maps.newHashMap();
        for (Integer regionId : regionIdList) {
            ClusterRegion clusterRegion = clusterRegionService.getRegionById(regionId.longValue());
            if (null == clusterRegion) {
                continue;
            }

            regionId2RegionNameMap.put(regionId, clusterRegion.getName());
        }

        for (ESClusterRoleHostWithRegionInfoVO clusterRoleHostWithRegionInfoVO : esClusterRoleHostWithRegionInfoVOS) {
            clusterRoleHostWithRegionInfoVO
                    .setRegionName(regionId2RegionNameMap.get(clusterRoleHostWithRegionInfoVO.getRegionId()));
            if(!HOST.equals(divideType)){
                String attributes = clusterRoleHostWithRegionInfoVO.getAttributes();
                clusterRoleHostWithRegionInfoVO.setAttributeValue(ConvertUtil.str2Map(attributes).get(divideType));
            }
        }
        return Result.buildSucc(esClusterRoleHostWithRegionInfoVOS);
    }

    @Override
    public Result<Boolean> checkMultiNode2Region(List<ClusterRegionWithNodeInfoDTO> params, String operator, Integer projectId) {
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        for(ClusterRegionWithNodeInfoDTO param : params){
            Result<Boolean> checkRet = baseCheckParamValid(param,OperationEnum.ADD);
            if (checkRet.failed()) {
                return checkRet;
            }

            if (CollectionUtils.isEmpty(param.getBindingNodeIds())) {
                return Result.buildFail(String.format("region名称[%s], errMsg=%s", param.getName(), "划分至该region的节点为空"));
            }
        }
        return Result.buildSucc(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<List<Long>> createMultiNode2Region(List<ClusterRegionWithNodeInfoDTO> params, String operator,
                                                     Integer projectId) throws AdminOperateException {
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }

        // 构建divideType2regionIdList的map，不同的划分方式Listener的处理方式不同
        Map<String/*Region划分方式*/, List<Long>/*regionId列表*/> divideType2RegionIdListMap = Maps.newHashMap();
        List<Long> regionIdList = Lists.newArrayList();
        for (ClusterRegionWithNodeInfoDTO param : params) {
            Result<Boolean> checkRet = baseCheckParamValid(param,OperationEnum.ADD);
            if (checkRet.failed()) {
                throw new AdminOperateException(checkRet.getMessage());
            }

            if (CollectionUtils.isEmpty(param.getBindingNodeIds())) {
                throw new AdminOperateException(
                    String.format("region名称[%s], errMsg=%s", param.getName(), "划分至该region的节点为空"));
            }

            Result<Long> addRegionRet = clusterRegionService.createPhyClusterRegion(param, operator);

            if (addRegionRet.success()) {
                param.setId(addRegionRet.getData());
                // 调用扩缩容region接口来添加region
                Result<Boolean> booleanResult = editNode2Region(param);
                if (booleanResult.success()) {
                    // 2. 操作记录 :Region变更
                     operateRecordService.saveOperateRecordWithManualTrigger(String.format("新增 region[%s]", param.getName()), operator, projectId,
                            param.getId(), OperateTypeEnum.PHYSICAL_CLUSTER_REGION_CHANGE);

                     // 构建多个regionId列表，把同种划分方式的regionId放在一起，发同一个事件，提高性能
                     if(param.getDivideAttributeKey() == null || param.getDivideAttributeKey().isEmpty()){
                         List<Long> hostRegionIdList = divideType2RegionIdListMap.getOrDefault(HOST, Lists.newArrayList());
                         hostRegionIdList.add(addRegionRet.getData());
                         divideType2RegionIdListMap.put(HOST, hostRegionIdList);
                     }else {
                         List<Long> attributeRegionIdList = divideType2RegionIdListMap
                                 .getOrDefault(param.getDivideAttributeKey(), Lists.newArrayList());
                         attributeRegionIdList.add(addRegionRet.getData());
                         divideType2RegionIdListMap.put(param.getDivideAttributeKey(), attributeRegionIdList);
                     }
                    regionIdList.add(addRegionRet.getData());
                } else {
                    throw new AdminOperateException(addRegionRet.getMessage());
                }
            }
        }

        // 发布事件，不同的划分方式发不同的事件
        for(Map.Entry<String, List<Long>> entry : divideType2RegionIdListMap.entrySet()) {
            String divideType = entry.getKey();
            List<Long> regionIds = divideType2RegionIdListMap.get(divideType);
            if(HOST.equals(divideType)){
                SpringTool.publish(new RegionEditByHostEvent(this, regionIds));
            }else {
                SpringTool.publish(new RegionEditByAttributeEvent(this, regionIds, divideType));
            }
        }

        return Result.buildSucc(regionIdList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> editMultiNode2Region(List<ClusterRegionWithNodeInfoDTO> params, String operator,
                                                Integer projectId, OperationEnum operationEnum) throws AdminOperateException {
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }

        // 构建divideType2regionIdList的map（不同的划分方式Listener的处理方式不同）
        Map<String/*Region划分方式*/, List<Long>/*regionId列表*/> divideType2RegionIdListMap = Maps.newHashMap();
        for (ClusterRegionWithNodeInfoDTO param : params) {
            Result<Boolean> checkRet = baseCheckParamValid(param,operationEnum);
            if (checkRet.failed()) {
                throw new AdminOperateException(checkRet.getMessage(), FAIL);
            }

            Result<Boolean> editNode2RegionRet = editNode2Region(param);
            if (editNode2RegionRet.failed()) {
                throw new AdminOperateException(editNode2RegionRet.getMessage(), FAIL);
            }else {
                // 构建多个regionId列表，把同种划分方式的regionId放在一起，发同一个事件，提高性能
                if(param.getDivideAttributeKey() == null || param.getDivideAttributeKey().isEmpty()){
                    List<Long> hostRegionIdList = divideType2RegionIdListMap.getOrDefault(HOST, Lists.newArrayList());
                    hostRegionIdList.add(param.getId());
                    divideType2RegionIdListMap.put(HOST, hostRegionIdList);
                }else {
                    List<Long> attributeRegionIdList = divideType2RegionIdListMap
                            .getOrDefault(param.getDivideAttributeKey(), Lists.newArrayList());
                    attributeRegionIdList.add(param.getId());
                    divideType2RegionIdListMap.put(param.getDivideAttributeKey(), attributeRegionIdList);
                }
            }
        }

        // 发布事件，不同的划分方式发不同的事件
        for(Map.Entry<String, List<Long>> entry : divideType2RegionIdListMap.entrySet()) {
            String divideType = entry.getKey();
            List<Long> regionIds = divideType2RegionIdListMap.get(divideType);
            if(HOST.equals(divideType)){
                SpringTool.publish(new RegionEditByHostEvent(this, regionIds));
            }else {
                SpringTool.publish(new RegionEditByAttributeEvent(this, regionIds, divideType));
            }
        }

        return Result.buildSucc(true);
    }

    @Override
    public Result<List<ESClusterRoleHostVO>> listClusterPhyNode(Integer clusterId) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterById(clusterId);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail(String.format("集群[%s]不存在", clusterId));
        }
        List<ClusterRoleHost> clusterRoleHostList = clusterRoleHostService.getNodesByCluster(clusterPhy.getCluster());
        return Result.buildSucc(buildClusterRoleHostStats(clusterPhy.getCluster(), clusterRoleHostList));
    }

    @Override
    public Result<List<ESClusterRoleHostVO>> listClusterLogicNode(Integer clusterId) {
        if (!clusterLogicService.existClusterLogicById(clusterId.longValue())) {
            return Result.buildFail(String.format("集群[%s]不存在", clusterId));
        }
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterId.longValue());
        if (clusterRegion == null) {
            return Result.buildFail(String.format("集群[%s]未绑定region", clusterId));
        }
        Result<List<ClusterRoleHost>> result = clusterRoleHostService
            .listByRegionId(Math.toIntExact(clusterRegion.getId()));
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        //节点名称列表
        return Result.buildSucc(buildClusterRoleHostStats(clusterRegion.getPhyClusterName(), result.getData()));
    }

    @Override
    public Result listClusterLogicNodeByName(String clusterLogicName) {
        ClusterLogic clusterLogic =
                clusterLogicService.listClusterLogicByNameThatProjectIdStrConvertProjectIdList(clusterLogicName).stream().findFirst().orElse(null);
        if (AriusObjUtils.isNull(clusterLogic)) {
            return Result.buildFail(String.format("集群[%s]不存在", clusterLogicName));
        }
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
        Result<List<ClusterRoleHost>> result = clusterRoleHostService
            .listByRegionId(Math.toIntExact(clusterRegion.getId()));

        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }

        //节点名称列表
        return Result
            .buildSucc(result.getData().stream().map(ClusterRoleHost::getNodeSet).collect(Collectors.toList()));
    }

    @Override
    public Result<List<ClusterNodeInfoVO>> listClusterLogicNodeInfosByName(String clusterLogicName) {
        ClusterLogic clusterLogic =
                clusterLogicService.listClusterLogicByNameThatProjectIdStrConvertProjectIdList(clusterLogicName).stream().findFirst().orElse(null);
        if (AriusObjUtils.isNull(clusterLogic)) {
            return Result.buildFail(String.format("集群[%s]不存在", clusterLogicName));
        }
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
        if (clusterRegion == null) {
            return Result.buildFail(String.format("集群[%s]未绑定region", clusterLogic.getId()));
        }
        Result<List<ClusterRoleHost>> result = clusterRoleHostService
                .listByRegionId(Math.toIntExact(clusterRegion.getId()));
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        //节点名称列表
        return Result.buildSucc(result.getData().stream().map(clusterRoleHost->new ClusterNodeInfoVO(clusterRoleHost.getNodeSet(),clusterRoleHost.getRole()))
                .collect(Collectors.toList()));
    }
    @Override
    public boolean collectNodeSettings(String cluster) throws AdminTaskException {
        return clusterRoleHostService.collectClusterNodeSettings(cluster);
    }
    
  
    /**
     * > 该功能用于删除集群节点，但该节点必须离线且未绑定region
     *
     * @param ids 要删除的节点的id
     * @param projectId 项目编号
     * @param operator 操作员是执行操作的用户。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(List<Integer> ids, Integer projectId, String operator) {
        Result<Void> checkProjectCorrectly = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (checkProjectCorrectly.failed()) {
            return checkProjectCorrectly;
        }
    
        List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.listById(ids);
        if (CollectionUtils.isEmpty(clusterRoleHosts)) {
            return Result.buildSucc();
        }
        //1. 校验当前节点是否 bind region, 如果是则不允许下线
        if (!clusterRoleHosts.stream().allMatch(clusterRoleHost -> Objects.equals(clusterRoleHost.getRegionId(), -1))) {
            return Result.buildFail("当前选中的节点绑定了 region，请先下线 region");
        }
        //2. 检验当前节点是否都离线
        if (!clusterRoleHosts.stream().allMatch(clusterRoleHost -> Objects.equals(clusterRoleHost.getStatus(),
                ESClusterNodeStatusEnum.OFFLINE.getCode()))) {
            return Result.buildFail("只可以下线离线状态的节点，当前选中的节点存在非离线状态的节点");
        }
        //3. 下线离线的节点
        List<String> clusterPhies = clusterRoleHosts.stream().map(ClusterRoleHost::getCluster).distinct()
                .collect(Collectors.toList());
        List<ClusterPhy> clusterPhyList = clusterPhyService.listClustersByNames(clusterPhies);
    
        boolean delete = clusterRoleHostService.deleteByIds(ids);
        if (delete) {
            Map<String, Integer> clusterPhy2ClusterId = ConvertUtil.list2Map(clusterPhyList, ClusterPhy::getCluster,
                    ClusterPhy::getId);
            Multimap<String, Long> clusterPhy2NodeIds = ConvertUtil.list2MulMap(clusterRoleHosts,
                    ClusterRoleHost::getCluster, ClusterRoleHost::getId);
            Map<Long, String> nodeId2IpMap = ConvertUtil.list2Map(clusterRoleHosts, ClusterRoleHost::getId,
                    ClusterRoleHost::getIp);
            for (Entry<String, Integer> clusterPhy2ClusterIdEntry : clusterPhy2ClusterId.entrySet()) {
                Integer clusterId = clusterPhy2ClusterIdEntry.getValue();
                String ipStr = clusterPhy2NodeIds.get(clusterPhy2ClusterIdEntry.getKey()).stream()
                        .map(nodeId2IpMap::get).distinct().collect(Collectors.joining(","));
                // 2. 操作记录 : 节点下线
                 operateRecordService.saveOperateRecordWithManualTrigger(String.format("下线节点的 ip 列表 ：[%s]", ipStr), operator, projectId, clusterId,
                        OperateTypeEnum.PHYSICAL_CLUSTER_NODE_CHANGE);
            }
        }
        return Result.build(delete);
    }

    /**
     * @param regionId
     * @return
     */
    @Override
    public Result<List<ESClusterRoleHostVO>> listClusterRoleHostByRegionId(Long regionId) {
        ClusterRegion region = clusterRegionService.getRegionById(regionId);
        if (region == null) {
            return Result.buildFail("region不存在");
        }
        Result<List<ClusterRoleHost>> ret = clusterRoleHostService.listByRegionId(region.getId().intValue());
        if (ret.failed()) {
            return Result.buildFail("获取host失败");

        }
        return Result.buildSucc(ConvertUtil.list2List(ret.getData(), ESClusterRoleHostVO.class));
    }

    /**************************************** private method ***************************************************/
    private List<ESClusterRoleHostVO> buildClusterRoleHostStats(String cluster,
                                                                List<ClusterRoleHost> clusterRoleHostList) {
        List<ESClusterRoleHostVO> roleHostList = ConvertUtil.list2List(clusterRoleHostList, ESClusterRoleHostVO.class);
        if (CollectionUtils.isNotEmpty(roleHostList)) {
            Map<String, String> regionMap = roleHostList.stream().map(ESClusterRoleHostVO::getRegionId)
                .filter(regionId -> null != regionId && regionId > 0).distinct()
                .map(regionId -> clusterRegionService.getRegionById(regionId.longValue())).filter(Objects::nonNull)
                .collect(
                    Collectors.toMap(region -> String.valueOf(region.getId()), ClusterRegion::getName, (r1, r2) -> r1));

            Map<String, Triple<Long, Long, Double>> nodeDiskUsageMap = esClusterNodeService
                .syncGetNodesDiskUsage(cluster);
            roleHostList.forEach(vo -> {
                Optional.ofNullable(regionMap.get(String.valueOf(vo.getRegionId()))).ifPresent(vo::setRegionName);
                Optional.ofNullable(nodeDiskUsageMap.get(vo.getNodeSet())).ifPresent(triple -> {
                    vo.setDiskTotal(triple.v1());
                    vo.setDiskUsage(triple.v2());
                    vo.setDiskUsagePercent(triple.v3());
                });
            });
        }
        return roleHostList;
    }

    private Result<Boolean> baseCheckParamValid(ClusterRegionWithNodeInfoDTO param, OperationEnum operationEnum) {
        if (null == param) {
            return Result.buildFail("参数为空");
        }
        if (operationEnum.equals(OperationEnum.ADD)) {
            if (AriusObjUtils.isBlank(param.getName())) {
                return Result.buildFail("region名称不允许为空或者空字符串");
            }
            if (clusterRegionService.isExistByRegionName(param.getName())) {
                return Result.buildFail(String.format("region名称[%s]已经存在", param.getName()));
            }
            if (CollectionUtils.isEmpty(param.getBindingNodeIds())) {
                return Result.buildFail("不允许绑定空region");
            }
        }
        if (operationEnum.equals(OperationEnum.EDIT)) {
            if (Objects.isNull(param.getId())) {
                return Result.buildFail("编辑id不能为空");
            }
            if (!clusterRegionService.isExistByRegionId(Math.toIntExact(param.getId()))) {
                return Result.buildFail(String.format("编辑的region %d 不存在", param.getId()));
            }
        }

        if (!clusterPhyService.isClusterExists(param.getPhyClusterName())) {
            return Result.buildFail(String.format("物理集群[%s]不存在", param.getPhyClusterName()));
        }

        if(operationEnum.equals(OperationEnum.DELETE)){
            return Result.buildSucc();
        }

        // 检查划分方式是否合法
        Result<Void> ret = divideTypeCheck(param);
        if(ret.failed()){
            return Result.buildFail(ret.getMessage());
        }

        return Result.buildSucc();
    }

    private Result<Void> divideTypeCheck(ClusterRegionWithNodeInfoDTO param) {
        // 先获取当前集群存在的region，根据已有region来校验本次操作
        List<ClusterRegion> clusterRegions = clusterRegionService.listRegionsByClusterName(param.getPhyClusterName());
        ClusterRegion clusterRegion = clusterRegions.stream().findFirst().orElse(null);

        if(StringUtils.isBlank(param.getDivideAttributeKey())){
            // 根据host划分
            if(clusterRegion != null && StringUtils.isNotBlank(clusterRegion.getDivideAttributeKey())){
                return Result.buildParamIllegal("当前集群region已存在划分方法，不支持其他划分方法");
            }
        }else {
            String divideType = param.getDivideAttributeKey();
            if(clusterRegion != null && (StringUtils.isBlank(clusterRegion.getDivideAttributeKey())
                    || !divideType.equals(clusterRegion.getDivideAttributeKey()))){
                return Result.buildParamIllegal("当前集群region已存在划分方法，不支持其他划分方法");
            }
        }

        return Result.buildSucc();
    }

    private Result<Boolean> editNode2Region(ClusterRegionWithNodeInfoDTO param) throws AdminOperateException {
       
        // 校验bindingNodeIds 和 unBindingNodeIds的重复性
        List<Integer> bindingNodeIds = param.getBindingNodeIds();
        List<Integer> unBindingNodeIds = param.getUnBindingNodeIds();
        if (CollectionUtils.isNotEmpty(bindingNodeIds)) {
            for (Integer bindingNodeId : bindingNodeIds) {
                if (CollectionUtils.isNotEmpty(unBindingNodeIds) && unBindingNodeIds.contains(bindingNodeId)) {
                    return Result.buildFail("region中绑定节点类别和解绑节点列表不能有相同节点");
                }
            }
        }

        if (CollectionUtils.isNotEmpty(bindingNodeIds)) {
            // 绑定node 到指定 region
            boolean editBingingNodeRegionIdFlag = clusterRoleHostService.editNodeRegionId(bindingNodeIds,
                param.getId().intValue());
            if (!editBingingNodeRegionIdFlag) {
                return Result.buildFail(String.format("新增region节点[%s]失败", bindingNodeIds));
            }
        }

        if (CollectionUtils.isNotEmpty(unBindingNodeIds)) {
            // 解绑node 到指定 region
            boolean editUnBingingNodeRegionFlag = clusterRoleHostService.editNodeRegionId(unBindingNodeIds, -1);
            if (!editUnBingingNodeRegionFlag) {
                return Result.buildFail(String.format("删除region节点[%s]失败", unBindingNodeIds));
            }
        }
        return Result.build(true);
    }
}
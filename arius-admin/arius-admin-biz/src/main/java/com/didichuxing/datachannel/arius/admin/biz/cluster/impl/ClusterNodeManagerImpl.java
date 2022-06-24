package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType.FAIL;

import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostWithRegionInfoVO;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionEditEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AriusRunTimeException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
    private ClusterLogicService clusterLogicService;

    @Override
    public Result<List<ESClusterRoleHostWithRegionInfoVO>> listDivide2ClusterNodeInfo(Long clusterId) {
        List<ClusterRoleHost> clusterRoleHostList = null;
        try {
            clusterRoleHostList = clusterRoleHostService.getByRoleAndClusterId(clusterId, DATA_NODE.getDesc());
        } catch (Exception e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=listDivide2ClusterNodeInfo||clusterId={}||errMsg={}",
                    clusterId, e.getMessage(), e);
        }
        List<ESClusterRoleHostWithRegionInfoVO> esClusterRoleHostWithRegionInfoVOS =
                ConvertUtil.list2List(clusterRoleHostList, ESClusterRoleHostWithRegionInfoVO.class);

        // 根据regionId获取region名称
        List<Integer> regionIdList = esClusterRoleHostWithRegionInfoVOS.stream()
                .map(ESClusterRoleHostWithRegionInfoVO::getRegionId)
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(regionIdList)) { return Result.buildSucc(esClusterRoleHostWithRegionInfoVOS);}

        Map<Integer, String> regionId2RegionNameMap = Maps.newHashMap();
        for (Integer regionId : regionIdList) {
            ClusterRegion clusterRegion = clusterRegionService.getRegionById(regionId.longValue());
            if (null == clusterRegion) { continue;}

            regionId2RegionNameMap.put(regionId, clusterRegion.getName());
        }

        for (ESClusterRoleHostWithRegionInfoVO clusterRoleHostWithRegionInfoVO : esClusterRoleHostWithRegionInfoVOS) {
            clusterRoleHostWithRegionInfoVO.setRegionName(regionId2RegionNameMap.get(clusterRoleHostWithRegionInfoVO.getRegionId()));
        }
        return Result.buildSucc(esClusterRoleHostWithRegionInfoVOS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<List<Long>> createMultiNode2Region(List<ClusterRegionWithNodeInfoDTO> params, String operator) {
        List<Long> regionIdLis = Lists.newArrayList();
        for (ClusterRegionWithNodeInfoDTO param : params) {
            Result<Boolean> checkRet = baseCheckParamValid(param);
            if (checkRet.failed()) { return Result.buildFrom(checkRet);}
            if (clusterRegionService.isExistByRegionName(param.getName())) {
                return Result.buildFail(String.format("region名称[%s]已经存在", param.getName()));
            }
            if (CollectionUtils.isEmpty(param.getBindingNodeIds())) { return Result.buildFail("region节点集合为空");}

            Result<Long> addRegionRet = clusterRegionService.createPhyClusterRegion(param.getPhyClusterName(), param.getBindingNodeIds(),
                    param.getName(), operator);
            if (addRegionRet.success()) {
                param.setId(addRegionRet.getData());
                Result<Boolean> booleanResult = editNode2Region(param);
                if (booleanResult.success()) {
                    // 2. 操作记录 :Region变更
                    operateRecordService.save(
                            new OperateRecord.Builder()
                                    .userOperation(operator)
                                    .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_REGION_CHANGE)
                                    .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                                    .content(String.format("新增region[%s]", param.getName()))
                                    .build());
                    // 3. 发送消息
                    regionIdLis.add(addRegionRet.getData());
                }else {
                    throw new AriusRunTimeException(addRegionRet.getMessage(), FAIL);
                }
            }
        }
        return Result.buildSucc(regionIdLis);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> editMultiNode2Region(List<ClusterRegionWithNodeInfoDTO> params, String operator) {
        for (ClusterRegionWithNodeInfoDTO param : params) {
            Result<Boolean> editNode2RegionRet = editNode2Region(param);
            if (editNode2RegionRet.failed()) { throw new AriusRunTimeException(editNode2RegionRet.getMessage(), FAIL);}
        }

        // 发布region变更的事件，对模板和索引生效
        List<Long> regionIdList = params.stream().distinct().map(ClusterRegionWithNodeInfoDTO::getId).collect(Collectors.toList());
        SpringTool.publish(new RegionEditEvent(this, regionIdList));

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
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(Long.valueOf(clusterId));
        if (AriusObjUtils.isNull(clusterLogic)) {
            return Result.buildFail(String.format("集群[%s]不存在", clusterId));
        }
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
        if (clusterRegion==null){
            return Result.buildFail(String.format("集群[%s]未绑定region", clusterId));
        }
        Result<List<ClusterRoleHost>> result = clusterRoleHostService.listByRegionId(Math.toIntExact(clusterRegion.getId()));
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        //节点名称列表
        return Result.buildSucc(ConvertUtil.list2List(result.getData(), ESClusterRoleHostVO.class));
    }

    @Override
    public Result listClusterLogicNodeByName(String clusterLogicName) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByName(clusterLogicName);
        if (AriusObjUtils.isNull(clusterLogic)) {
            return Result.buildFail(String.format("集群[%s]不存在", clusterLogicName));
        }
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
        Result<List<ClusterRoleHost>> result = clusterRoleHostService.listByRegionId(Math.toIntExact(clusterRegion.getId()));

        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }

        //节点名称列表
        return Result.buildSucc(result.getData().stream().map(ClusterRoleHost::getNodeSet).collect(Collectors.toList()));
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

            Map<String, Triple<Long, Long, Double>> nodeDiskUsageMap = esClusterNodeService.syncGetNodesDiskUsage(cluster);
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

    @Nullable
    private Result<Boolean> baseCheckParamValid(ClusterRegionWithNodeInfoDTO param) {
        if (null == param) {
            return Result.buildFail("参数为空");
        }

        if (AriusObjUtils.isBlank(param.getName())) { return Result.buildFail("region名称不允许为空或者空字符串");}

        if (!clusterPhyService.isClusterExists(param.getPhyClusterName())) {
            return Result.buildFail(String.format("物理集群[%s]不存在", param.getPhyClusterName()));
        }
        return Result.buildSucc();
    }

    private Result<Boolean> editNode2Region(ClusterRegionWithNodeInfoDTO param) {
        Result<Boolean> checkRet = baseCheckParamValid(param);
        if (checkRet.failed()) {
            return Result.buildFrom(checkRet);
        }
        if (!clusterRegionService.isExistByRegionId(param.getId().intValue())) {
            return Result.buildFail(String.format("regionId[%s]不存在", param.getId()));
        }

        // 校验bindingNodeIds 和 unBindingNodeIds的重复性
        List<Integer> bindingNodeIds   = param.getBindingNodeIds();
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
            boolean editBingingNodeRegionIdFlag = clusterRoleHostService.editNodeRegionId(bindingNodeIds, param.getId().intValue());
            if (!editBingingNodeRegionIdFlag) { return Result.buildFail(String.format("新增region节点[%s]失败", bindingNodeIds));}
        }

        if (CollectionUtils.isNotEmpty(unBindingNodeIds)) {
            // 解绑node 到指定 region
            boolean editUnBingingNodeRegionFlag = clusterRoleHostService.editNodeRegionId(unBindingNodeIds, -1);
            if (!editUnBingingNodeRegionFlag) { return Result.buildFail(String.format("删除region节点[%s]失败", unBindingNodeIds));}
        }
        return Result.build(true);
    }
}
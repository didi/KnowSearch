package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.RackMetaMetric;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostWithRegionInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.quota.NodeSpecifyEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.quota.Resource;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionEditEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AriusRunTimeException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;

import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.metadata.service.NodeStatisService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.CLUSTER_REGION;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType.FAIL;

@Component
public class ClusterNodeManagerImpl implements ClusterNodeManager {

    private static final ILog      LOGGER = LogFactory.getLog(ClusterNodeManager.class);

    @Autowired
    private NodeStatisService      nodeStatisService;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private ClusterLogicService    clusterLogicService;

    @Autowired
    private ClusterRegionService   clusterRegionService;

    @Autowired
    private ClusterPhyService      clusterPhyService;

    @Autowired
    private OperateRecordService   operateRecordService;

    /**
     * 物理集群节点转换
     *
     * @param clusterNodes       物理集群节点
     * @return
     */
    @Override
    public List<ESClusterRoleHostVO> convertClusterLogicNodes(List<ClusterRoleHost> clusterNodes) {
        List<ESClusterRoleHostVO> result = Lists.newArrayList();

        List<ClusterLogicRackInfo> clusterRacks = clusterRegionService.listAllLogicClusterRacks();

        Multimap<String, ClusterLogic> rack2LogicClusters = getRack2LogicClusterMappings(clusterRacks,
                clusterLogicService.listAllClusterLogics());
        Map<String, ClusterLogicRackInfo> rack2ClusterRacks = getRack2ClusterRacks(clusterRacks);

        for (ClusterRoleHost node : clusterNodes) {
            ESClusterRoleHostVO nodeVO = ConvertUtil.obj2Obj(node, ESClusterRoleHostVO.class);

            String clusterRack = createClusterRackKey(node.getCluster(), node.getRack());
            ClusterLogicRackInfo rackInfo = rack2ClusterRacks.get(clusterRack);
            if (rackInfo != null) {
                nodeVO.setRegionId(rackInfo.getRegionId().intValue());
            }

            Collection<ClusterLogic> clusterLogics = rack2LogicClusters.get(clusterRack);
            if (!CollectionUtils.isEmpty(clusterLogics)) {
                nodeVO.setLogicDepart(ListUtils.strList2String(clusterLogics.stream().map(ClusterLogic::getName).collect(Collectors.toList())));
            }

            result.add(nodeVO);
        }

        return result;
    }

    /**
     * 获取rack的资源统计信息
     *
     * @param clusterName 集群名字
     * @param racks       racks
     * @return list
     */
    @Override
    public Result<List<RackMetaMetric>> metaAndMetric(String clusterName, Collection<String> racks) {

        // 从ams获取rack资源统计信息
        Result<List<RackMetaMetric>> result = nodeStatisService.getRackStatis(clusterName, racks);
        if (result.failed()) {
            return result;
        }

        List<RackMetaMetric> rackMetaMetrics = result.getData();
        if (racks.size() != rackMetaMetrics.size()) {
            LOGGER.warn("class=ClusterNodeManagerImpl||method=metaAndMetric||racksSize={}||resultSize={}", racks.size(), rackMetaMetrics.size());
        }

        // 获取每个节点的规格，当前ECM提供的时候datanode的规格
        Result<Resource> dataNodeSpecifyResult = getDataNodeSpecify();
        if (dataNodeSpecifyResult.failed()) {
            return Result.buildFrom(dataNodeSpecifyResult);
        }
        Resource dataNodeSpecify = dataNodeSpecifyResult.getData();

        // 获取集群节点
        List<ClusterRoleHost> clusterNodes = clusterRoleHostService.getOnlineNodesByCluster(clusterName);
        // rack到集群节点的map
        Multimap<String, ClusterRoleHost> rack2ESClusterNodeMultiMap = ConvertUtil.list2MulMap(clusterNodes,
            ClusterRoleHost::getRack);
        // rack到rack资源信息的map
        Map<String, RackMetaMetric> rack2RackMetaMetricMap = ConvertUtil.list2Map(rackMetaMetrics,
            RackMetaMetric::getName);

        for (String rack : racks) {
            RackMetaMetric rackMetaMetric = rack2RackMetaMetricMap.get(rack);

            if (rackMetaMetric == null) {
                return Result.buildParamIllegal("AMS rack统计结果缺失：" + rack);
            }

            // rack的节点数
            if (rack2ESClusterNodeMultiMap.containsKey(rackMetaMetric.getName())) {
                rackMetaMetric.setNodeCount(rack2ESClusterNodeMultiMap.get(rackMetaMetric.getName()).size());
            } else {
                LOGGER.warn("class=ClusterNodeManagerImpl||method=metaAndMetric||rack={}||msg=offline", rackMetaMetric.getName());
                rackMetaMetric.setNodeCount(0);
            }

            // rack的cpu数
            rackMetaMetric.setCpuCount(dataNodeSpecify.getCpu().intValue() * rackMetaMetric.getNodeCount());
            // rack包含的磁盘大小
            rackMetaMetric.setTotalDiskG(dataNodeSpecify.getDisk() * 1.0 * rackMetaMetric.getNodeCount());
        }

        // 校验统计结果是否正确
        Result<Boolean> checkResult = checkRackMetrics(rackMetaMetrics, dataNodeSpecify);
        if (checkResult.failed()) {
            return Result.buildParamIllegal("AMS rack统计结果非法：" + checkResult.getMessage());
        }

        return Result.buildSucc(rackMetaMetrics);
    }

    /**
     * 获取rack的元信息
     *
     * @param clusterName 集群名字
     * @param racks       rack
     * @return list
     */
    @Override
    public Result<List<RackMetaMetric>> meta(String clusterName, Collection<String> racks) {

        List<RackMetaMetric> rackMetas = Lists.newArrayList();
        Set<String> rackSet = Sets.newHashSet(racks);

        // 获取每个节点的规格，当前ECM提供的时候datano的规格
        Result<Resource> dataNodeSpecifyResult = getDataNodeSpecify();
        if (dataNodeSpecifyResult.failed()) {
            return Result.buildFrom(dataNodeSpecifyResult);
        }
        Resource dataNodeSpecify = dataNodeSpecifyResult.getData();

        // 获取集群节点
        List<ClusterRoleHost> clusterNodes = clusterRoleHostService.getOnlineNodesByCluster(clusterName);
        // rack到rack下节点的map
        Multimap<String, ClusterRoleHost> rack2ESClusterNodeMultiMap = ConvertUtil.list2MulMap(clusterNodes,
            ClusterRoleHost::getRack);

        // 遍历rack
        for (Map.Entry<String, Collection<ClusterRoleHost>> entry : rack2ESClusterNodeMultiMap.asMap().entrySet()) {
            if (!rackSet.contains(entry.getKey())) {
                continue;
            }
            RackMetaMetric rackMeta = new RackMetaMetric();
            rackMeta.setCluster(clusterName);
            rackMeta.setName(entry.getKey());
            // rack下的节点数
            if (rack2ESClusterNodeMultiMap.containsKey(rackMeta.getName())) {
                rackMeta.setNodeCount(rack2ESClusterNodeMultiMap.get(rackMeta.getName()).size());
            } else {
                LOGGER.warn("class=ClusterNodeManagerImpl||method=meta||rack={}||msg=offline", entry.getKey());
                rackMeta.setNodeCount(0);
            }

            // rack下的CPU数
            rackMeta.setCpuCount(dataNodeSpecify.getCpu().intValue() * rackMeta.getNodeCount());
            // rack下的磁盘容量
            rackMeta.setTotalDiskG(dataNodeSpecify.getDisk() * 1.0 * rackMeta.getNodeCount());
            rackMetas.add(rackMeta);
        }

        return Result.buildSucc(rackMetas);
    }

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
                    // 2. 操作记录
                    operateRecordService.save(CLUSTER_REGION, ADD, addRegionRet.getMessage(), "", operator);
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

    /**************************************** private method ***************************************************/
    private Result<Resource> getDataNodeSpecify() {
        return Result.buildSucc(NodeSpecifyEnum.DOCKER.getResource());
    }

    private Result<Boolean> checkRackMetrics(List<RackMetaMetric> rackMetaMetrics, Resource dataNodeSpecify) {
        for (RackMetaMetric metaMetric : rackMetaMetrics) {
            if (AriusObjUtils.isNull(metaMetric.getName())) {
                return Result.buildParamIllegal("rack名字为空");
            }

            if (AriusObjUtils.isNull(metaMetric.getDiskFreeG())) {
                return Result.buildParamIllegal(metaMetric.getName() + "diskFreeG为空");
            }

            if (metaMetric.getDiskFreeG() < 0
                || metaMetric.getDiskFreeG() > (dataNodeSpecify.getDisk() * metaMetric.getNodeCount())) {

                LOGGER.warn("class=ESClusterPhyRackStatisServiceImpl||method=checkTemplateMetrics||errMsg=diskFree非法"
                            + "||metaMetric={}||dataNodeSpecify={}",
                    metaMetric, dataNodeSpecify);

                return Result.buildParamIllegal(metaMetric.getName() + "的diskFreeG非法");
            }
        }

        return Result.buildSucc(true);
    }

    /**************************************** private method ***************************************************/
    /**
     * 获取Rack名称与集群Rack映射关系
     * @param clusterRacks 集群Rack列表
     * @return
     */
    private Map<String, ClusterLogicRackInfo> getRack2ClusterRacks(List<ClusterLogicRackInfo> clusterRacks) {
        Map<String, ClusterLogicRackInfo> rack2ClusterRacks = new HashMap<>(1);
        if (CollectionUtils.isNotEmpty(clusterRacks)) {
            for (ClusterLogicRackInfo rackInfo : clusterRacks) {
                rack2ClusterRacks.put(createClusterRackKey(rackInfo.getPhyClusterName(), rackInfo.getRack()), rackInfo);
            }
        }

        return rack2ClusterRacks;
    }

    /**
     * 获取所有集群Rack与逻辑集群映射信息，同样的Rack可以对应多个逻辑集群
     *
     * @param logicClusterRacks 所有集群Racks
     * @param logicClusters     逻辑集群列表
     * @return
     */
    private Multimap<String, ClusterLogic> getRack2LogicClusterMappings(List<ClusterLogicRackInfo> logicClusterRacks,
                                                                        List<ClusterLogic> logicClusters) {

        Map<Long, ClusterLogic> logicClusterMappings = ConvertUtil.list2Map(logicClusters, ClusterLogic::getId);

        // 一个rack被多个逻辑集群集群所关联
        Multimap<String, ClusterLogic> logicClusterId2RackInfoMap = ArrayListMultimap.create();
        for (ClusterLogicRackInfo rackInfo : logicClusterRacks) {
            List<Long> logicClusterIds = ListUtils.string2LongList(rackInfo.getLogicClusterIds());
            if (CollectionUtils.isEmpty(logicClusterIds)) {
                continue;
            }
            logicClusterIds.forEach(logicClusterId -> logicClusterId2RackInfoMap.put(createClusterRackKey(rackInfo.getPhyClusterName(),
                    rackInfo.getRack()), logicClusterMappings.get(logicClusterId)));
        }

        return logicClusterId2RackInfoMap;
    }

    /**
     * 创建集群Rack分隔符
     * @param cluster 集群名称
     * @param rack Rack
     * @return
     */
    private String createClusterRackKey(String cluster, String rack) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(cluster)) {
            builder.append(cluster);
        }

        builder.append(AdminConstant.CLUSTER_RACK_COMMA);

        if (StringUtils.isNotBlank(rack)) {
            builder.append(rack);
        }

        return builder.toString();
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

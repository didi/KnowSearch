package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.RackMetaMetric;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.client.constant.quota.NodeSpecifyEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.quota.Resource;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;

import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.metadata.service.NodeStatisService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ClusterNodeManagerImpl implements ClusterNodeManager {

    private static final ILog      LOGGER = LogFactory.getLog(ClusterNodeManager.class);

    @Autowired
    private NodeStatisService      nodeStatisService;

    @Autowired
    private RoleClusterHostService roleClusterHostService;

    @Autowired
    private ClusterLogicService    clusterLogicService;

    @Autowired
    private RegionRackService      regionRackService;

    /**
     * 物理集群节点转换
     *
     * @param clusterNodes       物理集群节点
     * @return
     */
    @Override
    public List<ESRoleClusterHostVO> convertClusterLogicNodes(List<RoleClusterHost> clusterNodes) {
        List<ESRoleClusterHostVO> result = Lists.newArrayList();

        List<ClusterLogicRackInfo> clusterRacks = regionRackService.listAllLogicClusterRacks();

        Multimap<String, ClusterLogic> rack2LogicClusters = getRack2LogicClusterMappings(clusterRacks,
                clusterLogicService.listAllClusterLogics());
        Map<String, ClusterLogicRackInfo> rack2ClusterRacks = getRack2ClusterRacks(clusterRacks);

        for (RoleClusterHost node : clusterNodes) {
            ESRoleClusterHostVO nodeVO = ConvertUtil.obj2Obj(node, ESRoleClusterHostVO.class);

            String clusterRack = createClusterRackKey(node.getCluster(), node.getRack());
            ClusterLogicRackInfo rackInfo = rack2ClusterRacks.get(clusterRack);
            if (rackInfo != null) {
                nodeVO.setRegionId(rackInfo.getRegionId());
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
        List<RoleClusterHost> clusterNodes = roleClusterHostService.getOnlineNodesByCluster(clusterName);
        // rack到集群节点的map
        Multimap<String, RoleClusterHost> rack2ESClusterNodeMultiMap = ConvertUtil.list2MulMap(clusterNodes,
            RoleClusterHost::getRack);
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
        List<RoleClusterHost> clusterNodes = roleClusterHostService.getOnlineNodesByCluster(clusterName);
        // rack到rack下节点的map
        Multimap<String, RoleClusterHost> rack2ESClusterNodeMultiMap = ConvertUtil.list2MulMap(clusterNodes,
            RoleClusterHost::getRack);

        // 遍历rack
        for (Map.Entry<String, Collection<RoleClusterHost>> entry : rack2ESClusterNodeMultiMap.asMap().entrySet()) {
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
    public List<ESRoleClusterHostVO> convertClusterPhyNodes(List<RoleClusterHost> roleClusterHosts,
                                                            String clusterPhyName) {
        List<ESRoleClusterHostVO> esRoleClusterHostVOS = ConvertUtil.list2List(roleClusterHosts,
            ESRoleClusterHostVO.class);

        //获取host所在regionId
        List<ClusterRegion> regions = regionRackService.listPhyClusterRegions(clusterPhyName);
        esRoleClusterHostVOS.forEach(esRoleClusterHostVO->{
            buildHostRegionIdAndLogicName(esRoleClusterHostVO, regions);
        });

        return esRoleClusterHostVOS;
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

    private void buildHostRegionIdAndLogicName(ESRoleClusterHostVO esRoleClusterHostVO, List<ClusterRegion> regions) {
        Map<Long/*regionId*/, List<String>/*racks*/> regionId2RacksMap = ConvertUtil.list2Map(regions,
                ClusterRegion::getId, region -> ListUtils.string2StrList(region.getRacks()));

        regionId2RacksMap.forEach((key, value) -> {
            if (value.contains(esRoleClusterHostVO.getRack())) {
                esRoleClusterHostVO.setRegionId(key);
                // 根据region获取data节点被绑定的逻辑集群的信息并设置到host视图当中
                buildHostLogicName(esRoleClusterHostVO, key);
            }
        });
    }

    private void buildHostLogicName(ESRoleClusterHostVO esRoleClusterHostVO, Long key) {
        ClusterRegion clusterRegion = regionRackService.getRegionById(key);
        if (clusterRegion == null) {
            LOGGER.error("class=ClusterNodeManagerImpl||method=buildHostRegionIdAndLogicName||errMsg=clusterRegion doesn't exit!");
            return;
        }

        List<Long> logicClusterIds = ListUtils.string2LongList(clusterRegion.getLogicClusterIds());
        // region没有绑定到逻辑集群，则不设置关联的逻辑集群
        if (CollectionUtils.isEmpty(logicClusterIds) ||
                logicClusterIds.get(0).equals(Long.parseLong(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID))) {
            return;
        }

        //获取逻辑集群对应的名称列表
        List<String> clusterLogicNames = logicClusterIds.stream()
                .map(logicClusterId -> clusterLogicService.getClusterLogicById(logicClusterId))
                .map(ClusterLogic::getName)
                .collect(Collectors.toList());

        esRoleClusterHostVO.setClusterLogicNames(ListUtils.strList2String(clusterLogicNames));
    }
}

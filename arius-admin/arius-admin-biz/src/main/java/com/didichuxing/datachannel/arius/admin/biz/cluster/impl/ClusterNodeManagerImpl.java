package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.RackMetaMetric;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.client.constant.quota.NodeSpecifyEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.quota.Resource;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.metadata.service.NodeStatisService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ClusterNodeManagerImpl implements ClusterNodeManager {

    private static final ILog        LOGGER = LogFactory.getLog(ClusterNodeManager.class);

    @Autowired
    private NodeStatisService        nodeStatisService;

    @Autowired
    private ESRoleClusterHostService esRoleClusterHostService;

    @Autowired
    private ESClusterLogicService    esClusterLogicService;

    @Autowired
    private ESRegionRackService      esRegionRackService;

    /**
     * 物理集群节点转换
     *
     * @param clusterNodes       物理集群节点
     * @return
     */
    @Override
    public List<ESRoleClusterHostVO> convertClusterNodes(List<ESRoleClusterHost> clusterNodes) {
        List<ESRoleClusterHostVO> result = Lists.newArrayList();

        List<ESClusterLogicRackInfo> clusterRacks = esRegionRackService.listAllLogicClusterRacks();

        Map<String, ESClusterLogic> rack2LogicClusters = getRack2LogicClusterMappings(clusterRacks,
            esClusterLogicService.listAllLogicClusters());
        Map<String, ESClusterLogicRackInfo> rack2ClusterRacks = getRack2ClusterRacks(clusterRacks);

        for (ESRoleClusterHost node : clusterNodes) {
            ESRoleClusterHostVO nodeVO = ConvertUtil.obj2Obj(node, ESRoleClusterHostVO.class);
            nodeVO.setNodeSpec(NodeSpecifyEnum.DOCKER.getDesc());

            String clusterRack = createClusterRackKey(node.getCluster(), node.getRack());
            ESClusterLogicRackInfo rackInfo = rack2ClusterRacks.get(clusterRack);
            if (rackInfo != null) {
                nodeVO.setRegionId(rackInfo.getRegionId());
            }

            ESClusterLogic esClusterLogic = rack2LogicClusters.get(clusterRack);
            if (esClusterLogic != null) {
                nodeVO.setLogicDepart(esClusterLogic.getName());
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
            LOGGER.warn("method=metaAndMetric||racksSize={}||resultSize={}", racks.size(), rackMetaMetrics.size());
        }

        // 获取每个节点的规格，当前ECM提供的时候datanode的规格
        Result<Resource> dataNodeSpecifyResult = getDataNodeSpecify(clusterName);
        if (dataNodeSpecifyResult.failed()) {
            return Result.buildFrom(dataNodeSpecifyResult);
        }
        Resource dataNodeSpecify = dataNodeSpecifyResult.getData();

        // 获取集群节点
        List<ESRoleClusterHost> clusterNodes = esRoleClusterHostService.getOnlineNodesByCluster(clusterName);
        // rack到集群节点的map
        Multimap<String, ESRoleClusterHost> rack2ESClusterNodeMultiMap = ConvertUtil.list2MulMap(clusterNodes,
            ESRoleClusterHost::getRack);
        // rack到rack资源信息的map
        Map<String, RackMetaMetric> rack2RackMetaMetricMap = ConvertUtil.list2Map(rackMetaMetrics,
            RackMetaMetric::getName);

        for (String rack : racks) {
            RackMetaMetric rackMetaMetric = rack2RackMetaMetricMap.get(rack);

            if (rackMetaMetric == null) {
                return Result.buildFrom(Result.buildParamIllegal("AMS rack统计结果缺失：" + rack));
            }

            // rack的节点数
            if (rack2ESClusterNodeMultiMap.containsKey(rackMetaMetric.getName())) {
                rackMetaMetric.setNodeCount(rack2ESClusterNodeMultiMap.get(rackMetaMetric.getName()).size());
            } else {
                LOGGER.warn("method=metaAndMetric||rack={}||msg=offline", rackMetaMetric.getName());
                rackMetaMetric.setNodeCount(0);
            }

            // rack的cpu数
            rackMetaMetric.setCpuCount(dataNodeSpecify.getCpu().intValue() * rackMetaMetric.getNodeCount());
            // rack包含的磁盘大小
            rackMetaMetric.setTotalDiskG(dataNodeSpecify.getDisk() * 1.0 * rackMetaMetric.getNodeCount());
        }

        // 校验统计结果是否正确
        Result checkResult = checkRackMetrics(rackMetaMetrics, dataNodeSpecify);
        if (checkResult.failed()) {
            return Result.buildFrom(Result.buildParamIllegal("AMS rack统计结果非法：" + checkResult.getMessage()));
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
        Result<Resource> dataNodeSpecifyResult = getDataNodeSpecify(clusterName);
        if (dataNodeSpecifyResult.failed()) {
            return Result.buildFrom(dataNodeSpecifyResult);
        }
        Resource dataNodeSpecify = dataNodeSpecifyResult.getData();

        // 获取集群节点
        List<ESRoleClusterHost> clusterNodes = esRoleClusterHostService.getOnlineNodesByCluster(clusterName);
        // rack到rack下节点的map
        Multimap<String, ESRoleClusterHost> rack2ESClusterNodeMultiMap = ConvertUtil.list2MulMap(clusterNodes,
            ESRoleClusterHost::getRack);

        // 遍历rack
        for (Map.Entry<String, Collection<ESRoleClusterHost>> entry : rack2ESClusterNodeMultiMap.asMap().entrySet()) {
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
                LOGGER.warn("method=meta||rack={}||msg=offline", entry.getKey());
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

    /**************************************** private method ***************************************************/
    //todo：zqr
    private Result<Resource> getDataNodeSpecify(String clusterName) {
        return Result.buildFrom(Result.buildFail("获取集群规格失败"));
    }

    private Result checkRackMetrics(List<RackMetaMetric> rackMetaMetrics, Resource dataNodeSpecify) {
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

        return Result.buildSucc();
    }

    /**************************************** private method ***************************************************/
    /**
     * 获取Rack名称与集群Rack映射关系
     * @param clusterRacks 集群Rack列表
     * @return
     */
    private Map<String, ESClusterLogicRackInfo> getRack2ClusterRacks(List<ESClusterLogicRackInfo> clusterRacks) {
        Map<String, ESClusterLogicRackInfo> rack2ClusterRacks = new HashMap<>(1);
        if (CollectionUtils.isNotEmpty(clusterRacks)) {
            for (ESClusterLogicRackInfo rackInfo : clusterRacks) {
                rack2ClusterRacks.put(createClusterRackKey(rackInfo.getPhyClusterName(), rackInfo.getRack()), rackInfo);
            }
        }

        return rack2ClusterRacks;
    }

    /**
     * 获取所有集群Rack与逻辑集群映射信息
     *
     * @param logicClusterRacks 所有集群Racks
     * @param logicClusters             逻辑集群列表
     * @return
     */
    private Map<String, ESClusterLogic> getRack2LogicClusterMappings(List<ESClusterLogicRackInfo> logicClusterRacks,
                                                                     List<ESClusterLogic> logicClusters) {
        Map<String, ESClusterLogic> rack2LogicClusterMappings = Maps.newHashMap();

        Map<Long, ESClusterLogic> logicClusterMappings = ConvertUtil.list2Map(logicClusters, ESClusterLogic::getId);
        for (ESClusterLogicRackInfo rackInfo : logicClusterRacks) {
            if (logicClusterMappings.containsKey(rackInfo.getLogicClusterId())) {
                rack2LogicClusterMappings.put(createClusterRackKey(rackInfo.getPhyClusterName(), rackInfo.getRack()),
                    logicClusterMappings.get(rackInfo.getLogicClusterId()));
            }
        }

        return rack2LogicClusterMappings;
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
}

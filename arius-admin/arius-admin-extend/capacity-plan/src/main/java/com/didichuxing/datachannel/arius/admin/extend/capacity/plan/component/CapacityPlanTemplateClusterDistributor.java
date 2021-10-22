package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.component;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;
import static com.didichuxing.datachannel.arius.admin.common.util.RackUtils.belong;

import java.util.*;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.biz.component.DistributorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateDistributedRack;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.TemplateClusterDistributor;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanArea;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanAreaService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * @author d06679
 * @date 2019-07-09
 */
@Component("capacityPlanTemplateResourceDistributor")
public class CapacityPlanTemplateClusterDistributor implements TemplateClusterDistributor {

    private static final ILog         LOGGER = LogFactory.getLog( CapacityPlanTemplateClusterDistributor.class);

    @Autowired
    private CapacityPlanAreaService capacityPlanAreaService;

    @Autowired
    private CapacityPlanRegionService capacityPlanRegionService;

    @Autowired
    private AriusConfigInfoService    ariusConfigInfoService;

    @Autowired
    private DistributorUtils          distributorUtils;

    /**
     * 分配资源
     *
     * @param resourceId 资源
     * @param quota      配额
     * @return rack
     */
    @Override
    public Result<TemplateDistributedRack> distribute(Long resourceId, Double quota) {
        List<CapacityPlanRegion> regions = fetchMatchedRegions(resourceId);
        if (CollectionUtils.isEmpty(regions)) {
            return Result.buildFrom(Result.build(ResultType.NO_CAPACITY_PLAN));
        }

        return Result.buildSucc(fetchMaxSuitableRegion(resourceId, regions, quota));
    }

    /**
     * 根据Quota获取最匹配的region
     * @param resourceId 逻辑模板ID
     * @param regions 满足要求的region列表
     * @param quota Quota
     * @return
     */
    private TemplateDistributedRack fetchMaxSuitableRegion(Long resourceId, List<CapacityPlanRegion> regions, Double quota) {
        Multimap<String, CapacityPlanRegion> cluster2CapacityPlanRegionMultiMap =
                ConvertUtil.list2MulMap(regions, CapacityPlanRegion::getClusterName);

        String clusterMostFree = getMostFreeCluster(resourceId, cluster2CapacityPlanRegionMultiMap);

        LOGGER.info("method=distribute||resourceId={}||clusterMostFree={}", resourceId, clusterMostFree);

        List<CapacityPlanRegion> clusterRegions = Lists
                .newArrayList(cluster2CapacityPlanRegionMultiMap.get(clusterMostFree));

        clusterRegions.sort(Comparator.comparing(CapacityPlanRegion::getFreeQuota));
        Collections.reverse(clusterRegions);

        CapacityPlanRegion distributedRegion = clusterRegions.get(0);
        double freeRackQuota = getClusterFreeRackQuota(resourceId, clusterMostFree);
        boolean isRegionQuotaMatched = true;
        if (distributedRegion.getFreeQuota() + freeRackQuota < quota
                && AdminConstant.MIN_QUOTA < quota) {
            isRegionQuotaMatched = false;
        }

        LOGGER.info("method=distribute||resourceId={}||quota={}||freeRackQuota={}||region={}||resourceMatched={}",
                resourceId, quota, freeRackQuota, distributedRegion, isRegionQuotaMatched);

        TemplateDistributedRack distributedRack = new TemplateDistributedRack();
        distributedRack.setCluster(distributedRegion.getClusterName());
        distributedRack.setRack(distributedRegion.getRacks());
        distributedRack.setIsResourceSuitable(isRegionQuotaMatched);

        return distributedRack;
    }

    /**
     * 获取符合要求的IDC
     * @param resourceId
     * @return
     */
    private List<CapacityPlanRegion> fetchMatchedRegions(Long resourceId) {
        List<CapacityPlanRegion> regions = capacityPlanRegionService.listLogicClusterSharedRegions(resourceId);

        Set<String> clusterBlackList = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
                "auto.process.work.order.cluster.blacks", "", ",");

        double overSoldThreshold = ariusConfigInfoService.doubleSetting(ARIUS_COMMON_GROUP,
                "auto.process.work.order.oversold.threshold", 2.0);

        List<String> clusterNames = distributorUtils.fetchClusterNames();

        regions = regions.stream().filter(region -> {
            if (clusterBlackList.contains(region.getClusterName())) {
                return false;
            }

            if (region.getOverSold() != null && region.getOverSold() > overSoldThreshold) {
                return false;
            }

            if (clusterNames != null && !clusterNames.contains(region.getClusterName())) {
                return false;
            }

            return true;
        }).collect(Collectors.toList());

        return regions;
    }

    /**
     * 在指定逻辑集群的物理集群分配
     *
     * @param resourceId 资源
     * @param cluster    物理集群
     * @param quota      quota
     * @return result
     */
    @Override
    public Result<TemplateDistributedRack> indecrease(Long resourceId, String cluster, String rack, Double quota) {
        double overSoldThredhold = ariusConfigInfoService.doubleSetting(ARIUS_COMMON_GROUP,
            "auto.process.work.order.oversold.thredhold", 2.0);

        List<CapacityPlanRegion> regions = capacityPlanRegionService.listLogicClusterSharedRegions(resourceId);

        regions = regions.stream().filter(region -> {
            if (!region.getClusterName().equals(cluster) || !belong(rack, region.getRacks())) {
                return false;
            }

            if (region.getOverSold() != null && region.getOverSold() > overSoldThredhold) {
                return false;
            }

            return true;

        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(regions)) {
            return Result.buildFrom(Result.build(ResultType.NO_CAPACITY_PLAN));
        }

        regions.sort(Comparator.comparing(CapacityPlanRegion::getFreeQuota));
        Collections.reverse(regions);

        CapacityPlanRegion distributedRegion = regions.get(0);
        if (distributedRegion.getFreeQuota() + getClusterFreeRackQuota(resourceId, cluster) < quota) {
            return Result.buildFrom(Result.buildFail("集群空闲资源不足"));
        }

        LOGGER.info("method=distribute||resourceId={}||quota={}||region={}", resourceId, quota, distributedRegion);

        TemplateDistributedRack distributedRack = new TemplateDistributedRack();
        distributedRack.setCluster(distributedRegion.getClusterName());
        distributedRack.setRack(distributedRegion.getRacks());
        distributedRack.setIsResourceSuitable(true);

        return Result.buildSucc(distributedRack);
    }

    private String getMostFreeCluster(Long resourceId,
                                      Multimap<String, CapacityPlanRegion> cluster2CapacityPlanRegionMultiMap) {

        Map<String, Double> cluster2FreeQuotaMap = Maps.newHashMap();

        for (String cluster : cluster2CapacityPlanRegionMultiMap.keySet()) {

            Double freeQuota = 0.0;
            for (CapacityPlanRegion region : cluster2CapacityPlanRegionMultiMap.get(cluster)) {
                freeQuota += region.getFreeQuota();
            }

            freeQuota = freeQuota + getClusterFreeRackQuota(resourceId, cluster);

            LOGGER.info("method=getMostFreeCluster||cluster={}||freeQuota={}", cluster, freeQuota);

            cluster2FreeQuotaMap.put(cluster, freeQuota);
        }

        String mostFreeCluster = "";
        Double maxFreeQuota = -10000.0;
        for (String cluster : cluster2FreeQuotaMap.keySet()) {
            if (maxFreeQuota < cluster2FreeQuotaMap.get(cluster)) {
                maxFreeQuota = cluster2FreeQuotaMap.get(cluster);
                mostFreeCluster = cluster;
            }
        }

        return mostFreeCluster;
    }

    private Double getClusterFreeRackQuota(Long resourceId, String cluster) {
        CapacityPlanArea capacityPlanCluster = capacityPlanAreaService.getAreaByResourceIdAndCluster(resourceId, cluster);
        if (capacityPlanCluster == null) {
            return 0.0;
        }

        List<String> freeRacks = capacityPlanAreaService.listAreaFreeRacks(
                capacityPlanCluster.getResourceId());
        if (CollectionUtils.isEmpty(freeRacks)) {
            return 0.0;
        }

        // freeRack只能提供25%用于接新的需求
        return freeRacks.size() * 2 * AdminConstant.FREE_RACK_FOR_NEW_DEMAND_RATE;
    }
}

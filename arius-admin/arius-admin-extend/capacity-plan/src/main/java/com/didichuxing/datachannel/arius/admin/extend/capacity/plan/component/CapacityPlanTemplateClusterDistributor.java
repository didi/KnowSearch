package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.component;

import com.didichuxing.datachannel.arius.admin.biz.component.DistributorUtils;
import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.TemplateClusterDistributor;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateDistributedRack;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanArea;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanAreaService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.util.RackUtils.belong;

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
            return Result.build(ResultType.NO_CAPACITY_PLAN);
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

        LOGGER.info("class=CapacityPlanTemplateClusterDistributor||method=distribute||resourceId={}||clusterMostFree={}", resourceId, clusterMostFree);

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

        LOGGER.info("class=CapacityPlanTemplateClusterDistributor||method=distribute||resourceId={}||quota={}||freeRackQuota={}||region={}||resourceMatched={}",
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
        List<String> clusterNames = distributorUtils.fetchClusterNames();

        regions = regions.stream().filter(region -> {
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
        List<CapacityPlanRegion> regions = capacityPlanRegionService.listLogicClusterSharedRegions(resourceId);

        regions = regions.stream().filter(region -> {
            if (!region.getClusterName().equals(cluster) || !belong(rack, region.getRacks())) {
                return false;
            }

            return true;

        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(regions)) {
            return Result.build(ResultType.NO_CAPACITY_PLAN);
        }

        regions.sort(Comparator.comparing(CapacityPlanRegion::getFreeQuota));
        Collections.reverse(regions);

        CapacityPlanRegion distributedRegion = regions.get(0);
        if (distributedRegion.getFreeQuota() + getClusterFreeRackQuota(resourceId, cluster) < quota) {
            return Result.buildFail("集群空闲资源不足");
        }

        LOGGER.info("class=CapacityPlanTemplateClusterDistributor||method=distribute||resourceId={}||quota={}||region={}", resourceId, quota, distributedRegion);

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

            LOGGER.info("class=CapacityPlanTemplateClusterDistributor||method=getMostFreeCluster||cluster={}||freeQuota={}", cluster, freeQuota);

            cluster2FreeQuotaMap.put(cluster, freeQuota);
        }

        String mostFreeCluster = "";
        Double maxFreeQuota = -10000.0;

        for(Map.Entry<String, Double> entry : cluster2FreeQuotaMap.entrySet()){
            String cluster = entry.getKey();
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

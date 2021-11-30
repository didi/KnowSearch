package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.impl;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.RackMetaMetric;
import com.didichuxing.datachannel.arius.admin.client.bean.common.RegionMetric;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.quota.Resource;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.QuotaTool;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po.CapacityPlanRegionStatisESPO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanArea;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegionTask;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegionTaskItem;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.dao.es.CapacityPlanRegionStatisESDAO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanAreaService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionTaskService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanStatisticsService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.client.constant.quota.NodeSpecifyEnum.DOCKER;

/**
 * @author d06679
 * @date 2019-09-04
 */
@Service
public class CapacityPlanStatisticsServiceImpl implements CapacityPlanStatisticsService {

    private static final ILog             LOGGER     = LogFactory.getLog(CapacityPlanStatisticsServiceImpl.class);

    @Autowired
    private CapacityPlanAreaService       capacityPlanAreaService;

    @Autowired
    private CapacityPlanRegionService     capacityPlanRegionService;

    @Autowired
    private CapacityPlanRegionTaskService capacityPlanRegionTaskService;

    @Autowired
    private ClusterNodeManager      clusterNodeManager;

    @Autowired
    private QuotaTool                     quotaTool;

    @Autowired
    private CapacityPlanRegionStatisESDAO  capacityPlanRegionStatisESDAO;
    
    /**
     * 统计一个area的规划信息
     *
     * @param areaId areaId
     * @return true/false
     */
    @Override
    public Result<Void> statisticsPlanClusterById(Long areaId) {

        CapacityPlanArea capacityPlanArea = capacityPlanAreaService.getAreaById(areaId);

        if (capacityPlanArea == null) {
            LOGGER.info("class=CapacityPlanStatisticsServiceImpl||method=statisByArea||msg=not-found||areaId={}", areaId);
            return Result.buildNotExist("areaId不存在：" + areaId);
        }

        // 获取area下的region
        List<CapacityPlanRegion> regions = capacityPlanRegionService.listRegionsInArea(areaId);
        if (CollectionUtils.isEmpty(regions)) {
            LOGGER.info("class=CapacityPlanStatisticsServiceImpl||method=statisByArea||msg=no region||areaId={}", areaId);
            return Result.buildSucc();
        }

        CapacityPlanRegionStatisESPO base = new CapacityPlanRegionStatisESPO();
        base.setAreaId(areaId);
        base.setResourceId(capacityPlanArea.getResourceId());
        base.setCluster(capacityPlanArea.getClusterName());
        base.setTimestamp(new Date());

        List<CapacityPlanRegionStatisESPO> regionStatis = Lists.newArrayList();

        List<String> errMsgs = Lists.newArrayList();
        // 遍历region，统计资源使用情况
        for (CapacityPlanRegion region : regions) {
            Result<CapacityPlanRegionStatisESPO> regionStatisResult = statis(region, base);
            if (regionStatisResult.success()) {
                regionStatis.add(regionStatisResult.getData());
            } else {
                errMsgs.add(regionStatisResult.getMessage());
            }
        }

        if (CollectionUtils.isNotEmpty(regionStatis)) {
            if (capacityPlanRegionStatisESDAO.batchInsert(regionStatis)) {
                LOGGER.info("class=CapacityPlanStatisticsServiceImpl||method=statisByArea||msg=sendSucc||size={}", regionStatis.size());
            } else {
                LOGGER.info("class=CapacityPlanStatisticsServiceImpl||method=statisByArea||msg=sendFail||size={}", regionStatis.size());
            }

            if (saveRate(areaId, regionStatis)) {
                LOGGER.info("class=CapacityPlanStatisticsServiceImpl||method=statisByArea||msg=saveRateSucc||size={}", regionStatis.size());
            } else {
                LOGGER.info("class=CapacityPlanStatisticsServiceImpl||method=statisByArea||msg=saveRateFail||size={}", regionStatis.size());
            }

        }

        if (CollectionUtils.isEmpty(errMsgs)) {
            return Result.buildSucc();
        }

        return Result.buildFail(String.join(",", errMsgs));
    }

    private Result<CapacityPlanRegionStatisESPO> statis(CapacityPlanRegion region, CapacityPlanRegionStatisESPO base) {
        // 获取上一个check任务
        CapacityPlanRegionTask lastCheckTask = capacityPlanRegionTaskService.getLastCheckTask(region.getRegionId());
        if (lastCheckTask == null) {
            return Result.buildParamIllegal("regionId" + region.getRegionId() + "没有找到lastCheckTask");
        }

        // 当前region能够提供的资源
        Result<List<RackMetaMetric>> rackMetaMetricsResult = clusterNodeManager.meta(region.getClusterName(),
            Sets.newHashSet(region.getRacks().split(",")));
        if (rackMetaMetricsResult.failed()) {
            return Result.buildParamIllegal("regionId" + region.getRegionId() + "获取meta失败");
        }
        List<RackMetaMetric> rackMetas = rackMetaMetricsResult.getData();
        RegionMetric regionMetric = capacityPlanRegionService.calcRegionMetric(rackMetas);

        // 获取上一个check任务的所有任务item
        List<CapacityPlanRegionTaskItem> taskItems = capacityPlanRegionTaskService
            .getTaskItemByTaskId(lastCheckTask.getId());
        Double itemQuotaSum = 0.0;
        for (CapacityPlanRegionTaskItem item : taskItems) {
            itemQuotaSum += computeTemplateQuota(item);
        }

        CapacityPlanRegionStatisESPO statis = ConvertUtil.obj2Obj(base, CapacityPlanRegionStatisESPO.class);
        statis.setRegionId(region.getRegionId());
        statis.setRacks(region.getRacks());

        // 资源总量
        statis.setNodeCount(regionMetric.getNodeCount());
        statis.setRegionDiskG(regionMetric.getResource().getDisk());
        statis.setRegionCpuCount(regionMetric.getResource().getCpu());
        statis.setQuota(quotaTool.getResourceQuotaCountByCpuAndDisk(DOCKER.getCode(), statis.getRegionCpuCount(),
            statis.getRegionDiskG(), 0.0));

        // 资源消耗量
        statis.setCostDiskG(lastCheckTask.getRegionCostDiskG());
        statis.setCostCpuCount(lastCheckTask.getRegionCostCpuCount());
        statis.setCostQuota(quotaTool.getTemplateQuotaCountByCpuAndDisk(DOCKER.getCode(), statis.getCostCpuCount(),
            statis.getCostDiskG(), 0.0));

        // 资源售卖量
        statis.setSoldQuota(itemQuotaSum);
        Resource resource = quotaTool.getResourceOfQuota(DOCKER.getCode(), itemQuotaSum);
        statis.setSoldCpuCount(resource.getCpu());
        statis.setSoldDiskG(resource.getDisk());

        // 资源空闲量
        statis.setFreeQuota(region.getFreeQuota());
        statis.setFreeCpuCount(statis.getRegionCpuCount() - statis.getCostCpuCount());
        statis.setFreeDiskG(statis.getRegionDiskG() - statis.getCostDiskG());

        return Result.buildSucc(statis);
    }

    private Double computeTemplateQuota(CapacityPlanRegionTaskItem item) {
        if (item.getHotDay() > 0 && item.getHotDay() < item.getExpireTime()) {
            return item.getQuota() * (item.getHotDay() * 1.0 / item.getExpireTime());
        }
        return item.getQuota();
    }

    private boolean saveRate(Long areaId, List<CapacityPlanRegionStatisESPO> regionStatises) {
        double usageSum = 0.0;
        double overSoldSum = 0.0;
        int count = regionStatises.size();

        boolean succ = true;

        for (CapacityPlanRegionStatisESPO regionStatis : regionStatises) {
            double usage ;
            double overSold ;
            if (regionStatis.getQuota() != null && regionStatis.getQuota() > 0.0) {
                // 资源实际使用率
                usage = regionStatis.getCostQuota() / regionStatis.getQuota();
                // 资源售出率
                overSold = regionStatis.getSoldQuota() / regionStatis.getQuota();
                // 更新region的资源实际使用率和资源售出率
                succ = succ && capacityPlanRegionService.modifyRegionMetrics(regionStatis.getRegionId(), usage, overSold);
                // 累计资源使用率
                usageSum += usage;
                overSoldSum += overSold;
            }
        }

        double usageAvg = usageSum / count;
        double overSoldAvg = overSoldSum / count;
        succ = succ && capacityPlanAreaService.recordAreaStatis(areaId, usageAvg, overSoldAvg);

        return succ;
    }

}

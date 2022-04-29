package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyStatisManager;
import com.didichuxing.datachannel.arius.admin.common.constant.quota.Resource;
import com.didichuxing.datachannel.arius.admin.common.bean.common.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.exception.AmsRemoteException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common.CapacityPlanConfig;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common.CapacityPlanRegionContext;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.MILLIS_PER_DAY;
import static com.didichuxing.datachannel.arius.admin.common.util.RackUtils.belong;

/**
 * @author d06679
 * @date 2019-06-26
 */
@Component
public class RegionResourceManager {

    private static final ILog         LOGGER = LogFactory.getLog(RegionResourceManager.class);

    @Autowired
    private TemplatePhyService              templatePhyService;

    @Autowired
    private TemplatePhyStatisManager        templatePhyStatisManager;

    @Autowired
    private ClusterNodeManager              clusterNodeManager;

    @Autowired
    private CapacityPlanRegionService       capacityPlanRegionService;

    /**
     * 计算region在未来一段时间内的资源是否合适
     * @param regionPlanContext regionContext
     * @return 需要调整的资源
     */
    public Resource getIntervalResourceGap(CapacityPlanRegionContext regionPlanContext) {
        CapacityPlanRegion region = regionPlanContext.getRegion();
        CapacityPlanConfig regionConfig = region.getConfig();

        List<TemplateMetaMetric> templateMetaMetrics = getRegionTemplateMetrics(region,
            regionConfig.getPlanRegionResourceDays() * MILLIS_PER_DAY, regionConfig);

        // 实际的模板资源消耗
        Double regionCostDiskG = 0.0;
        Double regionCostCpuCount = 0.0;

        for (TemplateMetaMetric templateMetaMetric : templateMetaMetrics) {
            regionCostDiskG += templateMetaMetric.getCombinedDiskG();
            regionCostCpuCount += templateMetaMetric.getCombinedCpuCount();
        }

        // 当前region能够提供的资源
        List<RackMetaMetric> rackMetas = capacityPlanRegionService.fetchRegionRackMetrics(region);

        RegionMetric regionMetric = capacityPlanRegionService.calcRegionMetric(rackMetas);

        Resource resourceGap = new Resource(
            getGap(regionMetric.getResource().getCpu(), regionCostCpuCount, regionConfig), 0.0,
            getGap(regionMetric.getResource().getDisk(), regionCostDiskG, regionConfig));

        // 记录上下文
        regionPlanContext.setTemplateMetaMetrics(templateMetaMetrics);
        regionPlanContext.setRackMetas(rackMetas);
        regionPlanContext.setRegionMetric(regionMetric);
        regionPlanContext.setRegionCostDiskG(regionCostDiskG);
        regionPlanContext.setRegionCostCpuCount(regionCostCpuCount);

        LOGGER.info("class=RegionResourceManager||method=getIntervalResourceGap||region={}||resourceGap={}", regionPlanContext.getRegion(),
            resourceGap);

        Result<Boolean> globalCheckResult = checkGlobal(regionPlanContext, false);
        if (globalCheckResult.failed()) {
            LOGGER.info(
                "class=RegionResourceManager||method=getIntervalResourceGap||msg=checkGlobalFail||templateMetaMetrics={}||rackMetas={}||regionMetric={}||resourceGap={}",
                JSON.toJSON(templateMetaMetrics), JSON.toJSON(rackMetas), JSON.toJSON(regionMetric),
                JSON.toJSON(resourceGap));
            throw new AmsRemoteException(
                "regionId: " + region.getRegionId() + ", AMS统计指标失真: " + globalCheckResult.getMessage());
        }

        return resourceGap;
    }

    /**
     * 计算当前的region资源是否充足
     * @param regionPlanContext regionContext
     * @return result
     */
    public Resource getCurrentResourceGap(CapacityPlanRegionContext regionPlanContext) {
        CapacityPlanRegion region = regionPlanContext.getRegion();
        CapacityPlanConfig regionConfig = region.getConfig();

        // 获取磁盘的统计指标，包含各个rack的磁盘空闲空间
        Result<List<RackMetaMetric>> rackMetaMetricsResult = clusterNodeManager.metaAndMetric(region.getClusterName(),
            Sets.newHashSet(region.getRacks().split(",")));
        if (rackMetaMetricsResult.failed()) {
            throw new AmsRemoteException("获取资源Gap失败：" + rackMetaMetricsResult.getMessage());
        }
        List<RackMetaMetric> rackMetaMetrics = rackMetaMetricsResult.getData();

        RegionMetric regionMetric = capacityPlanRegionService.calcRegionMetric(rackMetaMetrics);

        // 通过region中各个模板的实时指标计算region实时的cpu消耗
        List<TemplateMetaMetric> templateMetaMetrics = getRegionTemplateMetrics(region,
            regionConfig.getCheckRegionResourceMinutes() * 60 * 1000L, regionConfig);
        Double regionCurrentCostCpuCount = computeCurrentCostCpuCount(templateMetaMetrics);

        Double regionCurrentCostDiskG = regionMetric.getResource().getDisk() - regionMetric.getDiskFreeG();

        Resource resourceGap = new Resource(
            getGap(regionMetric.getResource().getCpu(), regionCurrentCostCpuCount, regionConfig), 0.0,
            getGap(regionMetric.getResource().getDisk(), regionCurrentCostDiskG, regionConfig));

        // 记录上下文
        regionPlanContext.setTemplateMetaMetrics(templateMetaMetrics);
        regionPlanContext.setRegionMetric(regionMetric);
        regionPlanContext.setRegionCostDiskG(regionCurrentCostDiskG);
        regionPlanContext.setRegionCostCpuCount(regionCurrentCostCpuCount);

        LOGGER.info(
            "class=RegionResourceManager||method=getCurrentResourceGap||region={}||regionMetric={}||resourceGap={}||templateMetaMetricsSize={}",
            regionPlanContext.getRegion(), regionMetric, resourceGap, templateMetaMetrics.size());

        Result<Boolean> globalCheckResult = checkGlobal(regionPlanContext, true);
        if (globalCheckResult.failed()) {
            LOGGER.info(
                "class=RegionResourceManager||method=getCurrentResourceGap||msg=checkGlobalFail||rackMetaMetrics={}||regionMetric={}||templateMetaMetrics={}||resourceGap={}",
                JSON.toJSON(rackMetaMetrics), JSON.toJSON(regionMetric), JSON.toJSON(templateMetaMetrics),
                JSON.toJSON(resourceGap));
            throw new AmsRemoteException(
                "regionId: " + region.getRegionId() + ", AMS统计指标失真: " + globalCheckResult.getMessage());
        }

        return resourceGap;
    }

    /**
     * 获取指定模板中模板指定间隔的统计指标
     * @param templatePhysicalIds templatePhysicalIds
     * @param interval interval
     * @return list
     */
    public List<TemplateMetaMetric> getTemplateMetrics(List<Long> templatePhysicalIds, long interval,
                                                       CapacityPlanConfig capacityPlanConfig) {
        long now = System.currentTimeMillis();
        Result<List<TemplateMetaMetric>> result = templatePhyStatisManager.getTemplateMetricByPhysicals(
            templatePhysicalIds, now - interval, now,
            ConvertUtil.obj2Obj(capacityPlanConfig, TemplateResourceConfig.class));

        if (result.failed()) {
            throw new AmsRemoteException(result.getMessage());
        }

        List<TemplateMetaMetric> templateMetaMetrics = result.getData();

        double overSoldRate = capacityPlanConfig.getOverSoldRate();
        for (TemplateMetaMetric templateMetaMetric : templateMetaMetrics) {
            computeCombinedHotCost(templateMetaMetric, overSoldRate);
            LOGGER.info("class=RegionResourceManager||method=getTemplateMetrics||template={}||templateMetaMetric={}",
                templateMetaMetric.getTemplateName(), templateMetaMetric);
        }

        return templateMetaMetrics;
    }

    /**
     * 获取region中模板指定间隔的统计指标
     * @param region region
     * @param interval interval
     * @return list
     */
    public List<TemplateMetaMetric> getRegionTemplateMetrics(CapacityPlanRegion region, long interval,
                                                             CapacityPlanConfig capacityPlanConfig) {
        List<Long> templatePhysicalIds = templatePhyService.getNormalTemplateByCluster(region.getClusterName())
            .stream().filter(indexTemplatePhysical -> belong(indexTemplatePhysical.getRack(), region.getRacks()))
            .map( IndexTemplatePhy::getId).collect(Collectors.toList());
        return getTemplateMetrics(templatePhysicalIds, interval, capacityPlanConfig);
    }

    /***************************************** private method ****************************************************/

    /**
     * 计算模板实时的cpu消耗
     * @param templateMetaMetrics 模板指标列表
     * @return cpuCount
     */
    private Double computeCurrentCostCpuCount(List<TemplateMetaMetric> templateMetaMetrics) {
        Double regionActualCpuCount = 0.0;
        for (TemplateMetaMetric templateMetaMetric : templateMetaMetrics) {
            regionActualCpuCount += templateMetaMetric.getActualCpuCount();
            LOGGER.info("class=RegionResourceManager||method=computeCurrentCostCpuCount||template={}||templateMetaMetric={}",
                templateMetaMetric.getTemplateName(), templateMetaMetric);
        }
        return regionActualCpuCount;
    }

    private void computeCombinedHotCost(TemplateMetaMetric templateMetaMetric, double overSoldRate) {
        computeCombinedCost(templateMetaMetric, overSoldRate);
        templateMetaMetric.setCombinedDiskG(scaleByHotDay(templateMetaMetric.getCombinedDiskG(),
            templateMetaMetric.getHotTime(), templateMetaMetric.getExpireTime()));
    }

    private Double scaleByHotDay(Double src, Integer hotDay, Integer expireTime) {
        if (hotDay > 0 && hotDay < expireTime) {
            return src * hotDay / expireTime;
        } else {
            return src;
        }
    }

    private void computeCombinedCost(TemplateMetaMetric templateMetaMetric, double overSoldRate) {
        templateMetaMetric.setCombinedDiskG(
            combine(templateMetaMetric.getActualDiskG(), templateMetaMetric.getQuotaDiskG(), overSoldRate));
        templateMetaMetric.setCombinedCpuCount(
            combine(templateMetaMetric.getActualCpuCount(), templateMetaMetric.getQuotaCpuCount(), overSoldRate));

    }

    private Double combine(Double actual, Double quota, double overSoldRate) {
        return overSoldRate * actual + (1 - overSoldRate) * quota;
    }

    /**
     * 集群资源gap
     * @param has 当前资源保有量
     * @param cost 资源消耗量
     * @param regionConfig 配置
     * @return gap
     */
    private Double getGap(Double has, Double cost, CapacityPlanConfig regionConfig) {
        Double max  = has * regionConfig.getRegionWatermarkHigh();
        Double min  = has * regionConfig.getRegionWatermarkLow();
        Double temp = cost < min ? cost - min : 0.0d;

        return (cost > max) ? (cost - max) : temp;
    }

    private Result<Boolean> checkGlobal(CapacityPlanRegionContext regionPlanContext, boolean checkRegionFreeDisk) {

        // 实际的模板资源消耗
        Double regionCostDiskG = 0.0;
        Double regionCostCpuCount = 0.0;
        Double regionFreeDiskG = regionPlanContext.getRegionMetric().getDiskFreeG();

        Double regionTotalDiskG = regionPlanContext.getRegionMetric().getResource().getDisk();
        Double regionTotalCpuCount = regionPlanContext.getRegionMetric().getResource().getCpu();

        for (TemplateMetaMetric templateMetaMetric : regionPlanContext.getTemplateMetaMetrics()) {
            regionCostDiskG += scaleByHotDay(templateMetaMetric.getActualDiskG(), templateMetaMetric.getHotTime(),
                templateMetaMetric.getExpireTime());
            regionCostCpuCount += templateMetaMetric.getActualCpuCount();
        }

        if (regionCostDiskG >= regionTotalDiskG) {
            LOGGER.warn("class=RegionResourceManager||method=checkGlobal||regionCostDiskG={}||regionDiskTotal={}||msg=disk cost too large",
                regionCostDiskG, regionTotalDiskG);
            return Result.buildParamIllegal("磁盘消耗过大(" + regionCostDiskG + " >= " + regionTotalDiskG + ")");
        }

        if (regionCostCpuCount > regionTotalCpuCount) {
            LOGGER.warn("class=RegionResourceManager||method=checkGlobal||regionCostCpuCount={}||regionCpuTotal={}||msg=cpu cost too large",
                regionCostCpuCount, regionTotalCpuCount);
            return Result.buildParamIllegal("CPU消耗过大(" + regionCostCpuCount + " > " + regionTotalCpuCount + ")");
        }

        if (regionCostDiskG < 0) {
            LOGGER.warn("class=RegionResourceManager||method=checkGlobal||regionCostDiskG={}||msg=disk cost too small", regionCostDiskG);
            return Result.buildParamIllegal("磁盘消耗过小(" + regionCostDiskG + " < 0)");
        }

        if (regionCostCpuCount <= 0) {
            LOGGER.warn("class=RegionResourceManager||method=checkGlobal||regionCostCpuCount={}||msg=cpu cost too small", regionCostCpuCount);
            return Result.buildParamIllegal("CPU消耗过小(" + regionCostCpuCount + " <= 0)");
        }

        if (checkRegionFreeDisk) {
            if (regionFreeDiskG <= 0) {
                LOGGER.warn("class=RegionResourceManager||method=checkGlobal||regionFreeDiskG={}||msg=disk free too small", regionFreeDiskG);
                return Result.buildParamIllegal("磁盘空闲过小(" + regionFreeDiskG + " <= 0)");
            }

            if (regionFreeDiskG >= regionTotalDiskG) {
                LOGGER.warn("class=RegionResourceManager||method=checkGlobal||regionFreeDiskG={}||regionTotalDiskG={}||msg=disk free too large",
                    regionFreeDiskG, regionTotalDiskG);
                return Result.buildParamIllegal("磁盘空闲过大(" + regionFreeDiskG + " >=" + regionTotalDiskG + ")");
            }
        }

        return Result.buildSucc(true);
    }
}

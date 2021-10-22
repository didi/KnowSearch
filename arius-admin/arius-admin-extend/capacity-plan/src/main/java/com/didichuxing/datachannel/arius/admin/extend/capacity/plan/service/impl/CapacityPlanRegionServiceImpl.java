package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.client.bean.common.RackMetaMetric;
import com.didichuxing.datachannel.arius.admin.client.bean.common.RegionMetric;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateMetaMetric;
import com.didichuxing.datachannel.arius.admin.client.constant.quota.NodeSpecifyEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.quota.Resource;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ResourceItemMissEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didichuxing.datachannel.arius.admin.common.exception.AmsRemoteException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.RackUtils;
import com.didichuxing.datachannel.arius.admin.core.component.QuotaTool;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.notify.service.NotifyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common.CapacityPlanConfig;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common.CapacityPlanRegionContext;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanAreaDTO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanRegionDTO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.OpenPhyClusterPlanDTO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.*;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po.CapacityPlanRegionInfoPO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.component.RegionResourceManager;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.component.RegionResourceMover;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanAreaStatusEnum;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.dao.mysql.CapacityPlanRegionInfoDAO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.notify.mail.CapacityPlanTaskNotifyInfo;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanAreaService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionTaskService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.CAPACITY_PLAN_REGION;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.MILLIS_PER_DAY;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_CAPA_PLAN;
import static com.didichuxing.datachannel.arius.admin.common.util.RackUtils.belong;
import static com.didichuxing.datachannel.arius.admin.common.util.RackUtils.hasIntersect;
import static com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskEnum.CHECK;
import static com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskEnum.PLAN;
import static com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskStatusEnum.*;
import static com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskTypeEnum.*;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Service
public class CapacityPlanRegionServiceImpl extends BaseTemplateSrv implements CapacityPlanRegionService, ApplicationListener<ResourceItemMissEvent> {

    private static final ILog sLogger = LogFactory.getLog(CapacityPlanRegionServiceImpl.class);

    @Autowired
    private RegionResourceManager regionResourceManager;

    @Autowired
    private CapacityPlanRegionTaskService capacityPlanRegionTaskService;

    @Autowired
    private ClusterNodeManager clusterNodeManager;

    @Autowired
    private CapacityPlanAreaService capacityPlanAreaService;

    @Autowired
    private RegionResourceMover regionResourceMover;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private ESClusterPhyService esClusterPhyService;

    @Autowired
    private ESRegionRackService regionRackService;

    @Autowired
    private QuotaTool quotaTool;

    @Autowired
    private CapacityPlanRegionInfoDAO capacityPlanRegionInfoDAO;

    @Autowired
    private TemplatePhyManager templatePhyManager;

    private final Map<String, ReentrantLock> clusterLock = new ConcurrentHashMap<>();

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_CAPA_PLAN;
    }

    /**
     * 获取area的region列表
     * @param areaId 集群id
     * @return list
     */
    @Override
    public List<CapacityPlanRegion> listRegionsInArea(Long areaId) {
        CapacityPlanArea area = capacityPlanAreaService.getAreaById(areaId);
        if (area == null) {
            return new ArrayList<>();
        }

        // region元信息
        List<ClusterRegion> regionsInArea = regionRackService.listRegionsByLogicAndPhyCluster(area.getResourceId(), area.getClusterName());
        List<Long> regionIds = regionsInArea.stream().map(ClusterRegion::getId).collect(Collectors.toList());

        // region容量信息
        List<CapacityPlanRegionInfoPO> capacityPlanRegionInfos = capacityPlanRegionInfoDAO.listByRegionIds(regionIds);

        // 转换成map，key-regionId
        Map<Long, ClusterRegion> regionMap = ConvertUtil.list2Map(regionsInArea, ClusterRegion::getId);
        Map<Long, CapacityPlanRegionInfoPO> regionInfoMap = ConvertUtil.list2Map(capacityPlanRegionInfos, CapacityPlanRegionInfoPO::getRegionId);

        // 构建容量规划region对象
        return regionIds.stream()
            .map(regionId -> buildCapacityPlanRegion(area, regionMap.get(regionId), regionInfoMap.get(regionId)))
            .collect(Collectors.toList());

    }

    /**
     * 根据regionId获取容量规划region对象
     * @param regionId regionId
     * @return 容量规划region对象
     */
    @Override
    public CapacityPlanRegion getRegionById(Long regionId) {
        if (regionId == null) {
            return null;
        }

        // region元信息
        ClusterRegion region = regionRackService.getRegionById(regionId);

        // region容量信息
        CapacityPlanRegionInfoPO regionInfoPO = capacityPlanRegionInfoDAO.getByRegionId(regionId);

        // 获取region所属的area
        CapacityPlanArea capacityPlanArea = getAreaOfRegion(regionId);

        // 构建容量规划region对象
        return buildCapacityPlanRegion(capacityPlanArea, region, regionInfoPO);

    }

    /**
     * 获取全部的region
     * @return list
     */
    @Override
    public List<CapacityPlanRegion> listAllRegions() {

        // 获取所有的area
        List<CapacityPlanArea> allAreas = capacityPlanAreaService.listAllPlanAreas();

        // 获取area下的所有region
        List<CapacityPlanRegion> allRegions = new LinkedList<>();
        for (CapacityPlanArea area : allAreas){
            allRegions.addAll(listRegionsInArea(area.getId()));
        }

        return allRegions;
    }

    /**
     * 获取逻辑集群拥有的region
     * @param logicClusterId 逻辑集群ID
     * @return 逻辑集群拥有的region列表
     */
    public List<CapacityPlanRegion> listLogicClusterRegions(Long logicClusterId) {

        // 获取所有逻辑集群关联的area
        List<CapacityPlanArea> allAreas = capacityPlanAreaService.listAreasByLogicCluster(logicClusterId);

        // 获取area
        List<CapacityPlanRegion> regionsOfLogicCluster = new ArrayList<>();
        for (CapacityPlanArea area : allAreas) {
            regionsOfLogicCluster.addAll(listRegionsInArea(area.getId()));
        }
        return regionsOfLogicCluster;
    }


    @Override
    public Result createRegionCapacityInfo(CapacityPlanRegionDTO capacityPlanRegionDTO, String operator) {
        CapacityPlanRegionInfoPO regionInfoPO = new CapacityPlanRegionInfoPO();
        regionInfoPO.setRegionId(capacityPlanRegionDTO.getRegionId());
        regionInfoPO.setConfigJson(capacityPlanRegionDTO.getConfigJson());
        regionInfoPO.setFreeQuota(capacityPlanRegionDTO.getFreeQuota());
        regionInfoPO.setShare(capacityPlanRegionDTO.getShare());
        regionInfoPO.setOverSold(0.0);
        regionInfoPO.setUsage(capacityPlanRegionDTO.getUsage());

        boolean succeed = capacityPlanRegionInfoDAO.insert(regionInfoPO) == 1;
        if (succeed) {
            operateRecordService.save(CAPACITY_PLAN_REGION, ADD, capacityPlanRegionDTO.getRegionId(), "", operator);
        }

        return Result.build(succeed);
    }

    @Override
    public Result deleteRegionCapacityInfo(Long regionId, String operator) {

        CapacityPlanRegionInfoPO regionInfoPO =  capacityPlanRegionInfoDAO.getByRegionId(regionId);
        if (regionInfoPO == null){
            return Result.buildFail(String.format("region %d 的容量信息记录不存在", regionId));
        }

        boolean succeed = capacityPlanRegionInfoDAO.deleteByRegionId(regionId) == 1;
        if (succeed) {
            // 操作记录
            operateRecordService.save(CAPACITY_PLAN_REGION, DELETE, regionId, "", operator);
        }

        return Result.build(succeed);
    }

    /**
     *  修改一个region，只支持racks（旧版）, share, configJson, freeQuota, usage的修改
     * @param regionDTO regionDTO
     * @return result
     */
    @Override
    public Result editRegion(CapacityPlanRegionDTO regionDTO, String operator) {

        // 检查更新参数
        if (AriusObjUtils.isNull(regionDTO)) {
            return Result.buildParamIllegal("参数为空");
        }

        if (AriusObjUtils.isNull(regionDTO.getRegionId())) {
            return Result.buildParamIllegal("regionId为空");
        }

        CapacityPlanRegion oldCapacityPlanRegion = getRegionById(regionDTO.getRegionId());

        if (oldCapacityPlanRegion == null) {
            return Result.buildNotExist("region不存在");
        }

        // 更新rack
        if (regionDTO.getRacks() != null) {
            // 判断rack不能与其他region的rack重复
            List<CapacityPlanRegion> regions = listRegionsInArea(oldCapacityPlanRegion.getAreaId());
            for (CapacityPlanRegion region : regions) {
                // 同一个region，跳过
                if (region.getRegionId().equals(oldCapacityPlanRegion.getRegionId())) {
                    continue;
                }

                // 判断是否有冲突
                if (RackUtils.hasIntersect(regionDTO.getRacks(), region.getRacks())) {
                    return Result.buildParamIllegal("region rack冲突");
                }
            }

            regionRackService.editRegionRacks(regionDTO.getRegionId(), regionDTO.getRacks(), operator);
        }

        // 更新容量信息部分，share、configJson、freeQuota、usage
        if (Objects.nonNull(regionDTO.getShare())
            || Objects.nonNull(regionDTO.getConfigJson())
            || Objects.nonNull(regionDTO.getFreeQuota())
            || Objects.nonNull(regionDTO.getUsage())){
            CapacityPlanRegionInfoPO oldRegion = capacityPlanRegionInfoDAO.getByRegionId(regionDTO.getRegionId());
            CapacityPlanRegionInfoPO param = ConvertUtil.obj2Obj(regionDTO, CapacityPlanRegionInfoPO.class);
            boolean succeed = 1 == capacityPlanRegionInfoDAO.updateByRegionId(param);

            if (succeed) {
                operateRecordService.save(CAPACITY_PLAN_REGION, EDIT, param.getId(), AriusObjUtils.findChanged(oldRegion, param), operator);
            } else {
                return Result.buildFail();
            }
        }

        return Result.buildSucc();
    }

    /**
     * 记录统计数据
     * @param regionId regionId
     * @param usage    usage
     * @param overSold overSold
     * @return true/false
     */
    @Override
    public boolean modifyRegionMetrics(Long regionId, double usage, double overSold) {
        CapacityPlanRegionInfoPO param = new CapacityPlanRegionInfoPO();
        param.setId(regionId);
        param.setUsage(usage);
        param.setOverSold(overSold);
        return 1 == capacityPlanRegionInfoDAO.updateByRegionId(param);
    }

    /**
     * 平衡region
     * @param areaId areaId
     */
    @Override
    public Result<List<CapacityPlanRegionBalanceItem>> balanceRegion(Long areaId, boolean exe) {

        // 检查索引服务开启
        Result checkResult = checkTemplateSrvOpen(areaId);
        if (checkResult.failed()) {
            return checkResult;
        }

        // 获取area下的region
        List<CapacityPlanRegion> regions = listRegionsInArea(areaId);

        regions = regions.stream()
            .filter(region -> region.getUsage() != null && region.getShare().equals(AdminConstant.YES))
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(regions)) {
            return Result.buildFrom(Result.buildFail("area中没有region或者region的利用率没有统计"));
        }

        // 计算region的利用率的平均值
        Double usageAvg = calRegionAverageUsage(regions);
        sLogger.info("method=balanceRegion||areaId={}||usageAvg={}", areaId, usageAvg);

        // 利用率过大于均值的region
        List<CapacityPlanRegion> tooBigLists = regions.stream()
            .filter(region -> (region.getUsage() > usageAvg * 1.1))
            .collect(Collectors.toList());
        // 利用率过小于均值的region
        List<CapacityPlanRegion> tooSmallLists = regions.stream()
            .filter(region -> (region.getUsage() < usageAvg * 0.9))
            .collect(Collectors.toList());

        sLogger.info("method=balanceRegion||areaId={}||tooBigLists={}||tooSmallLists={}", areaId,
            tooBigLists.stream().map(CapacityPlanRegion::getRegionId).collect(Collectors.toList()),
            tooSmallLists.stream().map(CapacityPlanRegion::getRegionId).collect(Collectors.toList()));

        // 同时存利用率过大、过小的region才需要均衡
        if (CollectionUtils.isEmpty(tooBigLists) || CollectionUtils.isEmpty(tooSmallLists)) {
            return Result.buildFrom(Result.buildSucWithTips("area中region是均匀的"));
        }

        List<CapacityPlanRegionBalanceItem> result = Lists.newArrayList();
        // 利用率从高到低排序
        tooBigLists.sort((o1, o2) -> o2.getUsage().compareTo(o1.getUsage()));
        // 生成均衡任务
        for (CapacityPlanRegion tooBigRegion : tooBigLists) {
            result.addAll(doBalance(tooBigRegion, tooSmallLists, usageAvg));
        }

        if (exe && CollectionUtils.isNotEmpty(result)) {
            for (CapacityPlanRegionBalanceItem item : result) {
                try {
                    // 执行实际的均衡操作，将模板移动到利用率低的模板
                    moveTemplateToTgtRegion(item);
                } catch (Exception e) {
                    sLogger.warn("method=balanceRegion||item={}||errMsg={}", item, e.getMessage(), e);
                }
            }
        }

        return Result.buildSucc(result);
    }

    /**
     * 拆分region
     * <p>
     * 拿到region中所有的模板统计信息
     * 拿到region中各个rack的统计信息
     * @param regionId regionId
     * @return result
     */
    @Override
    public Result<List<CapacityPlanRegionSplitResult>> splitRegion(Long regionId, boolean exe) {

        CapacityPlanRegion region = getRegionById(regionId);
        if (region == null) {
            return Result.buildNotExist("region不存在");
        }

        // 检查索引服务开启
        Result checkResult = checkTemplateSrvOpen(region);
        if (checkResult.failed()) {
            return checkResult;
        }

        CapacityPlanConfig regionConfig = region.getConfig();

        // region上的模板数据
        List<TemplateMetaMetric> templateMetaMetrics = regionResourceManager.getRegionTemplateMetrics(region,
            regionConfig.getPlanRegionResourceDays() * MILLIS_PER_DAY, regionConfig);

        // 计算分裂方案
        Result<List<CapacityPlanRegionSplitResult>> result = doSplit(regionConfig, templateMetaMetrics, fetchRegionRackMetrics(region));

        // 执行分裂
        if (exe && result.success()) {
            splitRegions(region, result.getData());
        }

        return result;
    }

    @Override
    public Result openPhyClusterCapacityPlanFlags(String phyClusterName, String operator) {
        if (!esClusterPhyService.isClusterExists(phyClusterName)) {
            return Result.buildNotExist(String.format("物理集群%s不存在", phyClusterName));
        }

        List<CapacityPlanArea> areas = capacityPlanAreaService.listAreasByPhyCluster(phyClusterName);
        for (CapacityPlanArea area : areas) {
            CapacityPlanAreaDTO areaDTO = new CapacityPlanAreaDTO();
            areaDTO.setId(area.getId());
            areaDTO.setStatus(CapacityPlanAreaStatusEnum.PLANING.getCode());
            capacityPlanAreaService.modifyPlanArea(areaDTO, operator);
        }

        return Result.buildSucc();
    }

    @Override
    public Result closePhyClusterCapacityPlanFlags(String phyClusterName, String operator) {
        if (!esClusterPhyService.isClusterExists(phyClusterName)) {
            return Result.buildNotExist(String.format("物理集群%s不存在", phyClusterName));
        }

        List<CapacityPlanArea> areas = capacityPlanAreaService.listAreasByPhyCluster(phyClusterName);
        for (CapacityPlanArea area : areas) {
            CapacityPlanAreaDTO areaDTO = new CapacityPlanAreaDTO();
            areaDTO.setId(area.getId());
            areaDTO.setStatus(CapacityPlanAreaStatusEnum.SUSPEND.getCode());
            capacityPlanAreaService.modifyPlanArea(areaDTO, operator);
        }

        return Result.buildSucc();
    }

    /**
     * 修改一个region的freeQuota
     * @param regionId  regionId
     * @param freeQuota freeQuota
     */
    @Override
    public void editRegionFreeQuota(Long regionId, Double freeQuota) {
        CapacityPlanRegionDTO param = new CapacityPlanRegionDTO();
        param.setRegionId(regionId);
        param.setFreeQuota(freeQuota);
        editRegion(param, AriusUser.CAPACITY_PLAN.getDesc()).success();
    }

    /**
     * 修改一个region的rack
     * @param regionId regionId
     * @param tgtRacks tgtRacks
     * @return true/false
     */
    @Override
    public boolean modifyRegionRacks(Long regionId, String tgtRacks) {
        CapacityPlanRegionDTO param = new CapacityPlanRegionDTO();
        param.setRegionId(regionId);
        param.setRacks(tgtRacks);
        return editRegion(param, AriusUser.CAPACITY_PLAN.getDesc()).success();
    }

    /**
     * 规划一个region的资源，需要规划未来一天的资源
     * @param regionId regionId
     * @return result
     */
    @Override
    public Result planRegion(Long regionId) throws ESOperateException {
        CapacityPlanRegion region = getRegionById(regionId);
        if (region == null) {
            return Result.buildNotExist("region不存在");
        }

        // 检查索引服务开启
        Result checkResult = checkTemplateSrvOpen(region);
        if (checkResult.failed()) {
            return checkResult;
        }

        CapacityPlanRegionContext regionPlanContext = new CapacityPlanRegionContext(PLAN.getCode(), region);
        Resource resourceGap = regionResourceManager.getIntervalResourceGap(regionPlanContext);

        sLogger.info("method=planRegion||regionId={}||resourceGap={}", regionId, resourceGap);

        //保存模板的factor和group
        if (regionResourceMover.saveTemplateCapacityConfig(regionPlanContext)) {
            sLogger.info("method=planRegion||regionId={}||msg=saveTemplateCapacityConfig succ", regionId);
        } else {
            sLogger.warn("method=planRegion||regionId={}||msg=saveTemplateCapacityConfig fail", regionId);
        }

        // 任务需要调整的rack
        List<String> deltaRack = Lists.newArrayList();
        CapacityPlanRegionTaskStatusEnum statusEnum = FINISHED;
        CapacityPlanRegionTaskTypeEnum typeEnum = NORMAL;

        if (resourceGap.getCpu() > 0.0 || resourceGap.getDisk() > 0.0) {
            // 扩容
            statusEnum = increaseRegion(regionPlanContext,
                new Resource(resourceGap.getCpu(), resourceGap.getMem(), resourceGap.getDisk()), deltaRack);
            typeEnum = INCREASE;
        }

        boolean regionBigEnough = regionPlanContext.getRegionMetric().getRackCount() >
            regionPlanContext.getRegion().getConfig().getCountRackPerRegion();

        // 如果根据当前Region模板未来几天资源需求量需要缩容的话，需要根据当前磁盘使用情况
        // 综合评估下，再决定缩容的比例。
        if (resourceGap.getCpu() < 0.0 && resourceGap.getDisk() < 0.0 && regionBigEnough) {
            Resource currentResourceGap = regionResourceManager.getCurrentResourceGap(regionPlanContext);
            Double needDecreaseCpu = Math.max(resourceGap.getCpu(), currentResourceGap.getCpu());
            Double needDecreaseMem = Math.max(resourceGap.getMem(), currentResourceGap.getMem());
            Double needDecreaseDisk = Math.max(resourceGap.getDisk(), currentResourceGap.getDisk());

            sLogger.info("method=planRegion||regionId={}||templateResourceGap={}||currentResourceGap={}" +
                    "||needDecreaseCpu={}||needDecreaseMem={}||needDecreaseDisk={}",
                regionId, resourceGap, currentResourceGap,
                needDecreaseCpu, needDecreaseMem, needDecreaseDisk);

            if (needDecreaseCpu < 0.0 && needDecreaseDisk < 0.0) {
                statusEnum = decreaseRegion(regionPlanContext,
                    new Resource(needDecreaseCpu, needDecreaseMem, needDecreaseDisk),
                    deltaRack);
                typeEnum = DECREASE;
            }
        }

        // 跟新region的free量
        Double freeQuota = computeRegionFreeQuota(regionPlanContext);
        sLogger.info("method=planRegion||regionId={}||freeQuota={}", regionId, freeQuota);
        editRegionFreeQuota(regionId, freeQuota);

        // 保存任务信息
        if (!capacityPlanRegionTaskService.saveTask(typeEnum, regionPlanContext, deltaRack, statusEnum)) {
            sLogger.warn("method=planRegion||regionId={}||msg=save to db fail", regionId);
        }

        if (statusEnum == FINISHED || statusEnum == DATA_MOVING) {
            return Result.buildSucc();
        } else {
            return Result.buildFail(statusEnum.getDesc());
        }
    }

    /**
     * 检查一个region的资源，需要检查当前region的资源是否足够
     * @param regionId regionId
     * @return result
     */
    @Override
    public Result checkRegion(Long regionId) {

        CapacityPlanRegion region = getRegionById(regionId);
        if (region == null) {
            return Result.buildNotExist("region不存在");
        }

        // 检查索引服务开启
        Result checkResult = checkTemplateSrvOpen(region);
        if (checkResult.failed()) {
            return checkResult;
        }

        Result checkParamResult = validateRegionRacks(region);
        if (checkParamResult.failed()) {
            return checkParamResult;
        }

        CapacityPlanRegionContext regionPlanContext = new CapacityPlanRegionContext(CHECK.getCode(), region);

        Resource resourceGap = regionResourceManager.getCurrentResourceGap(regionPlanContext);

        //保存模板的factor和group
        regionResourceMover.raiseTemplateFactor(regionPlanContext);

        // 任务需要调整的rack
        List<String> deltaRack = Lists.newArrayList();
        CapacityPlanRegionTaskStatusEnum statusEnum = FINISHED;
        CapacityPlanRegionTaskTypeEnum typeEnum = NORMAL;

        if (resourceGap.getCpu() > 0.0 || resourceGap.getDisk() > 0.0) {
            // 扩容
            statusEnum = increaseRegion(regionPlanContext, resourceGap, deltaRack);
            sLogger.info("method=checkRegion||regionId={}||freeQuota={}||msg=region increase", regionId, 0.0);
            // 计算具体的free
            editRegionFreeQuota(regionId, 0.0);
            typeEnum = INCREASE;
        }

        // 保存任务信息
        if (!capacityPlanRegionTaskService.saveTask(typeEnum, regionPlanContext, deltaRack, statusEnum)) {
            sLogger.warn("method=checkRegion||regionId={}||msg=save to db fail", regionId);
        }

        if (statusEnum == FINISHED) {
            return Result.buildSucc();
        } else {
            return Result.buildFail(statusEnum.getDesc());
        }
    }

    /**
     * 获取region所属的area（如果存在的话）
     * @param regionId regionId
     * @return region所属的area
     */
    private CapacityPlanArea getAreaOfRegion(Long regionId) {
        if (regionId == null) {
            return null;
        }

        ClusterRegion region = regionRackService.getRegionById(regionId);
        // region没有被绑定
        if (region == null || regionRackService.isRegionBound(region)) {
            return null;
        }

        // 根据逻辑集群ID、物理集群名获取area
        return capacityPlanAreaService.getAreaByResourceIdAndCluster(region.getLogicClusterId(), region.getPhyClusterName());
    }

    /**
     * 构建容量规划region对象.
     * @param capacityPlanArea         region所属的area
     * @param clusterRegion            region元信息
     * @param capacityPlanRegionInfoPO region容量信息
     * @return 容量规划region对象，CapacityPlanRegion
     */
    private CapacityPlanRegion buildCapacityPlanRegion(CapacityPlanArea capacityPlanArea,
                                                       ClusterRegion clusterRegion,
                                                       CapacityPlanRegionInfoPO capacityPlanRegionInfoPO) {
        CapacityPlanRegion capacityPlanRegion = new CapacityPlanRegion();

        // area
        if (capacityPlanArea != null) {
            capacityPlanRegion.setAreaId(capacityPlanArea.getId());
            capacityPlanRegion.setLogicClusterId(capacityPlanArea.getResourceId());
        }

        // region
        if (clusterRegion != null) {
            capacityPlanRegion.setRegionId(clusterRegion.getId());
            capacityPlanRegion.setClusterName(clusterRegion.getPhyClusterName());
            capacityPlanRegion.setRacks(clusterRegion.getRacks());
        }

        if (capacityPlanRegionInfoPO != null) {
            // 配置 - json（注意配置json表示region的单独配置，不需要合并area的配置）
            capacityPlanRegion.setConfigJson(capacityPlanRegionInfoPO.getConfigJson());
            // 配置 - 合并area的配置，region的配置优先于area的配置
            capacityPlanRegion.setConfig(
                mergeCapacityPlanConfig(capacityPlanRegion.getConfigJson(), capacityPlanArea == null ? "" : capacityPlanArea.getConfigJson()));
            // 容量信息
            capacityPlanRegion.setFreeQuota(capacityPlanRegionInfoPO.getFreeQuota());
            capacityPlanRegion.setShare(capacityPlanRegionInfoPO.getShare());
            capacityPlanRegion.setUsage(capacityPlanRegionInfoPO.getUsage());
            capacityPlanRegion.setOverSold(capacityPlanRegionInfoPO.getOverSold());
        }

        return capacityPlanRegion;
    }

    /**
     * 合并容量规划配置，region的配置优先于area的配置
     * @param regionConfigJson region的容量规划配置
     * @param areaConfigJson   area的容量规划配置
     * @return
     */
    private CapacityPlanConfig mergeCapacityPlanConfig(String regionConfigJson, String areaConfigJson) {
        Map<String, Object> regionConfig = Maps.newHashMap();
        if (StringUtils.isNotBlank(regionConfigJson)) {
            regionConfig = JSON.parseObject(regionConfigJson);
        }

        Map<String, Object> areaConfig = Maps.newHashMap();
        if (StringUtils.isNotBlank(areaConfigJson)) {
            areaConfig = JSON.parseObject(areaConfigJson);
        }

        // 合并配置，area的配置作为默认值
        XContentHelper.mergeDefaults(regionConfig, areaConfig);

        return ConvertUtil.obj2ObjByJSON(regionConfig, CapacityPlanConfig.class);
    }

    /**
     * 校验Region Racks
     * @param region Region
     * @return
     */
    private Result validateRegionRacks(CapacityPlanRegion region) {
        List<CapacityPlanRegion> regions = listRegionsInArea(region.getAreaId());

        Collection<String> racks = RackUtils.racks2List(region.getRacks());

        for (CapacityPlanRegion other : regions) {
            if (other.getRegionId().equals(region.getRegionId())) {
                continue;
            }

            if (hasIntersect(other.getRacks(), racks)) {
                return Result.buildFail("regionId_" + region.getRegionId() + " 与 regionId_" + other.getRegionId() + " 存在冲突rack");
            }
        }

        // TODO ZHZ 应该检查每个region的rack与模板的rack是否一致，不一致的就改为一致

        return Result.buildSucc();
    }

    /**
     * 搬迁一个region内所有的索引到当前region的rack
     * @param regionId regionId
     * @return result
     */
    @Override
    public Result moveShard(Long regionId, boolean shouldUpdateIndex) {
        CapacityPlanRegion region = getRegionById(regionId);
        if (region == null) {
            return Result.buildNotExist("region不存在");
        }

        // 检查索引服务开启
        Result checkResult = checkTemplateSrvOpen(region);
        if (checkResult.failed()) {
            return checkResult;
        }

        return Result.build(regionResourceMover.moveShard(region, shouldUpdateIndex));
    }

    /**
     * 获取region资源信息
     * @param rackMetaMetrics rackMetrics
     * @return result
     */
    @Override
    public RegionMetric calcRegionMetric(List<RackMetaMetric> rackMetaMetrics) {
        List<String> rackNames = Lists.newArrayList();
        Integer nodeCount = 0;
        Double cpuCount = 0.0;
        Double diskTotalG = 0.0;
        Double diskFreeG = 0.0;

        for (RackMetaMetric rackMetaMetric : rackMetaMetrics) {
            rackNames.add(rackMetaMetric.getName());
            nodeCount += rackMetaMetric.getNodeCount();
            cpuCount += rackMetaMetric.getCpuCount();
            diskTotalG += rackMetaMetric.getTotalDiskG();
            if (rackMetaMetric.getDiskFreeG() != null) {
                diskFreeG += rackMetaMetric.getDiskFreeG();
            }
        }
        RegionMetric regionMetric = new RegionMetric();
        regionMetric.setCluster(rackMetaMetrics.get(0).getCluster());
        regionMetric.setRacks(RackUtils.list2Racks(rackNames));
        regionMetric.setNodeCount(nodeCount);
        regionMetric.setResource(new Resource(cpuCount, 0d, diskTotalG));
        regionMetric.setDiskFreeG(diskFreeG);

        return regionMetric;
    }

    /**
     * 根据用户逻辑资源id获取region
     * @param logicClusterId 用户的逻辑资源id
     * @return list
     */
    @Override
    public List<CapacityPlanRegion> listLogicClusterSharedRegions(Long logicClusterId) {

        List<CapacityPlanRegion> logicClusterRegions = listLogicClusterRegions(logicClusterId);

        // 筛选出share状态为1的
        return logicClusterRegions.stream()
            .filter(region -> region.getShare().equals(AdminConstant.YES))
            .collect(Collectors.toList());
    }

    /**
     * 获取物理模板的region
     * @param templatePhysical 模板
     * @return result
     */
    @Override
    public CapacityPlanRegion getRegionOfPhyTemplate(IndexTemplatePhy templatePhysical) {

        if (templatePhysical == null) {
            return null;
        }

        List<CapacityPlanRegion> regions = listAllRegions();
        if (CollectionUtils.isEmpty(regions)) {
            return null;
        }

        for (CapacityPlanRegion region : regions) {
            if (!region.getClusterName().equals(templatePhysical.getCluster())) {
                continue;
            }

            if (belong(templatePhysical.getRack(), region.getRacks())) {
                return region;
            }
        }

        return null;
    }

    /**
     * Handle an application event.
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ResourceItemMissEvent event) {
        sLogger.info("method=onApplicationEvent||meg=process cluster item miss");

        List<ESClusterLogicRackInfo> items = event.getItems();
        Multimap<Long, ESClusterLogicRackInfo> resourceId2ResourceLogicItemMultiMap = ConvertUtil.list2MulMap(items,
            ESClusterLogicRackInfo::getLogicClusterId);


        for (Long resourceId : resourceId2ResourceLogicItemMultiMap.keySet()) {
            Collection<ESClusterLogicRackInfo> resourceItems = resourceId2ResourceLogicItemMultiMap.get(resourceId);

            Multimap<String, ESClusterLogicRackInfo> cluster2ResourceLogicItemMultiMap = ConvertUtil
                .list2MulMap(Lists.newArrayList(resourceItems), ESClusterLogicRackInfo::getPhyClusterName);

            for (String cluster : cluster2ResourceLogicItemMultiMap.keySet()) {
                Collection<ESClusterLogicRackInfo> resourceClusterItems = cluster2ResourceLogicItemMultiMap.get(cluster);
                Set<String> missRackSet = resourceClusterItems.stream().map(ESClusterLogicRackInfo::getRack)
                    .collect(Collectors.toSet());


                sLogger.info("method=onApplicationEvent||resourceId={}||cluster={}||missRack={}", resourceId, cluster,
                    missRackSet);

                List<CapacityPlanRegion> regions = getRegionsByLogicAndPhyCluster(resourceId, cluster);

                if (CollectionUtils.isEmpty(regions)) {
                    sLogger.info("method=onApplicationEvent||resourceId={}||cluster={}||msg=no region planed",
                        resourceId, cluster);
                    continue;
                }

                for (CapacityPlanRegion region : regions) {
                    if (hasIntersect(region.getRacks(), missRackSet)) {
                        String tgtRacks = RackUtils.list2Racks(RackUtils.racks2List(region.getRacks()).stream()
                            .filter(rack -> !missRackSet.contains(rack)).collect(Collectors.toList()));
                        if (modifyRegionRacks(region.getRegionId(), tgtRacks)) {
                            sLogger.info(
                                "method=onApplicationEvent||msg=region rack edit succ||regionId={}||srcRack={}||tgtRack={}",
                                region.getRegionId(), region.getRacks(), tgtRacks);
                        } else {
                            sLogger.warn(
                                "method=onApplicationEvent||msg=region rack edit fail||regionId={}||srcRack={}||tgtRack={}",
                                region.getRegionId(), region.getRacks(), tgtRacks);
                        }
                    }
                }

            }

        }
    }

    /**
     * 获取Region rack Metrics信息
     * @param region Region信息
     * @return
     * @throws AmsRemoteException
     */
    @Override
    public List<RackMetaMetric> fetchRegionRackMetrics(CapacityPlanRegion region) throws AmsRemoteException {
        List<RackMetaMetric> result = new ArrayList<>();
        if (region != null) {
            Result<List<RackMetaMetric>> rackMetaMetricsResult = clusterNodeManager.meta(
                    region.getClusterName(),
                    Sets.newHashSet(region.getRacks().split(",")));
            if (rackMetaMetricsResult.failed()) {
                throw new AmsRemoteException("获取资源gap失败：" + rackMetaMetricsResult.getMessage());
            }
            result = rackMetaMetricsResult.getData();
        }

        return result;
    }

    /**************************************************** private method ****************************************************/
    /**
     * 获取逻辑集群具体物理集群的Region列表
     * @param logicClusterId 规划集群ID
     * @param phyClusterName       物理集群名称
     * @return
     */
    private List<CapacityPlanRegion> getRegionsByLogicAndPhyCluster(Long logicClusterId, String phyClusterName) {
        CapacityPlanArea area = capacityPlanAreaService.getAreaByResourceIdAndCluster(logicClusterId, phyClusterName);
        if (area == null) {
            return Lists.newArrayList();
        }

        return listRegionsInArea(area.getId());
    }

    /**
     * 扩容指定region
     * @param regionPlanContext regionPlanContext
     * @param resourceGap       节点个数
     * @param deltaRack         racks
     */
    private CapacityPlanRegionTaskStatusEnum increaseRegion(
        CapacityPlanRegionContext regionPlanContext,
        Resource resourceGap, List<String> deltaRack) {

        CapacityPlanRegion region = regionPlanContext.getRegion();

        try {
            if (!tryLock(region.getClusterName())) {
                sLogger.info("method=regionIncrease||regionId={}||msg=lock fail", region.getRegionId());
                return OP_ES_ERROR;
            }

            List<String> increaseRacks = Lists.newArrayList();
            CapacityPlanConfig capacityPlanConfig = region.getConfig();

            //从上次的任务中获取
            CapacityPlanRegionTask decreasingTask = capacityPlanRegionTaskService
                .getDecreasingTaskByRegionId(region.getRegionId());
            if (decreasingTask != null) {
                increaseRacks
                    .addAll(getRacksDecreasingTask(region, decreasingTask, resourceGap, capacityPlanConfig));
                if (capacityPlanRegionTaskService.finishTask(decreasingTask.getId())) {
                    sLogger.info(
                        "method=regionIncrease||regionId={}||msg=finish decreasing task succ||decreasingTaskId={}",
                        region.getRegionId(), decreasingTask.getId());
                } else {
                    throw new AdminTaskException("结束缩容任务失败：taskId: " + decreasingTask.getId());
                }
            }

            boolean resourceEnough = true;
            if (resourceGap.getCpu() > 0.0 || resourceGap.getDisk() > 0.0) {
                // 获取集群空闲的rack
                List<RackMetaMetric> freeRacks = getPlanClusterFreeRacksWithGap(region.getLogicClusterId(), resourceGap,
                    capacityPlanConfig);

                if (CollectionUtils.isEmpty(freeRacks)) {
                    // 获取不到，空闲资源不足
                    resourceEnough = false;
                    sLogger.warn("method=regionIncrease||regionId={}||cluster={}||msg=get cluster free rack fail",
                        region.getRegionId(), region.getClusterName());
                } else {
                    List<String> rackNames = freeRacks.stream().map(RackMetaMetric::getName)
                        .collect(Collectors.toList());
                    sLogger.info(
                        "method=regionIncrease||regionId={}||msg=get cluster free rack succ||cluster={}||rackNames={}",
                        region.getRegionId(), region.getClusterName(), rackNames);
                    increaseRacks.addAll(rackNames);
                }
            }

            CapacityPlanRegionTaskStatusEnum statusEnum = resourceEnough ? FINISHED : NO_FREE_RACK;

            if (resourceEnough) {
                if (regionResourceMover.increase(regionPlanContext, increaseRacks)) {
                    sLogger.info("method=regionIncrease||regionId={}||msg=move2ColdNode shard succ when increase",
                        region.getRegionId());
                } else {
                    statusEnum = OP_ES_ERROR;
                    sLogger.warn("method=regionIncrease||regionId={}||msg=move2ColdNode shard fail when increase",
                        region.getRegionId());
                }
            }

            // 扩容任务可以直接将rack修改掉
            String tgtRack = RackUtils.append(region.getRacks(), increaseRacks);
            sLogger.info("method=regionIncrease||regionId={}||tgtRack={}", region.getRegionId(), tgtRack);
            modifyRegionRacks(region.getRegionId(), tgtRack);

            notifyService.send(
                NotifyTaskTypeEnum.CAPACITY_PLAN_TASK,
                new CapacityPlanTaskNotifyInfo(regionPlanContext, INCREASE, RackUtils.list2Racks(increaseRacks), statusEnum),
                Arrays.asList()
            );

            deltaRack.addAll(increaseRacks);

            return statusEnum;
        } finally {
            unlock(region.getClusterName());
        }
    }

    /**
     * 缩容
     * @param regionPlanContext region上下文
     * @param resourceGap       资源gap
     * @param deltaRack         racks
     */
    private CapacityPlanRegionTaskStatusEnum decreaseRegion(
        CapacityPlanRegionContext regionPlanContext,
        Resource resourceGap, List<String> deltaRack) {

        CapacityPlanRegion region = regionPlanContext.getRegion();

        try {
            if (!tryLock(region.getClusterName())) {
                sLogger.info("method=regionDecrease||regionId={}||msg=lock fail", region.getRegionId());
                return OP_ES_ERROR;
            }

            List<String> decreaseRacks = Lists.newArrayList();

            Map<String, RackMetaMetric> rack2RackMetricMap = ConvertUtil.list2Map(regionPlanContext.getRackMetas(),
                RackMetaMetric::getName);

            LinkedList<String> racks = Lists.newLinkedList(RackUtils.racks2List(region.getRacks()));

            int regionSizeMin = Math.max(regionPlanContext.getRegion().getConfig().getCountRackPerRegion(),
                (int) (racks.size() * 0.8));

            while (resourceGap.getCpu() < 0.0 && resourceGap.getDisk() < 0.0 && racks.size() > regionSizeMin) {
                String lastRack = racks.pollLast();
                decreaseRacks.add(lastRack);

                RackMetaMetric rackMetaMetric = rack2RackMetricMap.get(lastRack);
                resourceGap.setCpu(resourceGap.getCpu() + rackMetaMetric.getCpuCount());
                resourceGap.setDisk(resourceGap.getDisk() + rackMetaMetric.getTotalDiskG());
            }

            sLogger.info("method=regionDecrease||regionId={}||decreaseRacks={}", region.getRegionId(), decreaseRacks);

            // 缩容任务，需要数据搬迁完成后修改region的状态
            CapacityPlanRegionTaskStatusEnum statusEnum = DATA_MOVING;

            if (regionResourceMover.decrease(regionPlanContext, decreaseRacks)) {
                sLogger.info("method=regionDecrease||regionId={}||msg=move2ColdNode shard succ when decrease", region.getRegionId());
            } else {
                statusEnum = OP_ES_ERROR;
                sLogger.warn("method=regionDecrease||regionId={}||msg=move2ColdNode shard fail when decrease", region.getRegionId());
            }

            notifyService.send(
                NotifyTaskTypeEnum.CAPACITY_PLAN_TASK,
                new CapacityPlanTaskNotifyInfo(regionPlanContext, DECREASE, RackUtils.list2Racks(decreaseRacks), statusEnum),
                Arrays.asList()
            );
            deltaRack.addAll(decreaseRacks);

            return statusEnum;
        } finally {
            unlock(region.getClusterName());
        }
    }

    /**
     * 获取缩容任务Racks
     * @param region             Region
     * @param decreasingTask     缩容任务
     * @param resourceGap        资源需求
     * @param capacityPlanConfig Region配置
     * @return
     */
    private Set<String> getRacksDecreasingTask(
        CapacityPlanRegion region, CapacityPlanRegionTask decreasingTask,
        Resource resourceGap, CapacityPlanConfig capacityPlanConfig) {

        Set<String> deltaRack = RackUtils.racks2Set(decreasingTask.getDeltaRacks());
        Result<List<RackMetaMetric>> rackMetaMetricsResult = clusterNodeManager.metaAndMetric(
            region.getClusterName(), deltaRack);

        sLogger.info("method=regionIncrease||regionId={}||dataMovingRack={}", region.getRegionId(), deltaRack);

        if (rackMetaMetricsResult.failed()) {
            throw new AmsRemoteException("恢复上次任务失败：" + rackMetaMetricsResult.getMessage());
        }

        List<RackMetaMetric> rackMetaMetrics = rackMetaMetricsResult.getData();
        RegionMetric increaseRackMetric = calcRegionMetric(rackMetaMetrics);

        Double cpuCountProvide = increaseRackMetric.getResource().getCpu() * capacityPlanConfig.getRegionWatermarkHigh();
        Double diskProvide = increaseRackMetric.getDiskFreeG() -
            (increaseRackMetric.getResource().getDisk() * capacityPlanConfig.getRegionWatermarkHigh());

        resourceGap.setCpu(resourceGap.getCpu() - cpuCountProvide);
        resourceGap.setDisk(resourceGap.getDisk() - diskProvide);

        sLogger.info("method=acquireFromDecreasingTask||regionId={}||cpuCountProvide={}||diskProvide={}||resourceGap={}",
            region.getRegionId(), cpuCountProvide, diskProvide, resourceGap);

        return deltaRack;

    }

    /**
     * 获取规划集群空闲Racks
     * @param planClusterId      规划集群ID
     * @param resourceGap        资源需求
     * @param capacityPlanConfig 规划配置
     * @return
     */
    private List<RackMetaMetric> getPlanClusterFreeRacksWithGap(
        Long planClusterId, Resource resourceGap,
        CapacityPlanConfig capacityPlanConfig) {
        CapacityPlanArea capacityPlanCluster = capacityPlanAreaService.getAreaById(planClusterId);

        List<String> freeRacks = capacityPlanAreaService.listAreaFreeRacks(planClusterId);
        if (CollectionUtils.isEmpty(freeRacks)) {
            return Lists.newArrayList();
        }

        sLogger.info("method=getAreaFreeRacksWithGap||cluster={}||freeRacks={}", capacityPlanCluster.getClusterName(),
            freeRacks);

        Result<List<RackMetaMetric>> rackMetaMetricsResult = clusterNodeManager.meta(capacityPlanCluster.getClusterName(),
            freeRacks);
        if (rackMetaMetricsResult.failed()) {
            throw new AmsRemoteException("获取集群空闲资源失败：" + rackMetaMetricsResult.getMessage());
        }
        List<RackMetaMetric> rackMetas = rackMetaMetricsResult.getData();

        List<RackMetaMetric> assignedRacks = Lists.newArrayList();

        // 依次遍历,知道获取到足够的资源
        for (RackMetaMetric rackMeta : rackMetas) {
            if (resourceGap.getCpu() <= 0.0 && resourceGap.getDisk() <= 0.0) {
                break;
            }
            Double cpuCountProvide = rackMeta.getCpuCount() * capacityPlanConfig.getRegionWatermarkHigh();
            Double diskProvide = rackMeta.getTotalDiskG() * capacityPlanConfig.getRegionWatermarkHigh();
            resourceGap.setCpu(resourceGap.getCpu() - cpuCountProvide);
            resourceGap.setDisk(resourceGap.getDisk() - diskProvide);

            assignedRacks.add(rackMeta);
        }

        if (resourceGap.getCpu() > 0.0 || resourceGap.getDisk() > 0.0) {
            return Lists.newArrayList();
        }

        return assignedRacks;
    }

    /**
     * 校验Region
     * @param regionDTO Region详情
     * @return
     */
    private Result validateRegionParams(CapacityPlanRegionDTO regionDTO) {
        if (AriusObjUtils.isNull(regionDTO)) {
            return Result.buildParamIllegal("region为空");
        }

        if (StringUtils.isBlank(regionDTO.getClusterName())) {
            return Result.buildParamIllegal("物理集群名称不能为空");
        }

        if (AriusObjUtils.isNull(regionDTO.getRacks())) {
            return Result.buildParamIllegal("rack为空");
        }

        return Result.buildSucc();
    }

    /**
     * 校验Racks是否合理
     * @param planClusterId 规划集群ID
     * @param racks         Rack列表
     * @return
     */
    private boolean validatePlanClusterRacks(Long planClusterId, String racks) {
        // TODO: change this logic.

        CapacityPlanArea capacityPlanCluster = capacityPlanAreaService.getAreaById(planClusterId);

        if (!esClusterPhyService.isRacksExists(capacityPlanCluster.getClusterName(), racks)) {
            return false;
        }

        if (!isValidRacks(capacityPlanCluster.getResourceId(),
            capacityPlanCluster.getClusterName(), racks)) {
            return false;
        }

        Set<String> rackUsedSet = capacityPlanAreaService.listAreaUsedRacks(planClusterId);

        return !hasIntersect(racks, rackUsedSet);
    }

    /**
     * Racks是否都合理
     * @param logicClusterId 逻辑集群ID
     * @param clusterName    物理集群名称
     * @param racks          Rack列表
     * @return
     */
    private boolean isValidRacks(Long logicClusterId, String clusterName, String racks) {
        Map<String, ESClusterLogicRackInfo> rack2ResourceLogicItemMap = ConvertUtil.list2Map(
            getClusterRacks(logicClusterId, clusterName),
            ESClusterLogicRackInfo::getRack);

        for (String rack : RackUtils.racks2List(racks)) {
            if (!rack2ResourceLogicItemMap.containsKey(rack)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取逻辑集群Rack列表
     * @param logicClusterId 逻辑集群ID
     * @param clusterName    物理集群名称
     * @return
     */
    private List<ESClusterLogicRackInfo> getClusterRacks(Long logicClusterId, String clusterName) {
        List<ESClusterLogicRackInfo> logicClusterRacks = new ArrayList<>();
        if (logicClusterId != null && StringUtils.isNotBlank(clusterName)) {
            logicClusterRacks = regionRackService.listLogicClusterRacks(logicClusterId);

            logicClusterRacks = logicClusterRacks.stream()
                .filter(item -> item.getPhyClusterName().equals(clusterName)).collect(Collectors.toList());
        }

        return logicClusterRacks;
    }

    /**
     * 计算Region空闲Quota
     * @param planRegionContext Region Context
     * @return
     */
    private Double computeRegionFreeQuota(CapacityPlanRegionContext planRegionContext) {
        CapacityPlanConfig config = planRegionContext.getRegion().getConfig();
        Double hasDiskG = planRegionContext.getRegionMetric().getResource().getDisk();
        Double hasCpuCount = planRegionContext.getRegionMetric().getResource().getCpu();

        Double costDiskMaxG = hasDiskG * config.getRegionWatermarkHigh();
        Double costCpuCountMax = hasCpuCount * config.getRegionWatermarkHigh();

        Double costDiskMinG = hasDiskG * config.getRegionWatermarkLow();
        Double costCpuCountMin = hasCpuCount * config.getRegionWatermarkLow();

        Double costDiskG = planRegionContext.getRegionCostDiskG();
        Double costCpuCount = planRegionContext.getRegionCostCpuCount();

        if (costDiskG >= costDiskMaxG || costCpuCount >= costCpuCountMax) {
            return 0.0;
        }

        Resource freeResource = new Resource(0.0, 0.0, 0.0);
        if (costDiskG <= costDiskMinG) {
            freeResource.setDisk(costDiskMaxG - costDiskMinG);
        } else {
            freeResource.setDisk(costDiskMaxG - costDiskG);
        }

        if (costCpuCount <= costCpuCountMin) {
            freeResource.setCpu(costCpuCountMax - costCpuCountMin);
        } else {
            freeResource.setCpu(costCpuCountMax - costCpuCount);
        }

        return quotaTool.getResourceQuotaCountByCpuAndDisk(NodeSpecifyEnum.DOCKER.getCode(), freeResource.getCpu(),
            freeResource.getDisk(), 0.0);
    }

    /**
     * 解锁
     * @param clusterName 集群名称
     */
    private void unlock(String clusterName) {
        ReentrantLock lock = clusterLock.get(clusterName);
        if (lock == null) {
            return;
        }
        lock.unlock();
    }

    /**
     * 锁定
     * @param clusterName 集群名称
     * @return
     */
    private boolean tryLock(String clusterName) {
        ReentrantLock lock = clusterLock.get(clusterName);
        if (lock == null) {
            lock = new ReentrantLock();
            clusterLock.put(clusterName, lock);
        }
        try {
            return lock.tryLock(10L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 计算Region平均使用率
     * @param regions Region列表
     * @return
     */
    private Double calRegionAverageUsage(List<CapacityPlanRegion> regions) {
        if (CollectionUtils.isEmpty(regions)) {
            return 0.0;
        }

        double sum = 0.0;

        for (CapacityPlanRegion region : regions) {
            sum += region.getUsage();
        }

        return sum / regions.size();
    }

    /**
     * 平衡region
     * @param tooBigRegion  利用率过高的region
     * @param tooSmallLists 所有的利用率过低的regions
     * @param usageAvg      area中的region的利用率的平均值
     * @return 均衡任务list
     */
    private List<CapacityPlanRegionBalanceItem> doBalance(CapacityPlanRegion tooBigRegion,
                                                          List<CapacityPlanRegion> tooSmallLists,
                                                          Double usageAvg) {

        // 获取region上分布的模板资源信息
        List<TemplateMetaMetric> templateMetaMetrics = regionResourceManager.getRegionTemplateMetrics(tooBigRegion,
            tooBigRegion.getConfig().getPlanRegionResourceDays() * MILLIS_PER_DAY, tooBigRegion.getConfig());

        // 模板按照quota从高到低排序
        templateMetaMetrics.sort((o1, o2) -> (getTemplateHotQuota(o2).compareTo(getTemplateHotQuota(o1))));

        // 需要均衡出的quota量
        double quotaExport = getRegionQuotas(tooBigRegion) * (tooBigRegion.getUsage() - usageAvg);

        sLogger.info("method=doBalance||areaId={}||regionId={}||quotaExport={}",
            tooBigRegion.getLogicClusterId(), tooBigRegion.getRegionId(), quotaExport);

        List<CapacityPlanRegionBalanceItem> result = Lists.newArrayList();
        for (TemplateMetaMetric template : templateMetaMetrics) {
            double templateHotQuota = getTemplateHotQuota(template);

            if (templateHotQuota > quotaExport) {
                continue;
            }

            if (quotaExport <= 0.02) {
                sLogger.info("method=doBalance||areaId={}||regionId={}||quotaExport={}||msg=export finish",
                    tooBigRegion.getLogicClusterId(), tooBigRegion.getRegionId(), quotaExport);
                break;
            }

            // 要移动到的region
            CapacityPlanRegion tgtRegion = findFreeQuotaMatchedRegion(
                tooSmallLists, templateHotQuota, usageAvg);

            if (tgtRegion != null) {
                quotaExport = quotaExport - templateHotQuota;
                tooBigRegion.setUsage(tooBigRegion.getUsage() - (templateHotQuota / getRegionQuotas(tooBigRegion)));
                tgtRegion.setUsage(tgtRegion.getUsage() + (templateHotQuota / getRegionQuotas(tgtRegion)));
                result.add(buildRegionBalance(tooBigRegion, template, tgtRegion));
                sLogger.info(
                    "method=doBalance||areaId={}||regionId={}||quotaExport={}||template={}||itemHotQuota={}||tgtRegionUsage={}",
                    tooBigRegion.getLogicClusterId(), tooBigRegion.getRegionId(), quotaExport, template.getTemplateName(), templateHotQuota,
                    tgtRegion.getUsage());
            }
        }

        if (quotaExport > 0.02) {
            sLogger.info(
                "method=doBalance||areaId={}||regionId={}||quotaExport={}||msg=export finish by item or small tooBigRegion not enough ",
                tooBigRegion.getLogicClusterId(), tooBigRegion.getRegionId(), quotaExport);
        }

        return result;
    }

    /**
     * 获取Region  Quota
     * @param region Region
     * @return
     */
    private double getRegionQuotas(CapacityPlanRegion region) {
        return RackUtils.racks2List(region.getRacks()).size() * 2;
    }

    /**
     * 创建Region均衡项
     * @param region    Region
     * @param template  模板Metrics
     * @param tgtRegion 目标Region
     * @return
     */
    private CapacityPlanRegionBalanceItem buildRegionBalance(CapacityPlanRegion region,
                                                             TemplateMetaMetric template,
                                                             CapacityPlanRegion tgtRegion) {
        CapacityPlanRegionBalanceItem balanceItem = new CapacityPlanRegionBalanceItem();
        balanceItem.setAreaId(region.getLogicClusterId());
        balanceItem.setTemplateId(template.getPhysicalId());
        balanceItem.setSrcRegion(region);
        balanceItem.setTgtRegion(tgtRegion);
        balanceItem.setTemplate(template);
        return balanceItem;
    }

    /**
     * @param tooSmallLists 相对比较空闲的Region列表
     * @param itemHotQuota  热存Quota
     * @param averageQuota  平均Quota
     * @return
     */
    private CapacityPlanRegion findFreeQuotaMatchedRegion(
        List<CapacityPlanRegion> tooSmallLists, double itemHotQuota,
        Double averageQuota) {
        tooSmallLists.sort((o1, o2) -> o2.getUsage().compareTo(o1.getUsage()));
        for (CapacityPlanRegion region : tooSmallLists) {
            double quotaDelta = RackUtils.racks2List(region.getRacks()).size() * 2 * (averageQuota - region.getUsage());
            if (quotaDelta >= itemHotQuota) {
                return region;
            }
        }

        return null;
    }

    /**
     * 获取模板热存Quota
     * @param template 模板
     * @return
     */
    private Double getTemplateHotQuota(TemplateMetaMetric template) {

        if (template.getHotTime() > 0 && template.getHotTime() < template.getExpireTime()) {
            return template.getQuota() * template.getHotTime() / template.getExpireTime();
        }

        return template.getQuota();
    }

    /**
     * 移动模板到目标Region
     * @param item Region均衡详情
     * @throws ESOperateException
     */
    private void moveTemplateToTgtRegion(CapacityPlanRegionBalanceItem item) throws ESOperateException {

        Result editResult = templatePhyManager.editTemplateRackWithoutCheck(item.getTemplateId(),
            item.getTgtRegion().getRacks(), AriusUser.CAPACITY_PLAN.getDesc(), 3);

        sLogger.info("method=moveTemplateToTgtRegion||template={}||srcRack={}||targetRack={}||editResult={}",
            item.getTemplateId(), item.getSrcRegion().getRacks(), item.getTgtRegion().getRacks(), editResult);

        if (editResult.success()) {
            CapacityPlanRegion srcRegion = getRegionById(item.getSrcRegion().getRegionId());
            CapacityPlanRegion tgtRegion = getRegionById(item.getTgtRegion().getRegionId());

            double itemHotQuota = getTemplateHotQuota(item.getTemplate());

            srcRegion.setUsage(srcRegion.getUsage() - (itemHotQuota / getRegionQuotas(srcRegion)));
            tgtRegion.setUsage(tgtRegion.getUsage() + (itemHotQuota / getRegionQuotas(tgtRegion)));

            editRegionUsage(srcRegion.getRegionId(), srcRegion.getUsage());
            editRegionUsage(tgtRegion.getRegionId(), tgtRegion.getUsage());

            Result upgradeResult = templatePhyManager.upgradeTemplateVersion(item.getTemplateId(),
                AriusUser.CAPACITY_PLAN.getDesc(), 3);

            sLogger.info("method=moveTemplateToTgtRegion||template={}||upgradeResult={}", item.getTemplateId(),
                upgradeResult);
        }
    }

    /**
     * 更新Region资源利用率
     * @param regionId regionId
     * @param usage    资源利用率
     * @return
     */
    private boolean editRegionUsage(Long regionId, Double usage) {
        CapacityPlanRegionDTO editParam = new CapacityPlanRegionDTO();
        editParam.setRegionId(regionId);
        editParam.setUsage(usage);
        return editRegion(editParam, AriusUser.CAPACITY_PLAN.getDesc()).success();
    }

    /**
     * 获取热存占有的磁盘空间
     * @param metric Metric信息
     * @return
     */
    private Double getHotDiskG(TemplateMetaMetric metric) {
        if (metric.getHotTime() > 0 && metric.getHotTime() < metric.getExpireTime()) {
            return metric.getActualDiskG() * metric.getHotTime() / metric.getExpireTime();
        }

        return metric.getActualDiskG();
    }

    /**
     * 执行裂变操作
     * @param regionConfig        Region配置
     * @param templateMetaMetrics 模板Metrics
     * @param rackMetas           Rack元数据信息
     * @return
     */
    private Result<List<CapacityPlanRegionSplitResult>> doSplit(CapacityPlanConfig regionConfig,
                                                                List<TemplateMetaMetric> templateMetaMetrics,
                                                                List<RackMetaMetric> rackMetas) {

        rackMetas.sort((o1, o2) -> RackUtils.compareByName(o1.getName(), o2.getName()));

        int rackIndex = 0;
        double missNodeCount = 0;

        List<CapacityPlanRegionSplitResult> splitResults = Lists.newArrayList();

        // 保存已经分配的模板列表
        Set<Long> assignedTemplateSet = Sets.newHashSet();
        // 大索引分配region
        for (TemplateMetaMetric template : templateMetaMetrics) {
            double hotDiskG = getHotDiskG(template);
            if (hotDiskG < regionConfig.getBigIndexHotDiskThreshold()) {
                continue;
            }

            if (assignedTemplateSet.contains(template.getPhysicalId())) {
                continue;
            }

            assignedTemplateSet.add(template.getPhysicalId());

            List<RackMetaMetric> matchedRacks = Lists.newArrayList();
            for (; rackIndex < rackMetas.size(); rackIndex++) {
                // 计算满足热存还需要增加的空间
                if (calHotDiskExtraDemand(matchedRacks, hotDiskG, regionConfig) <= 0) {
                    break;
                }
                matchedRacks.add(rackMetas.get(rackIndex));
            }

            // 计算满足热存还需要增加的空间
            double diskGap = calHotDiskExtraDemand(matchedRacks, hotDiskG, regionConfig);

            sLogger.info("method=doSplit||template={}||hotDiskG={}||matchedRacks={}", template.getTemplateName(),
                hotDiskG, matchedRacks);

            // 资源不足
            if (diskGap > 0) {
                missNodeCount += diskGap / NodeSpecifyEnum.DOCKER.getResource().getDisk();
            }

            splitResults.add(buildSplitResult(template, matchedRacks));
        }

        // 将剩余的模板放置在剩余的rack中
        List<RackMetaMetric> remainRacks = Lists.newArrayList(rackMetas.subList(rackIndex, rackMetas.size()));
        double remainTemplateDiskG = 0.0;

        for (TemplateMetaMetric template : templateMetaMetrics) {
            if (assignedTemplateSet.contains(template.getPhysicalId())) {
                continue;
            }
            remainTemplateDiskG += getHotDiskG(template);
            splitResults.add(buildSplitResult(template, remainRacks));
        }

        // 检查剩余的rack资源是否充足
        double diskGap = calHotDiskExtraDemand(remainRacks, remainTemplateDiskG, regionConfig);

        if (diskGap > 0) {
            missNodeCount += diskGap / NodeSpecifyEnum.DOCKER.getResource().getDisk();
        }

        // 资源不足
        if (missNodeCount > 0) {
            return Result.buildSuccWithTips(splitResults, "缺失节点：" + missNodeCount);
        }

        return Result.buildSucc(splitResults);
    }

    /**
     * 创建裂变结果
     * @param template   模板Metrics
     * @param remainRack
     * @return
     */
    private CapacityPlanRegionSplitResult buildSplitResult(TemplateMetaMetric template,
                                                           List<RackMetaMetric> remainRack) {
        CapacityPlanRegionSplitResult splitResult = new CapacityPlanRegionSplitResult();
        splitResult.setPhysicalId(template.getPhysicalId());
        splitResult.setTemplateName(template.getTemplateName());
        splitResult.setQuota(template.getQuota());
        splitResult.setHotDiskQuota(getHotDiskG(template) / NodeSpecifyEnum.DOCKER.getResource().getDisk());
        splitResult.setTgtRack(RackUtils.list2Racks(remainRack.stream().map(RackMetaMetric::getName).collect(Collectors.toList())));
        return splitResult;
    }

    /**
     * 计算满足热存还需要增加的空间
     * @param matchedRack  符合要求的Rack列表
     * @param hotDiskG     热存磁盘空间需求
     * @param regionConfig Region配置
     * @return
     */
    private double calHotDiskExtraDemand(List<RackMetaMetric> matchedRack,
                                         double hotDiskG,
                                         CapacityPlanConfig regionConfig) {
        double rackDiskGTotal = 0.0;
        for (RackMetaMetric rackMetric : matchedRack) {
            rackDiskGTotal += rackMetric.getTotalDiskG();
        }

        return hotDiskG - (rackDiskGTotal * regionConfig.getRegionWatermarkHigh());
    }

    /**
     * 裂变Region列表
     * @param region       Region
     * @param splitResults 裂变结果
     */
    private void splitRegions(CapacityPlanRegion region,
                              List<CapacityPlanRegionSplitResult> splitResults) {

        // key-racks, value-分配的模板
        Multimap<String, CapacityPlanRegionSplitResult> rack2ResultMultiMap = ConvertUtil.list2MulMap(splitResults,
            CapacityPlanRegionSplitResult::getTgtRack);

        for (String regionRack : rack2ResultMultiMap.keySet()) {
            for (CapacityPlanRegionSplitResult regionSplitResult : rack2ResultMultiMap.get(regionRack)) {
                try {
                    // 修改模板的racks
                    Result editResult = templatePhyManager.editTemplateRackWithoutCheck(
                        regionSplitResult.getPhysicalId(), regionSplitResult.getTgtRack(),
                        AriusUser.CAPACITY_PLAN.getDesc(), 3);

                    sLogger.info("method=exeRegionSplitResult||template={}||srcRack={}||targetRack={}||editResult={}",
                        regionSplitResult.getPhysicalId(), region.getRacks(), regionSplitResult.getTgtRack(),
                        editResult);

                    if (editResult.success()) {
                        // 模板升版本
                        Result upgradeResult = templatePhyManager.upgradeTemplateVersion(
                            regionSplitResult.getPhysicalId(), AriusUser.CAPACITY_PLAN.getDesc(), 3);

                        sLogger.info("method=exeRegionSplitResult||template={}||upgradeResult={}",
                            regionSplitResult.getPhysicalId(), upgradeResult);

                        regionSplitResult.setExeResult(true);
                    } else {
                        regionSplitResult.setExeResult(false);
                    }
                } catch (Exception e) {
                    sLogger.error(
                        "class=CapacityPlanRegionServiceImpl||method=exeRegionSplitResult||physicalId={}||errMsg={}",
                        regionSplitResult.getPhysicalId(), e.getMessage(), e);
                }
            }
        }

        // 删除原region
        regionRackService.deletePhyClusterRegionWithoutCheck(region.getRegionId(), AriusUser.CAPACITY_PLAN.getDesc());

        // 重新创建region并绑定
        for (String regionRacks : rack2ResultMultiMap.keySet()) {
            Result<Long> createAndBindRegionResult = regionRackService.createAndBindRegion(region.getClusterName(),
                regionRacks,
                region.getLogicClusterId(),
                rack2ResultMultiMap.get(regionRacks).size() > 1 ? 1 : 0,
                AriusUser.CAPACITY_PLAN.getDesc());

            sLogger.info("method=exeRegionSplitResult||rack={}||addRegionResult={}", regionRacks, createAndBindRegionResult);
        }
    }

    /**
     * 检查索引服务是否开启
     * @param areaId 容量规划areaId
     * @return
     */
    private Result checkTemplateSrvOpen(Long areaId) {
        CapacityPlanArea capacityPlanArea = capacityPlanAreaService.getAreaById(areaId);

        if (capacityPlanArea == null) {
            return Result.buildFail(String.format("容量规划area %s 不存在", areaId));
        }


        if (!isTemplateSrvOpen(capacityPlanArea.getClusterName())) {
            return Result.buildFail(String.format("%s 没有开启 %s", capacityPlanArea.getClusterName(), templateServiceName()));
        }

        return Result.buildSucc();
    }

    /**
     * 检查索引服务是否开启
     * @param capacityPlanRegion 容量规划region
     * @return
     */
    private Result checkTemplateSrvOpen(CapacityPlanRegion capacityPlanRegion) {

        if (capacityPlanRegion == null) {
            return Result.buildFail("region不存在");
        }


        if (!isTemplateSrvOpen(capacityPlanRegion.getClusterName())) {
            return Result.buildFail(String.format("%s 没有开启 %s", capacityPlanRegion.getClusterName(), templateServiceName()));
        }

        return Result.buildSucc();
    }

    /**
     * 校验开启集群容量规划参数
     * @param openPhyClusterPlanDTO 开启容量规划功能参数
     * @return
     */
    private Result validateRegionsFlagsParams(OpenPhyClusterPlanDTO openPhyClusterPlanDTO) {
        if (AriusObjUtils.isNull(openPhyClusterPlanDTO)) {
            return Result.buildParamIllegal("容量规划内容不能为空");
        }

        if (StringUtils.isBlank(openPhyClusterPlanDTO.getCluster())) {
            return Result.buildParamIllegal("容量规划目标集群不能为空");
        }

        ESClusterPhy clusterPhy = esClusterPhyService.getClusterByName(openPhyClusterPlanDTO.getCluster());
        if (clusterPhy == null) {
            return Result.buildParamIllegal("容量规划目标集群不存在");
        }

        return Result.buildSucc();
    }

}

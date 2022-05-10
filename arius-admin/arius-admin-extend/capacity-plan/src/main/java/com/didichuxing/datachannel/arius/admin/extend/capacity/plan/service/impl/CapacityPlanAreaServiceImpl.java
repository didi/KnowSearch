package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.common.RackMetaMetric;
import com.didichuxing.datachannel.arius.admin.common.bean.common.RegionMetric;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateMetaMetric;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.quota.NodeSpecifyEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.quota.Resource;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.RackUtils;
import com.didichuxing.datachannel.arius.admin.core.component.QuotaTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common.CapacityPlanConfig;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanAreaDTO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanArea;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegionTask;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po.CapacityPlanAreaPO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.component.RegionResourceManager;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanAreaStatusEnum;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.dao.mysql.CapacityPlanAreaDAO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.exception.ClusterMetadataException;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.exception.ResourceNotEnoughException;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanAreaService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionTaskService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.CAPACITY_PLAN_AREA;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.MILLIS_PER_DAY;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_CAPA_PLAN;
import static com.didichuxing.datachannel.arius.admin.common.util.RackUtils.belong;
import static com.didichuxing.datachannel.arius.admin.common.util.RackUtils.removeRacks;
import static com.didichuxing.datachannel.arius.admin.core.component.QuotaTool.TEMPLATE_QUOTA_MIN;
import static com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskStatusEnum.DATA_MOVING;
import static com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskStatusEnum.OP_ES_ERROR;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Service
public class CapacityPlanAreaServiceImpl extends BaseTemplateSrv implements CapacityPlanAreaService {

    private static final ILog LOGGER = LogFactory.getLog(CapacityPlanAreaServiceImpl.class);

    private static final Integer REGION_IS_TOO_SMALL = 0;
    private static final Integer NO_TEMPLATE = -1;
    private static final Integer REGION_PLAN_SUCCEED = 1;

    @Autowired
    private CapacityPlanAreaDAO capacityPlanAreaDAO;

    @Autowired
    private CapacityPlanRegionService capacityPlanRegionService;

    @Autowired
    private CapacityPlanRegionTaskService capacityPlanRegionTaskService;

    @Autowired
    private ClusterNodeManager clusterNodeManager;

    @Autowired
    private QuotaTool quotaTool;

    @Autowired
    private RegionRackService regionRackService;

    @Autowired
    private RegionResourceManager regionResourceManager;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    private final Cache<String, CapacityPlanAreaPO> cpcCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(1000).build();

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_CAPA_PLAN;
    }

    /**
     * 获取所有规划的集群信息
     *
     * @return list
     */
    @Override
    public List<CapacityPlanArea> listAllPlanAreas() {

        // 获取所有area
        List<CapacityPlanAreaPO> areaPOs = capacityPlanAreaDAO.listAll();

        List<CapacityPlanArea> capacityPlanAreas = Lists.newArrayList();
        for (CapacityPlanAreaPO areaPO : areaPOs) {
            try {
                CapacityPlanArea capacityPlanArea = ConvertUtil.obj2Obj(areaPO, CapacityPlanArea.class);
                capacityPlanArea.setConfig(JSON.parseObject(capacityPlanArea.getConfigJson(), CapacityPlanConfig.class));
                capacityPlanAreas.add(capacityPlanArea);
            } catch (Exception e) {
                LOGGER.warn("class=CapacityPlanAreaServiceImpl||method=listPlanArea||msg=parseCapacityPlanAreaFailed||config={}",
                    areaPO.getConfigJson(), e);
            }
        }
        return capacityPlanAreas;
    }

    @Override
    public List<CapacityPlanArea> listAreasByLogicCluster(Long logicClusterId) {
        return listAllPlanAreas().stream().filter(area -> area.getResourceId().equals(logicClusterId)).collect(Collectors.toList());
    }

    @Override
    public List<CapacityPlanArea> listAreasByPhyCluster(String phyClusterName) {
        return listAllPlanAreas().stream().filter(area -> area.getClusterName().equals(phyClusterName)).collect(Collectors.toList());
    }

    /**
     * 获取所有规划的集群信息
     *
     * @return list
     */
    @Override
    public List<CapacityPlanArea> listPlaningAreas() {
        List<CapacityPlanArea> areas = listAllPlanAreas();
        areas = areas.stream()
                .filter(capacityPlanArea -> checkTemplateSrvOpen(capacityPlanArea).success())
                .collect(Collectors.toList());
        return areas;
    }

    /**
     * 新增一个需要规划的集群
     *
     * @param capacityPlanAreaDTO 集群参数
     * @return result
     */
    @Override
    public Result<Long> createPlanAreaInNotExist(CapacityPlanAreaDTO capacityPlanAreaDTO, String operator) {
        // 检查参数
        Result<Void> checkResult = checkPlanAreaParams(capacityPlanAreaDTO);
        if (checkResult.failed()) {
            return Result.buildFrom(checkResult);
        }

        // 已经存在则不再创建
        CapacityPlanArea area = getAreaByResourceIdAndCluster(capacityPlanAreaDTO.getResourceId(), capacityPlanAreaDTO.getClusterName());
        if (area != null) {
            return Result.build(true, area.getId());
        }

        // 兼容旧版本：设置容量规划area中的status字段
        capacityPlanAreaDTO.setStatus(isTemplateSrvOpen(capacityPlanAreaDTO.getClusterName())
            ? CapacityPlanAreaStatusEnum.PLANING.getCode() : CapacityPlanAreaStatusEnum.SUSPEND.getCode());

        if (capacityPlanAreaDTO.getConfigJson() == null) {
            capacityPlanAreaDTO.setConfigJson("");
        }

        CapacityPlanAreaPO capacityPlanAreaPO = ConvertUtil.obj2Obj(capacityPlanAreaDTO, CapacityPlanAreaPO.class);
        boolean succeed = capacityPlanAreaDAO.insert(capacityPlanAreaPO) == 1;
        if (succeed) {
            operateRecordService.save(CAPACITY_PLAN_AREA, ADD, capacityPlanAreaPO.getId(), "", operator);
        }

        return Result.build(succeed, capacityPlanAreaPO.getId());
    }

    /**
     * 修改规划的集群 修改状态和配置
     *
     * @param areaDTO 集群参数
     * @return result
     */
    @Override
    public Result<Void> modifyPlanArea(CapacityPlanAreaDTO areaDTO, String operator) {
        if (AriusObjUtils.isNull(areaDTO)) {
            return Result.buildParamIllegal("修改参数为空");
        }

        if (AriusObjUtils.isNull(areaDTO.getId())) {
            return Result.buildParamIllegal("规划areaId为空");
        }

        if (areaDTO.getStatus() != null
            && (CapacityPlanAreaStatusEnum.valueOf(areaDTO.getStatus()) == null) ) {
            return Result.buildParamIllegal("状态非法");
        }

        CapacityPlanAreaPO oldPO = capacityPlanAreaDAO.getById(areaDTO.getId());
        if (oldPO == null) {
            return Result.buildNotExist("规划area不存在");
        }

        CapacityPlanAreaPO param = ConvertUtil.obj2Obj(areaDTO, CapacityPlanAreaPO.class);
        boolean succeed = (1 == capacityPlanAreaDAO.update(param));
        if (succeed) {
            operateRecordService.save(CAPACITY_PLAN_AREA, EDIT, param.getId(), AriusObjUtils.findChangedWithClear(oldPO, param), operator);
        }

        return Result.build(succeed);
    }

    /**
     * 记录area的统计数据
     *
     * @param areaId      areaId
     * @param usageAvg    usageAvg
     * @param overSoldAvg overSoldAvg
     * @return true/false
     */
    @Override
    public boolean recordAreaStatis(Long areaId, double usageAvg, double overSoldAvg) {
        CapacityPlanAreaPO param = new CapacityPlanAreaPO();
        param.setId(areaId);
        param.setUsage(usageAvg);
        param.setOverSold(overSoldAvg);
        return 1 == capacityPlanAreaDAO.update(param);
    }

    /**
     * 删除规划的集群
     *
     * @param areaId 逻辑集群ID
     * @return result
     */
    @Override
    public Result<Void> deletePlanArea(Long areaId, String operator) {
        CapacityPlanAreaPO areaPO = capacityPlanAreaDAO.getById(areaId);
        if (areaPO == null){
            return Result.buildFail(String.format("area %d 不存在", areaId));
        }

        // area中已经有region绑定
        List<ClusterRegion> regions =  regionRackService
				.listRegionsByLogicAndPhyCluster(areaPO.getResourceId(), areaPO.getClusterName());
        if (CollectionUtils.isNotEmpty(regions)) {
            return Result.buildParamIllegal("规划集群存在region，请先删除region");
        }

        boolean succeed = 1 == capacityPlanAreaDAO.delete(areaId);
        if (succeed) {
            operateRecordService.save(CAPACITY_PLAN_AREA, DELETE, areaId, "", operator);
        }
        return Result.build(succeed);
    }

    /**
     * 初始化一个集群的region，将没有绑定成region的racks根据racks上分布的模板绑定成region
     *
     * @param areaId areaId
     * @return 将保存到数据库的region列表返回
     */
    @Override
    public Result<List<CapacityPlanRegion>> initRegionsInPlanArea(Long areaId, String operator) {
        CapacityPlanArea capacityPlanArea = getAreaById(areaId);

        // 检查索引服务开启
        Result<Void> checkResult = checkTemplateSrvOpen(capacityPlanArea);
        if (checkResult.failed()) {
            return Result.buildFail("该集群的容量规划没有开启");
        }

        CapacityPlanConfig capacityPlanConfig = capacityPlanArea.getConfig();
        if (capacityPlanConfig == null) {
            return Result.buildNotExist("集群没有配置规划任务");
        }

        // 获取人工配置的region - 已经有的region
        List<CapacityPlanRegion> hasExistRegions = capacityPlanRegionService.listRegionsInArea(areaId);
        Set<String> hasRegionRackSet = getHasRegionRacks(hasExistRegions);

        // 获取剩余没有划分的rack
        Set<String> noRegionRackSet = getNoRegionRacks(capacityPlanArea.getId(), hasRegionRackSet);
        if (CollectionUtils.isEmpty(noRegionRackSet)) {
            return Result.buildSucc(hasExistRegions);
        }

        // 没有划分的rack上分布的模板
        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService
                .getNormalTemplateByClusterAndRack(capacityPlanArea.getClusterName(), noRegionRackSet);
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return Result.buildNotExist("集群中没有模板");
        }

        // 校验模板是否存在跨region的情况：集群中模板的rack要么全部在hasRegionRackList中，要么全部在noRegionRackSet中
        if (!checkTemplateAllocate(templatePhysicals, hasRegionRackSet, noRegionRackSet)) {
            return Result.buildParamIllegal("存在模板与规划的region不符的情况");
        }

        // 获取rack的元数据与资源指标
        Result<List<RackMetaMetric>> rackMetaMetricsResult = clusterNodeManager.meta(capacityPlanArea.getClusterName(),
                noRegionRackSet);
        if (rackMetaMetricsResult.failed()) {
            return Result.buildFrom(rackMetaMetricsResult);
        }
        List<RackMetaMetric> rackMetas = rackMetaMetricsResult.getData();

        // 获取模板的元数据与资源指标
        List<TemplateMetaMetric> templateMetas = regionResourceManager.getTemplateMetrics(
                getNoRegionTemplateIds(templatePhysicals, noRegionRackSet),
                capacityPlanConfig.getPlanRegionResourceDays() * MILLIS_PER_DAY, capacityPlanConfig);

        // 原始的quota中包含冷数据，应该去除这部分数据
        refreshTemplateQuota(templateMetas);

        // 划分region，key-逗号分隔的rack，value-分布在这些rack（作为一个region）上的模板
        Map<String, List<TemplateMetaMetric>> rack2templateMetasMap = initRegionInner(rackMetas, templateMetas,
                capacityPlanArea);

        LOGGER.info("class=CapacityPlanAreaServiceImpl||method=initRegionsInPlanArea||cluster={}||msg=get pan region result||result={}", capacityPlanArea.getClusterName(),
                JSON.toJSON(rack2templateMetasMap));

        // 将结果落盘
        if (!saveRegion(capacityPlanArea, rack2templateMetasMap)) {
            throw new IllegalArgumentException("region落盘失败");
        }

        operateRecordService.save(CAPACITY_PLAN_AREA, CAPACITY_PAN_INIT_REGION, areaId, "", operator);

        return Result.buildSucc(capacityPlanRegionService.listRegionsInArea(areaId));
    }

    /**
     * 获取area信息
     *
     * @param areaId areaId
     * @return result
     */
    @Override
    public CapacityPlanArea getAreaById(Long areaId) {

        CapacityPlanAreaPO areaPO = getCapacityPlanRegionPOFromCache(areaId);
        if (areaPO == null) {
            return null;
        }

        // 转换成领域对象
        CapacityPlanArea capacityPlanArea = ConvertUtil.obj2Obj(areaPO, CapacityPlanArea.class);

        // 默认配置
        CapacityPlanConfig capacityPlanConfig = new CapacityPlanConfig();

        // area指定的配置
        if (StringUtils.isNotBlank(areaPO.getConfigJson())) {
            BeanUtils.copyProperties(JSON.parseObject(areaPO.getConfigJson(), CapacityPlanConfig.class), capacityPlanConfig);
        }
        capacityPlanArea.setConfig(capacityPlanConfig);

        return capacityPlanArea;
    }

    /**
     * 获取集群的rack列表
     *
     * @param areaId 集群id
     * @return result
     */
    @Override
    public Set<String> listAreaRacks(Long areaId) {

        // 2.0版本：添加到arius_resource_logic_item表的属于area的rack
        // 3.0版本：area的物理集群的所有rack
        CapacityPlanAreaPO capacityPlanAreaPO = capacityPlanAreaDAO.getById(areaId);
        if (capacityPlanAreaPO == null) {
            return Sets.newHashSet();
        }

        return clusterPhyService.listHotRacks(capacityPlanAreaPO.getClusterName());
    }

    /**
     * 获取空闲的rack
     *
     * @param areaId areaId
     * @return list
     */
    @Override
    public List<String> listAreaFreeRacks(Long areaId) {
        CapacityPlanArea capacityPlanCluster = getAreaById(areaId);
        if (capacityPlanCluster == null) {
            return Lists.newArrayList();
        }

        Set<String> usedRacks = listAreaUsedRacks(areaId);
        Set<String> areaRacks = listAreaRacks(areaId);
        areaRacks.removeAll(usedRacks);

        List<String> freeRackList = Lists.newArrayList(areaRacks);
        freeRackList.sort(RackUtils::compareByName);

        return freeRackList;
    }

    /**
     * 获取已经被使用（被组成region的）的rack
     *
     * @param areaId areaId
     * @return set
     */
    @Override
    public Set<String> listAreaUsedRacks(Long areaId) {
        List<CapacityPlanRegion> regions = capacityPlanRegionService.listRegionsInArea(areaId);
        Set<String> racksInRegion = new HashSet<>();
        for (CapacityPlanRegion region : regions) {
            racksInRegion.addAll(RackUtils.racks2Set(region.getRacks()));
        }
        return racksInRegion;
    }

    /**
     * 容量规划
     *
     * @param areaId areaId
     * @return true/false
     */
    @Override
    public Result<Void> planRegionsInArea(Long areaId) throws ESOperateException {

        // 检查索引服务开启
        Result<Void> checkResult = checkTemplateSrvOpen(areaId);
        if (checkResult.failed()) {
            return checkResult;
        }

        List<CapacityPlanRegion> regions = capacityPlanRegionService.listRegionsInArea(areaId);

        if (CollectionUtils.isEmpty(regions)) {
            return Result.buildSucc();
        }

        // 错误信息记录
        List<String> failMsg = new LinkedList<>();
        for (CapacityPlanRegion region : regions) {
            try {
                Result<Void> result = capacityPlanRegionService.planRegion(region.getRegionId());
                if (result.failed()) {
                    failMsg.add(String.format("regionId:%d failMsg:%s", region.getRegionId(), result.getMessage()));
                    LOGGER.warn("class=CapacityPlanAreaServiceImpl||method=planRegion||region={}||failMag={}", region, result.getMessage());
                } else {
                    LOGGER.info("class=CapacityPlanAreaServiceImpl||method=planRegion||region={}||msg=succ", region, result.getMessage());
                }
            } catch (Exception e) {
                failMsg.add(String.format("regionId:%d errMsg:%s", region.getRegionId(), e.getMessage()));
                LOGGER.warn("class=CapacityPlanAreaServiceImpl||method=planRegion||region={}||errMag={}", region, e.getMessage(), e);
            }
        }

        if (CollectionUtils.isNotEmpty(failMsg)){
            return Result.buildFail(String.join(",", failMsg));
        }

        return Result.buildSucc();
    }

    /**
     * 容量检查
     *
     * @param areaId areaId
     * @return true/false
     */
    @Override
    public Result<Void> checkRegionsInArea(Long areaId) {

        // 检查索引服务开启
        Result<Void> checkResult = checkTemplateSrvOpen(areaId);
        if (checkResult.failed()) {
            return checkResult;
        }

        List<CapacityPlanRegion> regions = capacityPlanRegionService.listRegionsInArea(areaId);
        if (CollectionUtils.isEmpty(regions)) {
            return Result.buildSucc();
        }

        // 错误信息记录
        List<String> failMsg = new LinkedList<>();

        // 遍历area下的所有region进行检查
        for (CapacityPlanRegion region : regions) {
            try {
                Result<Void> result = capacityPlanRegionService.checkRegion(region.getRegionId());
                if (result.failed()) {
                    failMsg.add(String.format("regionId:%d failMsg:%s", region.getRegionId(), result.getMessage()));
                    LOGGER.warn("class=CapacityPlanAreaServiceImpl||method=checkRegion||region={}||failMag={}", region, result.getMessage());
                } else {
                    LOGGER.info("class=CapacityPlanAreaServiceImpl||method=checkRegion||region={}||msg=succ", region, result.getMessage());
                }
            } catch (Exception e) {
                failMsg.add(String.format("regionId:%d errMsg:%s", region.getRegionId(), e.getMessage()));
                LOGGER.warn("class=CapacityPlanAreaServiceImpl||method=checkRegion||region={}||errMag={}", region, e.getMessage(), e);
            }
        }

        if (StringUtils.isNotBlank(failMsg.toString())) {
            return Result.buildFail(failMsg.toString());
        }

        return Result.buildSucc();
    }

    /**
     * 容量均衡
     *
     * @return true/false
     */
    @Override
    public boolean balanceRegions() {

        List<CapacityPlanArea> areas = listAllPlanAreas();

        // 只对配置中指定的逻辑集群进行balance
        Set<String> enableResourceIdSet = ariusConfigInfoService.stringSettingSplit2Set(
                "capacity.plan.config.group",
                "capacity.plan.balance.region.resourceId", "", ",");

        areas = areas.stream()
                .filter(capacityPlanArea -> enableResourceIdSet.contains(String.valueOf(capacityPlanArea.getResourceId())))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(areas)) {
            return true;
        }

        for (CapacityPlanArea area : areas) {
            try {
                capacityPlanRegionService.balanceRegion(area.getId(), true);
            } catch (Exception e) {
                LOGGER.warn("class=CapacityPlanAreaServiceImpl||method=balanceRegion||areaId={}||errMsg={}", area.getResourceId(), e.getMessage(), e);
            }
        }

        return true;

    }

    /**
     * 根据资源id和cluster查询
     *
     * @param logicClusterId 逻辑集群ID
     * @param cluster        物理集群名称
     * @return area
     */
    @Override
    public CapacityPlanArea getAreaByResourceIdAndCluster(Long logicClusterId, String cluster) {
        return ConvertUtil.obj2Obj(capacityPlanAreaDAO.getByClusterAndResourceId(cluster, logicClusterId), CapacityPlanArea.class);
    }

    /**
     * 检查region的rack与region中模板的rack是否匹配
     *
     */
    @Override
    public void correctAllAreaRegionAndTemplateRacks() {
        List<CapacityPlanArea> areas = listAllPlanAreas();
        for (CapacityPlanArea area : areas) {
            try {
                correctAreaRegionAndTemplateRacks(area.getId());
            } catch (Exception e) {
                LOGGER.warn("class=CapacityPlanAreaServiceImpl||method=checkMeta||areaId={}||errMsg={}", area.getResourceId(), e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean correctAreaRegionAndTemplateRacks(Long areaId) {
        // 确保一个rack只在一个region里
        ensureAreaRacksUnique(areaId);
        // 确保规划area下的所有region下的物理模板Rack列表跟region是保持一致的
        ensureAreaPhysicalTemplatesRacksSameWithRegion(areaId);
        return true;
    }

    /***************************************** private method ****************************************************/
    /**
     * 校验并调整，确保规划集群中的Region Rack不会相互冲突
     *
     * @param areaId 规划集群
     */
    private void ensureAreaRacksUnique(Long areaId) {
        List<CapacityPlanRegion> regions = capacityPlanRegionService.listRegionsInArea(areaId);

        // 两层循环，判断是否有rack存在于不同的region里
        for (CapacityPlanRegion region1 : regions) {
            String targetRack = region1.getRacks();

            for (CapacityPlanRegion region2 : regions) {
                if (region2.getRegionId().equals(region1.getRegionId())) {
                    continue;
                }

                targetRack = removeRacks(targetRack, region2.getRacks());
            }

            if (!RackUtils.same(targetRack, region1.getRacks()) && StringUtils.isNotEmpty(targetRack)) {
                LOGGER.info("class=CapacityPlanAreaServiceImpl||method=checkRegionIsolation||regionId={}||srcRack={}||targetRack={}",
                        region1.getRegionId(), region1.getRacks(), targetRack);

                region1.setRacks(targetRack);
                // 有rack冲突，需要修改
                capacityPlanRegionService.modifyRegionRacks(region1.getRegionId(), targetRack);
            } else {
                LOGGER.info("class=CapacityPlanAreaServiceImpl||method=checkRegionIsolation||msg=rack no conflict", region1.getRegionId());
            }
        }
    }

    /**
     * 确保规划area下的所有region下的物理模板Rack列表跟region是保持一致的
     *
     * @param areaId 规划集群
     */
    private void ensureAreaPhysicalTemplatesRacksSameWithRegion(Long areaId) {
        Collection<CapacityPlanRegion> clusterRegions = capacityPlanRegionService.listRegionsInArea(areaId);
        CapacityPlanArea area = getAreaById(areaId);

        if(null == area){return;}

        // 物理集群下的物理模板
        List<IndexTemplatePhy> clusterTemplates = indexTemplatePhyService.getNormalTemplateByCluster(area.getClusterName());

        // 校验是否有模板rack不匹配
        for (CapacityPlanRegion region : clusterRegions) {

            // region的rack（去掉将要被缩容的rack）
            String tgtRack = getRegionFinalRack(region);

            List<IndexTemplatePhy> regionTemplates = clusterTemplates.stream()
                    .filter(indexTemplatePhysical -> belong(indexTemplatePhysical.getRack(), tgtRack))
                    .collect(Collectors.toList());

            // 遍历物理模板，检查rack是否一致
            for (IndexTemplatePhy templatePhysical : regionTemplates) {
                if (RackUtils.same(templatePhysical.getRack(), tgtRack)) {
                    continue;
                }

                try {
                    // 确保模板的rack与region的rack一致
                    templatePhyManager.editTemplateRackWithoutCheck(templatePhysical.getId(), tgtRack,
                            AriusUser.CAPACITY_PLAN.getDesc(), 0);
                    LOGGER.info("class=CapacityPlanAreaServiceImpl||method=checkRegionUniformity||template={}||srcRack={}||targetRack={}",
                            templatePhysical.getName(), templatePhysical.getRack(), tgtRack);
                } catch (Exception e) {
                    LOGGER.error(
                            "class=CapacityPlanRegionServiceImpl||method=checkMeta||errMsg={}||physicalId={}||tgtRack={}",
                            e.getMessage(), templatePhysical.getId(), tgtRack, e);
                }
            }
        }
    }

    /**
     * 获取region的正确的rack
     * 需要考虑扩缩容的场景
     *
     * @param region region
     * @return
     */
    private String getRegionFinalRack(CapacityPlanRegion region) {

        String racks = region.getRacks();

        CapacityPlanRegionTask lastDecreaseTask = capacityPlanRegionTaskService.getLastDecreaseTask(region.getRegionId(), 7);
        // 没有缩容任务
        if (lastDecreaseTask == null) {
            return racks;
        }

        // 最近的缩容任务已经成功
        if (!lastDecreaseTask.getStatus().equals(DATA_MOVING.getCode())
                && !lastDecreaseTask.getStatus().equals(OP_ES_ERROR.getCode())) {
            return racks;
        }

        return RackUtils.removeRacks(racks, lastDecreaseTask.getDeltaRacks());
    }

    /**
     * 保存region信息
     * @param capacityPlanArea      容量规划area
     * @param rack2templateMetasMap region下的模板数据，key-racks，value-racks上的模板
     * @return result
     */
    private boolean saveRegion(CapacityPlanArea capacityPlanArea,
                               Map<String, List<TemplateMetaMetric>> rack2templateMetasMap) {

        for (Map.Entry<String, List<TemplateMetaMetric>> entry : rack2templateMetasMap.entrySet()) {
            String racks = entry.getKey();
            List<TemplateMetaMetric> templateMetaMetrics = entry.getValue();

            // 创建并绑定region
            Result<Long> createAndBindResult = regionRackService.createAndBindRegion(
                capacityPlanArea.getClusterName(),
                racks,
                capacityPlanArea.getResourceId(),
                AdminConstant.YES,
                AriusUser.CAPACITY_PLAN.getDesc());


            if (createAndBindResult.failed()) {
                return false;
            }
            // 新创建的regionId
            Long regionId = createAndBindResult.getData();

            // 保存初始化任务
            if (!capacityPlanRegionTaskService.saveInitTask(regionId, racks, templateMetaMetrics)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 规划region
     *
     * @param rackMetas           rack指标
     * @param templateMetas       模板指标
     * @param capacityPlanArea 规划配置
     * @return result
     */
    private Map<String, List<TemplateMetaMetric>> initRegionInner(List<RackMetaMetric> rackMetas,
                                                                  List<TemplateMetaMetric> templateMetas,
                                                                  CapacityPlanArea capacityPlanArea) {
        // rack按着名字顺序排列
        rackMetas.sort((o1, o2) -> RackUtils.compareByName(o1.getName(), o2.getName()));

        // 记录已经被规划的模板
        Set<Long> planedTemplateSet = Sets.newHashSet();

        Map<String, List<TemplateMetaMetric>> rack2TemplateMetasMap = Maps.newHashMap();

        List<RackMetaMetric> newRegionRacks = Lists.newArrayList();
        for (int i = 0; i < rackMetas.size(); i++) {
            RackMetaMetric rackMeta = rackMetas.get(i);
            newRegionRacks.add(rackMeta);

            LOGGER.info("class=CapacityPlanAreaServiceImpl||method=planRegion||rack={}||msg=add new rack", rackMeta);

            if (newRegionRacks.size() >= capacityPlanArea.getConfig().getCountRackPerRegion()
                    || i == rackMetas.size() - 1) {
                RegionMetric regionMetric = capacityPlanRegionService.calcRegionMetric(newRegionRacks);

                LOGGER.info("class=CapacityPlanAreaServiceImpl||method=initRegionInner||regionMetric={}||count={}||msg=region count reach config count",
                        regionMetric, capacityPlanArea.getConfig().getCountRackPerRegion());

                List<TemplateMetaMetric> matchedTemplates = Lists.newArrayList();
                Integer result = findMatchedTemplates(getRegionCapacity(regionMetric.getResource()),
                        templateMetas.stream()
                                .filter(templateMeta -> !planedTemplateSet.contains(templateMeta.getPhysicalId()))
                                .collect(Collectors.toList()),
                        capacityPlanArea.getConfig(), matchedTemplates);

                if (NO_TEMPLATE.equals(result)) {
                    LOGGER.info("class=CapacityPlanAreaServiceImpl||method=planRegion||regionRacks={}||msg=no template", regionMetric.getRacks());
                    break;
                }

                if (REGION_IS_TOO_SMALL.equals(result)) {
                    LOGGER.info("class=CapacityPlanAreaServiceImpl||method=planRegion||regionRacks={}||msg=region is small", regionMetric.getRacks());
                    continue;
                }

                if (REGION_PLAN_SUCCEED.equals(result)) {
                    LOGGER.info("class=CapacityPlanAreaServiceImpl||method=planRegion||regionRacks={}||matchedTemplates={}||msg=plan succ",
                            regionMetric.getRacks(), matchedTemplates);

                    rack2TemplateMetasMap.put(regionMetric.getRacks(), matchedTemplates);

                    planedTemplateSet.addAll(
                            matchedTemplates.stream().map(TemplateMetaMetric::getPhysicalId).collect(Collectors.toSet()));

                    newRegionRacks = Lists.newArrayList();
                }
            }
        }

        // 资源不足
        if (planedTemplateSet.size() < templateMetas.size()) {
            List<TemplateMetaMetric> notPlanedTemplates = templateMetas.stream()
                    .filter(templateMeta -> !planedTemplateSet.contains(templateMeta.getPhysicalId()))
                    .collect(Collectors.toList());

            Double missQuotaSum = 0.0;
            for (TemplateMetaMetric templateMeta : notPlanedTemplates) {
                missQuotaSum += templateMeta.getQuota();
            }

            LOGGER.info(
                    "class=CapacityPlanAreaServiceImpl||method=planRegion||planedTemplateSet={}||templateMetas={}||missQuotaSum={}||notPlanedTemplates={}||msg=cluster not enough",
                    planedTemplateSet.size(), templateMetas.size(), missQuotaSum, notPlanedTemplates);

            throw new ResourceNotEnoughException("集群资源不足, 缺失Quota：" + missQuotaSum);
        }

        return rack2TemplateMetasMap;
    }

    /**
     * 计算region的容量
     * 模板的quota是按着DOCKER(16C32G3T)的规格计算的;为了规划容量的单位需要统计；这里返回的region的容量也是DOCKER(16C32G3T)单位的
     *
     * @param resource region
     * @return 容量
     */
    private Double getRegionCapacity(Resource resource) {
        return quotaTool.getResourceQuotaCountByCpuAndDisk(NodeSpecifyEnum.DOCKER.getCode(), resource.getCpu(),
                resource.getDisk(), 0.0);
    }

    /**
     * 为region规划模板
     *
     * @param regionCapacity   region的容量
     * @param templateMetas    带分配的模板
     * @param config           规划配置
     * @param matchedTemplates 结果
     * @return 成功或者资源不足或者模板不足
     */
    private Integer findMatchedTemplates(Double regionCapacity, List<TemplateMetaMetric> templateMetas,
                                         CapacityPlanConfig config, List<TemplateMetaMetric> matchedTemplates) {
        if (CollectionUtils.isEmpty(templateMetas)) {
            return NO_TEMPLATE;
        }

        LOGGER.info(
                "class=CapacityPlanAreaServiceImpl||method=findMatchedTemplates||regionCapacity={}||templateMetas={}||config={}||msg=begin init a region",
                regionCapacity, templateMetas, config);

        // 为了超大的模板能够被优先分配，先找能够独占region的模板
        List<TemplateMetaMetric> bigTemplates = templateMetas.stream()
                .filter(templateMeta -> templateMeta.getQuota() >= regionCapacity * config.getRegionWatermarkHigh())
                .sorted((t1, t2) -> t2.getQuota().compareTo(t1.getQuota())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(bigTemplates)) {
            TemplateMetaMetric templateMax = bigTemplates.get(0);

            LOGGER.info("class=CapacityPlanAreaServiceImpl||method=findMatchedTemplates||templateMax={}||msg=get big template", templateMax);

            if (templateMax.getQuota() > regionCapacity * config.getRegionWatermarkHigh()) {
                return REGION_IS_TOO_SMALL;
            }

            matchedTemplates.add(templateMax);
            return REGION_PLAN_SUCCEED;
        }

        // 依次遍历模板列表  知道quota利用率达标
        double resourceRate = (config.getRegionWatermarkHigh() + config.getRegionWatermarkLow()) / 2;
        Double quotaSum = 0.0;
        for (TemplateMetaMetric templateMeta : templateMetas) {
            if (quotaSum / regionCapacity >= resourceRate) {
                return REGION_PLAN_SUCCEED;
            }

            double templateQuota = templateMeta.getQuota();

            if (quotaSum + templateQuota <= regionCapacity * config.getRegionWatermarkHigh()) {
                quotaSum += templateQuota;
                matchedTemplates.add(templateMeta);
                LOGGER.info("class=CapacityPlanAreaServiceImpl||method=findMatchedTemplates||templateNormal={}||quotaSum={}||msg=get normal template",
                        templateMeta, quotaSum);
            }
        }

        LOGGER.info("class=CapacityPlanAreaServiceImpl||method=findMatchedTemplates||msg=no matched_template for not_full_region");

        return REGION_PLAN_SUCCEED;
    }

    private List<Long> getNoRegionTemplateIds(List<IndexTemplatePhy> templatePhysicals,
                                              Set<String> noRegionRackSet) {
        return templatePhysicals.stream()
                .filter(templatePhysical -> RackUtils.hasIntersect(templatePhysical.getRack(), noRegionRackSet))
                .map(IndexTemplatePhy::getId).collect(Collectors.toList());
    }

    private boolean checkTemplateAllocate(List<IndexTemplatePhy> templatePhysicals, Set<String> hasRegionRackSet,
                                          Set<String> noRegionRackSet) {
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            if (RackUtils.hasIntersect(templatePhysical.getRack(), hasRegionRackSet)
                    && RackUtils.hasIntersect(templatePhysical.getRack(), noRegionRackSet)) {
                LOGGER.warn("class=CapacityPlanAreaServiceImpl||method=checkTemplateAllocate||msg=template cross region||template={}||rack={}||hasRegionRackSet={}||noRegionRackSet={}",
                        templatePhysical.getName(), templatePhysical.getRack(), hasRegionRackSet, noRegionRackSet);
                return false;
            }
        }
        return true;
    }

    private Set<String> getHasRegionRacks(List<CapacityPlanRegion> hasExistRegions) {
        Set<String> rackPlanedSet = Sets.newHashSet();
        for (CapacityPlanRegion region : hasExistRegions) {
            rackPlanedSet.addAll(RackUtils.racks2List(region.getRacks()));
        }
        return rackPlanedSet;
    }

    /**
     * 获取没有被划分到region里的rack
     * @param areaId areaId
     * @param hasRegionRackSet 已经被划分到region里的rack
     * @return
     */
    private Set<String> getNoRegionRacks(Long areaId, Set<String> hasRegionRackSet) {

        // 所有racks
        Set<String> areaRacksTotal = listAreaRacks(areaId);
        if (CollectionUtils.isEmpty(areaRacksTotal)) {
            throw new ClusterMetadataException("获取集群rack失败");
        }

        // 校验，hasRegionRackSet应该完全属于areaRacksTotal
        Set<String> hasRegionRackSetCopy = Sets.newHashSet(hasRegionRackSet);
        hasRegionRackSetCopy.removeAll(areaRacksTotal);
        if (CollectionUtils.isNotEmpty(hasRegionRackSetCopy)) {
            throw new ClusterMetadataException("超过了逻辑集群的资源");
        }

        return areaRacksTotal.stream().filter(rack -> !hasRegionRackSet.contains(rack)).collect(Collectors.toSet());
    }

    /**
     * 校验参数
     *
     * @param areaDTO 规划area参数
     * @return
     */
    private Result<Void> checkPlanAreaParams(CapacityPlanAreaDTO areaDTO) {
        if (AriusObjUtils.isNull(areaDTO)) {
            return Result.buildParamIllegal("规划集群为空");
        }

        if (AriusObjUtils.isNull(areaDTO.getClusterName())) {
            return Result.buildParamIllegal("物理集群名称为空");
        }

        if (AriusObjUtils.isNull(areaDTO.getResourceId())) {
            return Result.buildParamIllegal("逻辑集群ID为空");
        }

        return Result.buildSucc();
    }

    private void refreshTemplateQuota(List<TemplateMetaMetric> templateMetas) {
        for (TemplateMetaMetric templateMetaMetric : templateMetas) {
            double quota = quotaTool.getTemplateQuotaCountByCpuAndDisk(NodeSpecifyEnum.DOCKER.getCode(),
                    templateMetaMetric.getCombinedCpuCount(), templateMetaMetric.getCombinedDiskG(), TEMPLATE_QUOTA_MIN);
            LOGGER.info("class=CapacityPlanAreaServiceImpl||method=refreshTemplateQuota||templateName={}||srcQuota={}||tgtQuota={}",
                    templateMetaMetric.getTemplateName(), templateMetaMetric.getQuota(), quota);
            templateMetaMetric.setQuota(quota);
        }
    }

    private CapacityPlanAreaPO getCapacityPlanRegionPOFromCache(Long logicClusterId){
        try {
            return cpcCache.get( "CPC@" + logicClusterId, () -> capacityPlanAreaDAO.getPlanClusterByLogicClusterId(logicClusterId));
        } catch (Exception e) {
            return capacityPlanAreaDAO.getPlanClusterByLogicClusterId(logicClusterId);
        }
    }

    /**
     * 检查索引服务是否开启
     * @param areaId 容量规划areaId
     * @return
     */
    private Result<Void> checkTemplateSrvOpen(Long areaId) {
        CapacityPlanArea capacityPlanArea = getAreaById(areaId);

        if (capacityPlanArea == null) {
            return Result.buildFail(String.format("容量规划area %s 不存在", areaId));
        }

        return checkTemplateSrvOpen(capacityPlanArea);
    }

    /**
     * 检查索引服务是否开启
     * @param capacityPlanArea 容量规划area
     * @return
     */
    private Result<Void> checkTemplateSrvOpen(CapacityPlanArea capacityPlanArea) {

        if (capacityPlanArea == null) {
            return Result.buildFail("capacityPlanArea不存在");
        }

        if (!isTemplateSrvOpen(capacityPlanArea.getClusterName())) {
            return Result.buildFail(String.format("%s 没有开启 %s", capacityPlanArea.getClusterName(), templateServiceName()));
        }

        return Result.buildSucc();
    }
}

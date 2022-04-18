package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service;

import com.didichuxing.datachannel.arius.admin.common.bean.common.RackMetaMetric;
import com.didichuxing.datachannel.arius.admin.common.bean.common.RegionMetric;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.exception.AmsRemoteException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanRegionDTO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegionBalanceItem;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegionSplitResult;

import java.util.List;

public interface CapacityPlanRegionService {

    /**
     * 获取area的region列表
     * @param areaId 集群id
     * @return list
     */
    List<CapacityPlanRegion> listRegionsInArea(Long areaId);

    /**
     * 根据regionId获取容量规划region对象
     * @param regionId regionId
     * @return 容量规划region对象
     */
    CapacityPlanRegion getRegionById(Long regionId);

    /**
     * 创建容量信息记录
     * @param capacityPlanRegionDTO capacityPlanRegionDTO
     * @return
     */
    Result<Void> createRegionCapacityInfo(CapacityPlanRegionDTO capacityPlanRegionDTO, String operator);

    /**
     * 删除容量信息记录
     * @param regionId regionId
     * @return
     */
    Result<Void> deleteRegionCapacityInfo(Long regionId, String operator);

    /**
     * 修改一个region，只支持racks（旧版）, share, configJson, freeQuota, usage的修改
     * @param regionDTO Region详情
     * @param operator 操作者
     * @return result
     */
    Result<Void> editRegion(CapacityPlanRegionDTO regionDTO, String operator);

    /**
     * 修改一个region的freeQuota
     * @param regionId regionId
     * @param freeQuota freeQuota
     */
    void editRegionFreeQuota(Long regionId, Double freeQuota);

    /**
     * 修改一个region的rack
     * @param regionId regionId
     * @param tgtRacks tgtRacks
     * @return
     */
    boolean modifyRegionRacks(Long regionId, String tgtRacks);

    /**
     * 规划一个region的资源，需要规划未来一天的资源
     * @param regionId  regionId
     * @return result
     * @throws ESOperateException 操作异常
     */
    Result<Void> planRegion(Long regionId) throws ESOperateException;

    /**
     * 检查一个region的资源，需要检查当前region的资源是否足够
     * @param regionId  regionId
     * @return result
     */
    Result<Void> checkRegion(Long regionId);

    /**
     * 平衡region
     * @param areaId areaId
     * @param exe 是否执行操作
     * @return
     */
    Result<List<CapacityPlanRegionBalanceItem>> balanceRegion(Long areaId, boolean exe);

    /**
     * 拆分region
     * @param regionId regionId
     * @param exe 是否执行
     * @return result
     */
    Result<List<CapacityPlanRegionSplitResult>> splitRegion(Long regionId, boolean exe);

    /**
     * 搬迁一个region内所有的索引到当前region的rack
     * @param regionId  regionId
     * @param shouldUpdateIndex 是否更新索引元数据信息
     * @return result
     */
    Result<Void> moveShard(Long regionId, boolean shouldUpdateIndex);

    /**
     * 获取region资源信息
     * @param rackMetaMetrics Rack元数据信息
     * @return
     */
    RegionMetric calcRegionMetric(List<RackMetaMetric> rackMetaMetrics);

    /**
     * 获取全部的region
     * @return list
     */
    List<CapacityPlanRegion> listAllRegions();

    /**
     * 获取指定逻辑集群下的share为1的reigon
     * @param logicClusterId 逻辑集群ID
     * @return list
     */
    List<CapacityPlanRegion> listLogicClusterSharedRegions(Long logicClusterId);

    /**
     * 获取物理模板所属的region
     * @param templatePhysical 模板
     * @return result
     */
    CapacityPlanRegion getRegionOfPhyTemplate(IndexTemplatePhy templatePhysical);

    /**
     * 记录统计数据
     * @param regionId  regionId
     * @param usage     usage
     * @param overSold  overSold
     * @return true/false
     */
    boolean modifyRegionMetrics(Long regionId, double usage, double overSold);

    /**
     * 获取Region rack Metrics信息
     * @param region Region信息
     * @return
     * @throws AmsRemoteException
     */
    List<RackMetaMetric> fetchRegionRackMetrics(CapacityPlanRegion region) throws AmsRemoteException;
}

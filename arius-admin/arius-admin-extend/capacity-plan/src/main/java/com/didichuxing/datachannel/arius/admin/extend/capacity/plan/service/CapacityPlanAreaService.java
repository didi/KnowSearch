package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service;

import java.util.List;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanAreaDTO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanArea;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;

/**
 * @author d06679
 * @date 2019-06-24
 */
public interface CapacityPlanAreaService {

    /**
     * 获取所有规划的集群信息
     * @return list
     */
    List<CapacityPlanArea> listAllPlanAreas();


    /**
     * 获取逻辑集群的所属的area列表，一个逻辑集群可能会绑定到多个物理集群，因此会有多个area
     * @return list
     */
    List<CapacityPlanArea> listAreasByLogicCluster(Long logicClusterId);

    /**
     * 获取物理集群的所属的area列表
     * @return list
     */
    List<CapacityPlanArea> listAreasByPhyCluster(String phyClusterName);

    /**
     * 获取所有规划的集群信息
     * @return list
     */
    List<CapacityPlanArea> listPlaningAreas();

    /**
     * 新增一个需要规划的area
     * @param capacityPlanAreaDTO area参数
     * @param operator 操作者
     * @return result
     */
    Result<Long> createPlanAreaInNotExist(CapacityPlanAreaDTO capacityPlanAreaDTO, String operator);

    /**
     * 修改规划的集群
     * @param areaDTO 集群参数
     * @param operator 操作者
     * @return result
     */
    Result<Void> modifyPlanArea(CapacityPlanAreaDTO areaDTO, String operator);

    /**
     * 删除规划的集群
     * @param areaId areaId
     * @param operator 操作者
     * @return result
     */
    Result<Void> deletePlanArea(Long areaId, String operator);

    /**
     * 初始化一个集群的region，将没有绑定成region的racks根据racks上分布的模板绑定成region
     * @param areaId areaId
     * @param operator 操作者
     * @return 将保存到数据库的region列表返回
     */
    Result<List<CapacityPlanRegion>> initRegionsInPlanArea(Long areaId, String operator);

    /**
     * 获取集群级别的配置
     * @param areaId areaId
     * @return result
     */
    CapacityPlanArea getAreaById(Long areaId);

    /**
     * 获取area拥有的rack列表
     * @param areaId areaId
     * @return result
     */
    Set<String> listAreaRacks(Long areaId);

    /**
     * 获取area拥有的空闲的rack
     * 空闲racks定义：没有被绑定成region的rack
     * 在2.0版本中为arius_resource_logic_item表中没有被绑定成region的rack
     * 在3.0版本中应返回的是空list
     * @param areaId areaId
     * @return list
     */
    List<String> listAreaFreeRacks(Long areaId);

    /**
     * 获取area中已经被使用的rack
     * 已经被使用的racks定义：已经比绑定成region并分配给了逻辑集群的rack，也就是逻辑集群拥有的region里的racks
     * @param areaId areaId
     * @return set
     */
    Set<String> listAreaUsedRacks(Long areaId);

    /**
     * 容量规划
     * @param areaId areaId
     * @throws ESOperateException 操作异常
     * @return true/false
     */
    Result<Void> planRegionsInArea(Long areaId) throws ESOperateException;

    /**
     * 容量检查
     * @param areaId areaId
     * @return true/false
     */
    Result<Void> checkRegionsInArea(Long areaId);

    /**
     * 容量均衡
     * @return true/false
     */
    boolean balanceRegions();

    /**
     * 根据资源id和cluster查询
     * @param resourceId resourceId
     * @param cluster 集群
     * @return area
     */
    CapacityPlanArea getAreaByResourceIdAndCluster(Long resourceId, String cluster);

    /**
     * 记录统计数据
     * @param areaId        areaId
     * @param usageAvg      usageAvg
     * @param overSoldAvg   overSoldAvg
     * @return true/false
     */
    boolean recordAreaStatis(Long areaId, double usageAvg, double overSoldAvg);

    /**
     * 检查所有的规划集群所有region的rack与region中模板的rack是否匹配
     */
    void correctAllAreaRegionAndTemplateRacks();

    /**
     * 检查给定的area下所有region的rack与region中模板的rack是否匹配
     * @param areaId areaID
     * @return true/false
     */
    boolean correctAreaRegionAndTemplateRacks(Long areaId);

}

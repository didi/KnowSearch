package com.didichuxing.datachannel.arius.admin.core.service.cluster.region;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterRackInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;

/**
 * 处理Region、Rack service
 * @author wangshu
 * @date 2020/10/12
 */
public interface ESRegionRackService {
    /**
     * 通过Rack ID删除Rack
     * @param rackId Rack ID
     * @return
     */
    @Deprecated
    boolean deleteRackById(Long rackId);

    /**
     * 获取region
     * @param regionId regionId
     */
    ClusterRegion getRegionById(Long regionId);

    /**
     * 获取逻辑集群有的region信息
     * @param logicClusterId 逻辑集群ID
     * @return 已经被绑定到指定逻辑集群的region
     */
    List<ClusterRegion> listLogicClusterRegions(Long logicClusterId);

    /**
     * 获取物理下的region
     * @param phyClusterName 物理集群名
     * @return 物理集群下的region
     */
    List<ClusterRegion> listRegionsByClusterName(String phyClusterName);

    /**
     * 获取所有逻辑集群拥有的rack信息
     * @return 列表
     */
    List<ESClusterLogicRackInfo> listAllLogicClusterRacks();

    /**
     * 条件查询逻辑集群拥有的rack信息
     * @param param 条件
     * @return Result
     */
    List<ESClusterLogicRackInfo> listLogicClusterRacks(ESLogicClusterRackInfoDTO param);

    /**
     * 获取执行逻辑集群拥有的rack信息
     * @param logicClusterId 逻辑集群ID
     * @return result
     */
    List<ESClusterLogicRackInfo> listLogicClusterRacks(Long logicClusterId);

    /**
     * 增加Rack到逻辑集群
     * @param param    参数
     * @param operator 操作者
     * @return
     */
    @Deprecated
    Result addRackToLogicCluster(ESLogicClusterRackInfoDTO param, String operator);

    /**
     * 获取物理集群下已经被分配的region下的rack信息
     * @param phyClusterName 物理集群名
     * @return list
     */
    List<ESClusterLogicRackInfo> listAssignedRacksByClusterName(String phyClusterName);

    /**
     * 根据逻辑集群ID获取物理集群名称列表（获取逻辑集群拥有资源的物理集群）
     * @param logicClusterId 逻辑集群ID
     * @return 逻辑集群被分配到的物理集群的集群名list
     */
    List<String> listPhysicClusterNames(Long logicClusterId);

    /**
     * 根据逻辑集群ID获取物理集群Id列表
     * @param logicClusterId 逻辑集群ID
     * @return 逻辑集群被分配到的物理集群的集群ID list
     */
    List<Integer> listPhysicClusterId(Long logicClusterId);

    /**
     * 获取指定物理集群中racks匹配到的region的个数
     * @param cluster 集群
     * @param racks   racks
     * @return count
     */
    int countRackMatchedRegion(String cluster, String racks);

    /**
     * 获取指定物理集群绑定到指定逻辑集群的region
     * @param logicClusterId 逻辑集群ID
     * @param clusterName    物理集群名
     * @return 物理集群clusterName下的被绑定到逻辑集群logicClusterId的region
     */
    List<ClusterRegion> listRegionsByLogicAndPhyCluster(Long logicClusterId, String clusterName);

    /**
     * 获取指定物理集群下的region
     * @param clusterName 物理集群名
     * @return 物理集群clusterName下的region
     */
    List<ClusterRegion> listPhyClusterRegions(String clusterName);

    /**
     * 获取所有已经被绑定到逻辑集群的region
     * @return 所有已经被绑定到逻辑集群的region
     */
    List<ClusterRegion> listAllBoundRegions();

    /**
     * 创建物理集群region
     * @param clusterName 物理集群名
     * @param racks       region里的rack
     * @param share       share
     * @param operator    操作人
     * @return 创建的regionId
     */
    Result<Long> createPhyClusterRegion(String clusterName, String racks, Integer share, String operator);

    /**
     * 创建并绑定region
     * @param clusterName    物理集群名
     * @param racks          region里的rack
     * @param logicClusterId 绑定到的逻辑集群ID
     * @param share          share
     * @param operator       操作人
     * @return 新创建的regionId
     */
    Result<Long> createAndBindRegion(String clusterName, String racks, Long logicClusterId, Integer share,
                                     String operator);

    /**
     * 删除物理集群region
     * @param regionId regionId
     * @param operator 操作人
     * @return
     */
    Result deletePhyClusterRegion(Long regionId, String operator);

    /**
     * 删除物理region，删除前不做检查，仅用于splitRegion
     * @param regionId regionId
     * @return
     */
    Result deletePhyClusterRegionWithoutCheck(Long regionId, String operator);

    /**
     * 解绑region
     * @param regionId regionId
     * @param operator operator
     * @return
     */
    Result unbindRegion(Long regionId, String operator);

    /**
     * 绑定region到逻辑集群
     * @param regionId       regionId
     * @param logicClusterId 逻辑集群ID
     * @param share          share
     * @param operator       操作人
     * @return
     */
    Result bindRegion(Long regionId, Long logicClusterId, Integer share, String operator);

    /**
     * 编辑region的rack
     * @param regionId regionId
     * @param racks    要修改成的racks
     * @param operator 操作人
     * @return
     */
    Result editRegionRacks(Long regionId, String racks, String operator);

    /**
     * 判断region是否已经被绑定给逻辑集群
     * @param region region
     * @return true-已经被绑定，false-没有被绑定
     */
    boolean isRegionBound(ClusterRegion region);

    /**
     * 根据物理集群id，获取该物理集群对应的逻辑集群的id
     * @param phyClusterId 物理集群id
     * @return 逻辑集群id
     */
    Long getLogicClusterIdByPhyClusterId(Integer phyClusterId);
}

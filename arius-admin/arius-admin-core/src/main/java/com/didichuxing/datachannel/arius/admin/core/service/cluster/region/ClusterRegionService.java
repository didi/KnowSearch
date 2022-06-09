package com.didichuxing.datachannel.arius.admin.core.service.cluster.region;

import java.util.List;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;

/**
 * @author ohushenglin_v
 * @date 2022-05-30
 */
public interface ClusterRegionService {
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
    @Deprecated
    //todo: logic cluster set to key
    List<ClusterRegion> listLogicClusterRegions(Long logicClusterId);

    ClusterRegion getRegionByLogicClusterId(Long logicClusterId);

    /**
     * 获取物理集群下的region
     * @param phyClusterName 物理集群名
     * @return 物理集群下的region
     */
    List<ClusterRegion> listRegionsByClusterName(String phyClusterName);

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
     * @param clusterName   物理集群名称
     * @param regionName    region名称
     * @param operator      操作人
     * @return              regionId
     */
    Result<Long> createPhyClusterRegion(String clusterName, List<Integer> nodeIds, String regionName, String operator);

    /**
     * 删除物理集群region
     * @param regionId regionId
     * @param operator 操作人
     * @return
     */
    Result<Void> deletePhyClusterRegion(Long regionId, String operator);

    /**
     * 批量删除物理集群中region
     * @param clusterPhyName 物理集群名称
     * @param operator   操作人
     * @return
     */
    Result<Void> deleteByClusterPhy(String clusterPhyName, String operator);

    /**
     * 解绑region
     * @param regionId regionId
     * @param operator operator
     * @param logicClusterId 逻辑集群id
     * @return
     */
    Result<Void> unbindRegion(Long regionId, Long logicClusterId, String operator);

    /**
     * 绑定region到逻辑集群
     * @param regionId       regionId
     * @param logicClusterId 逻辑集群ID
     * @param share          share
     * @param operator       操作人
     * @return
     */
    Result<Void> bindRegion(Long regionId, Long logicClusterId, Integer share, String operator);

    /**
     * 判断region是否已经被绑定给逻辑集群
     * @param region region
     * @return true-已经被绑定，false-没有被绑定
     */
    boolean isRegionBound(ClusterRegion region);

    /**
     * 根据物理集群id，获取该物理集群对应的逻辑集群的id列表
     * @param phyClusterId 物理集群id
     * @return 逻辑集群id列表
     */
    Set<Long> getLogicClusterIdByPhyClusterId(Integer phyClusterId);

    /**
     * 根据名称判断region是否存在
     * @param regionName region名称
     * @return   false or true
     */
    boolean isExistByRegionName(String regionName);

    /**
     * 根据regionId判断region是否存在
     * @param regionId  regionId
     * @return          false or true
     */
    boolean isExistByRegionId(Integer regionId);

    /**
     * 根据逻辑集群Id列表获取region列表, 注意这里入参列表元素不能太大，否则有性能影响
     * @param clusterLogicIdList  逻辑集群id列表
     * @return
     */
    List<ClusterRegion> listClusterRegionsByLogicIds(List<Long> clusterLogicIdList);
}

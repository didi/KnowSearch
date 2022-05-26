package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionWithNodeInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.LogicClusterRackVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PhyClusterRackVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/7
 * @Comment:
 */
public interface ClusterRegionManager {

    /**
     * 构建regionVO
     * @param regions region列表
     * @return
     */
    List<ClusterRegionVO> buildLogicClusterRegionVO(List<ClusterRegion> regions);

    /**
     * 根据逻辑集群的类型筛选出可以绑定的region信息
     * @param clusterLogicId 逻辑集群id
     * @param clusterLogicType 逻辑集群类型
     * @param phyCluster 物理集群名称
     * @return 筛选后的region列表
     */
    List<ClusterRegion> filterClusterRegionByLogicClusterType(Long clusterLogicId, String phyCluster, Integer clusterLogicType);

    /**
     * 构建regionVO
     * @param region region
     * @return
     */
    ClusterRegionVO buildLogicClusterRegionVO(ClusterRegion region);

    /**
     * 获取物理集群可划分至region的Racks信息
     * @param cluster       物理集群名
     * @return
     */
    List<PhyClusterRackVO> buildCanDividePhyClusterRackVOs(String cluster);

    /**
     * 构建逻辑集群物RackVO
     * @param logicClusterRackInfos 逻辑集群rack信息
     * @return
     */
    List<LogicClusterRackVO> buildLogicClusterRackVOs(List<ClusterLogicRackInfo> logicClusterRackInfos);

    /**
     * 逻辑集群批量绑定region
     * @param isAddClusterLogicFlag 是否要添加逻辑集群
     */
    Result<Void> batchBindRegionToClusterLogic(ESLogicClusterWithRegionDTO param, String operator,
                                               boolean isAddClusterLogicFlag);

    /**
     * 解绑逻辑集群已经绑定的region
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
     * 根据物理集群名称获取region信息
     * @param clusterName          物理集群名称
     * @return                     Result<List<ClusterRegionWithNodeInfoVO>>
     */
    Result<List<ClusterRegionWithNodeInfoVO>> getClusterRegionWithNodeInfoByClusterName(String clusterName);
}

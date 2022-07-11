package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionWithNodeInfoVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import java.util.List;

public interface ClusterRegionManager {

    /**
     * 构建regionVO
     * @param regions region列表
     * @return
     */
    List<ClusterRegionVO> buildLogicClusterRegionVO(List<ClusterRegion> regions);

    /**
     * 根据逻辑集群的类型筛选出可以绑定的region信息
     * @param clusterLogicType 逻辑集群类型
     * @param phyCluster 物理集群名称
     * @return 筛选后的region列表
     */
    Result<List<ClusterRegionVO>> listPhyClusterRegionsByLogicClusterTypeAndCluster(String phyCluster, Integer clusterLogicType);

    /**
     * 构建regionVO
     * @param region region
     * @return
     */
    ClusterRegionVO buildLogicClusterRegionVO(ClusterRegion region);

    /**
     * 逻辑集群批量绑定region
     *
     * @param isAddClusterLogicFlag 是否要添加逻辑集群
     */
    Result<Void> batchBindRegionToClusterLogic(ESLogicClusterWithRegionDTO param, String operator,
                                               boolean isAddClusterLogicFlag) throws AdminOperateException;

    /**
     * 解绑逻辑集群已经绑定的region
     *
     * @param regionId       regionId
     * @param logicClusterId 逻辑集群id
     * @param operator       operator
     * @param projectId
     * @return
     */
    Result<Void> unbindRegion(Long regionId, Long logicClusterId, String operator, Integer projectId);


    /**
     * 根据物理集群名称获region信息（包含空节点region），包含region中的数据节点信息
     * @param clusterName          物理集群名称
     * @return                     Result<List<ClusterRegionWithNodeInfoVO>>
     */
    Result<List<ClusterRegionWithNodeInfoVO>> listClusterRegionWithNodeInfoByClusterName(String clusterName);

    /**
     * 获取可分配至dcdr的物理集群名称获region列表, 不包含空节点region
     *
     * @param clusterName         物理集群名称
     * @return                    Result<List<ClusterRegionVO>>
     */
    Result<List<ClusterRegionVO>> listNotEmptyClusterRegionByClusterName(String clusterName);
    
    Result<Void> deletePhyClusterRegion(Long regionId, String operator, Integer projectId);

  
}
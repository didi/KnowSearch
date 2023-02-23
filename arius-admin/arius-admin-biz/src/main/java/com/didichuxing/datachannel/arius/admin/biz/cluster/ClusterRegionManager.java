package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicSpecCondition;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionWithNodeInfoVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;

public interface ClusterRegionManager {

    /**
     * 构建regionVO
     * @param regions region列表
     * @return
     */
    List<ClusterRegionVO> buildLogicClusterRegionVO(List<ClusterRegion> regions);

    /**
     * 根据逻辑集群的类型筛选出可以绑定的region信息，返回的region列表中不包含cold region
     * @param clusterLogicType 逻辑集群类型
     * @param phyCluster 物理集群名称
     * @return 筛选后的region列表
     */
    @Deprecated
    Result<List<ClusterRegionVO>> listPhyClusterRegionsByLogicClusterTypeAndCluster(String phyCluster,
                                                                                    Integer clusterLogicType);

    /**
     * 逻辑集群绑定同一个物理集群的region的时候需要根据类型进行过滤，之后再根据cold、region节点数量、节点规格进行过滤
     * @param phyCluster 物理集群名称
     * @param clusterLogicType 逻辑集群类型
     * @param condition 用户侧申请的集群规格（节点数量、机器规格）
     * @return
     */
    Result<List<ClusterRegionVO>> listPhyClusterRegionsByCondition(String phyCluster,
                                                                   Integer clusterLogicType,
                                                                   ClusterLogicSpecCondition condition);

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
     * 根据物理集群名称获region信息（包含空节点region），包含region中的数据节点信息
     * @param clusterName          物理集群名称
     * @return                     Result<List<ClusterRegionWithNodeInfoVO>>
     */
    Result<List<ClusterRegionWithNodeInfoVO>> listClusterRegionWithNodeInfoByClusterName(String clusterName);

    /**
     * 获取当前集群支持的所有attribute划分方式
     * @param clusterId 物理集群id
     * @return
     */
    Result<Set<String>> getClusterAttributeDivideType(Long clusterId);

    /**
     * 根据物理集群名称和划分方式获region信息，包含region中的数据节点信息
     * @param clusterName   物理集群名称
     * @param divideType  region划分方式
     * @return
     */
    Result<List<ClusterRegionWithNodeInfoVO>> listClusterRegionInfoWithDivideType(String clusterName, String divideType);

    /**
     * 获取可分配至dcdr的物理集群名称获region列表, 不包含空节点region
     *
     * @param clusterName         物理集群名称
     * @return                    Result<List<ClusterRegionVO>>
     */
    Result<List<ClusterRegionVO>> listNotEmptyClusterRegionByClusterName(String clusterName);

    /**
     * 删除物理集群region
     * @param regionId
     * @param operator
     * @param projectId
     * @return
     */
    Result<Void> deletePhyClusterRegion(Long regionId, String operator, Integer projectId) throws AdminOperateException;

    
    /**
     * 通过物理集群获取冷region
     *
     * @param phyCluster 物理集群名称
     * @return ClusterRegion 对象列表
     */
    List<ClusterRegion> getColdRegionByPhyCluster(String phyCluster);
    /**
     * 列出物理集群的所有region
     *
     * @param phyCluster 物理集群名称
     * @return ClusterRegion 对象列表
     */
    List<ClusterRegion> listRegionByPhyCluster(String phyCluster);
    
    /**
     * > 通过逻辑集群 id 构建逻辑集群region vo
     *
     * @param logicClusterId 逻辑集群 ID
     * @return 列表<ClusterRegionVO>
     */
    Result<List<ClusterRegionVO>> buildLogicClusterRegionVOByLogicClusterId(Long logicClusterId);
    
    
}
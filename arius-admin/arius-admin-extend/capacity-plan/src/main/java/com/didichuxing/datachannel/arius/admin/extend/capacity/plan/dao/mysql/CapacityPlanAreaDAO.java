package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.dao.mysql;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po.CapacityPlanAreaPO;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Repository
public interface CapacityPlanAreaDAO {

    CapacityPlanAreaPO getById(Long areaId);

    /**
     * 通过逻辑集群ID获取规划集群详情
     * @param logicClusterId 规划集群ID
     * @return
     */
    CapacityPlanAreaPO getPlanClusterByLogicClusterId(Long logicClusterId);

    /**
     * 获取所有的规划集群
     * @return
     */
    List<CapacityPlanAreaPO> listAll();

    /**
     * 创建规划集群
     * @param capacityPlanCluster 规划集群详情
     * @return
     */
    int insert(CapacityPlanAreaPO capacityPlanCluster);

    /**
     * 更新规划集群信息
     * @param capacityPlanCluster 规划集群详情
     * @return
     */
    int update(CapacityPlanAreaPO capacityPlanCluster);

    int delete(Long areaId);

    /**
     * 删除规划集群
     * @param logicClusterId ID
     * @return
     */
    int deleteByLogicClusterId(Long logicClusterId);

    /**
     * 删除规划集群
     * @param phyCluster 物理集群名称
     * @return
     */
    int deleteByPhyCluster(String phyCluster);

    /**
     * 根据物理集群名称获取规划集群详情
     * @param phyCluster 物理集群名称
     * @return
     */
    CapacityPlanAreaPO getPlanClusterByPhyClusterName(
            @Param("phyCluster") String phyCluster);

    /**
     * 获取规划集群
     * @param clusterName 物理集群名称
     * @param resourceId 逻辑集群ID
     * @return
     */
    CapacityPlanAreaPO getByClusterAndResourceId(
            @Param("clusterName") String clusterName,
            @Param("resourceId") Long resourceId);

}

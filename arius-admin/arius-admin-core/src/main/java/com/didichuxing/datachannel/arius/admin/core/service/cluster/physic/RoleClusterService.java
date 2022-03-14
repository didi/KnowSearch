package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleCluster;
import java.util.List;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESRoleClusterPO;

/**
 * ES集群角色 服务类
 * @author didi
 * @since 2020-08-24
 */
public interface RoleClusterService {

    /**
     * 保存集群角色
     * @param esRoleClusterDTO
     * @return id
     */
    Result<Void> save(ESRoleClusterDTO esRoleClusterDTO);

    /**
     * 角色不存在则创建，存在则返回已存在的角色
     * @param clusterName 集群名
     * @param role        角色, ESClusterNodeRoleEnum的desc
     * @return
     */
    RoleCluster createRoleClusterIfNotExist(String clusterName, String role);

    /**
     * 根据Id查询角色
     * @param id
     * @return ESRoleClusterPO
     */
    RoleCluster getById(Long id);

    /**
     * 根据集群Id查询名下全部角色
     * @param clusterId
     * @return List<RoleCluster>
     */
    List<RoleCluster> getAllRoleClusterByClusterId(Integer clusterId);

    Map<Long, List<RoleCluster>> getAllRoleClusterByClusterIds(List<Integer> clusterIds);

    /**
     * 根据集群Id和roleClusterRole查询角色
     * @param clusterId
     * @return ESRoleClusterPO
     */
    RoleCluster getByClusterIdAndClusterRole(Long clusterId, String roleClusterName);

    /**
     * 根据集群Id和role查询角色
     * @param clusterId
     * @return RoleCluster
     */
    RoleCluster getByClusterIdAndRole(Long clusterId, String role);

    /**
     * 根据集群Id和role查询角色
     * @param clusterName 集群名
     * @return RoleCluster
     */
    RoleCluster getByClusterNameAndRole(String clusterName, String role);

    /**
     * 修改集群角色的pod数量
     * @param roleCluster
     * @return Result
     */
    Result<Void> updatePodByClusterIdAndRole(RoleCluster roleCluster);

    /**
     * 修改集群角色的es版本
     * @param clusterId
     * @param role
     * @param version
     * @return
     */
    Result<Void> updateVersionByClusterIdAndRole(Long clusterId, String role, String version);

    /**
     * 删除集群角色
     * @param clusterId
     * @return
     */
    Result<Void> deleteRoleClusterByClusterId(Integer clusterId);

    /**
     * 根据集群的id和集群的角色删除对应的角色
     * @param clusterId  物理集群id
     * @param role 节点角色名称
     * @return
     */
    Result deleteRoleClusterByClusterIdAndRole(Long clusterId, String role);
}

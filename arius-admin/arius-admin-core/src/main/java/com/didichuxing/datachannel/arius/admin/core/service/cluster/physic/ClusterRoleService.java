package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;

import java.util.List;
import java.util.Map;

/**
 * ES集群角色 服务类
 * @author chengxiang
 * @date 2022/5/9
 */
public interface ClusterRoleService {

    /**
     * 保存集群角色
     * @param esClusterRoleDTO
     * @return id
     */
    Result<Void> save(ESClusterRoleDTO esClusterRoleDTO);

    /**
     * 角色不存在则创建，存在则返回已存在的角色
     * @param clusterName 集群名
     * @param role        角色, ESClusterNodeRoleEnum的desc
     * @return
     */
    ClusterRoleInfo createRoleClusterIfNotExist(String clusterName, String role);


    /**
     * 根据集群Id查询名下全部角色
     * @param clusterId
     * @return List<RoleCluster>
     */
    List<ClusterRoleInfo> getAllRoleClusterByClusterId(Integer clusterId);

    Map<Long, List<ClusterRoleInfo>> getAllRoleClusterByClusterIds(List<Integer> clusterIds);

    /**
     * 根据集群Id和roleClusterRole查询角色
     * @param clusterId
     * @return ESRoleClusterPO
     */
    ClusterRoleInfo getByClusterIdAndClusterRole(Long clusterId, String roleClusterName);

    /**
     * 根据集群Id和role查询角色
     * @param clusterId
     * @return RoleCluster
     */
    ClusterRoleInfo getByClusterIdAndRole(Long clusterId, String role);

    /**
     * 根据集群Id和role查询角色
     * @param clusterName 集群名
     * @return RoleCluster
     */
    ClusterRoleInfo getByClusterNameAndRole(String clusterName, String role);

    /**
     * 修改集群角色的pod数量
     * @param clusterRoleInfo
     * @return Result
     */
    Result<Void> updatePodByClusterIdAndRole(ClusterRoleInfo clusterRoleInfo);

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
     *
     * @param clusterId
     * @param projectId
     * @return
     */
    Result<Void> deleteRoleClusterByClusterId(Integer clusterId, Integer projectId);

}
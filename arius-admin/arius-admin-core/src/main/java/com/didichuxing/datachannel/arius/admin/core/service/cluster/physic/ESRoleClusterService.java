package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;

/**
 * ES集群角色 服务类
 * @author didi
 * @since 2020-08-24
 */
public interface ESRoleClusterService {

    /**
     * 保存集群角色
     * @param esRoleClusterDTO
     * @return id
     */
    Result save(ESRoleClusterDTO esRoleClusterDTO);

    /**
     * 角色不存在则创建，存在则返回已存在的角色
     * @param clusterName 集群名
     * @param role        角色, ESClusterNodeRoleEnum的desc
     * @return
     */
    ESRoleCluster createRoleClusterIfNotExist(String clusterName, String role);

    /**
     * 根据Id查询角色
     * @param id
     * @return ESRoleClusterPO
     */
    ESRoleCluster getById(Long id);

    /**
     * 根据集群Id查询名下全部角色
     * @param clusterId
     * @return List<ESRoleCluster>
     */
    List<ESRoleCluster> getAllRoleClusterByClusterId(Integer clusterId);

    /**
     * 根据集群Id和roleClusterRole查询角色
     * @param clusterId
     * @return ESRoleClusterPO
     */
    ESRoleCluster getByClusterIdAndClusterRole(Long clusterId, String roleClusterName);

    /**
     * 根据集群Id和role查询角色
     * @param clusterId
     * @return ESRoleCluster
     */
    ESRoleCluster getByClusterIdAndRole(Long clusterId, String role);

    /**
     * 根据集群Id和role查询角色
     * @param clusterName 集群名
     * @return ESRoleCluster
     */
    ESRoleCluster getByClusterNameAndRole(String clusterName, String role);

    /**
     * 修改集群角色的pod数量
     * @param esRoleCluster
     * @return Result
     */
    Result updatePodByClusterIdAndRole(ESRoleCluster esRoleCluster);

    /**
     * 修改集群角色的es版本
     * @param clusterId
     * @param role
     * @param version
     * @return
     */
    Result updateVersionByClusterIdAndRole(Long clusterId, String role, String version);

    /**
     * 删除集群角色
     * @param clusterId
     * @return
     */
    Result deleteRoleClusterByClusterId(Integer clusterId);
}

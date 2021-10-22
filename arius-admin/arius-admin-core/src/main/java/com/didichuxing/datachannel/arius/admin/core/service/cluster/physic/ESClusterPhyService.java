package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import java.util.List;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ESPlugin;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhyDiscover;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

/**
 * @author d06679
 * @date 2019/3/20
 */
public interface ESClusterPhyService {

    /**
     * 条件查询物理集群
     * @param params 条件
     * @return 集群列表
     *
     */
    List<ESClusterPhy> listClustersByCondt(ESClusterDTO params);

    /**
     * 删除物理集群
     * @param clusterId 集群id
     * @param operator 操作人
     * @return 成功 true 失败 false
     */
    Result deleteClusterById(Integer clusterId, String operator);

    /**
     * 新建物理集群
     * @param param 集群信息
     * @param operator 操作人
     * @return 成功 true 失败 false
     */
    Result createCluster(ESClusterDTO param, String operator);

    /**
     * 编辑物理集群信息
     * @param param 物理集群信息
     * @param operator 操作人
     * @return 成功 true 失败 false
     *
     */
    Result editCluster(ESClusterDTO param, String operator);

    /**
     * 根据集群名字查询集群
     * @param clusterName 集群名字
     * @return 集群
     */
    ESClusterPhy getClusterByName(String clusterName);

    /**
     * 列出所有集群
     * @return 集群列表,如果没有返回空列表
     */
    List<ESClusterPhy> listAllClusters();

    /**
     * 集群是否存在
     *
     * @param clusterName 集群名字
     * @return true 存在
     */
    boolean isClusterExists(String clusterName);

    /**
     * 集群是否是高版本
     * @param clusterName 集群名字
     * @return true-是高版本，false-不是高版本
     */
    boolean isHighVersionCluster(String clusterName);

    /**
     * rack是否存在
     *
     * @param cluster 集群名字
     * @param racks   rack名字  支持逗号间隔
     * @return true 存在
     */
    default boolean isRacksExists(String cluster, String racks) {
        return true;
    }

    /**
     * 获取集群全部的rack
     * @param cluster cluster
     * @return set
     */
    Set<String> getClusterRacks(String cluster);

    /**
     * 获取集群热存Rack列表
     * @param cluster 集群名称
     * @return
     */
    Set<String> listHotRacks(String cluster);

    /**
     * 获取冷存Rack列表
     * @param cluster 集群名称
     * @return
     */
    Set<String> listColdRacks(String cluster);

    /**
     * 获取集群插件列表
     * @param cluster 集群名称
     * @return
     */
    List<ESPlugin> listClusterPlugins(String cluster);

    /**
     * 获取物理集群Discover
     * @param cluster 集群名称
     * @return
     */
    List<ESClusterPhyDiscover> getClusterDiscovers(String cluster);

    /**
     * 查询指定集群
     * @param clusterId 集群id
     * @return 集群  不存在返回null
     */
    ESClusterPhy getClusterById(Integer clusterId);

    /**
     * 获取写节点的个数
     * @param cluster 集群
     * @return count
     */
    int getWriteClientCount(String cluster);

    /**
     * 确保集群配置了DCDR的远端集群地址，如果没有配置尝试配置
     * @param cluster 集群
     * @param remoteCluster 远端集群
     * @throws ESOperateException
     * @return
     */
    boolean ensureDcdrRemoteCluster(String cluster, String remoteCluster) throws ESOperateException;

    /**
     * 获取物理集群角色
     * @param clusterId  物理集群ID
     * @return 物理集群的角色列表
     */
    List<ESRoleCluster> listPhysicClusterRoles(Integer clusterId);

    /**
     * 获取集群的状态
     * @param phyClusterName 集群名
     * @return 集群状态
     */
    ClusterStatusEnum getEsStatus(String phyClusterName);
}

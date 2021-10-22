package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;

/**
 * ES集群节点 服务类
 *
 * @author didi
 * @since 2020-08-24
 */
public interface ESRoleClusterHostService {

    /**
     * 条件查询
     * @param condt 条件
     * @return 节点列表
     */
    List<ESRoleClusterHost> queryNodeByCondt(ESRoleClusterHostDTO condt);

    /**
     * 查询集群节点列表
     * @param cluster 集群
     * @return 节点列表 集群不存在返回空列表
     */
    List<ESRoleClusterHost> getNodesByCluster(String cluster);

    /**
     * 获取物理集群数据节点
     */
    List<ESRoleClusterHost> getDataNodesByCluster(String cluster);

    /**
     * 查询集群节点列表
     * @param cluster 集群
     * @return 节点列表 集群不存在返回空列表
     */
    List<ESRoleClusterHost> getOnlineNodesByCluster(String cluster);

    /**
     * 修改节点状态
     * @param param 参数
     * @param operator 操作人
     * @return 成功 true  失败  false
     */
    Result editNodeStatus(ESRoleClusterHostDTO param, String operator);

    /**
     * 修改节点参数
     * @param param 参数
     * @return 成功 true  失败  false
     */
    Result editNode(ESRoleClusterHostDTO param);

    /**
     * 采集集群节点配置信息到MySQL
     * @param cluster 集群名字
     * @return true/false
     * @throws AdminTaskException
     */
    boolean collectClusterNodeSettings(String cluster) throws AdminTaskException;

    /**
     * 获取指定rack的索引个数
     * @param cluster 集群
     * @param racks node
     * @return 个数
     */
    int getIndicesCount(String cluster, String racks);

    /**
     * 获取所有在线的节点
     * @return list
     */
    List<ESRoleClusterHost> listOnlineNode();

    /**
     * 保存节点信息
     * @param roleClusterHost
     * @return
     */
    Result<Long> save(ESRoleClusterHost roleClusterHost);

    /**
     * 获取节点信息
     * @param id 主键
     * @return 节点对象
     */
    ESRoleClusterHost getById(Long id);

    /**
     * 获取节点信息
     * @param roleClusterId 角色集群id
     * @return 节点对象
     */
    List<ESRoleClusterHost> getByRoleClusterId(Long roleClusterId);

    /**
     * 获取节点名
     * @param clusterId 集群id
     * @param role      角色
     * @return
     */
    List<String> getHostNamesByRoleAndClusterId(Long clusterId, String role);

    /**
     * 获取节点信息
     * @param clusterId 集群id
     * @param role      角色
     * @return
     */
    List<ESRoleClusterHost> getByRoleAndClusterId(Long clusterId, String role);

    /**
     * 逻辑删除节点
     * @param cluster  集群名称
     * @return
     */
    Result deleteByCluster(String cluster);

    /**
     * 获取所有节点(包括不在线)
     * @return
     */
    List<ESRoleClusterHost> listAllNode();

    /**
     * 获取指定集群指定racks包含的节点
     * @param clusterName 物理集群名
     * @param racks racks
     * @return
     */
    List<ESRoleClusterHost> listRacksNodes(String clusterName, String racks);

    /**
     * 删除
     * @param id
     * @return
     */
    Result deleteById(Long id);

    /**
     * 获取ClusterHost节点信息
     * @param hostName
     * @return
     */
    ESRoleClusterHost getByHostName(String hostName);

    /**
     * 获取已删除节点
     * @param hostname
     */
    ESRoleClusterHost getDeleteHostByHostNameAnRoleId(String hostname, Long roleId);

    /**恢复节点状态
     * @param deleteHost
     * @return
     */
    Result setHostValid(ESRoleClusterHost deleteHost);

}
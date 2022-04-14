package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import java.util.List;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.ESClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;

/**
 * ES集群节点 服务类
 *
 * @author didi
 * @since 2020-08-24
 */
public interface RoleClusterHostService {

    /**
     * 条件查询
     * @param condt 条件
     * @return 节点列表
     */
    List<RoleClusterHost> queryNodeByCondt(ESRoleClusterHostDTO condt);

    /**
     * 查询集群节点列表
     * @param cluster 集群
     * @return 节点列表 集群不存在返回空列表
     */
    List<RoleClusterHost> getNodesByCluster(String cluster);

    /**
     * 查询集群节点列表
     * @param cluster 集群
     * @return 节点列表 集群不存在返回空列表
     */
    List<RoleClusterHost> getOnlineNodesByCluster(String cluster);

    /**
     * 修改节点状态
     * @param param 参数
     * @param operator 操作人
     * @return 成功 true  失败  false
     */
    Result<Void> editNodeStatus(ESRoleClusterHostDTO param, String operator);

    /**
     * 修改节点参数
     * @param param 参数
     * @return 成功 true  失败  false
     */
    Result<Void> editNode(ESRoleClusterHostDTO param);

    /**
     * 采集集群节点配置信息到MySQL, 包括节点状态
     * @param cluster 集群名字
     * @return true/false
     * @throws AdminTaskException
     */
    boolean collectClusterNodeSettings(String cluster) throws AdminTaskException;


    /**
     * 全量录入根据配置的节点信息持久化到MySQL, 包括节点状态
     * @param param 参数
     * @return true/false
     */
    boolean saveClusterNodeSettings(ClusterJoinDTO param) throws AdminTaskException;

    /**
     * 创建或扩容集群节点时录入节点数据到MySQL, 包括节点规格
     * @param param               节点角色列表
     * @param phyClusterName      物理集群名称
     * @throws AdminTaskException 事务异常
     * @return                    操作结果
     */
    boolean createClusterNodeSettings(List<ESClusterRoleHost> param, String phyClusterName) throws AdminTaskException;

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
    List<RoleClusterHost> listOnlineNode();

    /**
     * 保存节点信息
     * @param roleClusterHost
     * @return
     */
    Result<Long> save(RoleClusterHost roleClusterHost);

    /**
     * 获取节点信息
     * @param id 主键
     * @return 节点对象
     */
    RoleClusterHost getById(Long id);

    /**
     * 获取节点信息
     * @param roleClusterId 角色集群id
     * @return 节点对象
     */
    List<RoleClusterHost> getByRoleClusterId(Long roleClusterId);

    /**
     * 获取节点信息
     * @param roleClusterIds 多个角色集群id
     * @return  Map<角色集群id,List<节点对象>>
     */
    Map<Long,List<RoleClusterHost>> getByRoleClusterIds(List<Long> roleClusterIds);
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
    List<RoleClusterHost> getByRoleAndClusterId(Long clusterId, String role);

    /**
     * 逻辑删除节点
     * @param cluster  集群名称
     * @return
     */
    Result<Void> deleteByCluster(String cluster);

    /**
     * 获取所有节点(包括不在线)
     * @return
     */
    List<RoleClusterHost> listAllNode();

    /**
     * 获取指定集群指定racks包含的节点
     * @param clusterName 物理集群名
     * @param racks racks
     * @return
     */
    List<RoleClusterHost> listRacksNodes(String clusterName, String racks);

    /**
     * 删除
     * @param id
     * @return
     */
    Result<Void> deleteById(Long id);

    /**
     * 根据主机名称和节点的角色id删除对应的节点
     *
     * @param hostname 主机名称列表
     * @param roleId   角色id
     * @return
     */
    Result deleteByHostNameAndRoleId(List<String> hostname, Long roleId);

    /**
     * 获取ClusterHost节点信息
     * @param hostName
     * @return
     */
    RoleClusterHost getByHostName(String hostName);

    /**
     * 获取已删除节点
     * @param hostname
     */
    RoleClusterHost getDeleteHostByHostNameAnRoleId(String hostname, Long roleId);

    /**恢复节点状态
     * @param deleteHost
     * @return
     */
    Result<Void> setHostValid(RoleClusterHost deleteHost);

    /**
     * 构建集群client角色的HttpAddresses地址
     * @return ip:port,ip:port
     */
    String buildESClientHttpAddressesStr(List<ESRoleClusterHostDTO> roleClusterHosts);

    /**
     * 构建集群client, master角色的HttpAddresses地址
     * @return List<ip:port>
     */
    List<String> buildESClientMasterHttpAddressesList(List<ESRoleClusterHostDTO> roleClusterHosts);

    /**
     * 构建集群master,client,data角色的HttpAddresses地址
     * @return List<ip:port>
     */
    List<String> buildESAllRoleHttpAddressesList(List<ESRoleClusterHostDTO> roleClusterHosts);

    /**
     * 获取角色id对应的机器数（ip数目）
     */
    int getPodNumberByRoleId(Long roleId);
}
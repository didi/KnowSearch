package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import java.util.List;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleHostInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHostInfo;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;

/**
 * ES集群节点 服务类
 *
 * @author chengxiang
 * @date 2022/5/9
 */
public interface ClusterRoleHostInfoService {

    /**
     * 条件查询
     * @param condt 条件
     * @return 节点列表
     */
    List<ClusterRoleHostInfo> queryNodeByCondt(ESClusterRoleHostInfoDTO condt);

    /**
     * 查询集群节点列表
     * @param cluster 集群
     * @return 节点列表 集群不存在返回空列表
     */
    List<ClusterRoleHostInfo> getNodesByCluster(String cluster);

    /**
     * 查询集群节点列表
     * @param cluster 集群
     * @return 节点列表 集群不存在返回空列表
     */
    List<ClusterRoleHostInfo> getOnlineNodesByCluster(String cluster);

    /**
     * 修改节点状态
     * @param param 参数
     * @param operator 操作人
     * @return 成功 true  失败  false
     */
    Result<Void> editNodeStatus(ESClusterRoleHostInfoDTO param, String operator);

    /**
     * 修改节点参数
     * @param param 参数
     * @return 成功 true  失败  false
     */
    Result<Void> editNode(ESClusterRoleHostInfoDTO param);

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
    List<ClusterRoleHostInfo> listOnlineNode();

    /**
     * 保存节点信息
     * @param clusterRoleHostInfo
     * @return
     */
    Result<Long> save(ClusterRoleHostInfo clusterRoleHostInfo);

    /**
     * 获取节点信息
     * @param id 主键
     * @return 节点对象
     */
    ClusterRoleHostInfo getById(Long id);

    /**
     * 获取节点信息
     * @param roleClusterId 角色集群id
     * @return 节点对象
     */
    List<ClusterRoleHostInfo> getByRoleClusterId(Long roleClusterId);

    /**
     * 获取节点信息
     * @param roleClusterIds 多个角色集群id
     * @return  Map<角色集群id,List<节点对象>>
     */
    Map<Long,List<ClusterRoleHostInfo>> getByRoleClusterIds(List<Long> roleClusterIds);
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
    List<ClusterRoleHostInfo> getByRoleAndClusterId(Long clusterId, String role);

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
    List<ClusterRoleHostInfo> listAllNode();

    /**
     * 获取指定集群指定racks包含的节点
     * @param clusterName 物理集群名
     * @param racks racks
     * @return
     */
    List<ClusterRoleHostInfo> listRacksNodes(String clusterName, String racks);

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
    ClusterRoleHostInfo getByHostName(String hostName);

    /**
     * 获取已删除节点
     * @param hostname
     */
    ClusterRoleHostInfo getDeleteHostByHostNameAnRoleId(String hostname, Long roleId);

    /**恢复节点状态
     * @param deleteHost
     * @return
     */
    Result<Void> setHostValid(ClusterRoleHostInfo deleteHost);

    /**
     * 构建集群client角色的HttpAddresses地址
     * @return ip:port,ip:port
     */
    String buildESClientHttpAddressesStr(List<ESClusterRoleHostInfoDTO> roleClusterHosts);

    /**
     * 构建集群client, master角色的HttpAddresses地址
     * @return List<ip:port>
     */
    List<String> buildESClientMasterHttpAddressesList(List<ESClusterRoleHostInfoDTO> roleClusterHosts);

    /**
     * 构建集群master,client,data角色的HttpAddresses地址
     * @return List<ip:port>
     */
    List<String> buildESAllRoleHttpAddressesList(List<ESClusterRoleHostInfoDTO> roleClusterHosts);

    /**
     * 获取角色id对应的机器数（ip数目）
     */
    int getPodNumberByRoleId(Long roleId);
}
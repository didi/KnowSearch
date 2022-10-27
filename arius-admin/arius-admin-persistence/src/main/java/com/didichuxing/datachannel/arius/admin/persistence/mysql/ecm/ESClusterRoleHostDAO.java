package com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESClusterRoleHostPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * ES集群表对应各角色主机列表 Mapper 接口
 * @author chengxiang
 * @date 20222/4/22
 */
@Repository
public interface ESClusterRoleHostDAO {
    /**
     * 插入一条主机记录
     * @param param
     * @return
     */
    int insert(ESClusterRoleHostPO param);

    /**
     * 根据id 获取一条主机记录
     * @param id
     * @return
     */
    ESClusterRoleHostPO getById(Long id);

    /**
     * 根据roleId 获取主机名称列表
     * @param roleId
     * @return
     */
    List<String> listHostNamesByRoleId(String roleId);

    /**
     * 根据roleClusterId 获取主机信息列表
     * @param roleClusterId
     * @return
     */
    List<ESClusterRoleHostPO> listByRoleClusterId(Long roleClusterId);

    /**
     * 根据多条roleClusterId 获取主机信息列表
     * @param roleClusterIds
     * @return
     */
    List<ESClusterRoleHostPO> listByRoleClusterIds(List<Long> roleClusterIds);

    /**
     * 根据集群和nodeSet 信息查询符合条件的主机信息列表
     * @param cluster
     * @param nodeSets
     * @return
     */
    List<ESClusterRoleHostPO> listByClusterAndNodeSets(@Param("cluster") String cluster,
                                                       @Param("nodeSets") List<String> nodeSets);

    /**
     * 根据条件查询符合条件的主机信息列表
     * @param param
     * @return
     */
    List<ESClusterRoleHostPO> listByCondition(ESClusterRoleHostPO param);

    /**
     * 根据集群信息查询符合条件的主机信息列表
     * @param cluster
     * @return
     */
    List<ESClusterRoleHostPO> listByCluster(String cluster);

    /**
     * 更新主机信息
     * @param param
     * @return
     */
    int update(ESClusterRoleHostPO param);

    int updateRegionId(@Param("ids") List<Integer> ids, @Param("regionId") Integer regionId);

    /**
     * 将所有在线的节点置为离线,在同步集群节点时调用;需要在事务内调用,保证失败后回滚
     * @param cluster
     * @return
     */
    int offlineByCluster(String cluster);

    /**
     * 查询在线节点信息列表
     * @return
     */
    List<ESClusterRoleHostPO> listOnlineNode();

    /**
     * 根据集群删除主机信息
     * @param cluster
     * @return
     */
    int deleteByCluster(String cluster);

    /**
     * 根据主机名和roleId 重置主机状态
     * @param hostname
     * @param roleId
     * @return
     */
    int restoreByHostNameAndRoleId(@Param("hostname") String hostname, @Param("roleId") Long roleId);

    /**
     * 获取所有节点信息
     * @return       List<ESClusterRoleHostPO>
     */
    List<ESClusterRoleHostPO> listAll();

    /**
     * 获取指定角色节点信息
     * @return           List<ESClusterRoleHostPO>
     */
    List<ESClusterRoleHostPO> listAllByRoleCode(Integer roleCode);

    /**
     * 根据主机名获取主机信息
     * @param hostName
     * @return
     */
    ESClusterRoleHostPO getByHostName(String hostName);

    /**
     * 根据id 删除主机信息
     * @param id
     * @return
     */
    int delete(Long id);
    int deleteByIds(@Param("ids")List<Integer> ids);

    /**
     * 根据主机名和roleId 删除主机信息
     * @param hostnames
     * @param roleId
     * @return
     */
    int deleteByHostNameAndRoleId(@Param("hostNames") List<String> hostnames, @Param("roleId") Long roleId);

    /**
     * 根据主机名和roleId 获取已删除的主机信息
     * @param hostname
     * @param roleId
     * @return
     */
    ESClusterRoleHostPO getDeleteHostByHostNameAnRoleId(@Param("hostname") String hostname,
                                                        @Param("roleId") Long roleId);

    /**
     * 更新主机状态，并将状态置为有效
     * @param param
     * @return
     */
    int updateHostValid(ClusterRoleHost param);

    /**
     *  根据主机名和roleId 获取主机数量
     * @param roleId
     * @return
     */
    int getPodNumberByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据regionId获取节点列表
     * @param regionId  regionId
     * @return   List<ESClusterRoleHostPO>
     */
    List<ESClusterRoleHostPO> listByRegionId(Integer regionId);

    /**
     * 根据集群名称获取region节点信息,
     * @param cluster  集群名称
     * @return         List<ESClusterRoleHostPO>
     */
    List<ESClusterRoleHostPO> listDataNodeByCluster(String cluster);
    
    /**
     * 它返回 ClusterRoleHost 对象的列表。
     *
     * @param ids 要查询的id列表。
     * @return 列表<ClusterRoleHost>
     */
    List<ESClusterRoleHostPO> listByIds(@Param("ids") List<Integer> ids);

    /**
     * 物理集群下的节点
     * @param phyClusterNames
     * @return
     */
    List<ESClusterRoleHostPO> listByClusters(List<String> phyClusterNames);
}
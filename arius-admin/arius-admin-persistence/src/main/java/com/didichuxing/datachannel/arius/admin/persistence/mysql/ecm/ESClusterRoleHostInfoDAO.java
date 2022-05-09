package com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHostInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESClusterRoleHostInfoPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * ES集群表对应各角色主机列表 Mapper 接口
 * @author chengxiang
 * @date 20222/4/22
 */
@Repository
public interface ESClusterRoleHostInfoDAO {
    /**
     * 插入一条主机记录
     * @param param
     * @return
     */
    int insert(ESClusterRoleHostInfoPO param);

    /**
     * 根据id 获取一条主机记录
     * @param id
     * @return
     */
    ESClusterRoleHostInfoPO getById(Long id);

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
    List<ESClusterRoleHostInfoPO> listByRoleClusterId(Long roleClusterId);

    /**
     * 根据多条roleClusterId 获取主机信息列表
     * @param roleClusterIds
     * @return
     */
    List<ESClusterRoleHostInfoPO> listByRoleClusterIds(List<Long> roleClusterIds);

    /**
     * 根据条件查询符合条件的主机信息列表
     * @param param
     * @return
     */
    List<ESClusterRoleHostInfoPO> listByCondition(ESClusterRoleHostInfoPO param);

    /**
     * 根据集群和rack 信息查询符合条件的主机信息列表
     * @param cluster
     * @param rack
     * @return
     */
    List<ESClusterRoleHostInfoPO> listByClusterAndRack(@Param("cluster") String cluster,
                                                       @Param("rack") String rack);

    /**
     * 根据集群信息查询符合条件的主机信息列表
     * @param cluster
     * @return
     */
    List<ESClusterRoleHostInfoPO> listByCluster(String cluster);

    /**
     * 更新主机信息
     * @param param
     * @return
     */
    int update(ESClusterRoleHostInfoPO param);

    /**
     * 根据主机信息批量插入记录
     * @param params
     * @return
     */
    int insertBatch(List<ESClusterRoleHostInfoPO> params);

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
    List<ESClusterRoleHostInfoPO> listOnlineNode();

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
    int restoreByHostNameAndRoleId(@Param("hostname") String hostname,
                                  @Param("roleId") Long roleId);

    /**
     * 获取所有主机信息
     * @return
     */
    List<ESClusterRoleHostInfoPO> listAll();

    /**
     * 根据主机名获取主机信息
     * @param hostName
     * @return
     */
    ESClusterRoleHostInfoPO getByHostName(String hostName);

    /**
     * 根据id 删除主机信息
     * @param id
     * @return
     */
    int delete(Long id);

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
    ESClusterRoleHostInfoPO getDeleteHostByHostNameAnRoleId(@Param("hostname") String hostname,
                                                            @Param("roleId") Long roleId);

    /**
     * 更新主机状态，并将状态置为有效
     * @param param
     * @return
     */
    int updateHostValid(ClusterRoleHostInfo param);

    /**
     *  根据主机名和roleId 获取主机数量
     * @param roleId
     * @return
     */
    int getPodNumberByRoleId(@Param("roleId") Long roleId);
}

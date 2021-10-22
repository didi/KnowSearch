package com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESRoleClusterHostPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ES集群表对应各角色主机列表 Mapper 接口
 * @author didi
 * @since 2020-08-24
 */
@Repository
public interface ESRoleClusterHostDAO {
    int insert(ESRoleClusterHostPO param);

    ESRoleClusterHostPO getById(Long id);

    List<String> listHostNamesByRoleId(String roleId);

    List<String> listHostNamesByRoleAndclusterId(@Param("clusterId") Long clusterId,
                                                 @Param("role") String role);

    List<ESRoleClusterHostPO> listByRoleAndClusterId(@Param("clusterId") Long clusterId,
                                                     @Param("role") String role);

    List<ESRoleClusterHostPO> listByRoleClusterId(Long roleClusterId);

    List<ESRoleClusterHostPO> listByCondition(ESRoleClusterHostPO param);

    List<ESRoleClusterHostPO> listByClusterAndRack(@Param("cluster") String cluster,
                                                   @Param("rack") String rack);

    List<ESRoleClusterHostPO> listByCluster(String cluster);

    int update(ESRoleClusterHostPO param);

    int insertBatch(List<ESRoleClusterHostPO> params);

    /**
     * 将所有在线的节点置为离线,在同步集群节点时调用;需要在事务内调用,保证失败后回滚
     */
    int offlineByCluster(String cluster);

    List<ESRoleClusterHostPO> listOnlineNode();

    int deleteByCluster(String cluster);

    List<ESRoleClusterHostPO> listAll();

    ESRoleClusterHostPO getByHostName(String hostName);

    int delete(Long id);

    ESRoleClusterHostPO getDeleteHostByHostNameAnRoleId(@Param("hostname") String hostname,
                                                        @Param("roleId") Long roleId);

    int updateHostValid(ESRoleClusterHost param);
}

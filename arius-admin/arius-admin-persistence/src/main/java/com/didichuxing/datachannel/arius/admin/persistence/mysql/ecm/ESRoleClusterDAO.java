package com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm;


import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESRoleClusterPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 集群角色DAO
 * @author  didi
 * @since 2020-08-24
 */
@Repository
public interface ESRoleClusterDAO {

    int insert(ESRoleClusterPO param);

    ESRoleClusterPO getById(Long id);

    ESRoleClusterPO getByClusterIdAndClusterRole(@Param("clusterId") Long clusterId,
                                                 @Param("roleClusterName")String roleClusterName);

    List<ESRoleClusterPO> listByClusterId(String clusterId);

    int update(ESRoleClusterPO param);

    int updateVersionByClusterIdAndRole(@Param("elasticClusterId") Long elasticClusterId,
                                        @Param("role")String role,
                                        @Param("esVersion")String esVersion);

    ESRoleClusterPO getByClusterIdAndRole(@Param("clusterId") Long clusterId,
                                          @Param("role")String role);

    int delete(Integer clusterId);

    int deleteRoleClusterByCluterIdAndRole(@Param("clusterId") Long clusterId,
                                           @Param("role")String role);

}

package com.didichuxing.datachannel.arius.admin.persistence.mysql.resource;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;

@Repository
public interface ClusterDAO {

    List<ClusterPO> listByCondition(ClusterPO param);

    List<ClusterPO> pagingByCondition(@Param("cluster") String cluster,     @Param("health") Integer health,
                                      @Param("esVersion") String esVersion, @Param("from") Long from,
                                      @Param("size") Long size);

    long getTotalHitByCondition(ClusterPO param);

    int insert(ClusterPO param);

    int update(ClusterPO param);

    int updatePluginIdsById(@Param("plugIds") String plugIds, @Param("clusterId") Integer clusterId);

    int delete(Integer clusterId);

    ClusterPO getById(Integer clusterId);

    ClusterPO getByName(String clusterName);

    List<ClusterPO> listAll();

    List<ClusterPO> listByIds(@Param("ids") Set<Long> ids);
}

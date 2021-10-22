package com.didichuxing.datachannel.arius.admin.persistence.mysql.resource;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;

/**
 * @author d06679
 * @date 2019/3/20
 */
@Repository
public interface ClusterDAO {

    List<ClusterPO> listByCondition(ClusterPO param);

    int insert(ClusterPO param);

    int update(ClusterPO param);

    int delete(Integer clusterId);

    ClusterPO getById(Integer clusterId);

    ClusterPO getByName(String clusterName);

    List<ClusterPO>  listAll();
}

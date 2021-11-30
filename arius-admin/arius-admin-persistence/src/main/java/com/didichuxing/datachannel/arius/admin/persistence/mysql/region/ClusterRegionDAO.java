package com.didichuxing.datachannel.arius.admin.persistence.mysql.region;

import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterRegionPO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wangshu
 * @date 2020/09/15
 */
@Repository
public interface ClusterRegionDAO {

    ClusterRegionPO getById(Long regionId);

    List<ClusterRegionPO> listByLogicClusterId(Long logicClusterId);

    List<ClusterRegionPO> getByPhyClusterName(String phyClusterName);

    int insert(ClusterRegionPO param);

    int update(ClusterRegionPO param);

    int delete(Long regionId);

    int deleteByClusterPhyName(String clusterPhyName);

    List<ClusterRegionPO> listAll();

    List<ClusterRegionPO> listBoundRegions();

    /**
     * 条件查询
     * @param condt 查询参数，仅仅id，logicClusterId，phyClusterName有效
     * @return
     */
    List<ClusterRegionPO> listBoundRegionsByCondition(ClusterRegionPO condt);

}

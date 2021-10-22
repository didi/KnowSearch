package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.LogicClusterRackVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.PhyClusterRackVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/7
 * @Comment:
 */
public interface ClusterRegionManager {

    /**
     * 构建regionVO
     * @param regions region列表
     * @return
     */
    List<ClusterRegionVO> buildLogicClusterRegionVO(List<ClusterRegion> regions);

    /**
     * 构建regionVO
     * @param region region
     * @return
     */
    ClusterRegionVO buildLogicClusterRegionVO(ClusterRegion region);

    /**
     * 构建物理集群Racks
     * @param cluster       物理集群名
     * @param clusterRacks  物理集群的所有Racks
     * @param usedRacksInfo 物理已经被使用（绑定成region）的rack信息
     * @return
     */
    List<PhyClusterRackVO> buildPhyClusterRackVOs(String cluster, Set<String> clusterRacks,
                                                  List<ESClusterLogicRackInfo> usedRacksInfo);

    /**
     * 构建逻辑集群物RackVO
     * @param logicClusterRackInfos 逻辑集群rack信息
     * @return
     */
    List<LogicClusterRackVO> buildLogicClusterRackVOs(List<ESClusterLogicRackInfo> logicClusterRackInfos);

    /**
     * 逻辑集群批量绑定region
     * @param isAddClusterLogicFlag 是否要添加逻辑集群
     */
    Result<Long> batchBindRegionToClusterLogic(ESLogicClusterWithRegionDTO param, String operator,
                                               boolean isAddClusterLogicFlag);
}

package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.LogicClusterRackVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.PhyClusterRackVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
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
     * 获取物理集群可划分至region的Racks信息
     * @param cluster       物理集群名
     * @return
     */
    List<PhyClusterRackVO> buildCanDividePhyClusterRackVOs(String cluster);

    /**
     * 构建逻辑集群物RackVO
     * @param logicClusterRackInfos 逻辑集群rack信息
     * @return
     */
    List<LogicClusterRackVO> buildLogicClusterRackVOs(List<ClusterLogicRackInfo> logicClusterRackInfos);

    /**
     * 逻辑集群批量绑定region
     * @param isAddClusterLogicFlag 是否要添加逻辑集群
     */
    Result<Void> batchBindRegionToClusterLogic(ESLogicClusterWithRegionDTO param, String operator,
                                               boolean isAddClusterLogicFlag);

    /**
     * 解绑region
     * @param regionId regionId
     * @param operator operator
     * @return
     */
    Result<Void> unbindRegion(Long regionId, String operator);

    /**
     * 绑定region到逻辑集群
     * @param regionId       regionId
     * @param logicClusterId 逻辑集群ID
     * @param share          share
     * @param operator       操作人
     * @return
     */
    Result<Void> bindRegion(Long regionId, Long logicClusterId, Integer share, String operator);
}

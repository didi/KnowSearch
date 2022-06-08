package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostWithRegionInfoVO;

/**
 * @author ohushenglin_v
 * @date 2022-05-30
 */
public interface ClusterNodeManager {
    /**
     * 获取可划分至region的节点信息
     * @param clusterId   物理集群Id
     * @return            Result<List<ESClusterRoleHostVO>>
     */
    Result<List<ESClusterRoleHostWithRegionInfoVO>> listDivide2ClusterNodeInfo(Long clusterId);

    /**
     * 划分指定节点至region
     * @param operator          操作者
     * @param params             集群带节点信息的Region实体
     * @return                  Result<Long>
     */
    Result<List<Long>> createMultiNode2Region(List<ClusterRegionWithNodeInfoDTO> params, String operator);

    /**
     * 编辑节点的region属性
     * @param operator          操作者
     * @param params             集群带节点信息的Region实体
     * @return                  Result<Boolean>
     */
    Result<Boolean> editMultiNode2Region(List<ClusterRegionWithNodeInfoDTO> params, String operator);

    /**
     * 获取物理集群节点列表
     *
     * @param clusterId 集群id
     * @return {@link Result}<{@link List}<{@link ESClusterRoleHostVO}>>
     */
    Result<List<ESClusterRoleHostVO>> listClusterPhyNode(Integer clusterId);
}

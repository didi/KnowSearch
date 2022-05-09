package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.Collection;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.RackMetaMetric;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHostInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESRoleClusterHostVO;

/**
 * ES集群工具类
 *
 * @author wangshu
 * @date 2020/09/10
 */
public interface ClusterNodeManager {
    /**
     * 逻辑集群节点转换
     *
     * @param clusterNodes       物理集群节点
     * @return
     */
    List<ESRoleClusterHostVO> convertClusterLogicNodes(List<ClusterRoleHostInfo> clusterNodes);

    /**
     * 获取rack的资源统计信息
     *
     * @param clusterName 集群名字
     * @param racks       racks
     * @return list
     */
    Result<List<RackMetaMetric>> metaAndMetric(String clusterName, Collection<String> racks);

    /**
     * 获取rack的元信息
     *
     * @param clusterName 集群名字
     * @param racks       rack
     * @return list
     */
    Result<List<RackMetaMetric>> meta(String clusterName, Collection<String> racks);

    /**
     * 物理集群节点转换
     * @param nodesInfo
     * @param clusterPhyName 物理集群名称
     * @return
     */
    List<ESRoleClusterHostVO> convertClusterPhyNodes(List<ClusterRoleHostInfo> nodesInfo, String clusterPhyName);
}

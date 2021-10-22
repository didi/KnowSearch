package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhyContext;

/**
 * Created by linyunan on 2021-06-08
 */
public interface ClusterContextManager {
    /**
     * 初始化校验模型, 模型中的数据使用为两个map作为存储容器
     */
    void flushClusterContext();

    /**
     * 根据逻辑集群Id 获取逻辑集群关联物理集群的数量信息
     */
    ESClusterLogicContext getESClusterLogicContext(Long clusterLogicId);

    /**
     * 根据物理集群Id 获取物理集群关联逻辑集群的数量信息
     */
    ESClusterPhyContext getESClusterPhyContext(String clusterPhyName);

    /**
     * 校验逻辑集群是可否关联物理集群, 不同类型的逻辑集群, 校验规则不一样
     */
    Result canClusterLogicAssociatedPhyCluster(Long clusterLogicId, String clusterPhyName, Integer clusterLogicType);

    /**
     * 获取可关联的物理集群名称列表
     */
    List<String> getCanBeAssociatedClustersPhys(Integer clusterLogicType);

    /**
     * 获取集群关联逻辑集群名称列表
     */
    List<String> getClusterPhyAssociatedClusterLogicNames(String clusterPhyName);
}

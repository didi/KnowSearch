package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhyContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;

/**
 * Created by linyunan on 2021-06-08
 */
public interface ClusterContextManager {
    /**
     * 刷新物理集群上下文
     * @param clusterPhyName  物理集群名称
     */
    ClusterPhyContext flushClusterPhyContext(String clusterPhyName);

    /**
     * 刷新逻辑集群上下文
     * @param clusterLogicId   逻辑集群Id
     */
    ClusterLogicContext flushClusterLogicContext(Long clusterLogicId);

    /**
     * 根据region信息更新集群上下文
     * @param clusterRegion
     */
    void flushClusterContextByClusterRegion(ClusterRegion clusterRegion);

    /**
     * 校验逻辑集群是可否关联物理集群, 不同类型的逻辑集群, 校验规则不一样
     */
    Result<Boolean> canClusterLogicAssociatedPhyCluster(Long clusterLogicId, String clusterPhyName, Long regionId,
                                                        Integer clusterLogicType);

    /**
     * 获取可关联的物理集群名称列表, 针对新建逻辑集群、逻辑集群关联region等操作
     */
    Result<List<String>> getCanBeAssociatedClustersPhys(Integer clusterLogicType, Long clusterLogicId);

    /**
     * 获取集群关联逻辑集群名称列表
     */
    List<String> getClusterPhyAssociatedClusterLogicNames(String clusterPhyName);

    /**
     * 构建物理集群上下文
     * @param cluster
     * @return
     */
    ClusterPhyContext getClusterPhyContext(String cluster);

    ClusterPhyContext getClusterPhyContextCache(String cluster);

    /**
     * @return key-> 物理集群名称, value 上下文信息
     */
    Map<String, ClusterPhyContext> listClusterPhyContextMap();

    /**
     * 从缓存中获取逻辑集群上下文
     *
     * @param clusterLogicId
     * @return
     */
    ClusterLogicContext getClusterLogicContextCache(Long clusterLogicId);

    /**
     * 构建逻辑集群上下文
     * @param clusterLogicId
     * @return
     */
    ClusterLogicContext getClusterLogicContext(Long clusterLogicId);
}

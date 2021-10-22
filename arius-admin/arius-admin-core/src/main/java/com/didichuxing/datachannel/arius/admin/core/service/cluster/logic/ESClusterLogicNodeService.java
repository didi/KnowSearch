package com.didichuxing.datachannel.arius.admin.core.service.cluster.logic;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;

import java.util.List;

/**
 * 逻辑集群节点服务
 * @author wangshu
 * @date 2020/09/10
 */
public interface ESClusterLogicNodeService {
    /**
     * 获取逻辑集群所有节点列表
     * @param clusterId 逻辑集群ID
     * @return
     */
    List<ESRoleClusterHost> getLogicClusterNodes(Long clusterId);

    /**
     * 获取逻辑集群所有节点包括对应物理集群非数据节点
     * @param clusterId 逻辑集群ID
     * @return
     */
    List<ESRoleClusterHost> getLogicClusterNodesIncludeNonDataNodes(Long clusterId);
}

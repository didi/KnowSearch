package com.didichuxing.datachannel.arius.admin.core.service.gateway;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.gateway.GatewayNode;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;

/**
 * @author d06679
 * @date 2019-07-26
 */
public interface GatewayManageService {

    /**
     * gateway节点提交心跳
     * @param heartbeat 心跳
     * @return result
     */
    Result heartbeat(GatewayHeartbeat heartbeat);

    /**
     * 计算当前存活的节点数目
     * @param clusterName 集群
     * @param gapTime  时间
     * @return count
     */
    Result<Integer> aliveCount(String clusterName, long gapTime);

    /**
     * 重新加载集群
     */
    void reloadClusterName();

    /**
     * 获取集群存活的节点列表
     * @param clusterName 集群名字
     * @param timeout  存活超时时间
     * @return list
     */
    List<GatewayNode> getAliveNode(String clusterName, long timeout);
}

package com.didichuxing.datachannel.arius.admin.core.service.gateway;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.gateway.GatewayNode;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;

public interface GatewayService {

    /**
     * gateway节点提交心跳
     * @param heartbeat 心跳
     * @return result
     */
    Result<Void> heartbeat(GatewayHeartbeat heartbeat);

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

    /**
     * sql语句直接操作,可以进行sql语句的直接查询,也可以进行sql到dsl语句的转换
     *
     * @param sql     sql查询语句
     * @param appId   项目id
     * @param postFix sql语句操作后缀
     * @return 数据查询结果
     */
    Result<String> sqlOperate(String sql, Integer appId, String postFix);
}

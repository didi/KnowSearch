package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.didichuxing.datachannel.arius.admin.common.constant.routing.ESRoutingConstant.*;

/**
 * Created by linyunan on 3/11/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardStats extends BaseESPO {
    /**
     * 统计的时间戳，单位：毫秒
     */
    private Long                          timestamp;
    /**
     * 是否物理集群 0 物理集群 1 逻辑集群
     */
    private Integer                       physicCluster;

    /**
     * dashboard集群信息
     */
    private ClusterMetrics                cluster;

    /**
     * dashboard节点信息
     */
    private NodeMetrics                   node;

    /**
     * dashboard模板信息
     */
    private TemplateMetrics               template;

    /**
     * dashboard索引信息
     */
    private IndexMetrics                  index;

    /**
     * dashboard节点线程queue信息
     */
    private ClusterThreadPoolQueueMetrics clusterThreadPoolQueue;

    /**
     * dashboard状态信息
     */
    private ClusterPhyHealthMetrics       clusterPhyHealth;

    @Override
    public String getKey() {
        if (null != cluster) {
            return String.format("cluster:%s@%d", cluster.getCluster(), cluster.getTimestamp());
        }

        if (null != node) {
            return String.format("%s@node:%s@%d", node.getCluster(), node.getNode(), node.getTimestamp());
        }

        if (null != template) {
            return String.format("%s@template:%s@%d", template.getCluster(), template.getTemplate(),
                template.getTimestamp());
        }

        if (null != index) {
            return String.format("%s@index:%s@%d", index.getCluster(), index.getIndex(), index.getTimestamp());
        }

        if (null != clusterThreadPoolQueue) {
            return String.format("%s@%s@%d", clusterThreadPoolQueue.getCluster(), THREAD_POOL_ROUTING,
                clusterThreadPoolQueue.getTimestamp());
        }

        if (null != clusterPhyHealth) {
            return String.format("%s@%d", CLUSTER_PHY_HEALTH_ROUTING, timestamp);
        }

        return String.format("%s@%d", CLUSTER_PHY_ROUTING, timestamp);
    }

    @Override
    public String getRoutingValue() {
        if (null != cluster) {
            return String.format("cluster:%s", cluster.getCluster());
        }

        if (null != node) {
            return String.format("%s@node:%s", node.getCluster(), node.getNode());
        }

        if (null != template) {
            return String.format("%s@template:%s", template.getCluster(), template.getTemplate());
        }

        if (null != index) {
            return String.format("%s@index:%s", index.getCluster(), index.getIndex());
        }

        if (null != clusterThreadPoolQueue) {
            return String.format("%s@%s", clusterThreadPoolQueue.getCluster(), THREAD_POOL_ROUTING);
        }

        if (null != clusterPhyHealth) {
            return CLUSTER_PHY_HEALTH_ROUTING;
        }

        return CLUSTER_PHY_ROUTING;
    }
}

package com.didichuxing.datachannel.arius.admin.core.service.es;

import java.util.List;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.BigIndexMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.BigShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.MovingShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.PendingTask;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;

/**
 * Created by linyunan on 2021-08-09
 */
public interface ESClusterNodeService {

    /**
     * 一次获取ES集群的所有节点Fs信息
     * @param clusterName 集群名称
     * @return 集群节点stats，key-节点ID，value-节点stats
     */
    Map<String, ClusterNodeStats> syncGetNodeFsStatsMap(String clusterName);

    /**
     * 获取ES集群节点ip列表
     */
    List<String> syncGetNodeHosts(String clusterName);

    /**
     * 获取ES集群节点
     */
    Map<String, ClusterNodeInfo> syncGetNodeInfo(String clusterName);

    /**
     * 获取ES集群节点名称列表
     */
    List<String> syncGetNodeNames(String clusterName);

    /**
     * 获取ES集群PendingTask
     */
    List<PendingTask> syncGetPendingTask(String clusterName);

    /**
     * 获取ES集群movingShard信息
     */
    List<MovingShardMetrics> syncGetMovingShards(String clusterName);

    /**
     * 获取ES集群大Shard信息
     */
    List<BigShardMetrics> syncGetBigShards(String clusterName);

    /**
     * 获取ES集群大索引(大于10亿文档数)信息
     */
    List<BigIndexMetrics> syncGetBigIndices(String clusterName);

    /**
     * 获取ES集群某个节点上的索引个数
     * @param nodes 主机名或IP集合字符串 , 用逗号分隔  
     */
    int syncGetIndicesCount(String cluster, String nodes);
}

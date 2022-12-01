package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESResponsePluginInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.BigIndexMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.ClusterMemInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.PendingTask;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.NodeStateVO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    Map<String, ClusterNodeStats> syncGetNodePartStatsMap(String clusterName);

    /**
     * 获取ES集群节点ip列表
     */
    List<String> syncGetNodeHosts(String clusterName);

    List<String> syncGetNodeIp(String clusterName);

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
    List<PendingTask> syncGetPendingTask(String clusterName) throws ESOperateException;

    Map<String/*node*/, Long /*shardNum*/> syncGetNode2ShardNumMap(String clusterName);

    /**
     * 获取ES集群大索引(大于10亿文档数)信息
     */
    List<BigIndexMetrics> syncGetBigIndices(String clusterName) throws ESOperateException;

    /**
     * 获取ES集群某个节点上的索引个数
     * @param nodes 主机名或IP集合字符串 , 用逗号分隔  
     */
    int syncGetIndicesCount(String cluster, String nodes) throws ESOperateException;

    /**
     * 获取ES集群的内存使用统计信息
     * @param cluster 物理集群名称
     * @return 集群的内存使用信息统计
     */
    ClusterMemInfo synGetClusterMem(String cluster);

    /**
     * 同步节点磁盘使用情况
     * 同步获取节点磁盘使用情况
     *
     * @param cluster 集群
     * @return {@link Map}<{@link String}, {@link Triple}<{@link Long}, {@link Long}, {@link Double}>>
     */
    Map<String, Triple<Long, Long, Double>> syncGetNodesDiskUsage(String cluster);

    /**
     * node_state分析
     * @param cluster
     * @return
     */
    List<NodeStateVO> syncNodeStateAnalysis(String cluster);

    /**
     * 同步获取节点内存和磁盘
     *
     * @param cluster 集群
     * @return {@link Map}<{@link String}, {@link Tuple}<{@link Long}, {@link Long}>>
     */
    Map<String, Tuple<Long, Long>> syncGetNodesMemoryAndDisk(String cluster);

    /**
     * 同步获取节点的cpu数量
     *
     * @param cluster 集群
     * @return {@link Map}<{@link String}, {@link Integer}>
     */
    Map<String, Integer> syncGetNodesCpuNum(String cluster) throws ESOperateException;
    
    /**
     * 同步获取节点插件元组列表
     *
     * @param phyCluster phy集群
     * @return {@code List<TupleTwo<String, List<String>>>}
     */
    public List<TupleTwo</*node name*/String,/*plugin names*/List<String>>> syncGetNodePluginTupleList(String phyCluster) throws ESOperateException;
    
    /**
     * 确定dcdr 和pipeline存在于集群中
     *
     * @param phyClusterName phy集群名称
     * @return {@code TupleTwo<Boolean, Boolean>}
     */
    public TupleTwo</*dcdrExist*/Boolean,/*pipelineExist*/ Boolean> existDCDRAndPipelineModule(String phyClusterName);

    /**
     * WriteRejected数
     *
     * @param cluster 集群 WriteRejectedNum
     * @return {@code Long}
     */
    public Long getWriteRejectedNum(String cluster,String node);

    /**
     * SearchRejected数
     *
     * @param cluster 集群 WriteRejectedNum
     * @param node
     * @return {@code Long}
     */
    public Long getSearchRejectedNum(String cluster,String node);

    /**
     * 获取nodeStats信息
     * @param cluster
     * @return
     */
    List<ClusterNodeStats> syncGetNodeStats(String cluster);

		/**
		 * 从名为 clusterName 的集群中获取所有插件。
		 *
		 * @param clusterName 从中获取插件的集群的名称。
		 * @return 对象的集合。
		 */
	Collection<ESResponsePluginInfo> syncGetPlugins(String clusterName) throws ESOperateException;
}
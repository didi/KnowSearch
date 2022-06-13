package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.MovingShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.ShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.shard.Segments;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.ShardAssignmentDescriptionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.ShardDistributionVO;

import java.util.List;

/**
 * Created by linyunan on 3/22/22
 */
public interface ESShardService {

    /**
     * 获取ES集群movingShard信息
     */
    List<MovingShardMetrics> syncGetMovingShards(String clusterName);

    /**
     * 获取ES集群大Shard(50G)信息
     */
    List<ShardMetrics> syncGetBigShards(String clusterName);

    /**
     * 获取ES集群小Shard(1G以内)信息
     */
    List<ShardMetrics> syncGetSmallShards(String clusterName);

    /**
     * 获取ES集群大Shard(50G)信息 和 小Shard(1G以内)信息
     */
    Tuple</*大shard列表*/List<ShardMetrics>, /*小shard列表*/List<ShardMetrics>> syncGetBigAndSmallShards(String clusterName);

    /**
     * 获取集群segments信息
     * @param clusterName
     * @return
     */
    List<Segments> syncGetSegments(String clusterName);

    /**
     * shard分布
     * @param cluster
     * @return
     */
    List<ShardDistributionVO> shardDistribution(String cluster);

    ShardAssignmentDescriptionVO shardAssignmentDescription(String cluster);
}

package com.didichuxing.datachannel.arius.admin.core.service.es;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.MovingShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.ShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.UnAssignShardMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.shard.Segment;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand.ShardAssignmentDescriptionVO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

/**
 * Created by linyunan on 3/22/22
 */
public interface ESShardService {

    /**
     * 获取ES集群movingShard信息
     */
    List<MovingShardMetrics> syncGetMovingShards(String clusterName) throws ESOperateException;

    /**
     * 获取ES集群unassignShard信息
     */
    List<UnAssignShardMetrics> syncGetUnAssignShards(String clusterName) throws ESOperateException;

    /**
     * 获取ES集群大Shard(平台可配置，默认50G)信息
     */
    List<ShardMetrics> syncGetBigShards(String clusterName) throws ESOperateException;

    /**
     * 获取ES集群大Shard(50G)信息 和 小Shard(1G以内)信息
     */
    Tuple</*大shard列表*/List<ShardMetrics>, /*小shard列表*/List<ShardMetrics>> syncGetBigAndSmallShards(String clusterName,long configBigShard,long configSmallShard);

    /**
     * 获取集群segments信息, 不包含全量segment属性
     * @param clusterName
     * @return
     */
    List<Segment> syncGetSegments(String clusterName);

    /**
     * 获取集群segments数量统计信息
     * @param clusterName
     * @return
     */
    List<Segment> syncGetSegmentsCountInfo(String clusterName) throws ESOperateException;


    /**
     *  shard分配说明
     * @param cluster
     * @return
     */
    ShardAssignmentDescriptionVO syncShardAssignmentDescription(String cluster) throws ESOperateException;

}
package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.MetricsVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by linyunan on 2021-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("物理集群指标总览信息")
public class ESClusterOverviewMetricsVO extends MetricsVO {

    @ApiModelProperty("集群名称，all则表示全集群信息")
    private String                                   clusterName;

    @ApiModelProperty("是否是物理集群 1：是；0：不是")
    private Integer                                  physicCluster;

    @ApiModelProperty("数据中心")
    private String                                   dataCenter;

    /*****************************************1.基础信息********************************************/

    @ApiModelProperty("物理集群基础指标信息")
    private ESClusterPhyBasicMetricsVO               basic;

    /*****************************************2.cpu 相关********************************************/

    @ApiModelProperty("CPU使用率指标信息")
    private List<CpuUsageMetricsVO>                  cpuUsage;

    @ApiModelProperty("CPU1分钟负载指标信息")
    private List<CpuLoadFor1MinMetricsVO>            cpuLoad1M;

    @ApiModelProperty("CPU5分钟负载指标信息")
    private List<CpuLoadFor5MinMetricsVO>            cpuLoad5M;

    @ApiModelProperty("CPU15分钟负载指标信息")
    private List<CpuLoadFor15MinMetricsVO>           cpuLoad15M;

    /******************************************3.磁盘信息********************************************/

    @ApiModelProperty("集群磁盘使用率指标信息")
    private List<DiskUsageMetricsVO>                 diskUsage;

    @ApiModelProperty("磁盘使用指标信息")
    private List<DiskInfoMetricsVO>                  diskInfo;

    /*************************************4.网络流量信息(单位 MB)***************************************/

    @ApiModelProperty("每秒接受流量")
    private List<RecvTransMetricsVO>                 recvTransSize;

    @ApiModelProperty("每秒发送流量")
    private List<SendTransMetricsVO>                 sendTransSize;

    /****************************************5.shard对比信息******************************************/

    @ApiModelProperty("shard数量(总数, 未分配Shard)")
    private List<ShardInfoMetricsVO>                 shardNu;

    @ApiModelProperty("正在迁移shard列表")
    private List<MovingShardMetricsVO>               movingShards;

    @ApiModelProperty("大shard列表")
    private List<BigShardMetricsVO>                  bigShards;

    @ApiModelProperty("大shard阈值，单位g")
    private Double                                   bigShardThreshold;

    /******************************************6.节点信息******************************************/

    @ApiModelProperty("大于75%磁盘利用率节点列表")
    private List<NodeInfoForDiskUsageGte75PercentVO> nodesForDiskUsageGte75Percent;

    @ApiModelProperty("无效节点的Ip列表")
    private List<ClusterRoleHost>                             invalidNodes;

    /****************************************7.进程任务信息******************************************/

    @ApiModelProperty("Pending任务列表")
    private List<PendingTaskVO>                      pendingTasks;

    /*****************************************8.read/write 单位:/s******************************************/

    @ApiModelProperty("查询QPS")
    private List<ReadQPSMetricsVO>                   readTps;

    @ApiModelProperty("写入TPS")
    private List<WriteTPSMetricsVO>                  writeTps;

    @ApiModelProperty("查询延时")
    private List<SearchLatencyMetricsVO>             searchLatency;

    @ApiModelProperty("写入延时")
    private List<IndexingLatencyMetricsVO>           indexingLatency;

    @ApiModelProperty("当前执行的task耗时")
    private List<TaskCostMetricVO>                   taskCost;

    @ApiModelProperty("当前执行的task数量")
    private List<TaskCountMetricVO>                  taskCount;

    /******************************************6.索引信息******************************************/

    @ApiModelProperty("大索引列表,大于10亿文档的索引列表")
    private List<BigIndexMetricsVO>                  bigIndices;
}

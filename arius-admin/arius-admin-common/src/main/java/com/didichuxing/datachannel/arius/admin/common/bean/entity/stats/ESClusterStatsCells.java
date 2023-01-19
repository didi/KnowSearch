package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESClusterStatsCells {
    /**
     * 集群名称，all则表示全集群信息
     */
    private String clusterName;
    /**
     * 集群状态
     */
    private String status;
    /**
     * 集群状态
     */
    private Integer statusType;
    /**
     * 未分配shard
     */
    private Long unAssignedShards;
    /**
     * pengdingtask梳理
     */
    private Long numberPendingTasks;
    /**
     * dataNode数量
     */
    private Long numberDataNodes;
    private Long numberMasterNodes;
    private Long numberClientNodes;
    private Long numberIngestNodes;
    /**
     * 集群节点数量
     */
    private Long numberNodes;
    /**
     * 节点存活率
     */
    private Integer alivePercent;
    /**
     * 集群重要等级
     */
    private Integer level;
    /**
     * 集群已使用容量，单位bytes
     */
    private Long storeSize;
    /**
     * 集群总容量，单位bytes
     */
    private Long totalStoreSize;
    /**
     * 集群空余容量，单位bytes
     */
    private Long freeStoreSize;
    /**
     * 索引容量，单位bytes
     */
    private Long indexStoreSize;
    /**
     * 集群索引数
     */
    private Long totalIndicesNu;
    /**
     * 集群模板数
     */
    private Integer totalTemplateNu;
    /**
     * 集群文档数
     */
    private Long totalDocNu;
    /**
     * 集群shard数量
     */
    private Long shardNu;
    /**
     * 每秒接受流量
     */
    private Double recvTransSize;
    /**
     * 每秒发送流量
     */
    private Double sendTransSize;
    /**
     * 集群写入tps
     */
    private Double writeTps;
    /**
     * 集群读取tps
     */
    private Double readTps;
    /**
     * cpu相关
     */
    private Double cpuUsage;
    private Double cpuLoad1M;
    private Double cpuLoad5M;
    private Double cpuLoad15M;
    /**
     * 磁盘使用率
     */
    private Double diskUsage;
    /**
     * 集群总数
     */
    private Integer clusterNu;
    /**
     * es节点数
     */
    private Long esNodeNu;
    /**
     * 应用数量
     */
    private Integer appNu;
    /**
     * 每日查询总量
     */
    private Long queryTimesPreDay;
    /**
     * 集群稳定性
     */
    private Double sla;
    private Long memUsed;
    private Long memFree;
    private Long memTotal;
    private Long memUsedPercent;
    private Long memFreePercent;
    /**
     * 查询耗时
     */
    private Double searchLatency;
    /**
     * 写入耗时
     */
    private Double indexingLatency;
    /**
     * 任务耗时
     */
    private double taskCost;
    /**
     * 任务数量
     */
    private long taskCount;
}

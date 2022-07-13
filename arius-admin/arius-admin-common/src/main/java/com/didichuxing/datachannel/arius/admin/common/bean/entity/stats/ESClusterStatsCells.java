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
    private int    statusType;

    /**
     * 未分配shard
     */
    private long   unAssignedShards;

    /**
     * pengdingtask梳理
     */
    private long   numberPendingTasks;

    /**
     * dataNode数量
     */
    private long   numberDataNodes;

    private long   numberMasterNodes;
    private long   numberClientNodes;
    private long   numberIngestNodes;

    /**
     * 集群节点数量
     */
    private long   numberNodes;

    /**
     * 节点存活率
     */
    private int    alivePercent;

    /**
     * 集群重要等级
     */
    private int    level;

    /**
     * 集群已使用容量，单位
     */
    private double storeSize;

    /**
     * 集群总容量，单位
     */
    private double totalStoreSize;

    /**
     * 集群空余容量，单位
     */
    private double freeStoreSize;

    /**
     * 索引容量，单位
     */
    private double indexStoreSize;

    /**
     * 集群索引数
     */
    private double totalIndicesNu;

    /**
     * 集群模板数
     */
    private int    totalTemplateNu;

    /**
     * 集群文档数
     */
    private long   totalDocNu;

    /**
     * 集群shard数量
     */
    private long   shardNu;

    /**
     * 每秒接受流量
     */
    private double recvTransSize;

    /**
     * 每秒发送流量
     */
    private double sendTransSize;

    /**
     * 集群写入tps
     */
    private double writeTps;

    /**
     * 集群读取tps
     */
    private double readTps;

    /**
     * cpu相关
     */
    private double cpuUsage;
    private double cpuLoad1M;
    private double cpuLoad5M;
    private double cpuLoad15M;

    /**
     * 磁盘使用率
     */
    private double diskUsage;

    /**
     * 集群总数
     */
    private int    clusterNu;

    /**
     * es节点数
     */
    private double esNodeNu;

    /**
     * 应用数量
     */
    private int    appNu;

    /**
     * 每日查询总量
     */
    private long   queryTimesPreDay;
    /**
     * 集群稳定性
     */
    private double sla;

    private long   memUsed;
    private long   memFree;
    private long   memTotal;
    private double memUsedPercent;
    private double memFreePercent;

    /**
     * 查询耗时
     */
    private double searchLatency;

    /**
     * 写入耗时
     */
    private double indexingLatency;

    /**
     * 任务耗时
     */
    private double taskCost;

    /**
     * 任务数量
     */
    private long   taskCount;
}

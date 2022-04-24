package com.didichuxing.datachannel.arius.admin.common.bean.po.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterLogicStatisPO {
    /**
     * 逻辑集群名称
     */
    private String      name;

    /**
     * 逻辑集群id
     */
    private long        id;

    /**
     * 逻辑集群状态
     */
    private String      status;

    /**
     * 逻辑集群状态
     */
    private int         statusType;

    /**
     * docNu
     */
    private double      docNu;

    /**
     * 总的磁盘容量
     */
    private double      totalDisk;

    /**
     * 已使用的磁盘容量
     */
    private double      usedDisk;

    /**
     * 空闲的磁盘容量
     */
    private double      freeDisk;

    /**
     * 索引数量
     */
    private long        indexNu;

    /**
     * dataNode个数
     */
    private long        numberDataNodes;

    /**
     * masterNode个数
     */
    private long        masterNodeNu;

    /**
     * 未分配shard
     */
    private long        unAssignedShards;

    /**
     * pengdingtask梳理
     */
    private long        numberPendingTasks;

    /**
     * 节点存活率
     */
    private int    alivePercent;
    /**
     * cpu相关
     */
    private double cpuUsedPercent;
}

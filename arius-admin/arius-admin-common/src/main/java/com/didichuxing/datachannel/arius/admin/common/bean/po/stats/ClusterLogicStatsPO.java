package com.didichuxing.datachannel.arius.admin.common.bean.po.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterLogicStatsPO {
    /**
     * 逻辑集群名称
     */
    private String name;
    /**
     * 逻辑集群id
     */
    private Long id;
    /**
     * 逻辑集群状态
     */
    private String status;
    /**
     * 逻辑集群状态
     */
    private Integer statusType;
    /**
     * docNu
     */
    private Long docNu;
    /**
     * 总的磁盘容量
     */
    private Double totalDisk;
    /**
     * 已使用的磁盘容量
     */
    private Double usedDisk;
    /**
     * 空闲的磁盘容量
     */
    private Double freeDisk;
    /**
     * 索引数量
     */
    private Long indexNu;
    /**
     * dataNode个数
     */
    private Long numberDataNodes;
    /**
     * masterNode个数
     */
    private Long masterNodeNu;
    /**
     * 未分配shard
     */
    private Long unAssignedShards;
    /**
     * pengdingtask梳理
     */
    private Long numberPendingTasks;
    /**
     * cpu最大使用率
     */
    private Double cpuUsedPercent;
    /**
     * 节点存活率
     */
    private Integer alivePercent;
}

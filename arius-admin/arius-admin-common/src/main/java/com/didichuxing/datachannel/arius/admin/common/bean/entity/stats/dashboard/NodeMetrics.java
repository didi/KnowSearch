package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 3/11/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeMetrics {
    /**
     * 统计的时间戳，单位：毫秒
     */
    private Long    timestamp;

    /**
     * 集群名称
     */
    private String  cluster;

    /**
     * 节点名称
     */
    private String  node;

    /**
     * 是否死亡
     */
    private Boolean dead;

    /**
     * 磁盘利用率超红线节点（阈值85%）
     */
    private Double  largeDiskUsage;

    /**
     * 堆内存利用率超红线（阈值80% 且持续5分钟）
     */
    private Double  largeHead;

    /**
     * CPU利用率超红线节点（80%  持续30分钟）
     */
    private Double  largeCpuUsage;

    /**
     * WriteRejected数
     */
    private Long    writeRejectedNum;

    /**
     * SearchRejected数
     */
    private Long    SearchRejectedNum;

    /**
     * 节点分片个数
     */
    private Long    shardNum;

    /**
     * 节点任务耗时(单位 ms)
     */
    private Long    taskConsuming;

    /**
     * 消耗时间 （开始采集到结束采集的时间）
     */
    private Long    elapsedTime;
}
package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * monitor 采集任务信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitorTaskInfo {

    /**
     * 集群名称
     */
    private String  clusterName;
    /**
     * 任务开始时刻
     */
    private Long    startTick;
    /**
     * 任务执行耗时
     */
    private Long    totalCost;
    /**
     * 是否在运行
     */
    private Boolean running;
}

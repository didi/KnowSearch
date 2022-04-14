package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingTask implements Serializable {

    /**
     * 序号
     */
    private long   insertOrder;

    /**
     * 在队列里等待时间
     */
    private String timeInQueue;

    /**
     * 优先级, HIGH
     */
    private String priority;

    /**
     * 任务来源说明
     */
    private String source;
}

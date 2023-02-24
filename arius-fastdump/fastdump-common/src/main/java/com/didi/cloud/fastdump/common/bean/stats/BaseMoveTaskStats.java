package com.didi.cloud.fastdump.common.bean.stats;

import com.didi.cloud.fastdump.common.bean.BaseEntity;

import lombok.Data;

/**
 * Created by linyunan on 2022/9/5
 */
@Data
public abstract class BaseMoveTaskStats extends BaseEntity {
    /**
     * 任务唯一标识 uuid
     */
    private String           taskId;
    private volatile boolean interruptMark = false;
}

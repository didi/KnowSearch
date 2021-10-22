package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.Data;

@Data
public class Silence {
    private Long silenceId;

    /**
     * 屏蔽的策略ID
     */
    private Long strategyId;

    /**
     * 屏蔽开始时间
     */
    private Long beginTime;

    /**
     * 屏蔽结束时间
     */
    private Long endTime;

    /**
     * 备注
     */
    private String description;
}

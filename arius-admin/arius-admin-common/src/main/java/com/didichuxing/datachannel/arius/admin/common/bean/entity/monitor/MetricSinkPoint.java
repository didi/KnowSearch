package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricSinkPoint {
    /**
     * 指标名
     */
    private String name;

    /**
     * 指标值
     */
    private String value;

    /**
     * 上报周期
     */
    private int step;

    /**
     * 当前时间戳，单位为s
     */
    private long timestamp;

    /**
     * tags
     */
    private BaseTag tags;
}

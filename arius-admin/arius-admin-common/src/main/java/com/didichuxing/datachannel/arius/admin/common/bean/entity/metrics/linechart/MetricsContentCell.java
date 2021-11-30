package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by linyunan on 2021-08-12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsContentCell implements Serializable {
    /**
     * 指标值
     */
    private double value;

    /**
     * 时间戳
     */
    private long   timeStamp;
}

package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.Data;

@Data
public class MetricPoint {
    private Double value;

    private Long timestamp;
}
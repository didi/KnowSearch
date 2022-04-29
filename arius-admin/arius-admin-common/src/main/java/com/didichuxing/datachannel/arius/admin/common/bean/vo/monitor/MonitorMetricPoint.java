package com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor;

import lombok.Data;

@Data
public class MonitorMetricPoint {
    private Long timestamp;

    private Double value;

    public MonitorMetricPoint(Long timestamp, Double value) {
        this.timestamp = timestamp;
        this.value = value;
    }
}
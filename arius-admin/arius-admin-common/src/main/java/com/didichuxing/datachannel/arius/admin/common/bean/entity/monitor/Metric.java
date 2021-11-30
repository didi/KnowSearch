package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metric {
    private String metricName;

    private Integer step;

    private List<MetricPoint> values;

    private Integer comparison;

    private Integer delta;

    private Boolean origin;
}
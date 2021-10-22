package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.Data;

import java.util.List;

@Data
public class Metric {
    private String metric;

    private Integer step;

    private List<MetricPoint> values;

    private Integer comparison;

    private Integer delta;

    private Boolean origin;
}
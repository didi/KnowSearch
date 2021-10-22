package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.Data;

@Data
public class MonitorAlertDetail {
    private Alert alert;

    private Metric metric;

    public MonitorAlertDetail(Alert alert, Metric metric) {
        this.alert = alert;
        this.metric = metric;
    }
}
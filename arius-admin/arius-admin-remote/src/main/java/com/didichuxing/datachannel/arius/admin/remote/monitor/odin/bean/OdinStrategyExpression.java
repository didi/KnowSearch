package com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean;

import lombok.Data;

@Data
public class OdinStrategyExpression {
    private String metric;

    private String func;

    private String eopt;

    private Long   threshold;

    private String params;
}
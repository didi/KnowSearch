package com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OdinStrategyExpression {
    private String metric;

    private String func;

    private String eopt;

    private Long   threshold;

    private String params;
}
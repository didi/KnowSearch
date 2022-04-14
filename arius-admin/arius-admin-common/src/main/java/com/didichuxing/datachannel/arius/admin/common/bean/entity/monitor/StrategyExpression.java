package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyExpression {
    private String metric;

    private String func;

    private String eopt;

    private Long threshold;

    private String params;

    public boolean paramLegal() {
        if (metric == null
                || func == null
                || eopt == null
                || threshold == null
                || params == null) {
            return false;
        }
        return true;
    }
}

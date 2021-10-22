package com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean;

import lombok.Data;

import java.util.List;

@Data
public class OdinStrategy {
    private Long                         id;

    private String                       name;

    private String                       ns;

    private Integer                      priority;

    private String                       period_hours_of_day;

    private String                       period_days_of_week;

    private List<OdinStrategyExpression> strategy_expressions;

    private List<OdinStrategyFilter>     strategy_filters;

    private List<OdinStrategyAction>     strategy_actions;
}
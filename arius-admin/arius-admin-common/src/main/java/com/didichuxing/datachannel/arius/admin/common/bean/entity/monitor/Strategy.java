package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Strategy {
    private Long id;

    private String name;

    private Integer priority;

    private String periodHoursOfDay;

    private String periodDaysOfWeek;

    private List<StrategyExpression> strategyExpressionList;

    private List<StrategyFilter> strategyFilterList;

    private List<StrategyAction> strategyActionList;

    public boolean paramLegal() {
        if (name == null
                || priority == null
                || periodHoursOfDay == null
                || periodDaysOfWeek == null
                || strategyExpressionList == null || strategyExpressionList.isEmpty()
                || strategyFilterList == null || strategyFilterList.isEmpty()
                || strategyActionList == null || strategyActionList.isEmpty()) {
            return false;
        }

        for (StrategyExpression dto: strategyExpressionList) {
            if (!dto.paramLegal()) {
                return false;
            }
        }

        for (StrategyFilter dto: strategyFilterList) {
            if (!dto.paramLegal()) {
                return false;
            }
        }
        for (StrategyAction dto: strategyActionList) {
            if (!dto.paramLegal()) {
                return false;
            }
        }
        return true;
    }
}

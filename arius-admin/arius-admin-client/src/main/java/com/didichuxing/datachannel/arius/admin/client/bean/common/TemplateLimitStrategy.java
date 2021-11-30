package com.didichuxing.datachannel.arius.admin.client.bean.common;

import com.didichuxing.datachannel.arius.admin.client.constant.quota.QuotaCtlStrategyEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

/**
 * @author d06679
 * @date 2019-08-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "限流策略")
public class TemplateLimitStrategy {

    public static final Integer TPS_ADJUST_PERCENT_MAX = 1000;
    public static final Integer TPS_ADJUST_PERCENT_MIN = -99;

    /**
     * 调账策略
     * @see QuotaCtlStrategyEnum
     */
    @ApiModelProperty("策略")
    private Integer             adjustStrategy;

    /**
     * 调整比例
     */
    @ApiModelProperty("比例")
    private Integer             tpsAdjustPercent;

    public static TemplateLimitStrategy merge(List<TemplateLimitStrategy> strategies) {
        strategies.sort(Comparator.comparing(TemplateLimitStrategy::getTpsAdjustPercent));
        return strategies.get(0);
    }

    public static TemplateLimitStrategy buildDefault() {
        TemplateLimitStrategy strategy = new TemplateLimitStrategy();
        strategy.setTpsAdjustPercent(TPS_ADJUST_PERCENT_MAX - 1);
        strategy.setAdjustStrategy(QuotaCtlStrategyEnum.INCREASE.getCode());
        return strategy;
    }
}

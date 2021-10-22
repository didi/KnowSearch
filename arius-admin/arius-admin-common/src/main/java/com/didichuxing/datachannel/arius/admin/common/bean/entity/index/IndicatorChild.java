package com.didichuxing.datachannel.arius.admin.common.bean.entity.index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndicatorChild {
    /**
     * 主键
     */
    private int    id;
    /**
     * 指标编码
     */
    private int    code;
    /**
     * 指标区间上值
     */
    private int    upper;
    /**
     * 指标区间下值
     */
    private int    lower;
    /**
     * 指标得分
     */
    private String scoreExpr = "";
    /**
     * 是否有效
     */
    private int    isActive;
}

package com.didichuxing.datachannel.arius.admin.common.bean.entity.index;

import com.didichuxing.datachannel.arius.admin.common.constant.IndicatorsType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseDegree {

    @ApiModelProperty(value = "健康分计算类型：健康分基数、实时写入量、实时查询量、实时JVM内存使用、实时查询时间耗时、实时磁盘利用率、实时CPU利用率")
    private IndicatorsType indicatorsType;

    @ApiModelProperty(value = "得分")
    private double         score;

    /**
     * 乘以权重的最终得分，weightScore=score*weightRate
     */
    @ApiModelProperty(value = "乘以权重的最终得分，weightScore=score*weightRate")
    private double         weightScore;

    /**
     * 权重
     */
    @ApiModelProperty(value = "权重")
    private int            weight;

    /**
     * 权重比例
     */
    @ApiModelProperty(value = "权重比例")
    private double         weightRate;

    /**
     * 计算处理过程
     */
    @ApiModelProperty(value = "计算处理过程")
    private String         process;

    /**
     * 惩罚机制计算
     */
    @ApiModelProperty(value = "惩罚机制计算")
    private String         punishment;

    @ApiModelProperty(value = "得分描述")
    private String         desc;
}

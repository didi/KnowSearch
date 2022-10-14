package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 指标字典查询.
 *
 * @ClassName MetricDictionaryDTO
 * @Author gyp
 * @Date 2022/9/29
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "指标字典信息")
public class MetricDictionaryDTO {
    @ApiModelProperty("指标分类")
    private String type;

    @ApiModelProperty("指标分类")
    private String metricType;

    @ApiModelProperty("指标分类")
    private String name;

    @ApiModelProperty("是否黄金指标")
    private Integer isGold;

    @ApiModelProperty("告警指标")
    private Integer isWarning;

    @ApiModelProperty("阈值配置")
    private Integer isThreshold;

    @ApiModelProperty("指标来源")
    private String source;

    @ApiModelProperty("指标标签")
    private String tags;

    @ApiModelProperty("模块")
    private String model;

}
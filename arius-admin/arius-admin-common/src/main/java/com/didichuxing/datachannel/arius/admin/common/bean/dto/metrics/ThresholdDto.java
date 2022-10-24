package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * dashboard阈值的信息.
 *
 * @ClassName ThresholdDto
 * @Author gyp
 * @Date 2022/7/26
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@Builder
public class ThresholdDto {
    /**
     * 名称
     */
    private String name;
    /**
     * 指标项名称
     */
    private String metrics;
    /**
     * 描述
     */
    private String desc;
    /**
     * 单位
     */
    private String unit;
    /**
     * 比较符号
     */
    private String compare;
    /**
     * 阈值
     */
    private Double value;
}
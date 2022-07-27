package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import com.didichuxing.datachannel.arius.admin.common.constant.metrics.DashBoardMetricListTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阈值详细信息.
 *
 * @ClassName DashBoardMetricThresholdDTO
 * @Author gyp
 * @Date 2022/7/26
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashBoardMetricThresholdDTO {
    /**
     * 配置名称
     */
    private String configName;
    /**
     *
     */
    private DashBoardMetricListTypeEnum typeEnum;
    /**
     * 名称
     */
    private String name;
    /**
     * 指标项名称
     */
    private String metrics;
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
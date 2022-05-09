package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Dashboard大盘列表类型的指标信息, 默认时间为当前时间")
public class MetricsDashboardListDTO extends BaseDTO {
    @ApiModelProperty("聚合类型")
    private String       aggType;

    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = false)
    private Boolean      orderByDesc = true;

    @ApiModelProperty("指标类型")
    private List<String> metricsTypes;

}

package com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@ApiModel(description = "报警规则")
public class MonitorStrategyExpressionDTO extends BaseDTO {
    @ApiModelProperty(value = "指标名")
    private String metric;

    @ApiModelProperty(value = "计算规则")
    private String func;

    @ApiModelProperty(value = "操作符")
    private String eopt;

    @ApiModelProperty(value = "阈值")
    private Long threshold;

    @ApiModelProperty(value = "参数见规则描述")
    private String params;

    public boolean paramLegal() {
        if (StringUtils.isBlank(metric)
                || StringUtils.isBlank(func)
                || StringUtils.isBlank(eopt)
                || null == threshold
                || StringUtils.isBlank(params)) {
            return false;
        }
        return true;
    }
}
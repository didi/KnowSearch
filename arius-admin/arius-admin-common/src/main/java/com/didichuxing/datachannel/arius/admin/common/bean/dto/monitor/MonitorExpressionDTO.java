package com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@ApiModel(description = "规则表达式")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class MonitorExpressionDTO extends BaseDTO {
    @ApiModelProperty(value = "操作符")
    private String optr;
    @ApiModelProperty(value = "计算规则")
    private String func;
    @ApiModelProperty(value = "指标")
    private String metric;
    @ApiModelProperty(value = "参数")
    private String params;
    @ApiModelProperty(value = "阈值")
    private Integer threshold;

    public boolean paramLegal() {
        if (StringUtils.isBlank(optr)
                || StringUtils.isBlank(func)
                || StringUtils.isBlank(metric)) {
            return false;
        }
        return true;
    }
}
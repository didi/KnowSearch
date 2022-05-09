package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-08-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description ="模板成本")
public class TemplateCostVO {

    @ApiModelProperty("单价")
    private Double unitPrice  = 0.0;

    @ApiModelProperty("总价")
    private Double totalPrice = 0.0;

}

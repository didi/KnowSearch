package com.didichuxing.datachannel.arius.admin.common.bean.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * fitz 2021-08-23
 */
@Data
@ApiModel(description = "name, value数据结构")
@NoArgsConstructor
@AllArgsConstructor
public class NameValue {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("值")
    private String value;

}

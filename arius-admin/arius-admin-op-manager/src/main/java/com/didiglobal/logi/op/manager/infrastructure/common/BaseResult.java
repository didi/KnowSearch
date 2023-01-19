package com.didiglobal.logi.op.manager.infrastructure.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author didi
 * @date 2022-07-05 10:20 上午
 */
@Data
public class BaseResult {

    @ApiModelProperty(value = "返回信息")
    protected String message;

    @ApiModelProperty(value = "返回编号（200成功，其他见message）")
    protected Integer code;
}

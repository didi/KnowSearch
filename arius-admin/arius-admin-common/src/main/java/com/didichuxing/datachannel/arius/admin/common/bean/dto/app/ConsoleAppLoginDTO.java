package com.didichuxing.datachannel.arius.admin.common.bean.dto.app;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-07-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "APP登录信息")
@Deprecated
public class ConsoleAppLoginDTO extends BaseDTO {

    @ApiModelProperty("APPID")
    private Integer appId;

    @ApiModelProperty("验证码")
    private String  verifyCode;

}
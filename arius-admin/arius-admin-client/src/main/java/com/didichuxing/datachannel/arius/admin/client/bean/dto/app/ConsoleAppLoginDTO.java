package com.didichuxing.datachannel.arius.admin.client.bean.dto.app;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019-07-10
 */
@Data
@ApiModel(description = "APP登录信息")
public class ConsoleAppLoginDTO extends BaseDTO {

    /**
     * appid
     */
    @ApiModelProperty("APPID")
    private Integer appId;

    /**
     * 验证码
     */
    @ApiModelProperty("验证码")
    private String  verifyCode;

}

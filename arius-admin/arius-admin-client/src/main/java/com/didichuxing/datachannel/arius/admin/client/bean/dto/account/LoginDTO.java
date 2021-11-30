package com.didichuxing.datachannel.arius.admin.client.bean.dto.account;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "登录信息")
public class LoginDTO extends BaseDTO {

    @ApiModelProperty("账号")
    private String domainAccount;

    @ApiModelProperty("密码")
    private String password;
}
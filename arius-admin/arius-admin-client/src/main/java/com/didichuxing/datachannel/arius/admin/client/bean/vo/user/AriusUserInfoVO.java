package com.didichuxing.datachannel.arius.admin.client.bean.vo.user;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户信息")
public class AriusUserInfoVO extends BaseVO {

    @ApiModelProperty(value = "角色权限 -1:未知 0:普通, 1:平台运营角色(RD) 2:OP角色")
    private Integer role;

    @ApiModelProperty(value = "用户名")
    private String  name;

    @ApiModelProperty(value = "账号")
    private String  domainAccount;

    @ApiModelProperty(value = "邮箱")
    private String  email;

    @ApiModelProperty(value = "手机号")
    private String  mobile;

    @ApiModelProperty(value = "状态")
    private Integer status;
}

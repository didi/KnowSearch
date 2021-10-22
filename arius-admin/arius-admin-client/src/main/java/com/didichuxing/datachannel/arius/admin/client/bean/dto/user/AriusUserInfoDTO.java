package com.didichuxing.datachannel.arius.admin.client.bean.dto.user;

import org.apache.commons.lang3.StringUtils;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/18
 */
@Data
@ApiModel(description = "用户信息")
public class AriusUserInfoDTO extends BaseDTO {
    @ApiModelProperty("主键")
    private Long    id;

    @ApiModelProperty("账号")
    private String  domainAccount;

    @ApiModelProperty("密码")
    private String  password;

    @ApiModelProperty("用户实名")
    private String  name;

    @ApiModelProperty("邮箱")
    private String  email;

    @ApiModelProperty("手机号")
    private String  mobile;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("角色权限")
    private Integer role;

    public boolean legal() {
        if (StringUtils.isBlank(name) || !(role == 0 || role == 1 || role == 2)) {
            return false;
        }
        return true;
    }
}

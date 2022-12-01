package com.didichuxing.datachannel.arius.admin.common.bean.dto.app;

import com.didiglobal.knowframework.security.common.dto.user.UserDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@ApiModel(description = "用户信息")
public class UserExtendDTO extends UserDTO {
	@ApiModelProperty(value = "用户旧密码", dataType = "Integer", required = false)
    private String  oldPw;
	@ApiModelProperty(value = "忽略密码比对", dataType = "boolean", hidden = true)
	private boolean ignorePasswordMatching = false;
	
}
package com.didichuxing.datachannel.arius.admin.common.bean.vo.project;

import com.didiglobal.logi.security.common.vo.user.UserVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Authoer: zyl
 * @Date: 2022/11/17
 * @Version: 1.0
 */
@Data
public class UserWithPwVO extends UserVO {
    @ApiModelProperty(value = "用户密码", dataType = "String", required = false)
    private String password;
}

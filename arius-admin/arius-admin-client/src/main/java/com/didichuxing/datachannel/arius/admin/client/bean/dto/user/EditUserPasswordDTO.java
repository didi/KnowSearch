package com.didichuxing.datachannel.arius.admin.client.bean.dto.user;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-06-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "编辑用户密码")
public class EditUserPasswordDTO extends BaseDTO {

    @ApiModelProperty("账号")
    private String domainAccount;

    @ApiModelProperty("旧密码")
    private String oldPassWord;

    @ApiModelProperty("新密码")
    private String newPassWord;
}

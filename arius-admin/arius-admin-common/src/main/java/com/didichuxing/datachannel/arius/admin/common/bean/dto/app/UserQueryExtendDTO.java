package com.didichuxing.datachannel.arius.admin.common.bean.dto.app;

import com.didiglobal.logi.security.common.dto.user.UserQueryDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserQueryExtendDTO extends UserQueryDTO {
    @ApiModelProperty(value = "是否包含管理员角色的用户", dataType = "Integer", required = false)
    private Boolean containsAdminRole = true;
}
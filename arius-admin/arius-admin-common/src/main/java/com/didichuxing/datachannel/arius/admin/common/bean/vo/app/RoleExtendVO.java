package com.didichuxing.datachannel.arius.admin.common.bean.vo.app;

import com.didiglobal.logi.security.common.vo.role.RoleVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 角色扩展
 *
 * @author shizeying
 * @date 2022/06/17
 */
@Data
public class RoleExtendVO extends RoleVO {
    /**
     * 是默认角色:
     *
     */
    @ApiModelProperty(value = "是否为默认角色", dataType = "Boolean", required = false)
    private Boolean isDefaultRole=false;
}
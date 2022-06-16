package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import com.didichuxing.datachannel.arius.admin.biz.app.UserExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didiglobal.logi.security.common.dto.user.UserDTO;
import com.didiglobal.logi.security.service.UserService;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserExtendManagerImpl implements UserExtendManager {
    @Autowired
    private UserService userService;
    
    /**
     * 添加用户
     *
     * @param param    入参
     * @param operator 操作人或角色
     * @return {@code Result<Void>}
     */
    @Override
    public Result<Void> addUser(UserDTO param, String operator) {
        param.setRoleIds(Collections.singletonList(AuthConstant.RESOURCE_OWN_ROLE_ID));
        final com.didiglobal.logi.security.common.Result<Void> result = userService.addUser(param, operator);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        return Result.buildSucc();
        
    }
}
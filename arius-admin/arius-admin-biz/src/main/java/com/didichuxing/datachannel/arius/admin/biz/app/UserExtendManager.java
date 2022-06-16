package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didiglobal.logi.security.common.dto.user.UserDTO;

/**
 * 用户扩展管理器
 *
 * @author shizeying
 * @date 2022/06/16
 */
public interface UserExtendManager {
    /**
     * 添加用户
     *
     * @param param    入参
     * @param operator 操作人或角色
     * @return {@code Result<Void>}
     */
    Result<Void> addUser(UserDTO param, String operator);
}
package com.didichuxing.datachannel.arius.admin.core.service.common;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.login.UserLoginRecord;

/**
 *
 *
 * @author d06679
 * @date 2018/8/27
 */
public interface AriusUserLoginRecordService {

    /**
     * 保存一个用户登录信息
     * @param userLoginRecord 用户登录信息
     * @return userId
     */
    Long save(UserLoginRecord userLoginRecord);

    /**
     * 是否第一次登录
     * @param userName 用户名
     * @return 是否第一次登录
     */
    Boolean isFirstLogin(String userName);

    /**
     * 用户是否今天第一次登录
     * @param userName 用户名
     * @return 用户是否今天第一次登录
     */
    Boolean isTodayFirstLogin(String userName);

}

package com.didichuxing.datachannel.arius.admin.core.service.app;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppUserInfo;

/**
 * @author d06679
 * @date 2019/5/20
 * @deprecated
 */
public interface AppUserInfoService {

    /**
     * 记录appid和user的对应关系
     *  1、appid有人登陆时调用
     *  2、新增修改app时,填写的责任人也需要调用
     *
     * @param appid appid
     * @param userName 用户名
     * @return
     */
    boolean recordAppidAndUser(Integer appid, String userName);

    /**
     * 获取用户历史登录过的appid
     * @param user
     * @return
     */
    List<AppUserInfo> getByUser(String user);

    /**
     * 获取APP最后一次登录记录
     * @param appId appId
     * @return record
     */
    AppUserInfo getAppLastLoginRecord(Integer appId);

}
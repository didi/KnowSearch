package com.didichuxing.datachannel.arius.admin.core.service.app.impl;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppUserInfoPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppUserInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppUserInfoDAO;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 * @author d06679
 * @date 2019/5/20
 */
@Service
public class AppUserInfoServiceImpl implements AppUserInfoService {

    private static final ILog LOGGER = LogFactory.getLog(AppUserInfoServiceImpl.class);

    @Autowired
    private AppUserInfoDAO    appUserInfoDAO;

    /**
     * 记录appid和user的对应关系
     *
     * @param appId    appId
     * @param userName 用户名
     * @return
     */
    @Override
    public boolean record(Integer appId, String userName) {
        if (appId == null || StringUtils.isBlank(userName)) {
            LOGGER.warn("record app user param illegal");
            return false;
        }

        boolean succ = true;
        for (String name : userName.split(",")) {
            succ = succ && doRecord(appId, name);
        }

        return succ;
    }

    /**
     * 获取用户历史登录过的appid
     *
     * @param user
     * @return
     */
    @Override
    public List<AppUserInfo> getByUser(String user) {
        return ConvertUtil.list2List(appUserInfoDAO.listByUser(user), AppUserInfo.class);
    }

    /**
     * 获取APP最后一次登录记录
     *
     * @param appId appId
     * @return record
     */
    @Override
    public AppUserInfo getAppLastLoginRecord(Integer appId) {
        return ConvertUtil.obj2Obj(appUserInfoDAO.getLastLoginRecordByAppId(appId), AppUserInfo.class);
    }

    /**************************************** private method ****************************************************/
    private boolean doRecord(Integer appId, String userName) {
        AppUserInfoPO oldUserInfoPO = appUserInfoDAO.getByAppIdAndUser(appId, userName);
        if (oldUserInfoPO == null) {
            AppUserInfoPO infoPO = new AppUserInfoPO();
            infoPO.setAppId(appId);
            infoPO.setUserName(userName);

            LOGGER.info("record new appid&user||appid={}||user={}", appId, userName);

            return 1 == appUserInfoDAO.insert(infoPO);
        } else {
            // 登陆次数累加
            AppUserInfoPO param = new AppUserInfoPO();
            param.setId(oldUserInfoPO.getId());
            param.setLoginCount(1 + oldUserInfoPO.getLoginCount());
            return 1 == appUserInfoDAO.update(param);
        }
    }
}

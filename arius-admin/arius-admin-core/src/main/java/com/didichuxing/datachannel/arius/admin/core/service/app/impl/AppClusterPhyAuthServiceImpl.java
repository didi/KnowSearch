package com.didichuxing.datachannel.arius.admin.core.service.app.impl;

import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.constant.app.AppClusterPhyAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterPhyAuth;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterPhyAuthService;

/**
 * Created by linyunan on 2021-10-15
 */
@Service
public class AppClusterPhyAuthServiceImpl implements AppClusterPhyAuthService {

    @Override
    public AppClusterPhyAuth buildClusterPhyAuth(Integer appId, String clusterPhyName, AppClusterPhyAuthEnum appClusterPhyAuthEnum) {
        if (null == appClusterPhyAuthEnum || null == appId || AriusObjUtils.isBlack(clusterPhyName)) {
            return null;
        }

        if (!AppClusterPhyAuthEnum.isExitByCode(appClusterPhyAuthEnum.getCode())) {
            return null;
        }

        AppClusterPhyAuth appClusterPhyAuth = new AppClusterPhyAuth();
        appClusterPhyAuth.setAppId(appId);
        appClusterPhyAuth.setClusterPhyName(clusterPhyName);
        appClusterPhyAuth.setType(appClusterPhyAuthEnum.getCode());
        return appClusterPhyAuth;
    }
}

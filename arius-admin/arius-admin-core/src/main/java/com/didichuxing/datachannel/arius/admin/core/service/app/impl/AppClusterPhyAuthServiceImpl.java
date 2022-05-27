package com.didichuxing.datachannel.arius.admin.core.service.app.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectClusterPhyAuth;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterPhyAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterPhyAuthService;

/**
 * Created by linyunan on 2021-10-15
 */
@Service
public class AppClusterPhyAuthServiceImpl implements AppClusterPhyAuthService {

    @Override
    public ProjectClusterPhyAuth buildClusterPhyAuth(Integer appId, String clusterPhyName, AppClusterPhyAuthEnum appClusterPhyAuthEnum) {
        if (null == appClusterPhyAuthEnum || null == appId || AriusObjUtils.isBlack(clusterPhyName)) {
            return null;
        }

        if (!AppClusterPhyAuthEnum.isExitByCode(appClusterPhyAuthEnum.getCode())) {
            return null;
        }

        ProjectClusterPhyAuth projectClusterPhyAuth = new ProjectClusterPhyAuth();
        projectClusterPhyAuth.setProjectId(appId);
        projectClusterPhyAuth.setClusterPhyName(clusterPhyName);
        projectClusterPhyAuth.setType(appClusterPhyAuthEnum.getCode());
        return projectClusterPhyAuth;
    }
}
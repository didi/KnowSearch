package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.didichuxing.datachannel.arius.admin.biz.app.AppClusterLogicAuthManager;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.google.common.collect.Lists;

/**
 * Created by linyunan on 2021-10-17
 */
@Component
public class AppClusterLogicAuthManagerImpl implements AppClusterLogicAuthManager {
    @Autowired
    private AppService                 appService;

    @Autowired
    private AppClusterLogicAuthService appClusterLogicAuthService;

    @Override
    public List<AppClusterLogicAuth> getByClusterLogicListAndAppId(Integer appId, List<ClusterLogic> clusterLogicList) {
        List<AppClusterLogicAuth> appClusterLogicAuthList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(clusterLogicList)) {
            return appClusterLogicAuthList;
        }

        if (!appService.isAppExists(appId)) {
            appClusterLogicAuthList = clusterLogicList
                    .stream()
                    .map(r -> appClusterLogicAuthService.buildClusterLogicAuth(appId, r.getId(), AppClusterLogicAuthEnum.NO_PERMISSIONS))
                    .collect(Collectors.toList());
            return appClusterLogicAuthList;
        }

        if (appService.isSuperApp(appId)) {
            appClusterLogicAuthList = clusterLogicList
                    .stream()
                    .map(r -> appClusterLogicAuthService.buildClusterLogicAuth(appId, r.getId(), AppClusterLogicAuthEnum.OWN))
                    .collect(Collectors.toList());
            return appClusterLogicAuthList;
        }

        appClusterLogicAuthList = clusterLogicList
                            .stream()
                            .map(clusterLogic -> appClusterLogicAuthService.getLogicClusterAuth(appId, clusterLogic.getId()))
                            .collect(Collectors.toList());

        //处理无权限
        for (AppClusterLogicAuth appClusterLogicAuth : appClusterLogicAuthList) {
            if (null == appClusterLogicAuth.getType()) {
                appClusterLogicAuth.setType(AppClusterLogicAuthEnum.NO_PERMISSIONS.getCode());
            }
        }
        return appClusterLogicAuthList;
    }
}

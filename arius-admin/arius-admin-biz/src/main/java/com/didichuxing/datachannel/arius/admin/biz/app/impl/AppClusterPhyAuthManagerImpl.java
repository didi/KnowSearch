package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.app.AppClusterPhyAuthManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppClusterPhyAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterPhyAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhyContext;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterPhyAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.google.common.collect.Lists;

/**
 * Created by linyunan on 2021-10-15
 */
@Component
public class AppClusterPhyAuthManagerImpl implements AppClusterPhyAuthManager {

    @Autowired
    private AppService                 appService;

    @Autowired
    private AppClusterPhyAuthService   appClusterPhyAuthService;

    @Autowired
    private AppClusterLogicAuthService appClusterLogicAuthService;

    @Autowired
    private ClusterContextManager      clusterContextManager;

    private static final FutureUtil futureUtil = FutureUtil.initBySystemAvailableProcessors("AppClusterPhyAuthManagerImpl",100);

    @Override
    public List<AppClusterPhyAuth> getByClusterPhyListAndAppId(Integer appId, List<ClusterPhy> clusterPhyList) {
        List<AppClusterPhyAuth> appClusterPhyAuthList = Lists.newCopyOnWriteArrayList();
        if (CollectionUtils.isEmpty(clusterPhyList)) {
            return appClusterPhyAuthList;
        }

        if (!appService.isAppExists(appId)) {
            appClusterPhyAuthList = clusterPhyList.stream()
                    .map(r -> appClusterPhyAuthService.buildClusterPhyAuth(appId, r.getCluster(), AppClusterPhyAuthEnum.NO_PERMISSIONS))
                    .collect(Collectors.toList());
            return appClusterPhyAuthList;
        }

        if (appService.isSuperApp(appId)) {
            appClusterPhyAuthList = clusterPhyList.stream()
                    .map(r -> appClusterPhyAuthService.buildClusterPhyAuth(appId, r.getCluster(), AppClusterPhyAuthEnum.OWN))
                    .collect(Collectors.toList());
            return appClusterPhyAuthList;
        }

        for (ClusterPhy clusterPhy : clusterPhyList) {
            List<AppClusterPhyAuth> finalAppClusterPhyAuthList = appClusterPhyAuthList;
            futureUtil.runnableTask(() -> finalAppClusterPhyAuthList.add(buildAppClusterPhyAuth(appId, clusterPhy)));
        }
        futureUtil.waitExecute();

        return appClusterPhyAuthList;
    }

    @Override
    public List<AppClusterPhyAuth> getAppAccessClusterPhyAuths(Integer appId) {
        List<AppClusterPhyAuth> appAccessClusterPhyAuthList = Lists.newArrayList();
        List<AppClusterLogicAuth> logicClusterAccessAuths   = appClusterLogicAuthService.getLogicClusterAccessAuths(appId);
        if (CollectionUtils.isEmpty(logicClusterAccessAuths)) {
            return appAccessClusterPhyAuthList;
        }
        
        List<List<String>> clusterPhyLists = logicClusterAccessAuths
                                            .stream()
                                            .map(AppClusterLogicAuth::getLogicClusterId)
                                            .map(clusterContextManager::getClusterLogicContext)
                                            .map(ClusterLogicContext::getAssociatedClusterPhyNames)
                                            .collect(Collectors.toList());
        
        for (List<String> clusterPhyList : clusterPhyLists) {
            clusterPhyList.forEach(clusterPhy -> {
                AppClusterPhyAuth appClusterPhyAuth = appClusterPhyAuthService.buildClusterPhyAuth(appId, clusterPhy,
                                                      AppClusterPhyAuthEnum.ACCESS);
                appAccessClusterPhyAuthList.add(appClusterPhyAuth);
            });
        }
        
        return appAccessClusterPhyAuthList;
    }

    private AppClusterPhyAuthEnum getFinalAppClusterLogicAuthEnum(Integer appId, List<Long> associatedClusterLogicIds) {
        Set<Integer> appClusterLogicAuthTypeSet = associatedClusterLogicIds
                .stream()
                .map(clusterLogicId -> appClusterLogicAuthService.getLogicClusterAuth(appId, clusterLogicId))
                .map(AppClusterLogicAuth::getType)
                .collect(Collectors.toSet());

        return computeAppClusterPhyAuthEnum(appClusterLogicAuthTypeSet);
    }

    private AppClusterPhyAuthEnum computeAppClusterPhyAuthEnum(Set<Integer> appClusterLogicAuthTypeSet) {
        AppClusterPhyAuthEnum appClusterPhyAuthEnum = null;

        if (appClusterLogicAuthTypeSet.size() == 1) {
            //当前项目对物理关联的逻辑集群的权限类型相同
            for (Integer type : appClusterLogicAuthTypeSet) {
                appClusterPhyAuthEnum = AppClusterPhyAuthEnum.valueOf(type);
            }
        }else if (appClusterLogicAuthTypeSet.size() > 1){
            //当前项目对物理关联的逻辑集群的权限类型不相同，取最低权限
            for (Integer type : appClusterLogicAuthTypeSet) {
                if (null == appClusterPhyAuthEnum){
                    appClusterPhyAuthEnum  = AppClusterPhyAuthEnum.valueOf(type);
                }else {
                    //0 超级、1 管理、2 访问、-1 无权限
                    if (type > appClusterPhyAuthEnum.getCode()){
                        appClusterPhyAuthEnum = AppClusterPhyAuthEnum.valueOf(type);
                    }
                }
            }

            if(appClusterLogicAuthTypeSet.contains(AppClusterPhyAuthEnum.NO_PERMISSIONS.getCode())) {
                appClusterPhyAuthEnum = AppClusterPhyAuthEnum.NO_PERMISSIONS;
            }
        }else {
            appClusterPhyAuthEnum = AppClusterPhyAuthEnum.NO_PERMISSIONS;
        }
        return appClusterPhyAuthEnum;
    }

    private AppClusterPhyAuth buildAppClusterPhyAuth(Integer appId, ClusterPhy clusterPhy) {
        ClusterPhyContext clusterPhyContext     =   clusterContextManager.getClusterPhyContext(clusterPhy.getCluster());
        if (null == clusterPhyContext) {
            return null;
        }

        List<Long> associatedClusterLogicIds        =   clusterPhyContext.getAssociatedClusterLogicIds();
        AppClusterPhyAuthEnum appClusterPhyAuthEnum = getFinalAppClusterLogicAuthEnum(appId, associatedClusterLogicIds);
        AppClusterPhyAuth appClusterPhyAuth = appClusterPhyAuthService.buildClusterPhyAuth(appId, clusterPhy.getCluster(), appClusterPhyAuthEnum);

        return appClusterPhyAuth;
    }
}

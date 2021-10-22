package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppLogicClusterAuth;

/**
 * App逻辑集群权限增加事件
 * @author wangshu
 * @date 2020/09/19
 */
public class AppLogicClusterAuthAddEvent extends AppAuthEvent {
    private AppLogicClusterAuth clusterAuth;

    public AppLogicClusterAuthAddEvent(Object source, AppLogicClusterAuth logicClusterAuth) {
        super(source);
        this.clusterAuth = logicClusterAuth;
    }

    /**
     * 获取App逻辑权限
     * @return
     */
    public AppLogicClusterAuth getClusterAuth() {
        return this.clusterAuth;
    }
}

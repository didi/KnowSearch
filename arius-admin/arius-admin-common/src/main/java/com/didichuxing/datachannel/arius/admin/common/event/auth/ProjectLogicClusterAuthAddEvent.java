package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;

/**
 * App逻辑集群权限增加事件
 * @author wangshu
 * @date 2020/09/19
 */
public class ProjectLogicClusterAuthAddEvent extends ProjectAuthEvent {
    private AppClusterLogicAuth clusterAuth;

    public ProjectLogicClusterAuthAddEvent(Object source, AppClusterLogicAuth logicClusterAuth) {
        super(source);
        this.clusterAuth = logicClusterAuth;
    }

    /**
     * 获取App逻辑权限
     * @return
     */
    public AppClusterLogicAuth getClusterAuth() {
        return this.clusterAuth;
    }
}
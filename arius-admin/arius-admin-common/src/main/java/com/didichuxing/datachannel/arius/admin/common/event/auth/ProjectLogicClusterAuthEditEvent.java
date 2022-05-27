package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;

/**
 * App逻辑集群权限编辑事件
 * @author wangshu
 * @date 2020/09/19
 */
public class ProjectLogicClusterAuthEditEvent extends ProjectAuthEvent {
    private AppClusterLogicAuth srcAuth;
    private AppClusterLogicAuth tgtAuth;

    public ProjectLogicClusterAuthEditEvent(Object source, AppClusterLogicAuth srcAuth, AppClusterLogicAuth tgtAuth) {
        super(source);
        this.srcAuth = srcAuth;
        this.tgtAuth = tgtAuth;
    }

    public AppClusterLogicAuth getSrcAuth() {
        return srcAuth;
    }

    public AppClusterLogicAuth getTgtAuth() {
        return tgtAuth;
    }
}
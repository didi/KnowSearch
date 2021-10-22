package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppLogicClusterAuth;

/**
 * App逻辑集群权限编辑事件
 * @author wangshu
 * @date 2020/09/19
 */
public class AppLogicClusterAuthEditEvent extends AppAuthEvent {
    private AppLogicClusterAuth srcAuth;
    private AppLogicClusterAuth tgtAuth;

    public AppLogicClusterAuthEditEvent(Object source, AppLogicClusterAuth srcAuth, AppLogicClusterAuth tgtAuth) {
        super(source);
        this.srcAuth = srcAuth;
        this.tgtAuth = tgtAuth;
    }

    public AppLogicClusterAuth getSrcAuth() {
        return srcAuth;
    }

    public AppLogicClusterAuth getTgtAuth() {
        return tgtAuth;
    }
}

package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppLogicClusterAuth;

/**
 * App逻辑集群删除事件
 * @author wangshu
 * @date 2020/09/19
 */
public class AppLogicClusterAuthDeleteEvent extends AppAuthEvent {
    private AppLogicClusterAuth logicClusterAuth;

    public AppLogicClusterAuthDeleteEvent(Object source, AppLogicClusterAuth AppLogicClusterAuth) {
        super(source);
        this.logicClusterAuth = AppLogicClusterAuth;
    }

    public AppLogicClusterAuth getLogicClusterAuth() {
        return logicClusterAuth;
    }
}

package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectClusterLogicAuth;

/**
 * App逻辑集群权限增加事件
 * @author wangshu
 * @date 2020/09/19
 */
public class ProjectLogicClusterAuthAddEvent extends ProjectAuthEvent {
    private ProjectClusterLogicAuth clusterAuth;

    public ProjectLogicClusterAuthAddEvent(Object source, ProjectClusterLogicAuth logicClusterAuth) {
        super(source);
        this.clusterAuth = logicClusterAuth;
    }

    /**
     * 获取App逻辑权限
     * @return
     */
    public ProjectClusterLogicAuth getClusterAuth() {
        return this.clusterAuth;
    }
}
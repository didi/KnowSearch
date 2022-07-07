package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectClusterLogicAuth;

/**
 * App逻辑集群删除事件
 * @author wangshu
 * @date 2020/09/19
 */
public class ProjectLogicClusterAuthDeleteEvent extends ProjectAuthEvent {
    private final ProjectClusterLogicAuth logicClusterAuth;

    public ProjectLogicClusterAuthDeleteEvent(Object source, ProjectClusterLogicAuth projectClusterLogicAuth) {
        super(source);
        this.logicClusterAuth = projectClusterLogicAuth;
    }

    public ProjectClusterLogicAuth getLogicClusterAuth() {
        return logicClusterAuth;
    }
}
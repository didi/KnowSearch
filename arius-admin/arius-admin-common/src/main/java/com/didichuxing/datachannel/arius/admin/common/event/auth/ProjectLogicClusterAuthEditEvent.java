package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectClusterLogicAuth;

/**
 * App逻辑集群权限编辑事件
 * @author wangshu
 * @date 2020/09/19
 */
public class ProjectLogicClusterAuthEditEvent extends ProjectAuthEvent {
    private ProjectClusterLogicAuth srcAuth;
    private ProjectClusterLogicAuth tgtAuth;

    public ProjectLogicClusterAuthEditEvent(Object source, ProjectClusterLogicAuth srcAuth,
                                            ProjectClusterLogicAuth tgtAuth) {
        super(source);
        this.srcAuth = srcAuth;
        this.tgtAuth = tgtAuth;
    }

    public ProjectClusterLogicAuth getSrcAuth() {
        return srcAuth;
    }

    public ProjectClusterLogicAuth getTgtAuth() {
        return tgtAuth;
    }
}
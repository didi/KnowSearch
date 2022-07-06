package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;

/**
 * @author d06679
 * @date 2019/4/18
 */
public class ProjectTemplateAuthEditEvent extends ProjectAuthEvent {

    private ProjectTemplateAuth srcAuth;

    private ProjectTemplateAuth tgtAuth;

    public ProjectTemplateAuthEditEvent(Object source, ProjectTemplateAuth srcAuth, ProjectTemplateAuth tgtAuth) {
        super(source);
        this.srcAuth = srcAuth;
        this.tgtAuth = tgtAuth;
    }

    public ProjectTemplateAuth getSrcAuth() {
        return srcAuth;
    }

    public ProjectTemplateAuth getTgtAuth() {
        return tgtAuth;
    }
}
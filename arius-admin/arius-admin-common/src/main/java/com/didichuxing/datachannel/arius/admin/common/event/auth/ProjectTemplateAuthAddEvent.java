package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectTemplateAuth;

/**
 * @author d06679
 * @date 2019/4/18
 */
public class ProjectTemplateAuthAddEvent extends ProjectAuthEvent {

    private ProjectTemplateAuth projectTemplateAuth;

    public ProjectTemplateAuthAddEvent(Object source, ProjectTemplateAuth projectTemplateAuth) {
        super(source);
        this.projectTemplateAuth = projectTemplateAuth;
    }

    public ProjectTemplateAuth getAppTemplateAuth() {
        return projectTemplateAuth;
    }
}
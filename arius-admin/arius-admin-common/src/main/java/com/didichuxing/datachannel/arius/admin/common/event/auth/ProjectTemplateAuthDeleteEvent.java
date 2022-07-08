package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectTemplateAuth;

/**
 * @author d06679
 * @date 2019/4/18
 */
public class ProjectTemplateAuthDeleteEvent extends ProjectAuthEvent {

    private ProjectTemplateAuth projectTemplateAuth;

    public ProjectTemplateAuthDeleteEvent(Object source, ProjectTemplateAuth projectTemplateAuth) {
        super(source);
        this.projectTemplateAuth = projectTemplateAuth;
    }

    public ProjectTemplateAuth getAppTemplateAuth() {
        return projectTemplateAuth;
    }
}
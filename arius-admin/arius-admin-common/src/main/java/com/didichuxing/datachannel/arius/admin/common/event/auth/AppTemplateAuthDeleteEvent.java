package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;

/**
 * @author d06679
 * @date 2019/4/18
 */
public class AppTemplateAuthDeleteEvent extends AppAuthEvent {

    private AppTemplateAuth appTemplateAuth;

    public AppTemplateAuthDeleteEvent(Object source, AppTemplateAuth appTemplateAuth) {
        super(source);
        this.appTemplateAuth = appTemplateAuth;
    }

    public AppTemplateAuth getAppTemplateAuth() {
        return appTemplateAuth;
    }
}

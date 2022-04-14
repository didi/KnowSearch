package com.didichuxing.datachannel.arius.admin.common.event.auth;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;

/**
 * @author d06679
 * @date 2019/4/18
 */
public class AppTemplateAuthEditEvent extends AppAuthEvent {

    private AppTemplateAuth srcAuth;

    private AppTemplateAuth tgtAuth;

    public AppTemplateAuthEditEvent(Object source, AppTemplateAuth srcAuth, AppTemplateAuth tgtAuth) {
        super(source);
        this.srcAuth = srcAuth;
        this.tgtAuth = tgtAuth;
    }

    public AppTemplateAuth getSrcAuth() {
        return srcAuth;
    }

    public AppTemplateAuth getTgtAuth() {
        return tgtAuth;
    }
}

package com.didichuxing.datachannel.arius.admin.common.event.auth;

import org.springframework.context.ApplicationEvent;

/**
 * @author d06679
 * @date 2019/4/18
 */
public abstract class AppAuthEvent extends ApplicationEvent {

    public AppAuthEvent(Object source) {
        super(source);
    }
}

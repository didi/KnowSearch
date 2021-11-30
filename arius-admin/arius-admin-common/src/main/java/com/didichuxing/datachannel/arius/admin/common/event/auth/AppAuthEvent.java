package com.didichuxing.datachannel.arius.admin.common.event.auth;

import org.springframework.context.ApplicationEvent;

public abstract class AppAuthEvent extends ApplicationEvent {

    protected AppAuthEvent(Object source) {
        super(source);
    }
}

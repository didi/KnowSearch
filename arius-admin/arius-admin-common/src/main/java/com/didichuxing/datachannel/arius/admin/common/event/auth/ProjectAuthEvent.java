package com.didichuxing.datachannel.arius.admin.common.event.auth;

import org.springframework.context.ApplicationEvent;

public abstract class ProjectAuthEvent extends ApplicationEvent {

    protected ProjectAuthEvent(Object source) {
        super(source);
    }
}
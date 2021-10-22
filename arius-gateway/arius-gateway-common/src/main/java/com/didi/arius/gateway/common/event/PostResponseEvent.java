package com.didi.arius.gateway.common.event;

import org.springframework.context.ApplicationEvent;

public abstract class PostResponseEvent extends ApplicationEvent {

    public PostResponseEvent(Object source) {
        super( source );
    }
}

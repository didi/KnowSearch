package com.didichuxing.datachannel.arius.admin.common.event.app;

import org.springframework.context.ApplicationEvent;

public abstract class AppEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    protected AppEvent(Object source) {
        super(source);
    }
}

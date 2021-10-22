package com.didichuxing.datachannel.arius.admin.common.event.app;

import org.springframework.context.ApplicationEvent;

/**
 * @author d06679
 * @date 2019/4/25
 */
public abstract class AppEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public AppEvent(Object source) {
        super(source);
    }
}

package com.didichuxing.datachannel.arius.admin.common.event.metrics;

import org.springframework.context.ApplicationEvent;

public abstract class MetaDataMetricsEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public MetaDataMetricsEvent(Object source) {
        super(source);
    }
}

package com.didichuxing.datachannel.arius.admin.common.event.app;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;

public class ESUserDeleteEvent extends AppEvent {

    private ESUser app;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ESUserDeleteEvent(Object source, ESUser app) {
        super(source);
        this.app = app;
    }

    public ESUser getApp() {
        return app;
    }
}
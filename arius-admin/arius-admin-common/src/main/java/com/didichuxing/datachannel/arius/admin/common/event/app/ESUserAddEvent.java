package com.didichuxing.datachannel.arius.admin.common.event.app;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ESUser;

public class ESUserAddEvent extends AppEvent {

    private ESUser app;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ESUserAddEvent(Object source, ESUser app) {
        super(source);
        this.app = app;
    }

    public ESUser getApp() {
        return app;
    }
}
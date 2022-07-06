package com.didichuxing.datachannel.arius.admin.common.event.app;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ESUser;

public class ESUserEditEvent extends AppEvent {

    private ESUser srcApp;

    private ESUser tgtApp;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ESUserEditEvent(Object source, ESUser srcApp, ESUser tgtApp) {
        super(source);
        this.srcApp = srcApp;
        this.tgtApp = tgtApp;
    }

    public ESUser getSrcApp() {
        return srcApp;
    }

    public ESUser getTgtApp() {
        return tgtApp;
    }
}
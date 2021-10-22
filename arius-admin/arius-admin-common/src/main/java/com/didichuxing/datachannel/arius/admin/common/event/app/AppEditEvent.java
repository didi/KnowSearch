package com.didichuxing.datachannel.arius.admin.common.event.app;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;

/**
 * @author d06679
 * @date 2019/4/25
 */
public class AppEditEvent extends AppEvent {

    private App srcApp;

    private App tgtApp;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public AppEditEvent(Object source, App srcApp, App tgtApp) {
        super(source);
        this.srcApp = srcApp;
        this.tgtApp = tgtApp;
    }

    public App getSrcApp() {
        return srcApp;
    }

    public App getTgtApp() {
        return tgtApp;
    }
}

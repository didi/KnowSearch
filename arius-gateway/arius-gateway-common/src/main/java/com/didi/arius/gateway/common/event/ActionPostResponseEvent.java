package com.didi.arius.gateway.common.event;

import com.didi.arius.gateway.common.metadata.ActionContext;

public class ActionPostResponseEvent extends PostResponseEvent {

    private ActionContext actionContext;

    public ActionPostResponseEvent(Object source, ActionContext actionContext) {
        super( source );
        this.actionContext = actionContext;
    }

    public ActionContext getActionContext(){
        return actionContext;
    }
}

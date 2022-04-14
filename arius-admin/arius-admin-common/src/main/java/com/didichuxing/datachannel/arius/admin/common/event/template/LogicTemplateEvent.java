package com.didichuxing.datachannel.arius.admin.common.event.template;

/**
 * @author d06679
 * @date 2019/4/18
 */
public abstract class LogicTemplateEvent extends TemplateEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    protected LogicTemplateEvent(Object source) {
        super(source);
    }
}

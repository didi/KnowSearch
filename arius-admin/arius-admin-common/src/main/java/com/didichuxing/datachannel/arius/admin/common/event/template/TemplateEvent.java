package com.didichuxing.datachannel.arius.admin.common.event.template;

import org.springframework.context.ApplicationEvent;

/**
 * @author d06679
 * @date 2019/4/18
 */
public abstract class TemplateEvent extends ApplicationEvent {

    protected TemplateEvent(Object source) {
        super(source);
    }
}

package com.didichuxing.datachannel.arius.admin.common.event.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;

/**
 *
 * @author d06679
 * @date 2019/4/18
 */
public class LogicTemplateAddEvent extends LogicTemplateEvent {

    private IndexTemplate newTemplate;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public LogicTemplateAddEvent(Object source, IndexTemplate newTemplate) {
        super(source);
        this.newTemplate = newTemplate;
    }

    public IndexTemplate getNewTemplate() {
        return newTemplate;
    }
}

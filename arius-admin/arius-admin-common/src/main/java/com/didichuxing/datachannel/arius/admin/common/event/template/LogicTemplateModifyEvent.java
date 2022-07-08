package com.didichuxing.datachannel.arius.admin.common.event.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;

/**
 *
 * @author d06679
 * @date 2019/4/18
 */
public class LogicTemplateModifyEvent extends LogicTemplateEvent {

    private IndexTemplate oldTemplate;

    private IndexTemplate newTemplate;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public LogicTemplateModifyEvent(Object source, IndexTemplate oldTemplate, IndexTemplate newTemplate) {
        super(source);
        this.oldTemplate = oldTemplate;
        this.newTemplate = newTemplate;
    }

    public IndexTemplate getOldTemplate() {
        return oldTemplate;
    }

    public IndexTemplate getNewTemplate() {
        return newTemplate;
    }
}

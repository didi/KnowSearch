package com.didichuxing.datachannel.arius.admin.common.event.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;

/**
 *
 * @author d06679
 * @date 2019/4/18
 */
public class LogicTemplateModifyEvent extends LogicTemplateEvent {

    private IndexTemplateLogic oldTemplate;

    private IndexTemplateLogic newTemplate;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public LogicTemplateModifyEvent(Object source, IndexTemplateLogic oldTemplate, IndexTemplateLogic newTemplate) {
        super(source);
        this.oldTemplate = oldTemplate;
        this.newTemplate = newTemplate;
    }

    public IndexTemplateLogic getOldTemplate() {
        return oldTemplate;
    }

    public IndexTemplateLogic getNewTemplate() {
        return newTemplate;
    }
}

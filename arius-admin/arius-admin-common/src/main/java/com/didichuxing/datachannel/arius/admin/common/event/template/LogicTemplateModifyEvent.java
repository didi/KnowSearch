package com.didichuxing.datachannel.arius.admin.common.event.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfo;

/**
 *
 * @author d06679
 * @date 2019/4/18
 */
public class LogicTemplateModifyEvent extends LogicTemplateEvent {

    private IndexTemplateInfo oldTemplate;

    private IndexTemplateInfo newTemplate;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public LogicTemplateModifyEvent(Object source, IndexTemplateInfo oldTemplate, IndexTemplateInfo newTemplate) {
        super(source);
        this.oldTemplate = oldTemplate;
        this.newTemplate = newTemplate;
    }

    public IndexTemplateInfo getOldTemplate() {
        return oldTemplate;
    }

    public IndexTemplateInfo getNewTemplate() {
        return newTemplate;
    }
}

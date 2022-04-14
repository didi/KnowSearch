package com.didichuxing.datachannel.arius.admin.common.event.quota;

import org.springframework.context.ApplicationEvent;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.LogicTemplateQuotaUsage;

/**
 * 用户模板cpu达到100%
 * @author d06679
 * @date 2019/4/25
 */
public abstract class TemplateQuotaEvent extends ApplicationEvent {

    private LogicTemplateQuotaUsage templateQuotaUsage;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    protected TemplateQuotaEvent(Object source, LogicTemplateQuotaUsage templateQuotaUsage) {
        super(source);
        this.templateQuotaUsage = templateQuotaUsage;
    }

    public LogicTemplateQuotaUsage getTemplateQuotaUsage() {
        return templateQuotaUsage;
    }
}

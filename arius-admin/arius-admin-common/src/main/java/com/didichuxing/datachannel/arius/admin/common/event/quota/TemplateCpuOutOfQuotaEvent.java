package com.didichuxing.datachannel.arius.admin.common.event.quota;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.LogicTemplateQuotaUsage;

/**
 * 用户模板cpu达到100%
 * @author d06679
 * @date 2019/4/25
 */
public class TemplateCpuOutOfQuotaEvent extends TemplateQuotaEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public TemplateCpuOutOfQuotaEvent(Object source, LogicTemplateQuotaUsage templateQuotaUsage) {
        super(source, templateQuotaUsage);
    }
}

package com.didichuxing.datachannel.arius.admin.common.event.template;

/**
 * 逻辑模板创建pipeline
 *
 * @author shizeying
 * @date 2022/07/22
 */
public class LogicTemplateCreatePipelineEvent extends LogicTemplateEvent{
    private final Integer logicTemplateId;
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public LogicTemplateCreatePipelineEvent(Object source,Integer logicTemplateId) {
        super(source);
        this.logicTemplateId=logicTemplateId;
    }
    
    public Integer getLogicTemplateId() {
        return logicTemplateId;
    }
}
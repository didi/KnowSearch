package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.PipelineManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateCreatePipelineEvent;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 逻辑模板创建pipeline
 *
 * @author shizeying
 * @date 2022/07/22
 */
@Component
public class LogicTemplateCreatePipelineListener implements ApplicationListener<LogicTemplateCreatePipelineEvent> {
    private static final ILog            LOGGER = LogFactory.getLog(LogicTemplateCreatePipelineListener.class);
    @Autowired
    private              PipelineManager pipelineManager;
    
    /**
     * @param logicTemplateCreatePipelineEvent
     */
    @Override
    public void onApplicationEvent(LogicTemplateCreatePipelineEvent logicTemplateCreatePipelineEvent) {
        try {
            final Result<Void> result = pipelineManager.syncPipeline(
                    logicTemplateCreatePipelineEvent.getLogicTemplateId());
            if (result.failed()) {
                LOGGER.warn(
                        "class=LogicTemplateCreatePipelineListener||method=onApplicationEvent||logicTemplateI={}||msg={}",
                        logicTemplateCreatePipelineEvent.getLogicTemplateId()
                
                );
            }
            
        } catch (Exception e) {
                LOGGER.error(
                "class=LogicTemplateCreatePipelineListener||method=onApplicationEvent",
                e);
        }
    }
}
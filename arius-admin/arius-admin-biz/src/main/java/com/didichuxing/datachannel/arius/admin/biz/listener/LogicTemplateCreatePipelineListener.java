package com.didichuxing.datachannel.arius.admin.biz.listener;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.PipelineManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateCreatePipelineEvent;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

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
    @Autowired
    private IndexTemplateService indexTemplateService;
    
    /**
     * @param logicTemplateCreatePipelineEvent
     */
    @Override
    public void onApplicationEvent(LogicTemplateCreatePipelineEvent logicTemplateCreatePipelineEvent) {
        try {
             //保证数据已经刷到数据库，如果立即执行，会存在获取不到数据库中数据的状态，所以等待3s
            TimeUnit.SECONDS.sleep(3);
            Long masterTemplateId = indexTemplateService.getMasterTemplatePhyIdByLogicTemplateId(
                    logicTemplateCreatePipelineEvent.getLogicTemplateId());
            if (masterTemplateId == null) {
                LOGGER.error(
                        "class=LogicTemplateCreatePipelineListener||method=onApplicationEvent||{} get templatePhy is null",
                        logicTemplateCreatePipelineEvent.getLogicTemplateId());
                return;
            }
            final Result<Void> result = pipelineManager.createPipeline(
                    masterTemplateId.intValue());
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
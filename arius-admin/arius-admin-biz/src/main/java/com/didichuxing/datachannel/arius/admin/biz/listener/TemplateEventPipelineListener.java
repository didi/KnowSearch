package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.PipelineManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.TemplateEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 处理模板pipeline
 *
 *
 * @author d06679
 * @date 2019-09-03
 */
@Component
public class TemplateEventPipelineListener implements ApplicationListener<TemplateEvent> {

    private static final ILog       LOGGER = LogFactory.getLog(TemplateEventPipelineListener.class);

    @Autowired
    private PipelineManager templatePipelineManager;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(TemplateEvent event) {
        try {
            if (event instanceof PhysicalTemplateAddEvent) {
                handlePhysicalTemplateAddEvent((PhysicalTemplateAddEvent) event);
            } else if (event instanceof PhysicalTemplateDeleteEvent) {
                handlePhysicalTemplateDeleteEvent((PhysicalTemplateDeleteEvent) event);
            } else if (event instanceof LogicTemplateModifyEvent) {
                handleLogicTemplateModifyEvent((LogicTemplateModifyEvent) event);
            } else if (event instanceof PhysicalTemplateModifyEvent) {
                handlePhysicalTemplateModifyEvent((PhysicalTemplateModifyEvent) event);
            }
        } catch (Exception e) {
            LOGGER.error("class=TemplateEventPipelineProcessor||method=onApplicationEvent||errMsg={}", e.getMessage(),
                e);
        }
    }

    /****************************************************** private methods ******************************************************/
    private void handlePhysicalTemplateModifyEvent(PhysicalTemplateModifyEvent event) throws ESOperateException {
        PhysicalTemplateModifyEvent e = event;
        LOGGER.info("class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateModifyEvent||templateName={}",
            e.getOldTemplate().getName());
        if (templatePipelineManager.editFromTemplatePhysical(e.getOldTemplate(), e.getNewTemplate(),
            e.getLogicWithPhysical())) {
            LOGGER.info("class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateModifyEvent||templateName={}||msg=succ",
                e.getOldTemplate().getName());
        } else {
            LOGGER.warn("class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateModifyEvent||templateName={}||msg=fail",
                e.getOldTemplate().getName());
        }
    }

    private void handleLogicTemplateModifyEvent(LogicTemplateModifyEvent event) {
        LogicTemplateModifyEvent e = event;
        final Result<Void> result = templatePipelineManager.editFromTemplateLogic(e.getOldTemplate(),
                e.getNewTemplate());
        if (result.success()) {
            LOGGER.info("class=TemplateEventPipelineListener||method=onApplicationEvent||msg=LogicTemplateModifyEvent||templateName={}||msg=succ",
                e.getOldTemplate().getName());
        } else {
            LOGGER.warn("class=TemplateEventPipelineListener||method=onApplicationEvent||msg=LogicTemplateModifyEvent||templateName={}||msg=fail",
                e.getOldTemplate().getName());
        }
    }

    private void handlePhysicalTemplateDeleteEvent(PhysicalTemplateDeleteEvent event) throws ESOperateException {
        PhysicalTemplateDeleteEvent e = event;
        if (templatePipelineManager.deletePipeline(e.getDelTemplate())) {
            LOGGER.info("class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateDeleteEvent||templateName={}||msg=succ",
                e.getDelTemplate().getName());
        } else {
            LOGGER.warn("class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateDeleteEvent||templateName={}||msg=fail",
                e.getDelTemplate().getName());
        }
    }

    private void handlePhysicalTemplateAddEvent(PhysicalTemplateAddEvent event) throws ESOperateException {
        PhysicalTemplateAddEvent e = event;
        if (templatePipelineManager.createPipeline(e.getNewTemplate(), e.getLogicWithPhysical())) {
            LOGGER.info("class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateAddEvent||templateName={}||msg=succ",
                e.getNewTemplate().getName());
        } else {
            LOGGER.warn("class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateAddEvent||templateName={}||msg=fail",
                e.getNewTemplate().getName());
        }
    }
}
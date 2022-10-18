package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.PipelineManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.TemplateEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.EventException;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理模板pipeline
 *
 *
 * @author d06679
 * @date 2019-09-03
 */
@Component
public class TemplateEventPipelineListener  extends ApplicationRetryListener<TemplateEvent> {

    private static final ILog LOGGER = LogFactory.getLog(TemplateEventPipelineListener.class);

    @Autowired
    private PipelineManager   templatePipelineManager;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public boolean onApplicationRetryEvent(TemplateEvent event) throws EventException {
        try {
            if (event instanceof PhysicalTemplateAddEvent) {
                return handlePhysicalTemplateAddEvent((PhysicalTemplateAddEvent) event);
            } else if (event instanceof PhysicalTemplateDeleteEvent) {
                return handlePhysicalTemplateDeleteEvent((PhysicalTemplateDeleteEvent) event);
            } else if (event instanceof LogicTemplateModifyEvent) {
                return handleLogicTemplateModifyEvent((LogicTemplateModifyEvent) event);
            } else if (event instanceof PhysicalTemplateModifyEvent) {
                return handlePhysicalTemplateModifyEvent((PhysicalTemplateModifyEvent) event);
            }
            return false;
        } catch (Exception e) {
            LOGGER.error("class=TemplateEventPipelineProcessor||method=onApplicationEvent||errMsg={}", e.getMessage(),
                    e);
            throw new EventException(e.getMessage(), e);
        }
    }

    /****************************************************** private methods ******************************************************/
    private boolean handlePhysicalTemplateModifyEvent(PhysicalTemplateModifyEvent event) throws ESOperateException {
        PhysicalTemplateModifyEvent e = event;
        LOGGER.info(
            "class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateModifyEvent||templateName={}",
            e.getOldTemplate().getName());
        if (templatePipelineManager.editFromTemplatePhysical(e.getOldTemplate(), e.getNewTemplate(),
            e.getLogicWithPhysical())) {
            LOGGER.info(
                "class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateModifyEvent||templateName={}||msg=succ",
                e.getOldTemplate().getName());
            return true;
        } else {
            LOGGER.warn(
                "class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateModifyEvent||templateName={}||msg=fail",
                e.getOldTemplate().getName());
            return false;
        }
    }

    private boolean handleLogicTemplateModifyEvent(LogicTemplateModifyEvent event) {
        LogicTemplateModifyEvent e = event;
        final Result<Void> result = templatePipelineManager.editFromTemplateLogic(e.getOldTemplate(),
            e.getNewTemplate());
        if (result.success()) {
            LOGGER.info(
                "class=TemplateEventPipelineListener||method=onApplicationEvent||msg=LogicTemplateModifyEvent||templateName={}||msg=succ",
                e.getOldTemplate().getName());
            return true;
        } else {
            LOGGER.warn(
                "class=TemplateEventPipelineListener||method=onApplicationEvent||msg=LogicTemplateModifyEvent||templateName={}||msg=fail",
                e.getOldTemplate().getName());
            return false;
        }
    }

    private boolean handlePhysicalTemplateDeleteEvent(PhysicalTemplateDeleteEvent event) throws ESOperateException {
        PhysicalTemplateDeleteEvent e = event;
        Result<Void> result = templatePipelineManager.deletePipeline(e.getDelTemplate().getId().intValue());
        if (result.success()) {
            LOGGER.info(
                "class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateDeleteEvent||templateName={}||msg=succ",
                e.getDelTemplate().getName());
            return true;
        } else {
            LOGGER.warn(
                "class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateDeleteEvent||templateName={}||msg={}",
                e.getDelTemplate().getName(),result.getMessage());
            return false;
        }
    }

    private boolean handlePhysicalTemplateAddEvent(PhysicalTemplateAddEvent event) throws ESOperateException {
        PhysicalTemplateAddEvent e = event;
        if (templatePipelineManager.createPipeline(e.getNewTemplate(), e.getLogicWithPhysical())) {
            LOGGER.info(
                "class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateAddEvent||templateName={}||msg=succ",
                e.getNewTemplate().getName());
            return true;
        } else {
            LOGGER.warn(
                "class=TemplateEventPipelineListener||method=onApplicationEvent||msg=PhysicalTemplateAddEvent||templateName={}||msg=fail",
                e.getNewTemplate().getName());
            return false;
        }
    }
}
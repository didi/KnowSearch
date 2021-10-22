package com.didichuxing.datachannel.arius.admin.biz.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.TemplatePipelineManager;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.TemplateEvent;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

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
    private TemplatePipelineManager templatePipelineManager;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(TemplateEvent event) {
        try {
            if (event instanceof PhysicalTemplateAddEvent) {

                PhysicalTemplateAddEvent e = (PhysicalTemplateAddEvent) event;
                if (templatePipelineManager.createPipeline(e.getNewTemplate(), e.getLogicWithPhysical())) {
                    LOGGER.info("method=onApplicationEvent||msg=PhysicalTemplateAddEvent||templateName={}||msg=succ",
                        e.getNewTemplate().getName());
                } else {
                    LOGGER.warn("method=onApplicationEvent||msg=PhysicalTemplateAddEvent||templateName={}||msg=fail",
                        e.getNewTemplate().getName());
                }

            } else if (event instanceof PhysicalTemplateDeleteEvent) {

                PhysicalTemplateDeleteEvent e = (PhysicalTemplateDeleteEvent) event;
                if (templatePipelineManager.deletePipeline(e.getDelTemplate())) {
                    LOGGER.info("method=onApplicationEvent||msg=PhysicalTemplateDeleteEvent||templateName={}||msg=succ",
                        e.getDelTemplate().getName());
                } else {
                    LOGGER.warn("method=onApplicationEvent||msg=PhysicalTemplateDeleteEvent||templateName={}||msg=fail",
                        e.getDelTemplate().getName());
                }

            } else if (event instanceof LogicTemplateModifyEvent) {

                LogicTemplateModifyEvent e = (LogicTemplateModifyEvent) event;
                if (templatePipelineManager.editFromTemplateLogic(e.getOldTemplate(), e.getNewTemplate())) {
                    LOGGER.info("method=onApplicationEvent||msg=LogicTemplateModifyEvent||templateName={}||msg=succ",
                        e.getOldTemplate().getName());
                } else {
                    LOGGER.warn("method=onApplicationEvent||msg=LogicTemplateModifyEvent||templateName={}||msg=fail",
                        e.getOldTemplate().getName());
                }

            } else if (event instanceof PhysicalTemplateModifyEvent) {

                PhysicalTemplateModifyEvent e = (PhysicalTemplateModifyEvent) event;
                LOGGER.info("method=onApplicationEvent||msg=PhysicalTemplateModifyEvent||templateName={}",
                    e.getOldTemplate().getName());
                if (templatePipelineManager.editFromTemplatePhysical(e.getOldTemplate(), e.getNewTemplate(),
                    e.getLogicWithPhysical())) {
                    LOGGER.info("method=onApplicationEvent||msg=PhysicalTemplateModifyEvent||templateName={}||msg=succ",
                        e.getOldTemplate().getName());
                } else {
                    LOGGER.warn("method=onApplicationEvent||msg=PhysicalTemplateModifyEvent||templateName={}||msg=fail",
                        e.getOldTemplate().getName());
                }

            }
        } catch (Exception e) {
            LOGGER.error("class=TemplateEventPipelineProcessor||method=onApplicationEvent||errMsg={}", e.getMessage(),
                e);
        }
    }
}

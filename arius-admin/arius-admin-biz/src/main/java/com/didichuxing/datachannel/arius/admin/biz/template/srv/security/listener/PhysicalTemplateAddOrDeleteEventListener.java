package com.didichuxing.datachannel.arius.admin.biz.template.srv.security.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityService;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateEvent;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

@Component
public class PhysicalTemplateAddOrDeleteEventListener implements ApplicationListener<PhysicalTemplateEvent> {

    private static final ILog LOGGER = LogFactory.getLog(PhysicalTemplateAddOrDeleteEventListener.class);

    @Autowired
    private SecurityService securityService;

    /**
     * 处理物理模板的新建和删除对应的权限事件
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(PhysicalTemplateEvent event) {
        try {
            if (event instanceof PhysicalTemplateAddEvent) {
                PhysicalTemplateAddEvent addEvent = (PhysicalTemplateAddEvent) event;
                IndexTemplatePhy templatePhysical = addEvent.getNewTemplate();

                LOGGER.info("class=PhysicalTemplateAddOrDeleteEventListener||method=onApplicationEvent||event=PhysicalTemplateAddEvent||appid={}||template={}",
                    addEvent.getLogicWithPhysical().getAppId(), templatePhysical.getName());

                securityService.saveAppPhysicalTemplateAuth(templatePhysical,
                    addEvent.getLogicWithPhysical().getAppId(), AppTemplateAuthEnum.OWN.getCode(), 20);
                return;
            }

            if (event instanceof PhysicalTemplateDeleteEvent) {
                PhysicalTemplateDeleteEvent deleteEvent = (PhysicalTemplateDeleteEvent) event;
                IndexTemplatePhy templatePhysical = deleteEvent.getDelTemplate();

                LOGGER.info("class=PhysicalTemplateAddOrDeleteEventListener||method=onApplicationEvent||event=PhysicalTemplateDeleteEvent||appid={}||template={}",
                    deleteEvent.getLogicWithPhysical().getAppId(), templatePhysical.getName());

                securityService.deleteAppPhysicalTemplateAuth(templatePhysical,
                    deleteEvent.getLogicWithPhysical().getAppId(), AppTemplateAuthEnum.OWN.getCode(), 20);
            }
        } catch (Exception e) {
            LOGGER.info("class=PhysicalTemplateAddOrDeleteEventListener||method=onApplicationEvent||errMsg={}", e.getMessage(), e);
        }

    }
}

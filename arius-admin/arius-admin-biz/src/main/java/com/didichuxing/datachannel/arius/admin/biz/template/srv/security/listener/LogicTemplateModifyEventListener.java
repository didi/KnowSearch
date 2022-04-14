package com.didichuxing.datachannel.arius.admin.biz.template.srv.security.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityService;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateModifyEvent;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

@Component
public class LogicTemplateModifyEventListener implements ApplicationListener<LogicTemplateModifyEvent> {

    private static final ILog  LOGGER = LogFactory.getLog(LogicTemplateModifyEventListener.class);

    @Autowired
    private SecurityService    securityService;

    /**
     * 处理逻辑模板APPID发生变更时，对应权限的变更.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(LogicTemplateModifyEvent event) {
        IndexTemplateLogic oldTemplate = event.getOldTemplate();
        IndexTemplateLogic newTemplate = event.getNewTemplate();
        if (oldTemplate.getAppId().equals(newTemplate.getAppId())) {
            return;
        }

        LOGGER.info(
            "class=LogicTemplateModifyEventListener||method=onApplicationEvent||event=LogicTemplateModifyEvent||srcAppid={}||tgtAppid={}||templateId={}",
            oldTemplate.getAppId(), newTemplate.getAppId(), newTemplate.getId());

        securityService.editLogicTemplateOwnApp(newTemplate.getId(), oldTemplate.getAppId(), newTemplate.getAppId(),
            20);
    }
}

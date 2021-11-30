package com.didichuxing.datachannel.arius.admin.biz.template.srv.security.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityService;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppAuthEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppTemplateAuthAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppTemplateAuthDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppTemplateAuthEditEvent;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

@Component
public class AppAuthEventListener implements ApplicationListener<AppAuthEvent> {

    private static final ILog LOGGER = LogFactory.getLog(AppAuthEventListener.class);

    @Autowired
    private SecurityService   securityService;

    /**
     * 处理集群中已经存在的模板的权限变更事件.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(AppAuthEvent event) {
        if (event instanceof AppTemplateAuthAddEvent) {
            AppTemplateAuthAddEvent appTemplateAuthAddEvent = (AppTemplateAuthAddEvent) event;
            AppTemplateAuth appTemplateAuth = appTemplateAuthAddEvent.getAppTemplateAuth();

            LOGGER.info("class=AppAuthEventListener||method=onApplicationEvent||event=AppAuthAddEvent||appid={}||template={}||authType={}",
                appTemplateAuth.getAppId(), appTemplateAuth.getTemplateId(), appTemplateAuth.getType());

            securityService.saveAppLogicTemplateAuth(appTemplateAuth.getAppId(),
                appTemplateAuth.getTemplateId(), appTemplateAuth.getType(), 20);
            return;
        }

        if (event instanceof AppTemplateAuthEditEvent) {
            AppTemplateAuthEditEvent appTemplateAuthEditEvent = (AppTemplateAuthEditEvent) event;
            AppTemplateAuth srcAuth = appTemplateAuthEditEvent.getSrcAuth();
            AppTemplateAuth tgtAuth = appTemplateAuthEditEvent.getTgtAuth();

            LOGGER.info("class=AppAuthEventListener||method=onApplicationEvent||event=AppAuthEditEvent||appid={}||template={}||authType={}",
                tgtAuth.getAppId(), tgtAuth.getTemplateId(), tgtAuth.getType());

            if (!srcAuth.getType().equals(tgtAuth.getType())) {
                securityService.saveAppLogicTemplateAuth(tgtAuth.getAppId(), tgtAuth.getTemplateId(),
                    tgtAuth.getType(), 20);
            }
            return;
        }

        if (event instanceof AppTemplateAuthDeleteEvent) {
            AppTemplateAuthDeleteEvent appTemplateAuthDeleteEvent = (AppTemplateAuthDeleteEvent) event;
            AppTemplateAuth appTemplateAuth = appTemplateAuthDeleteEvent.getAppTemplateAuth();

            LOGGER.info("class=AppAuthEventListener||method=onApplicationEvent||event=AppAuthDeleteEvent||appid={}||template={}||authType={}",
                appTemplateAuth.getAppId(), appTemplateAuth.getTemplateId(), appTemplateAuth.getType());

            securityService.deleteAppLogicTemplateAuth(appTemplateAuth.getAppId(),
                appTemplateAuth.getTemplateId(), appTemplateAuth.getType(), 20);
        }

    }
}

package com.didichuxing.datachannel.arius.admin.biz.template.srv.security.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.event.app.AppEditEvent;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

@Component
public class AppEditEventListener implements ApplicationListener<AppEditEvent> {

    private static final ILog LOGGER = LogFactory.getLog(AppEditEventListener.class);

    @Autowired
    private SecurityService   securityService;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(AppEditEvent event) {
        App srcApp = event.getSrcApp();
        App tgtApp = event.getTgtApp();

        if (srcApp.getVerifyCode().equals(tgtApp.getVerifyCode())) {
            return;
        }

        LOGGER.info("method=onApplicationEvent||appId={}||event=AppEditEvent", srcApp.getId());

        securityService.editAppVerifyCode(tgtApp.getId(), tgtApp.getVerifyCode(), 20);
    }
}

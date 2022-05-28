package com.didichuxing.datachannel.arius.admin.biz.template.srv.security.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityService;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.event.auth.ProjectAuthEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.ProjectTemplateAuthAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.ProjectTemplateAuthDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.ProjectTemplateAuthEditEvent;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

@Component
public class AppAuthEventListener implements ApplicationListener<ProjectAuthEvent> {

    private static final ILog LOGGER = LogFactory.getLog(AppAuthEventListener.class);

    @Autowired
    private SecurityService   securityService;

    /**
     * 处理集群中已经存在的模板的权限变更事件.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ProjectAuthEvent event) {
        if (event instanceof ProjectTemplateAuthAddEvent) {
            ProjectTemplateAuthAddEvent projectTemplateAuthAddEvent = (ProjectTemplateAuthAddEvent) event;
            ProjectTemplateAuth projectTemplateAuth = projectTemplateAuthAddEvent.getAppTemplateAuth();

            LOGGER.info("class=AppAuthEventListener||method=onApplicationEvent||event=AppAuthAddEvent||appid={}||template={}||authType={}",
                projectTemplateAuth.getProjectId(), projectTemplateAuth.getTemplateId(), projectTemplateAuth.getType());

            securityService.saveAppLogicTemplateAuth(projectTemplateAuth.getProjectId(),
                projectTemplateAuth.getTemplateId(), projectTemplateAuth.getType(), 20);
            return;
        }

        if (event instanceof ProjectTemplateAuthEditEvent) {
            ProjectTemplateAuthEditEvent projectTemplateAuthEditEvent = (ProjectTemplateAuthEditEvent) event;
            ProjectTemplateAuth srcAuth = projectTemplateAuthEditEvent.getSrcAuth();
            ProjectTemplateAuth tgtAuth = projectTemplateAuthEditEvent.getTgtAuth();

            LOGGER.info("class=AppAuthEventListener||method=onApplicationEvent||event=AppAuthEditEvent||appid={}||template={}||authType={}",
                tgtAuth.getProjectId(), tgtAuth.getTemplateId(), tgtAuth.getType());

            if (!srcAuth.getType().equals(tgtAuth.getType())) {
                securityService.saveAppLogicTemplateAuth(tgtAuth.getProjectId(), tgtAuth.getTemplateId(),
                    tgtAuth.getType(), 20);
            }
            return;
        }

        if (event instanceof ProjectTemplateAuthDeleteEvent) {
            ProjectTemplateAuthDeleteEvent appTemplateAuthDeleteEvent = (ProjectTemplateAuthDeleteEvent) event;
            ProjectTemplateAuth projectTemplateAuth = appTemplateAuthDeleteEvent.getAppTemplateAuth();

            LOGGER.info("class=AppAuthEventListener||method=onApplicationEvent||event=AppAuthDeleteEvent||appid={}||template={}||authType={}",
                projectTemplateAuth.getProjectId(), projectTemplateAuth.getTemplateId(), projectTemplateAuth.getType());

            securityService.deleteProjectLogicTemplateAuth(projectTemplateAuth.getProjectId(),
                projectTemplateAuth.getTemplateId(), projectTemplateAuth.getType(), 20);
        }

    }
}
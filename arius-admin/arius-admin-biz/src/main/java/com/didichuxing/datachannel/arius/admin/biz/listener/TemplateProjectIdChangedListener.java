package com.didichuxing.datachannel.arius.admin.biz.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

@Component
public class TemplateProjectIdChangedListener implements ApplicationListener<LogicTemplateModifyEvent> {
    private static final ILog               LOGGER = LogFactory.getLog(TemplateProjectIdChangedListener.class);

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    @Override
    public void onApplicationEvent(LogicTemplateModifyEvent logicTemplateEvent) {
        if (null == logicTemplateEvent.getOldTemplate()) {
            return;
        }
        if (null == logicTemplateEvent.getNewTemplate()) {
            return;
        }

        //在模板的projectId发生变更的时候，处理权限问题
        handleTemplateProjectId(logicTemplateEvent.getOldTemplate(), logicTemplateEvent.getNewTemplate());
    }

    /**************************************** private method ****************************************************/
    private void handleTemplateProjectId(IndexTemplate oldIndexTemplate, IndexTemplate newIndexTemplate) {
        Integer logicTemplateId = oldIndexTemplate.getId();

        LOGGER.debug(
                "class=LogicTemplateModifyEventListener||method=handleTemplateProjectId||oldIndexTemplate={}||newIndexTemplate={}",
                JSON.toJSONString(oldIndexTemplate), JSON.toJSONString(newIndexTemplate));

        if (null == newIndexTemplate) {
            return;
        }
        if (newIndexTemplate.getProjectId().intValue() == oldIndexTemplate.getProjectId().intValue()) {
            return;
        }

        //如果模板的projectid发生变更了，代表模板的管理权限发生变更，但是原projectid还要拥有模板的读写权限
        //给原projectid赋予索引的读写权限
        Result<Void> result = projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(
            oldIndexTemplate.getProjectId(), logicTemplateId, ProjectTemplateAuthEnum.RW,
             AriusUser.SYSTEM.getDesc());

        LOGGER.debug("class=LogicTemplateModifyEventListener||method=handleTemplateProjectId||result={}", result.success());

    }
}
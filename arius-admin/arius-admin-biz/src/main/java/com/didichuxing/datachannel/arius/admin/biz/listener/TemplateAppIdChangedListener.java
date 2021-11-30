package com.didichuxing.datachannel.arius.admin.biz.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.event.template.LogicTemplateModifyEvent;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

@Component
public class TemplateAppIdChangedListener implements ApplicationListener<LogicTemplateModifyEvent> {
    private static final ILog           LOGGER = LogFactory.getLog(TemplateAppIdChangedListener.class);

    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Override
    public void onApplicationEvent(LogicTemplateModifyEvent logicTemplateEvent) {
        if (null == logicTemplateEvent.getOldTemplate()) {
            return;
        }
        if (null == logicTemplateEvent.getNewTemplate()) {
            return;
        }

        //在模板的appid发生变更的时候，处理权限问题
        handleTemplateAppid(logicTemplateEvent.getOldTemplate(), logicTemplateEvent.getNewTemplate());
    }

    /**************************************** private method ****************************************************/
    private void handleTemplateAppid(IndexTemplateLogic oldIndexTemplate, IndexTemplateLogic newIndexTemplate) {
        Integer logicTemplateId = oldIndexTemplate.getId();

        if (!EnvUtil.isOnline()) {
            LOGGER.info(
                "class=LogicTemplateModifyEventListener||method=handleTemplateAppid||oldIndexTemplate={}||newIndexTemplate={}",
                JSON.toJSONString(oldIndexTemplate), JSON.toJSONString(newIndexTemplate));
        }

        if (null == newIndexTemplate) {
            return;
        }
        if (newIndexTemplate.getAppId().intValue() == oldIndexTemplate.getAppId().intValue()) {
            return;
        }

        //如果模板的appid发生变更了，代表模板的管理权限发生变更，但是原appid还要拥有模板的读写权限
        //给原appid赋予索引的读写权限
        Result<Void> result = appLogicTemplateAuthService.ensureSetLogicTemplateAuth(oldIndexTemplate.getAppId(),
            logicTemplateId, AppTemplateAuthEnum.RW, oldIndexTemplate.getResponsible(), AriusUser.SYSTEM.getDesc());

        if (!EnvUtil.isOnline()) {
            LOGGER.info("class=LogicTemplateModifyEventListener||method=handleTemplateAppid||result={}",
                result.success());
        }
    }
}

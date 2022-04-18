package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.template;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseConsoleTemplateController {

    protected static final ILog    LOGGER = LogFactory.getLog(ConsoleTemplateController.class);

    @Autowired
    protected TemplateLogicService templateLogicService;

    @Autowired
    protected TemplateLabelService templateLabelService;

    @Autowired
    protected AppService           appService;

    /**
     * Check是否有逻辑索引操作权限
     * @param logicId 逻辑模板ID
     * @param appId App Id
     * @return
     */
    protected Result<Void> checkAppAuth(Integer logicId, Integer appId) {

        if (AriusObjUtils.isNull(logicId)) {
            return Result.buildParamIllegal("索引id为空");
        }

        if (AriusObjUtils.isNull(appId)) {
            return Result.buildParamIllegal("应用Id为空");
        }

        IndexTemplateLogic templateLogic = templateLogicService.getLogicTemplateById(logicId);
        if (templateLogic == null) {
            return Result.buildNotExist("索引不存在");
        }

        if (templateLabelService.isImportantIndex(logicId)) {
            return Result.buildOpForBidden("禁止操作重要索引，请联系Arius服务号处理");
        }

        if (appService.isSuperApp(appId)) {
            return Result.buildSucc();
        }

        if (!templateLogic.getAppId().equals(appId)) {
            return Result.buildOpForBidden("您无权对该索引进行操作");
        }

        return Result.buildSucc();
    }

}

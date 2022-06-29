package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.template;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseConsoleTemplateController {

    protected static final ILog    LOGGER = LogFactory.getLog(ConsoleTemplateController.class);

    @Autowired
    protected IndexTemplateService indexTemplateService;

    @Autowired
    protected TemplateLabelService templateLabelService;

    @Autowired
    protected ProjectService projectService;

    /**
     * Check是否有逻辑索引操作权限
     * @param logicId 逻辑模板ID
     * @param projectId App Id
     * @return
     */
    protected Result<Void> checkAppAuth(Integer logicId, Integer projectId) {

        if (AriusObjUtils.isNull(logicId)) {
            return Result.buildParamIllegal("索引id为空");
        }

        if (AriusObjUtils.isNull(projectId)) {
            return Result.buildParamIllegal("应用Id为空");
        }

        IndexTemplate templateLogic = indexTemplateService.getLogicTemplateById(logicId);
        if (templateLogic == null) {
            return Result.buildNotExist("索引不存在");
        }

        if (templateLabelService.isImportantIndex(logicId)) {
            return Result.buildOpForBidden("禁止操作重要索引，请联系Arius服务号处理");
        }

        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return Result.buildSucc();
        }

        if (!templateLogic.getProjectId().equals(projectId)) {
            return Result.buildOpForBidden("您无权对该索引进行操作");
        }

        return Result.buildSucc();
    }

}
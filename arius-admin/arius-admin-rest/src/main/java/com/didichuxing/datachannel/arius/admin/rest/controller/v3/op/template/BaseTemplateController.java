package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseTemplateController {

    protected static final ILog    LOGGER = LogFactory.getLog(TemplateController.class);

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
    protected Result<Void> checkProjectAuth(Integer logicId, Integer projectId) {

        if (AriusObjUtils.isNull(logicId)) {
            return Result.buildParamIllegal("索引id为空");
        }

        if (AriusObjUtils.isNull(projectId)) {
            return Result.buildParamIllegal("应用Id为空");
        }

        Integer projectIdByTemplateLogicId = indexTemplateService.getProjectIdByTemplateLogicId(logicId);
        if (projectIdByTemplateLogicId == null) {
            return Result.buildNotExist("索引不存在");
        }

        if (templateLabelService.isImportantIndex(logicId)) {
            return Result.buildOpForBidden("禁止操作重要索引，请联系Arius服务号处理");
        }
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectIdByTemplateLogicId, projectId);
        if (result.failed()) {
            return result;
        }
    
        

        return Result.buildSucc();
    }

}
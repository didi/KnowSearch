package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
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
     *
     * @param logicId 逻辑模板ID
     * @return
     */
    protected Result<Void> checkProjectAuth(Integer logicId) {

      

        if (templateLabelService.isImportantIndex(logicId)) {
            return Result.buildOpForBidden("禁止操作重要索引，请联系Arius服务号处理");
        }
       
        

        return Result.buildSucc();
    }

}
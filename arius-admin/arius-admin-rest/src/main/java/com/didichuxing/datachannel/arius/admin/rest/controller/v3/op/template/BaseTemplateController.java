package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.service.ProjectService;

public class BaseTemplateController {

    protected static final ILog    LOGGER = LogFactory.getLog(TemplateController.class);

    @Autowired
    protected IndexTemplateService indexTemplateService;

   

    @Autowired
    protected ProjectService       projectService;

    /**
     * Check是否有逻辑索引操作权限
     *
     * @param logicId 逻辑模板ID
     * @return
     */
    protected Result<Void> checkProjectAuth(Integer logicId) {


        return Result.buildSucc();
    }

}
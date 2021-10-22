package com.didichuxing.datachannel.arius.admin.task.op;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.consensual.ConsensualConstant;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author d06679
 * @date 2019-08-29
 */
@Task(name = "platformMetaCheckTaskDriver", description = "元数据校验任务", cron = "0 30 20 * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class PlatFormMetaCheckTaskDriver implements Job {

    private static final ILog           LOGGER = LogFactory.getLog(PlatFormMetaCheckTaskDriver.class);

    @Autowired
    private TemplateLogicManager        templateLogicManager;

    @Autowired
    private TemplatePhyManager          templatePhyManager;

    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=PlatFormMetaCheckTaskDriver||method=execute||msg=templateLogicService.checkMeta");
        templateLogicManager.checkAllLogicTemplatesMeta();

        LOGGER.info("class=PlatFormMetaCheckTaskDriver||method=execute||msg=templatePhyService.checkMeta");
        templatePhyManager.checkMeta();

        LOGGER.info("class=PlatFormMetaCheckTaskDriver||method=execute||msg=appAuthService.checkMeta");
        appLogicTemplateAuthService.deleteExcessTemplateAuthsIfNeed(true);

        return "success";
    }
}

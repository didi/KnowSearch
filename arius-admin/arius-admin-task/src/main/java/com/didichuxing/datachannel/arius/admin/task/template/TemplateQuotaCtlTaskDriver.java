package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.consensual.ConsensualConstant;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author d06679
 * Created by d06679 on 2018/3/14.
 */
@Task(name = "templateQuotaCtlTaskDriver", description = "Quota使用率统计任务", cron = "0 */15 * * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class TemplateQuotaCtlTaskDriver implements Job {

    private static final ILog    LOGGER = LogFactory.getLog(TemplateQuotaCtlTaskDriver.class);

    @Autowired
    private TemplateQuotaCtlTask task;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=TemplateQuotaCtlTaskDriver||method=execute||msg=templateQuotaCtlTask start.");
        if (task.execute()) {
            return "success";
        }
        return "fail";
    }
}

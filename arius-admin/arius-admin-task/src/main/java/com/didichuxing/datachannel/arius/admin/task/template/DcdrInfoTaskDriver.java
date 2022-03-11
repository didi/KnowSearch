package com.didichuxing.datachannel.arius.admin.task.template;

import org.springframework.beans.factory.annotation.Autowired;

import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

@Task(name = "DcdrInfoTaskDriver", description = "采集dcdr相关数据", cron = "0 0/3 * * * ? *", autoRegister = true)
public class DcdrInfoTaskDriver implements Job {

    private static final ILog    LOGGER = LogFactory.getLog(DcdrInfoTaskDriver.class);

    @Autowired
    private DcdrInfoTask task;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=TemplateQuotaCtlTaskDriver||method=execute||msg=templateQuotaCtlTask start.");
        if (task.execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }
}

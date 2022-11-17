package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.dsl.DslTemplateDelExpiredJob;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "DslTemplateDelExpiredRandomTask", description = "删除过期查询模板", cron = "0 0 5 */1 * ?", autoRegister = true)
public class DslTemplateDelExpiredRandomTask implements Job {
    private static final ILog LOGGER = LogFactory.getLog(DslTemplateDelExpiredRandomTask.class);

    @Autowired
    private DslTemplateDelExpiredJob dslTemplateDelExpiredJob;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=DslTemplateDelExpiredRandomTask||method=execute||msg=start");

        dslTemplateDelExpiredJob.handleJobTask("");

        return TaskResult.SUCCESS;
    }
}

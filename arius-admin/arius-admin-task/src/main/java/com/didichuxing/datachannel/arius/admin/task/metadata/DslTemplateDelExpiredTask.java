package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.dsl.DslTemplateDelExpiredJob;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "deleteExpiredDslTemplateJobHandler", description = "删除过期查询模板", cron = "0 0 5 */1 * ?", autoRegister = true)
public class DslTemplateDelExpiredTask implements Job {
    private final static Logger LOGGER = LoggerFactory.getLogger(DslTemplateDelExpiredTask.class);

    @Autowired
    private DslTemplateDelExpiredJob dslTemplateDelExpiredJob;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=DslTemplateDelExpiredTask||method=syncTaskStatus||msg=start");

        return dslTemplateDelExpiredJob.handleJobTask("");
    }
}

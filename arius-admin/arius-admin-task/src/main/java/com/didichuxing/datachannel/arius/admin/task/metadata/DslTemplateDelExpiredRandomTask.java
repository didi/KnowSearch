package com.didichuxing.datachannel.arius.admin.task.metadata;

import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.metadata.job.dsl.DslTemplateDelExpiredJob;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;

@Task(name = "DslTemplateDelExpiredRandomTask", description = "删除过期查询模板", cron = "0 0 5 */1 * ?", autoRegister = true)
public class DslTemplateDelExpiredRandomTask implements Job {

    @Autowired
    private DslTemplateDelExpiredJob dslTemplateDelExpiredJob;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {

        dslTemplateDelExpiredJob.handleJobTask("");

        return TaskResult.buildSuccess();
    }
}

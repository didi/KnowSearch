package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.dsl.DslTemplateUpdateNearestQueryLimitJob;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cjm
 * 更新最近的DSL模版限流值任务
 * 每分钟执行一次
 */
@Task(name = "DslTemplateUpdateNearestQueryLimitRandomTask", description = "更新最近的DSL模版限流值任务", cron = "0 */5 * * * ?", autoRegister = true)
public class DslTemplateUpdateNearestQueryLimitRandomTask implements Job {

    @Autowired
    private DslTemplateUpdateNearestQueryLimitJob dslTemplateUpdateNearestQueryLimitJob;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        // 更新最近的DSL模版限流值任务
        dslTemplateUpdateNearestQueryLimitJob.handleJobTask("");
        return TaskResult.buildSuccess();
    }
}

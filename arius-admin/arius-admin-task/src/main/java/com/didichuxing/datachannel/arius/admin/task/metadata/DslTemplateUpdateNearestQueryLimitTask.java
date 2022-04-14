package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.dsl.DslTemplateUpdateNearestQueryLimitJob;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cjm
 * 更新最近的DSL模版限流值任务
 * 每分钟执行一次
 */
@Task(name = "dslTemplateUpdateNearestQueryLimitTask", description = "更新最近的DSL模版限流值任务", cron = "0 */1 * * * ?", autoRegister = true)
public class DslTemplateUpdateNearestQueryLimitTask implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(DslTemplateUpdateNearestQueryLimitTask.class);

    @Autowired
    private DslTemplateUpdateNearestQueryLimitJob dslTemplateUpdateNearestQueryLimitJob;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=DslTemplateUpdateNearestQueryLimitTask||method=execute||msg=start");
        // 更新最近的DSL模版限流值任务
        dslTemplateUpdateNearestQueryLimitJob.handleJobTask("");
        return TaskResult.SUCCESS;
    }
}

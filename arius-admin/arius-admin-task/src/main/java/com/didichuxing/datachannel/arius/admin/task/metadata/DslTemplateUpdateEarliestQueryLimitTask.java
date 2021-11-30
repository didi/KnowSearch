package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.dsl.DslTemplateUpdateEarliestQueryLimitJob;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cjm
 * 定时更新历史DSL模版限流值任务
 * 每天上午6点执行一次
 */
@Task(name = "dslTemplateUpdateEarliestQueryLimitTask", description = "定时更新历史DSL模版限流值任务", cron = "0 0 6 */1 * ?", autoRegister = true)
public class DslTemplateUpdateEarliestQueryLimitTask implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(DslTemplateUpdateEarliestQueryLimitTask.class);

    @Autowired
    private DslTemplateUpdateEarliestQueryLimitJob dslTemplateUpdateEarliestQueryLimitJob;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=DslTemplateUpdateEarliestQueryLimitTask||method=execute||msg=start");
        // 更新历史DSL模版限流值任务
        dslTemplateUpdateEarliestQueryLimitJob.handleJobTask("");
        return TaskResult.SUCCESS;
    }
}

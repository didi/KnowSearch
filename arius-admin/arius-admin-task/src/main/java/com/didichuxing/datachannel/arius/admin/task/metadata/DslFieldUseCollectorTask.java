package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.dsl.DslFieldUseCollector;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//@Task(name = "dslFieldUseCollectorTask", description = "分析dsl查询使用的字段信息", cron = "0 30 3 */1 * ?", autoRegister = false)
public class DslFieldUseCollectorTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(DslFieldUseCollectorTask.class);

    @Autowired
    private DslFieldUseCollector dslFieldUseCollector;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=DslFieldUseCollectorTask||method=execute||msg=start");
        dslFieldUseCollector.handleJobTask("");

        return TaskResult.SUCCESS;
    }
}

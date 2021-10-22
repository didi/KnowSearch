package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.dsl.DslFieldUseCollector;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "dslFieldUseCollectorTask", description = "分析dsl查询使用的字段信息", cron = "0 30 3 */1 * ?", autoRegister = false)
public class DslFieldUseCollectorTask implements Job {
    private final static Logger LOGGER = LoggerFactory.getLogger(DslFieldUseCollectorTask.class);

    @Autowired
    private DslFieldUseCollector dslFieldUseCollector;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=DslFieldUseCollectorTask||method=syncTaskStatus||msg=start");
        return dslFieldUseCollector.handleJobTask("");
    }
}

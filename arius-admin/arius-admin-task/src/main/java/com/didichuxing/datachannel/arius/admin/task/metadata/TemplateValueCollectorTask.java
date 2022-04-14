package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.template.TemplateValueCollector;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//@Task(name = "templateValueCollectorTask", description = "索引价值统计任务", cron = "0 20 12 * * ?", autoRegister = true)
public class TemplateValueCollectorTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateValueCollectorTask.class);

    @Autowired
    private TemplateValueCollector templateValueCollector;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=TemplateValueCollectorTask||method=execute||msg=start");

        templateValueCollector.handleJobTask("");

        return TaskResult.SUCCESS;
    }
}

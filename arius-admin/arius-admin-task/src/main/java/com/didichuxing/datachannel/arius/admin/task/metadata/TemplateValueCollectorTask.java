package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.template.TemplateValueCollector;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "templateValueCollectorTask", description = "索引价值统计任务", cron = "0 20 12 * * ?", autoRegister = true)
public class TemplateValueCollectorTask implements Job {
    private final static Logger LOGGER = LoggerFactory.getLogger(TemplateValueCollectorTask.class);

    @Autowired
    private TemplateValueCollector templateValueCollector;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=TemplateValueCollectorTask||method=syncTaskStatus||msg=start");

        return templateValueCollector.handleJobTask("");
    }
}

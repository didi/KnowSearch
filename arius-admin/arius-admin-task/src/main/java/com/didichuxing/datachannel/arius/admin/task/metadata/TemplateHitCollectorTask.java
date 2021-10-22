package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.template.TemplateHitCollector;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "templateHitCollector", description = "模版使用情况分析", cron = "0 0 1 */1 * ?", autoRegister = true)
public class TemplateHitCollectorTask implements Job {
    private final static Logger LOGGER = LoggerFactory.getLogger(TemplateHitCollectorTask.class);

    @Autowired
    private TemplateHitCollector templateHitCollector;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=TemplateHitCollectorTask||method=syncTaskStatus||msg=start");

        return templateHitCollector.handleJobTask("");
    }
}

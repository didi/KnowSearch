package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.template.TemplateHitCollector;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//@Task(name = "templateHitCollector", description = "模版使用情况分析", cron = "0 0 1 */1 * ?", autoRegister = true)
public class TemplateHitCollectorTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateHitCollectorTask.class);

    @Autowired
    private TemplateHitCollector templateHitCollector;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=TemplateHitCollectorTask||method=execute||msg=start");

        templateHitCollector.handleJobTask("");

        return TaskResult.SUCCESS;
    }
}

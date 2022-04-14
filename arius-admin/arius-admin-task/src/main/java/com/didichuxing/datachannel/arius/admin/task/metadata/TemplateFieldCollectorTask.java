package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.template.TemplateFieldCollector;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//@Task(name = "indexTemplateFieldCollector", description = "分析目前集群中所有索引模板字段信息", cron = "0 30 7 */1 * ?", autoRegister = true)
public class TemplateFieldCollectorTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateFieldCollectorTask.class);

    @Autowired
    private TemplateFieldCollector templateFieldCollector;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=TemplateFieldCollectorTask||method=execute||msg=start");

        templateFieldCollector.handleJobTask("");

        return TaskResult.SUCCESS;
    }
}

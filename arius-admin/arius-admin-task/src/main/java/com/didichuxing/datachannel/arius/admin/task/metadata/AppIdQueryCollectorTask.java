package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.appid.AppIdQueryCollector;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "appIdQueryCollectorTask", description = "分析昨天每个应用查询情况", cron = "0 40 5 */1 * ?", autoRegister = true)
public class AppIdQueryCollectorTask implements Job {
    private final static Logger LOGGER = LoggerFactory.getLogger(AppIdQueryCollectorTask.class);

    @Autowired
    private AppIdQueryCollector appIdQueryCollector;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=AppIdQueryCollectorTask||method=syncTaskStatus||msg=start");
        return appIdQueryCollector.handleJobTask("");
    }
}

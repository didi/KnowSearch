package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.query.QueryStatisticsCollector;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "queryStatisticsCollector", description = "访问统计", cron = "0 30 1 */1 * ?", autoRegister = true)
public class QueryStatisticsCollectorTask implements Job {
    private final static Logger LOGGER = LoggerFactory.getLogger(QueryStatisticsCollectorTask.class);

    @Autowired
    private QueryStatisticsCollector  queryStatisticsCollector;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=QueryStatisticsCollectorTask||method=syncTaskStatus||msg=start");

        return queryStatisticsCollector.handleJobTask("");
    }
}

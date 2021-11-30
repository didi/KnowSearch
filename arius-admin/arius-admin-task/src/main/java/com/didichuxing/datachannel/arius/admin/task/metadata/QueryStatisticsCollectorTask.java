package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.query.QueryStatisticsCollector;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "queryStatisticsCollector", description = "访问统计", cron = "0 30 1 */1 * ?", autoRegister = true)
public class QueryStatisticsCollectorTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryStatisticsCollectorTask.class);

    @Autowired
    private QueryStatisticsCollector  queryStatisticsCollector;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=QueryStatisticsCollectorTask||method=execute||msg=start");

        queryStatisticsCollector.handleJobTask("");

        return TaskResult.SUCCESS;
    }
}

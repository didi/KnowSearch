package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.query.QueryMaxQpsLimitCollector;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//@Task(name = "maxQpsQueryLimitCollector", description = "分析昨天每个查询语句最大QPS", cron = "0 30 1 */1 * ?", autoRegister = true)
public class QueryMaxQpsLimitCollectorTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryMaxQpsLimitCollectorTask.class);

    @Autowired
    private QueryMaxQpsLimitCollector queryMaxQpsLimitCollector;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=QueryMaxQpsLimitCollectorTask||method=execute||msg=start");

        queryMaxQpsLimitCollector.handleJobTask("");

        return TaskResult.SUCCESS;
    }
}

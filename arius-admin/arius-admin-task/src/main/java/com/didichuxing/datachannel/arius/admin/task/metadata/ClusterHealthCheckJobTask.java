package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.ClusterHealthCheckJobHandler;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//@Task(name = "clusterHealthCheckJobTask", description = "集群健康检查", cron = "0 0 3 1/1 * ? *", autoRegister = true)
public class ClusterHealthCheckJobTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterHealthCheckJobTask.class);

    @Autowired
    private ClusterHealthCheckJobHandler clusterHealthCheckJobHandler;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ClusterHealthCheckJobTask||method=execute||msg=start");
        clusterHealthCheckJobHandler.handleJobTask("");

        return TaskResult.SUCCESS;
    }
}

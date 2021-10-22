package com.didichuxing.datachannel.arius.admin.task.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.ClusterMonitorJobHandler;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;

@Task(name = "clusterMonitor", description = "arius 集群信息统计", cron = "0 0/1 * * * ? *", autoRegister = true)
public class ClusterMonitorJobTask implements Job {
    private final static Logger LOGGER = LoggerFactory.getLogger(ClusterMonitorJobTask.class);

    @Autowired
    private ClusterMonitorJobHandler clusterMonitorJobHandler;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ClusterMonitorJobTask||method=syncTaskStatus||msg=start");
        return clusterMonitorJobHandler.handleJobTask("");
    }
}

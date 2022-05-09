package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.ClusterMonitorJobHandler;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.consensual.ConsensualEnum;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "ESClusterMetricsCollectorBroadcastTask", description = "集群指标采集调度任务", cron = "0 0/1 * * * ? *", autoRegister = true, consensual = ConsensualEnum.BROADCAST)
public class ESClusterMetricsCollectorBroadcastTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger( ESClusterMetricsCollectorBroadcastTask.class);

    @Autowired
    private ClusterMonitorJobHandler clusterMonitorJobHandler;
    
    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ESClusterMetricsCollectorBroadcastTask||method=execute||msg=start");
        clusterMonitorJobHandler.handleJobTask("");
        return TaskResult.SUCCESS;
    }
}

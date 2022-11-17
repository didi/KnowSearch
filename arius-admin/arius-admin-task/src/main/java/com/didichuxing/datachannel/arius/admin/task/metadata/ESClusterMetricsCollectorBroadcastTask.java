package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.ClusterMonitorJobHandler;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.consensual.ConsensualEnum;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "ESClusterMetricsCollectorBroadcastTask", description = "集群指标采集调度任务", cron = "0 0/1 * * * ? *", autoRegister = true, consensual = ConsensualEnum.BROADCAST)
public class ESClusterMetricsCollectorBroadcastTask implements Job {
    private static final ILog LOGGER = LogFactory.getLog(ESClusterMetricsCollectorBroadcastTask.class);

    @Autowired
    private ClusterMonitorJobHandler clusterMonitorJobHandler;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ESClusterMetricsCollectorBroadcastTask||method=execute||msg=start");
        clusterMonitorJobHandler.handleJobTask("");
        return TaskResult.SUCCESS;
    }
}

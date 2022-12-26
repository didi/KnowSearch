package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.LogicClusterMonitorJobHandler;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.consensual.ConsensualEnum;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "ESLogicClusterMetricsCollectorBroadcastTask", description = "逻辑集群调度任务", cron = "0 0/1 * * * ? *", autoRegister = true, consensual = ConsensualEnum.BROADCAST)
public class ESLogicClusterMetricsCollectorBroadcastTask implements Job {
    private static final ILog LOGGER = LogFactory
        .getLog(ESLogicClusterMetricsCollectorBroadcastTask.class);

    @Autowired
    private LogicClusterMonitorJobHandler logicClusterMonitorJobHandler;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ESLogicClusterMetricsCollectorBroadcastTask||method=execute||msg=start");
        logicClusterMonitorJobHandler.handleJobTask("");
        return TaskResult.buildSuccess();
    }
}

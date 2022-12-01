package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didiglobal.knowframework.job.core.consensual.ConsensualEnum;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.MonitorJobHandler;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;

@Task(name = "ESNodeAndIndicesMetricsCollectorBroadcastTask", description = "节点和索引指标信息采集调度任务", cron = "0 0/1 * * * ? *", autoRegister = true, consensual = ConsensualEnum.BROADCAST)
public class ESNodeAndIndicesMetricsCollectorBroadcastTask implements Job {
    private static final ILog LOGGER = LogFactory.getLog(ESNodeAndIndicesMetricsCollectorBroadcastTask.class);

    @Autowired
    private MonitorJobHandler   monitorJobHandler;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ESNodeAndIndicesMetricsCollectorBroadcastTask||method=execute||msg=start");
        monitorJobHandler.handleBrocastJobTask("", jobContext.getCurrentWorkerCode(), jobContext.getAllWorkerCodes());
        return TaskResult.buildSuccess();
    }
}

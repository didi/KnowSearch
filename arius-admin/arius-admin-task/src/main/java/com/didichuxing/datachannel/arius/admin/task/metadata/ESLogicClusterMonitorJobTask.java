package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.LogicClusterMonitorJobHandler;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//@Task(name = "esLogicClusterMonitorJob", description = "Logicclustermonitor调度任务", cron = "0 0/1 * * * ? *", autoRegister = true, consensual = ConsensualEnum.BROADCAST)
public class ESLogicClusterMonitorJobTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger( ESLogicClusterMonitorJobTask.class);

    @Autowired
    private LogicClusterMonitorJobHandler logicClusterMonitorJobHandler;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ESLogicClusterMonitorJobTask||method=execute||msg=start");
        logicClusterMonitorJobHandler.handleJobTask("");
        return TaskResult.SUCCESS;
    }
}

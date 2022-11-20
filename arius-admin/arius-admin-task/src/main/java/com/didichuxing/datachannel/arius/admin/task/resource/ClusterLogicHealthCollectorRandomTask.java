package com.didichuxing.datachannel.arius.admin.task.resource;

import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterLogicTask;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 * Created by linyunan on 2021-10-15
 */
@Task(name = "ClusterLogicHealthCollectorRandomTask", description = "采集逻辑集群状态信息", cron = "0 0/2 * * * ? *", autoRegister = true)
public class ClusterLogicHealthCollectorRandomTask extends BaseConcurrentClusterLogicTask implements Job {
    private static final ILog LOGGER = LogFactory.getLog(ClusterLogicHealthCollectorRandomTask.class);

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ClusterLogicHealthCollectorRandomTask||method=execute||msg=start");
        if (execute()) {
            return TaskResult.buildSuccess();
        }
        return TaskResult.buildFail();
    }

    @Override
    public String getTaskName() {
        return "ClusterLogicHealthCollectorRandomTask";
    }

    @Override
    public int poolSize() {
        return 5;
    }

    @Override
    public int current() {
        return 10;
    }

    @Override
    protected boolean executeByClusterLogic(Long clusterLogicId) {
        return clusterLogicManager.updateClusterLogicHealth(clusterLogicId);
    }
}
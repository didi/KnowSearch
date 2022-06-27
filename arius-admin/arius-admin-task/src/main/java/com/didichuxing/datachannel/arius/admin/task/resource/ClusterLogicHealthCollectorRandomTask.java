package com.didichuxing.datachannel.arius.admin.task.resource;

import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterLogicTask;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by linyunan on 2021-10-15
 */
@Task(name = "ClusterLogicHealthCollectorRandomTask", description = "采集逻辑集群状态信息", cron = "0 0/2 * * * ? *", autoRegister = true)
public class ClusterLogicHealthCollectorRandomTask extends BaseConcurrentClusterLogicTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterLogicHealthCollectorRandomTask.class);

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ClusterLogicHealthCollectorRandomTask||method=execute||msg=start");
        if (execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
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
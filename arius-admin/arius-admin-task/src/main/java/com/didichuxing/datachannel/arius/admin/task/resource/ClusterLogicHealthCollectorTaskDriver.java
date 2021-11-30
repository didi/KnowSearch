package com.didichuxing.datachannel.arius.admin.task.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.task.metadata.IndexCatInfoCollectorTask;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;

/**
 * Created by linyunan on 2021-10-15
 */
@Task(name = "ClusterLogicHealthCollectorTaskDriver", description = "采集逻辑集群状态信息", cron = "0 0/2 * * * ? *", autoRegister = true)
public class ClusterLogicHealthCollectorTaskDriver implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexCatInfoCollectorTask.class);

    @Autowired
    private ClusterLogicHealthTask task;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=AriusClusterHealthCollectorTask||method=execute||msg=start");
        if (task.execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }
}

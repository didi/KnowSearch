package com.didichuxing.datachannel.arius.admin.task.resource;

import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;
import com.didichuxing.datachannel.arius.admin.task.metadata.IndicesCatInfoCollectorRandomTask;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 * Created by linyunan on 2021-10-15
 */
@Task(name = "ClusterResourceInfoCollectorRandomTask", description = "采集物理集群资源信息", cron = "0 0/3 * * * ? *", autoRegister = true)
public class ClusterResourceInfoCollectorRandomTask extends BaseConcurrentClusterTask implements Job {
    private static final ILog LOGGER = LogFactory.getLog(ClusterResourceInfoCollectorRandomTask.class);

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ClusterResourceInfoCollectorRandomTask||method=execute||msg=start");
        if (execute()) {
            return TaskResult.buildSuccess();
        }
        return TaskResult.buildFail();
    }

    @Override
    public String getTaskName() {
        return "ClusterResourceInfoCollectorRandomTask";
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
    protected boolean executeByCluster(String cluster) {
        return clusterPhyManager.updateClusterInfo(cluster, AriusUser.SYSTEM.getDesc());
    }
}

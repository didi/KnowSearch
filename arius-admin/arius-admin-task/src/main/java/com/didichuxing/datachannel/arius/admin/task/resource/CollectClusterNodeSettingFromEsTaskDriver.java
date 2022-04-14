package com.didichuxing.datachannel.arius.admin.task.resource;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "collectClusterNodeSettingFromEsTaskDriver", description = "同步节点配置任务", cron = "0 0/3 * * * ?", autoRegister = true)
public class CollectClusterNodeSettingFromEsTaskDriver implements Job {

    private static final ILog LOGGER = LogFactory.getLog(CollectClusterNodeSettingFromEsTaskDriver.class);

    @Autowired
    private CollectClusterNodeSettingFromEsTask task;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=CollectClusterNodeSettingFromEsTaskDriver||method=execute||msg=CollectClusterNodeSettingFromEsTask start.");
        if (task.execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }
}

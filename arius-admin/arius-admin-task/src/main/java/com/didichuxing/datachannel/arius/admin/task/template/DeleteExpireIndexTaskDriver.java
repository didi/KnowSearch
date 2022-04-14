package com.didichuxing.datachannel.arius.admin.task.template;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by d06679 on 2018/3/14.
 */
@Task(name = "deleteExpireIndexTaskDriver", description = "删除过期索引任务", cron = "0 55 23 */1 * ?", autoRegister = true)
public class DeleteExpireIndexTaskDriver implements Job {

    private static final ILog     LOGGER = LogFactory.getLog(DeleteExpireIndexTaskDriver.class);

    @Autowired
    private DeleteExpireIndexTask task;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=DeleteExpireIndexTaskDriver||method=execute||msg=DeleteExpireIndexTask start.");
        if (task.execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }
}

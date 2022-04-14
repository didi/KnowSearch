package com.didichuxing.datachannel.arius.admin.task.template;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author didi
 * Created by d06679 on 2018/3/14.
 */
@Task(name = "syncTemplateMetadateTaskDriver", description = "集群模板元数据同步任务", cron = "0 20 2 * * ?", autoRegister = true)
public class SyncTemplateMetadateTaskDriver implements Job {

    private static final ILog        LOGGER = LogFactory.getLog(SyncTemplateMetadateTaskDriver.class);

    @Autowired
    private SyncTemplateMetadateTask task;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=SyncTemplateMetadateTaskDriver||method=execute||msg=DeleteExpireIndexTask start.");
        if (task.execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }
}

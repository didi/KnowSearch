package com.didichuxing.datachannel.arius.admin.task.capaciryplan;

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
//@Task(name = "capacityAreaCheckTaskDriver", description = "容量检查任务", cron = "0 15/30 * * * ?", autoRegister = true)
public class CapacityAreaCheckTaskDriver implements Job {

    private static final ILog     LOGGER = LogFactory.getLog(CapacityAreaCheckTaskDriver.class);

    @Autowired
    private CapacityAreaCheckTask task;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=CapacityAreaCheckTaskDriver||method=execute||msg=CapacityAreaCheckTask start.");
        if (task.execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }
}

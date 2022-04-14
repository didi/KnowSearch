package com.didichuxing.datachannel.arius.admin.task.workorder;

import com.didichuxing.datachannel.arius.admin.biz.workorder.WorkOrderAutoProcessor;
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
//@Task(name = "workOrderAutoProcessTaskDriver", description = "工单处理任务，定期处理工单", cron = "0 */5 * * * ?", autoRegister = true)
public class WorkOrderAutoProcessTaskDriver implements Job {

    private static final ILog      LOGGER = LogFactory.getLog(WorkOrderAutoProcessTaskDriver.class);

    @Autowired
    private WorkOrderAutoProcessor task;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=WorkOrderAutoProcessTaskDriver||method=execute||msg=WorkOrderAutoProcessor start.");
        task.process();
        return TaskResult.SUCCESS;
    }
}

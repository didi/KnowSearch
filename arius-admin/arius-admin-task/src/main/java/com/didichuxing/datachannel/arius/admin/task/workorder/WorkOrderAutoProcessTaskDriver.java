package com.didichuxing.datachannel.arius.admin.task.workorder;

import com.didichuxing.datachannel.arius.admin.biz.workorder.WorkOrderAutoProcessor;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.consensual.ConsensualConstant;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by d06679 on 2018/3/14.
 */
@Task(name = "workOrderAutoProcessTaskDriver", description = "工单处理任务，定期处理工单", cron = "0 */5 * * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class WorkOrderAutoProcessTaskDriver implements Job {

    private static final ILog      LOGGER = LogFactory.getLog(WorkOrderAutoProcessTaskDriver.class);

    @Autowired
    private WorkOrderAutoProcessor task;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=WorkOrderAutoProcessTaskDriver||method=execute||msg=WorkOrderAutoProcessor start.");
        task.process();
        return "success";
    }
}

package com.didichuxing.datachannel.arius.admin.task.capaciryplan;

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
@Task(name = "capacityAreaPlanTaskDriver", description = "容量规划任务", cron = "0 0 11 * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class CapacityAreaPlanTaskDriver implements Job {

    private static final ILog    LOGGER = LogFactory.getLog(CapacityAreaPlanTaskDriver.class);

    @Autowired
    private CapacityAreaPlanTask task;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=CapacityAreaPlanTaskDriver||method=execute||msg=CapacityAreaPlanTask start.");
        if (task.execute()) {
            return "success";
        }
        return "fail";
    }
}

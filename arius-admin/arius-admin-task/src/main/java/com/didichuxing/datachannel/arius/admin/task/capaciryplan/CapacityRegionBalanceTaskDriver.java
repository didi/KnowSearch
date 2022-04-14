package com.didichuxing.datachannel.arius.admin.task.capaciryplan;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanAreaService;

/**
 * Created by d06679 on 2018/3/14.
 */
//@Task(name = "capacityRegionBalanceTaskDriver", description = "region之间均衡任务", cron = "0 20 12 * * ?", autoRegister = true)
public class CapacityRegionBalanceTaskDriver implements Job {

    private static final ILog       LOGGER = LogFactory.getLog(CapacityRegionBalanceTaskDriver.class);

    @Autowired
    private CapacityPlanAreaService task;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=CapacityRegionBalanceTaskDriver||method=execute||msg=CapacityRegionBalanceTask start.");
        if (task.balanceRegions()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }
}

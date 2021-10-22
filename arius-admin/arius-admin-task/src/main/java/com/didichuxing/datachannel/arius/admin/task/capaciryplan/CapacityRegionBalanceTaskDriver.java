package com.didichuxing.datachannel.arius.admin.task.capaciryplan;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.consensual.ConsensualConstant;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanAreaService;

/**
 * Created by d06679 on 2018/3/14.
 */
@Task(name = "capacityRegionBalanceTaskDriver", description = "region之间均衡任务", cron = "0 20 12 * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class CapacityRegionBalanceTaskDriver implements Job {

    private static final ILog       LOGGER = LogFactory.getLog(CapacityRegionBalanceTaskDriver.class);

    @Autowired
    private CapacityPlanAreaService task;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=CapacityRegionBalanceTaskDriver||method=execute||msg=CapacityRegionBalanceTask start.");
        if (task.balanceRegions()) {
            return "success";
        }
        return "fail";
    }
}

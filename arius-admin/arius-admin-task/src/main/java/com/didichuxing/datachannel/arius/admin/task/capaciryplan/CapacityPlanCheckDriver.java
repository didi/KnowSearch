package com.didichuxing.datachannel.arius.admin.task.capaciryplan;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.consensual.ConsensualConstant;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanAreaService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionTaskService;

/**
 * Created by d06679 on 2018/3/14.
 *
 * 1、检查是否有数据搬迁完成的rack
 * 2、检查是否有需要重试的任务
 *
 */
@Task(name = "capacityPlanCheckDriver", description = "规划任务检查", cron = "0 */10 * * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class CapacityPlanCheckDriver implements Job {

    private static final ILog             LOGGER = LogFactory.getLog(CapacityPlanCheckDriver.class);

    @Autowired
    private CapacityPlanRegionTaskService regionTaskService;

    @Autowired
    private CapacityPlanAreaService       capacityPlanAreaService;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=CapacityPlanCheckDriver||method=execute||msg=CapacityPlanCheckDriver start.");
        regionTaskService.checkTasks();
        capacityPlanAreaService.correctAllAreaRegionAndTemplateRacks();
        return "success";
    }
}

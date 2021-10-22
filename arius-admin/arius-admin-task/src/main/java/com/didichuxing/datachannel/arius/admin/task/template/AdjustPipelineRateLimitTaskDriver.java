package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.consensual.ConsensualConstant;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author didi
 * Created by d06679 on 2018/3/14.
 */
@Task(name = "adjustPipelineRateLimitTaskDriver", description = "Flink写入动态限流", cron = "0 */15 * * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class AdjustPipelineRateLimitTaskDriver implements Job {

    private static final ILog           LOGGER = LogFactory.getLog(AdjustPipelineRateLimitTaskDriver.class);

    @Autowired
    private AdjustPipelineRateLimitTask task;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=AdjustPipelineRateLimitTaskDriver||method=execute||msg=AdjustPipelineRateLimitTaskDriver start.");
        if (task.execute()) {
            return "success";
        }
        return "fail";
    }
}

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
//@Task(name = "adjustPipelineRateLimitTaskDriver", description = "Flink写入动态限流", cron = "0 */15 * * * ?", autoRegister = true)
public class AdjustPipelineRateLimitTaskDriver implements Job {

    private static final ILog           LOGGER = LogFactory.getLog(AdjustPipelineRateLimitTaskDriver.class);

    @Autowired
    private AdjustPipelineRateLimitTask task;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=AdjustPipelineRateLimitTaskDriver||method=execute||msg=AdjustPipelineRateLimitTaskDriver start.");
        if (task.execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }
}

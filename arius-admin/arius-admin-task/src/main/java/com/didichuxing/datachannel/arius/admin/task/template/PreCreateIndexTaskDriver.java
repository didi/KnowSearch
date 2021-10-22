package com.didichuxing.datachannel.arius.admin.task.template;

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
@Task(name = "preCreateIndexTaskDriver",
        description = "明天索引预先创建任务，预先创建平台中模板明天索引，避免凌晨大规模创建索引",
        cron = "0 0 15-19 * * ?",
        autoRegister = true,
        consensual = ConsensualConstant.RANDOM)
public class PreCreateIndexTaskDriver implements Job {

    private static final ILog  LOGGER = LogFactory.getLog(PreCreateIndexTaskDriver.class);

    @Autowired
    private PreCreateIndexTask task;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=PreCreateIndexTaskDriver||method=execute||msg=PreCreateIndexTask start.");
        if (task.execute()) {
            return "success";
        }
        return "fail";
    }
}

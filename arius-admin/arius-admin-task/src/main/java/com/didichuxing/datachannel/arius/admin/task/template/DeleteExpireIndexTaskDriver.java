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
@Task(name = "deleteExpireIndexTaskDriver", description = "删除过期索引任务", cron = "0 05 0/12 * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class DeleteExpireIndexTaskDriver implements Job {

    private static final ILog     LOGGER = LogFactory.getLog(DeleteExpireIndexTaskDriver.class);

    @Autowired
    private DeleteExpireIndexTask task;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=DeleteExpireIndexTaskDriver||method=execute||msg=DeleteExpireIndexTask start.");
        if (task.execute()) {
            return "success";
        }
        return "fail";
    }
}

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
@Task(name = "coldDataMoveTaskDriver", description = "admin冷数据搬迁服务", cron = "0 30 22 * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class ColdDataMoveTaskDriver implements Job {

    private static final ILog LOGGER = LogFactory.getLog(ColdDataMoveTaskDriver.class);

    @Autowired
    private ColdDataMoveTask  coldDataMoveTask;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ColdDataMoveTaskDriver||method=execute||msg=ColdDataMoveTask start.");
        if (coldDataMoveTask.execute()) {
            return "success";
        }
        return "fail";
    }
}

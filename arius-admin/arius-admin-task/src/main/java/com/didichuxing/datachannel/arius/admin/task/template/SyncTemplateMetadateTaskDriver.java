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
@Task(name = "syncTemplateMetadateTaskDriver", description = "集群模板元数据同步任务", cron = "0 20 14 * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class SyncTemplateMetadateTaskDriver implements Job {

    private static final ILog        LOGGER = LogFactory.getLog(SyncTemplateMetadateTaskDriver.class);

    @Autowired
    private SyncTemplateMetadateTask task;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=SyncTemplateMetadateTaskDriver||method=execute||msg=DeleteExpireIndexTask start.");
        if (task.execute()) {
            return "success";
        }
        return "fail";
    }
}

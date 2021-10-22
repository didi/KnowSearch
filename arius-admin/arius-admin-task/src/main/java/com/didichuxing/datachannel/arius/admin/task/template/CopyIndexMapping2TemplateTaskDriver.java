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
@Task(name = "copyIndexMapping2TemplateTaskDriver",
        description = "copyMapping任务，定期将索引中的mapping拷贝到模板中，避免大量的put-mappin",
        cron = "0 45 9/12 * * ?",
        autoRegister = true,
        consensual = ConsensualConstant.RANDOM)
public class CopyIndexMapping2TemplateTaskDriver implements Job {

    private static final ILog             LOGGER = LogFactory.getLog(CopyIndexMapping2TemplateTaskDriver.class);

    @Autowired
    private CopyIndexMapping2TemplateTask task;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=CopyIndexMapping2TemplateTaskDriver||method=execute||msg=DeleteExpireIndexTask start.");
        if (task.execute()) {
            return "success";
        }
        return "fail";
    }
}

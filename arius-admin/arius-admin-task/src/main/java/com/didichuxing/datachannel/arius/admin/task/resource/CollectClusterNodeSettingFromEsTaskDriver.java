package com.didichuxing.datachannel.arius.admin.task.resource;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.consensual.ConsensualConstant;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author d06679
 * @Scheduled(cron = "0 40 0 * * ?")
 * Created by d06679 on 2018/3/14.
 */
@Task(name = "collectClusterNodeSettingFromEsTaskDriver", description = "同步节点配置任务", cron = "0 20 1/12 * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class CollectClusterNodeSettingFromEsTaskDriver implements Job {

    private static final ILog LOGGER = LogFactory.getLog(CollectClusterNodeSettingFromEsTaskDriver.class);

    @Autowired
    private CollectClusterNodeSettingFromEsTask task;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=CollectClusterNodeSettingFromEsTaskDriver||method=execute||msg=CollectClusterNodeSettingFromEsTask start.");
        if (task.execute()) {
            return "success";
        }
        return "fail";
    }
}

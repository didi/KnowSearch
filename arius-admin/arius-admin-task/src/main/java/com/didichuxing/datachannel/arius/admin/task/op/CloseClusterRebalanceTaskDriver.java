package com.didichuxing.datachannel.arius.admin.task.op;

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
@Task(name = "closeClusterRebalanceTaskDriver", description = "关闭集群rebalance任务", cron = "0 40 0 * * ?", autoRegister = true, consensual = ConsensualConstant.RANDOM)
public class CloseClusterRebalanceTaskDriver implements Job {

    private static final ILog         LOGGER = LogFactory.getLog(CloseClusterRebalanceTaskDriver.class);

    @Autowired
    private CloseClusterRebalanceTask task;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=CloseClusterRebalanceTaskDriver||method=execute||msg=sync cluster node info task start.");
        if (task.execute()) {
            return "success";
        }
        return "fail";
    }
}

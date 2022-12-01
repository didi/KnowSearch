package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.index.IndexCatInfoCollector;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "IndicesCatInfoCollectorRandomTask", description = "采集索引Cat/Index基本信息", cron = "0 0/3 * * * ? *", autoRegister = true)
public class IndicesCatInfoCollectorRandomTask implements Job {
    private static final ILog LOGGER = LogFactory.getLog(IndicesCatInfoCollectorRandomTask.class);

    @Autowired
    private IndexCatInfoCollector indexCatInfoCollector;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception{
        LOGGER.info("class=IndicesCatInfoCollectorRandomTask||method=execute||msg=start");
        indexCatInfoCollector.handleJobTask("");
        return TaskResult.buildSuccess();
    }
}
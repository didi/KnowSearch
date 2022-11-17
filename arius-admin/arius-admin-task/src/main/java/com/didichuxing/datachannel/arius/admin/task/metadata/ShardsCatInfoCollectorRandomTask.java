package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.metadata.job.shard.ShardCatInfoCollector;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;

@Task(name = "ShardsCatInfoCollectorRandomTask", description = "采集索引Cat/Shards基本信息", cron = "0 0/3 * * * ? *", autoRegister = true)
public class ShardsCatInfoCollectorRandomTask implements Job {
    private static final ILog LOGGER = LogFactory.getLog(ShardsCatInfoCollectorRandomTask.class);

    @Autowired
    private ShardCatInfoCollector shardCatInfoCollector;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ShardsCatInfoCollectorRandomTask||method=execute||msg=start");
        shardCatInfoCollector.handleJobTask("");
        return TaskResult.SUCCESS;
    }
}

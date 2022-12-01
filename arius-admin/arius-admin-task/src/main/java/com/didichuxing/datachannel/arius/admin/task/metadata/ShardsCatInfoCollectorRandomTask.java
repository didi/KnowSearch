package com.didichuxing.datachannel.arius.admin.task.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.metadata.job.shard.ShardCatInfoCollector;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;

//@Task(name = "ShardsCatInfoCollectorRandomTask", description = "采集索引Cat/Shards基本信息", cron = "0 0/3 * * * ? *", autoRegister = true)
public class ShardsCatInfoCollectorRandomTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardsCatInfoCollectorRandomTask.class);

    @Autowired
    private ShardCatInfoCollector shardCatInfoCollector;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ShardsCatInfoCollectorRandomTask||method=execute||msg=start");
        shardCatInfoCollector.handleJobTask("");
        return TaskResult.SUCCESS;
    }
}

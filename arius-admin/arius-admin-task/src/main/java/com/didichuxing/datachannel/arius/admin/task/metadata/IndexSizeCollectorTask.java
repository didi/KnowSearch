package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.index.IndexSizeCollector;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//@Task(name = "indexSizeCollector", description = "分析目前集群中所有索引大小信息", cron = "0 40 0 */1 * ?", autoRegister = true)
public class IndexSizeCollectorTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexSizeCollectorTask.class);

    @Autowired
    private IndexSizeCollector indexSizeCollector;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=IndexSizeCollectorTask||method=execute||msg=start");

        indexSizeCollector.handleJobTask("");

        return TaskResult.SUCCESS;
    }
}

package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.index.IndexSizeCollector;
import com.didiglobal.logi.auvjob.annotation.Task;
import com.didiglobal.logi.auvjob.core.job.Job;
import com.didiglobal.logi.auvjob.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Task(name = "indexSizeCollector", description = "分析目前集群中所有索引大小信息", cron = "0 40 0 */1 * ?", autoRegister = true)
public class IndexSizeCollectorTask implements Job {
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexSizeCollectorTask.class);

    @Autowired
    private IndexSizeCollector indexSizeCollector;

    @Override
    public Object execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=IndexSizeCollectorTask||method=syncTaskStatus||msg=start");

        return indexSizeCollector.handleJobTask("");
    }
}

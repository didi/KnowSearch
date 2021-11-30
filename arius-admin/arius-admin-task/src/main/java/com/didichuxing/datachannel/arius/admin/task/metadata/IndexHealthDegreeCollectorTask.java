package com.didichuxing.datachannel.arius.admin.task.metadata;

import com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.IndexHealthDegreeCollector;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//@Task(name = "indexHealthDegreeJobHandler", description = "索引健康分计算", cron = "0 0/20 * * * ? *", autoRegister = true)
public class IndexHealthDegreeCollectorTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexHealthDegreeCollectorTask.class);

    @Autowired
    private IndexHealthDegreeCollector indexHealthDegreeCollector;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=IndexHealthDegreeCollectorTask||method=execute||msg=start");

        indexHealthDegreeCollector.handleJobTask("");

        return TaskResult.SUCCESS;
    }
}

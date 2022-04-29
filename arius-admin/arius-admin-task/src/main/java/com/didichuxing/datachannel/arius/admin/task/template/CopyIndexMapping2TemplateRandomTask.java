package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;

/**
 * @author didi
 * Created by d06679 on 2018/3/14.
 */
@Task(name = "CopyIndexMapping2TemplateRandomTask",
        description = "copyMapping任务，定期将索引中的mapping拷贝到模板中，避免大量的put-mappin",
        cron = "0 45 9/12 * * ?",
        autoRegister = true)
public class CopyIndexMapping2TemplateRandomTask extends BaseConcurrentClusterTask implements Job {

    private static final ILog             LOGGER = LogFactory.getLog(CopyIndexMapping2TemplateRandomTask.class);

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=CopyIndexMapping2TemplateRandomTask||method=execute||msg=CopyIndexMapping2TemplateRandomTask start.");
        if (execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    @Override
    public String getTaskName() {
        return "CopyIndexMapping2TemplateRandomTask";
    }

    /**
     * 任务的线程个数
     * @return 任务的线程个数
     */
    @Override
    public int poolSize() {
        return 20;
    }

    /**
     * 并发度
     *
     * @return
     */
    @Override
    public int current() {
        return TaskConcurrentConstants.COPY_INDEX_MAPPING2_TEMPLATE_TASK_CONCURRENT;
    }

    /**
     * 同步处理指定集群
     *
     * @param cluster 集群名字
     */
    @Override
    public boolean executeByCluster(String cluster) throws AdminOperateException {
        return clusterPhyManager.copyMapping(cluster, 5);
    }
}

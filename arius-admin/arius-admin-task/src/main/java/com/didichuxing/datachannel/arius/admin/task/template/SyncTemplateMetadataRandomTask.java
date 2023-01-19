package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 * @author didi
 * Created by d06679 on 2018/3/14.
 */
@Task(name = "SyncTemplateMetadataRandomTask", description = "集群模板元数据同步任务", cron = "0 20 2 * * ?", autoRegister = true)
public class SyncTemplateMetadataRandomTask extends BaseConcurrentClusterTask implements Job {

    private static final ILog LOGGER = LogFactory.getLog(SyncTemplateMetadataRandomTask.class);

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=SyncTemplateMetadataRandomTask||method=execute||msg=SyncTemplateMetadataRandomTask start.");
        if (execute()) {
            return TaskResult.buildSuccess();
        }
        return TaskResult.buildFail();
    }

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    @Override
    public String getTaskName() {
        return "SyncTemplateMetadataRandomTask";
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
        return TaskConcurrentConstants.SYNC_TEMPLATE_META_DATE_TASK_CONCURRENT;
    }

    /**
     * 同步处理指定集群
     *
     * @param cluster 集群名字
     */
    @Override
    public boolean executeByCluster(String cluster) throws AdminOperateException {
        clusterPhyManager.syncTemplateMetaData(cluster, 5);
        return true;
    }
}

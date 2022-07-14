package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.consensual.ConsensualEnum;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by d06679 on 2018/3/14.
 */
@Task(name = "preCreateIndexTaskDriver", description = "明天索引预先创建任务，预先创建平台中模板明天索引，避免凌晨大规模创建索引", cron = "0 0 03-06 * * ?", autoRegister = true, consensual = ConsensualEnum.RANDOM)
public class PreCreateIndexRandomTask extends BaseConcurrentClusterTask implements Job {

    private static final ILog LOGGER = LogFactory.getLog(PreCreateIndexRandomTask.class);

    @Autowired
    private PreCreateManager  templatePreCreateManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=PreCreateIndexRandomTask||method=execute||msg=PreCreateIndexRandomTask start.");
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
        return "PreCreateIndexRandomTask";
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
        return TaskConcurrentConstants.PRE_CREATE_INDEX_TASK_CONCURRENT;
    }

    /**
     * 同步处理指定集群
     *
     * @param cluster 集群名字
     */
    @Override
    public boolean executeByCluster(String cluster) throws AdminOperateException {
        return templatePreCreateManager.preCreateIndex(cluster, 5);
    }
}
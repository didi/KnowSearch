package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.ColdManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by d06679 on 2018/3/14.
 */
@Task(name = "ColdDataMoveRandomTask", description = "admin冷数据搬迁服务", cron = "0 30 22 * * ?", autoRegister = true)
public class ColdDataMoveRandomTask extends BaseConcurrentClusterTask implements Job {

    private static final ILog LOGGER = LogFactory.getLog(ColdDataMoveRandomTask.class);

    @Autowired
    private ColdManager       templateColdManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ColdDataMoveRandomTask||method=execute||msg=ColdDataMoveRandomTask start.");
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
        return "ColdDataMoveRandomTask";
    }

    /**
     * 任务的线程个数
     * @return 任务的线程个数
     */
    @Override
    public int poolSize() {
        return 3;
    }

    /**
     * 并发度
     *
     * @return
     */
    @Override
    public int current() {
        return TaskConcurrentConstants.COLD_DATA_MOVE_TASK_CONCURRENT;
    }

    /**
     * 同步处理指定集群
     *
     * @param cluster 集群名字
     */
    @Override
    public boolean executeByCluster(String cluster) {
        Result<Boolean> result = templateColdManager.move2ColdNode(cluster);
        if (result.failed()) {
            LOGGER.warn("class=ColdDataMoveRandomTask||method=executeByCluster||cluster={}||failMsg={}", cluster,
                result.getMessage());
            return false;
        }
        return true;
    }
}
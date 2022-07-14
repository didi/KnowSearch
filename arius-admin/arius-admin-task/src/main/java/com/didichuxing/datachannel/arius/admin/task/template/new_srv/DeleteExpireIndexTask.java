package com.didichuxing.datachannel.arius.admin.task.template.new_srv;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.expire.ExpireManager;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentTemplateTask;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chengxiang
 * @date 2022/5/13
 */
//@Task(name = "DeleteExpireIndexTask", description = "删除过期索引任务", cron = "0 55 23 */1 * ?", autoRegister = true)
public class DeleteExpireIndexTask extends BaseConcurrentTemplateTask implements Job {
    private static final ILog LOGGER = LogFactory.getLog(IndexRolloverTask.class);

    @Autowired
    private ExpireManager     expireManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=DeleteExpireIndexTask||method=execute||msg=DeleteExpireIndexTask start");
        if (execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }

    @Override
    public String getTaskName() {
        return "DeleteExpireIndexTask";
    }

    @Override
    public int poolSize() {
        return 10;
    }

    @Override
    public int current() {
        return 5;
    }

    @Override
    protected boolean executeByLogicTemplate(Integer logicId) {
        return expireManager.deleteExpireIndex(logicId).success();
    }
}
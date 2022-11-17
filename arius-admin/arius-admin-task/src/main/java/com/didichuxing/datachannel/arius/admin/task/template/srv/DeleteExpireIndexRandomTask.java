package com.didichuxing.datachannel.arius.admin.task.template.srv;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.expire.ExpireManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentTemplateTask;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chengxiang
 * @date 2022/5/13
 */
@Task(name = "DeleteExpireIndexRandomTask", description = "删除过期索引任务", cron = "0 55 23 */1 * ?", autoRegister = true)
public class DeleteExpireIndexRandomTask extends BaseConcurrentTemplateTask implements Job {
    private static final ILog LOGGER = LogFactory.getLog(DeleteExpireIndexRandomTask.class);

    @Autowired
    private ExpireManager     expireManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=DeleteExpireIndexRandomTask||method=execute||msg=DeleteExpireIndexRandomTask start");
        if (execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
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
        final Result<Void> result = expireManager.deleteExpireIndex(logicId);
        if (result.failed()){
            LOGGER.warn("class=DeleteExpireIndexRandomTask||method=executeByLogicTemplate||logicId={}||msg={}", logicId,
                        result.getMessage());
        }
        return result.success();
    }
}
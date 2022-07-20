package com.didichuxing.datachannel.arius.admin.task.template.new_srv;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentTemplateTask;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.consensual.ConsensualEnum;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chengxiang
 * @date 2022/5/12
 */
@Task(name = "preCreateIndexTask", description = "明天索引预先创建任务，预先创建平台中模板明天索引，避免凌晨大规模创建索引", cron = "0 0 03-06 * * ?", autoRegister = true, consensual = ConsensualEnum.RANDOM)
public class PreCreateIndexTask extends BaseConcurrentTemplateTask implements Job {

    private static final ILog LOGGER = LogFactory.getLog(PreCreateIndexTask.class);

    @Autowired
    private PreCreateManager  preCreateManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=PreCreateIndexTask||method=execute||msg=PreCreateIndexTask start");
        if (execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }

    @Override
    public String getTaskName() {
        return "PreCreateIndexTask";
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
        try {
            final Result<Boolean> result = preCreateManager.preCreateIndex(logicId);
            if (Boolean.FALSE.equals(result.getData())) {
                LOGGER.warn("class=PreCreateIndexTask||method=executeByLogicTemplate||logicId={}||msg={}", logicId,
                        result.getMessage());
            }
            return result.getData();
        } catch (Exception e) {
            LOGGER.error("class=PreCreateIndexTask||method=executeByLogicTemplate||logicId={}||msg=预创建失败", logicId, e);
        }
        
        return Boolean.TRUE;
    }
}
package com.didichuxing.datachannel.arius.admin.task.template.srv;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentTemplateTask;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.consensual.ConsensualEnum;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chengxiang
 * @date 2022/5/12
 */
@Task(name = "PreCreateIndexRandomTask", description = "明天索引预先创建任务，预先创建平台中模板明天索引，避免凌晨大规模创建索引", cron = "0 0 03-06 * * ?", autoRegister = true, consensual = ConsensualEnum.RANDOM)
public class PreCreateIndexRandomTask extends BaseConcurrentTemplateTask implements Job {

    private static final ILog LOGGER = LogFactory.getLog(PreCreateIndexRandomTask.class);

    @Autowired
    private PreCreateManager  preCreateManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=PreCreateIndexRandomTask||method=execute||msg=PreCreateIndexRandomTask start");
        if (execute()) {
            return TaskResult.buildSuccess();
        }
        return TaskResult.buildFail();
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
            if (ResultType.FAIL.getCode() == result.getCode()) {
                LOGGER.warn("class=PreCreateIndexRandomTask||method=executeByLogicTemplate||logicId={}||msg={}", logicId,
                        result.getMessage());
            }
            return result.getCode() == ResultType.SUCCESS.getCode();
        } catch (Exception e) {
            LOGGER.error("class=PreCreateIndexRandomTask||method=executeByLogicTemplate||logicId={}||msg=预创建失败", logicId, e);
        }

        return Boolean.TRUE;
    }
}
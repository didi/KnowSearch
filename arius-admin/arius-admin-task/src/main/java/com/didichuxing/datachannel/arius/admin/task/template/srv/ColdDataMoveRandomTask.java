package com.didichuxing.datachannel.arius.admin.task.template.srv;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.ColdManager;
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
 * @date 2022/06/07
 */
@Task(name = "ColdDataMoveRandomTask", description = "admin冷数据搬迁服务", cron = "0 30 22 * * ?", autoRegister = true)
public class ColdDataMoveRandomTask extends BaseConcurrentTemplateTask implements Job {

    private static final ILog LOGGER = LogFactory.getLog(ColdDataMoveRandomTask.class);

    @Autowired
    private ColdManager       coldManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=ColdDataMoveRandomTask||method=execute||msg=ColdDataMoveRandomTask start");
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
        try {
            final Result<Boolean> result = coldManager.move2ColdNode(logicId);
            if (Boolean.FALSE.equals(result.getData())) {
                LOGGER.warn("class=ColdDataMoveRandomTask||method=executeByLogicTemplate||logicId={}||msg={}", logicId,
                        result.getMessage());
            }
            return result.getData();
        } catch (Exception e) {
            LOGGER.error("class=ColdDataMoveRandomTask||method=executeByLogicTemplate||logicId={}||msg=admin冷数据搬迁服务",
                    logicId, e);
        }
        
        return Boolean.TRUE;
    }
}
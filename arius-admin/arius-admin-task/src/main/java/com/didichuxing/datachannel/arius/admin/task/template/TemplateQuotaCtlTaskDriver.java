package com.didichuxing.datachannel.arius.admin.task.template;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author d06679
 * Created by d06679 on 2018/3/14.
 *
 * 暂时不需要模板quota数据，屏蔽
 */
//@Task(name = "templateQuotaCtlTaskDriver", description = "Quota使用率统计任务", cron = "0 */15 * * * ?", autoRegister = true)
public class TemplateQuotaCtlTaskDriver implements Job {

    private static final ILog    LOGGER = LogFactory.getLog(TemplateQuotaCtlTaskDriver.class);

    @Autowired
    private TemplateQuotaCtlTask task;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=TemplateQuotaCtlTaskDriver||method=execute||msg=templateQuotaCtlTask start.");
        if (task.execute()) {
            return TaskResult.SUCCESS;
        }
        return TaskResult.FAIL;
    }
}

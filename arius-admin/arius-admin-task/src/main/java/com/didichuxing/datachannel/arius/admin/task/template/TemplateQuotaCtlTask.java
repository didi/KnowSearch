package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentTemplateTask;

/**
 * @author d06679
 * @date 2019/5/24
 */
@Component
public class TemplateQuotaCtlTask extends BaseConcurrentTemplateTask {

    @Autowired
    private TemplateQuotaManager templateQuotaManager;

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    @Override
    public String getTaskName() {
        return "模板Quota管控";
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
        return TaskConcurrentConstants.TEMPLATE_QUOTA_CTL_TASK_CONCURRENT;
    }

    /**
     * 处理
     *
     * @param logicId
     * @return
     */
    @Override
    protected boolean executeByLogicTemplate(Integer logicId) throws AdminOperateException {
        return templateQuotaManager.controlAndPublish(logicId);
    }

}

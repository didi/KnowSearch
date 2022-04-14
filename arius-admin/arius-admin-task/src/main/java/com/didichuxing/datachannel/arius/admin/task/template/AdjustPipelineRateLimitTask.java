package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.limit.TemplateLimitManager;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentTemplateTask;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 删除过期索引任务
 * @author d06679
 * @date 2019/4/8
 */
@Component
public class AdjustPipelineRateLimitTask extends BaseConcurrentTemplateTask {

    @Autowired
    private TemplateLimitManager templateLimitManager;

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    @Override
    public String getTaskName() {
        return "pipeline动态限流";
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
        return TaskConcurrentConstants.ADJUST_PIPELINE_RATE_LIMIT_TASK_CONCURRENT;
    }

    /**
     * 同步处理指定集群
     *
     * @param logicId 模板ID
     */
    @Override
    protected boolean executeByLogicTemplate(Integer logicId) throws AdminOperateException {
        return templateLimitManager.adjustPipelineRateLimit(logicId);
    }
}

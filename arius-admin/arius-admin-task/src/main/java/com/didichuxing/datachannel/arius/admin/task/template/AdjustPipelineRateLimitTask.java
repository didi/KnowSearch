package com.didichuxing.datachannel.arius.admin.task.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.limit.TemplateLimitManager;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentTemplateTask;

/**
 * 删除过期索引任务
 * @author d06679
 * @date 2019/4/8
 */
@Component
public class AdjustPipelineRateLimitTask extends BaseConcurrentTemplateTask {

    @Autowired
    private AriusConfigInfoService  ariusConfigInfoService;

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
        if (ariusConfigInfoService.booleanSetting(ARIUS_COMMON_GROUP, "quota.dynamic.limit.flink.sink.enable", false)) {
            return templateLimitManager.adjustPipelineRateLimit(logicId);
        }
        return true;
    }
}

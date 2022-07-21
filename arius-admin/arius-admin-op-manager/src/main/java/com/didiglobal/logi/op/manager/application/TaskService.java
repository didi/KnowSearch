package com.didiglobal.logi.op.manager.application;

import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.domain.script.service.impl.ScriptDomainService;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-13 2:06 下午
 */
@Component
public class TaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskDomainService taskDomainService;

    /**
     * 执行脚本
     * @param task
     * @return
     */
    public Result<Void> execute(Integer taskId) {
        if (null == taskId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "task id为空");
        }

        //新建脚本
        return taskDomainService.executeTask(taskId);
    }

    /**
     * 获取任务执行的分组配置
     * @param taskId
     * @param groupName
     * @return
     */
    public Result<GeneralGroupConfig> getGroupConfig(Integer taskId, String groupName) {
        if (null == taskId || null == groupName) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "taskId or groupName为null");
        }
        return taskDomainService.getConfig(taskId, groupName);
    }
}

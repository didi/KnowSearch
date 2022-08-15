package com.didiglobal.logi.op.manager.application;

import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.Tuple;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralInstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostActionEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskActionEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.ComponentHandlerFactory;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author didi
 * @date 2022-07-13 2:06 下午
 */
@Component
public class TaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskDomainService taskDomainService;

    @Autowired
    private PackageDomainService packageDomainService;

    @Autowired
    private ComponentHandlerFactory componentHandlerFactory;

    /**
     * 执行任务
     * @param taskId
     * @return
     */
    public Result<Void> execute(Integer taskId) {
        Result<Task> result = taskDomainService.getTaskById(taskId);
        if (result.failed() || null == result.getData()) {
            return Result.fail(ResultCode.TASK_NOT_EXIST_ERROR);
        }
        //执行任务
        return componentHandlerFactory.getByType(result.getData().getType()).execute(result.getData());
    }

    /**
     * 对任务执行相应的操作，暂停，取消，杀死，继续
     * @param taskId
     * @param action
     * @return
     */
    public Result<Void> operateTask(Integer taskId, String action) {
        if (null == taskId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "task id为空");
        }
        TaskActionEnum taskAction = TaskActionEnum.find(action);
        if (taskAction == TaskActionEnum.UN_KNOW) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "action未知");
        }
        return taskDomainService.actionTask(taskId, taskAction);
    }

    public Result<Void> retryTask(Integer taskId) {
        if (null == taskId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "task id为空");
        }
        return taskDomainService.retryTask(taskId);
    }

    /**
     * 对host执行相应的操作，取消，重试，kill
     * @param taskId
     * @param action
     * @return
     */
    public Result<Void> operateHost(Integer taskId, String action, String host, String groupName) {
        if (null == taskId || null == host || null == groupName) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "id or host or groupName can not be null");
        }
        HostActionEnum hostActionEnum = HostActionEnum.find(action);
        if (hostActionEnum == HostActionEnum.UN_KNOW) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "action未知");
        }
        return taskDomainService.actionHost(taskId, host, groupName, hostActionEnum);
    }

    /**
     * 获取任务执行的分组配置
     * @param taskId
     * @param groupName
     * @return
     */
    public Result<Tuple<GeneralGroupConfig, String>> getGroupConfig(Integer taskId, String groupName) {
        Tuple<GeneralGroupConfig, String> request;
        if (null == taskId || Strings.isNullOrEmpty(groupName)) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "taskId or groupName为null");
        }

        Result<Task> taskResult = taskDomainService.getTaskById(taskId);
        if (null == taskResult.getData()) {
            return Result.fail(ResultCode.TASK_NOT_EXIST_ERROR);
        }

        Task task = taskResult.getData();
        Result<GeneralGroupConfig> configResult = taskDomainService.getConfig(task, groupName);
        if (configResult.failed()) {
            return Result.fail(configResult.getCode(), configResult.getMessage());
        }

        if (task.getType() == OperationEnum.INSTALL.getType() ||
                task.getType() == OperationEnum.UPGRADE.getType()) {
            Integer packageId = ConvertUtil.obj2ObjByJSON(task.getContent(), GeneralInstallComponent.class).getPackageId();
            String url = packageDomainService.queryPackage(Package.builder().id(packageId).build()).getData().stream().findFirst().get().getUrl();
            request = new Tuple<>(configResult.getData(), url);
        } else {
            request = new Tuple<>(configResult.getData(), null);
        }
        return Result.success(request);
    }
}

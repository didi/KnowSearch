package com.didiglobal.logi.op.manager.application;

import com.alibaba.fastjson.JSON;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.entity.value.TaskDetail;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralInstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralRollbackComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralUpgradeComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostActionEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskActionEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.ComponentHandlerFactory;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author didi
 * @date 2022-07-13 2:06 下午
 */
@org.springframework.stereotype.Component
public class TaskService {

    @Autowired
    private TaskDomainService taskDomainService;

    @Autowired
    private PackageDomainService packageDomainService;

    @Autowired
    private ComponentHandlerFactory componentHandlerFactory;

    @Autowired
    private ComponentDomainService componentDomainService;

    /**
     * 执行任务
     *
     * @param taskId 任务id
     * @return
     */
    public Result<Void> execute(Integer taskId) {
        Result<Task> result = taskDomainService.getTaskById(taskId);
        if (result.failed() || null == result.getData()) {
            return Result.fail(ResultCode.TASK_NOT_EXIST_ERROR);
        }
        //执行任务，根据类型进行相应的处理
        return componentHandlerFactory.getByType(result.getData().getType()).execute(result.getData());
    }
    
    /**
     * 任务重试
     *
     * @param taskId 任务id
     * @return
     */
    public Result<Void> retryTask(Integer taskId) {
        if (null == taskId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "task id为空");
        }
        return taskDomainService.retryTask(taskId);
    }

    /**
     * 对任务执行相应的操作，暂停，取消，杀死，继续
     *
     * @param taskId 任务id
     * @param action 操作
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

    /**
     * 对host执行相应的操作，重试，kill以及忽略
     *
     * @param taskId    任务id
     * @param action    操作
     * @param host      主机
     * @param groupName 分组名
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
     *
     * @param taskId    任务id
     * @param groupName 分组名
     * @return 通用配置
     */
    public Result<GeneralGroupConfig> getGroupConfig(Integer taskId, String groupName) {
        if (null == taskId || Strings.isNullOrEmpty(groupName)) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "taskId or groupName为null");
        }

        Result<Task> taskResult = taskDomainService.getTaskById(taskId);
        if (null == taskResult.getData()) {
            return Result.fail(ResultCode.TASK_NOT_EXIST_ERROR);
        }

        //分组名是否匹配
        Task task = taskResult.getData();
        Result<GeneralGroupConfig> configResult = taskDomainService.getConfig(task, groupName);
        if (configResult.failed()) {
            return Result.fail(configResult.getCode(), configResult.getMessage());
        }

        if (task.getType() == OperationEnum.INSTALL.getType()) {
            GeneralInstallComponent installComponent = ConvertUtil.str2ObjByJson(task.getContent(), GeneralInstallComponent.class);
            //如果是安装和升级，设置url
            Integer packageId = installComponent.getPackageId();
            configResult.getData().setUrl(packageDomainService.getPackageById(packageId).getData().getUrl());
            configResult.getData().setUsername(installComponent.getUsername());
            configResult.getData().setPassword(installComponent.getPassword());
            configResult.getData().setIsOpenTSL(installComponent.getIsOpenTSL());
        } else if(task.getType() == OperationEnum.UPGRADE.getType()){
            GeneralUpgradeComponent upgradeComponent = ConvertUtil.str2ObjByJson(task.getContent(), GeneralUpgradeComponent.class);
            Component component = componentDomainService.getComponentById(upgradeComponent.getComponentId()).getData();
            Integer packageId = upgradeComponent.getPackageId();
            configResult.getData().setUrl(packageDomainService.getPackageById(packageId).getData().getUrl());
            configResult.getData().setUsername(component.getUsername());
            configResult.getData().setPassword(component.getPassword());
            configResult.getData().setIsOpenTSL(component.getIsOpenTSL());
        } else {
            Integer componentId = JSON.parseObject(task.getContent()).getInteger("componentId");
            Component component = componentDomainService.getComponentById(componentId).getData();
            configResult.getData().setUrl(packageDomainService.getPackageById(component.getPackageId()).getData().getUrl());
            //如果是回滚，设置回滚类型以及url
            if (task.getType() == OperationEnum.ROLLBACK.getType()) {
                GeneralRollbackComponent rollbackComponent = ConvertUtil.str2ObjByJson(task.getContent(), GeneralRollbackComponent.class);
                configResult.getData().setType(rollbackComponent.getType());
            }
            configResult.getData().setUsername(component.getUsername());
            configResult.getData().setPassword(component.getPassword());
            configResult.getData().setIsOpenTSL(component.getIsOpenTSL());
        }
        return Result.success(configResult.getData());
    }

    /**
     * 获取任务执行完成后的输出
     *
     * @param taskId   任务id
     * @param hostname 主机名
     * @return String
     */
    public Result<String> getTaskLog(Integer taskId, String hostname, Integer taskLogEnumType) {
        if (null == taskId || null == hostname) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "taskId或者hostname 为null");
        }
        Result<String> taskLogResult = taskDomainService.getTaskLog(taskId, hostname, taskLogEnumType);
        return taskLogResult;
    }

    /**
     * 根据任务id获取任务详情
     *
     * @param taskId 任务id
     * @return Result<List < TaskDetail>>
     */
    public Result<List<TaskDetail>> getTaskDetail(Integer taskId) {
        if (null == taskId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "taskId为null");
        }
        Result<List<TaskDetail>> taskDetailListResult = taskDomainService.listTaskDetailByTaskId(taskId);
        return taskDetailListResult;
    }
    
    /**
     * > 如果任务不为 null 且任务已经完成，则返回 true
     *
     * @param taskId 任务编号
     * @return Result<Boolean>
     */
    public Result<Boolean> tasksToBeAchieved(Integer taskId) {
        final Result<Task> taskRes = taskDomainService.getTaskById(taskId);
        return Result.build(Objects.isNull(taskRes.getData()) || taskRes.getData().getIsFinish() == 1);
    }
    
   
    /**
     * 如果任务存在，则返回真，否则返回假。
     *
     * @param taskId 任务 ID。
     * @return Result<Boolean>
     */
    public Result<Boolean> hasTask(Integer taskId) {
        final Result<Task> taskRes = taskDomainService.getTaskById(taskId);
        return Result.build(Objects.nonNull(taskRes.getData()));
    }
    
    /**
     * 按 ID 获取任务列表。
     *
     * @param taskIds 要查询的任务ID列表。
     * @return 任务清单
     */
    public Result<List<Task>> getTaskListByIds(List<Integer> taskIds) {
        return taskDomainService.getTaskListByIds(taskIds);
    }
    
    /**
     * 通过其 ID 获取任务。
     *
     * @param taskId 要检索的任务的 ID。
     * @return 结果<任务>
     */
    public Result<Task> getTaskById(Integer taskId) {
        return taskDomainService.getTaskById(taskId);
    }
}
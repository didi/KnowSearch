package com.didiglobal.logi.op.manager.domain.task.service.impl;

import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.entity.value.TaskDetail;
import com.didiglobal.logi.op.manager.domain.task.repository.TaskDetailRepository;
import com.didiglobal.logi.op.manager.domain.task.repository.TaskRepository;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralBaseOperationComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostActionEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskActionEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.deployment.DeploymentService;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import org.apache.logging.log4j.util.Strings;
import org.elasticsearch.common.collect.Tuple;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author didi
 * @date 2022-07-13 10:51 上午
 */
@Service
public class TaskDomainServiceImpl implements TaskDomainService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskDetailRepository taskDetailRepository;

    @Autowired
    private DeploymentService deploymentService;

    private static final Character REX = ',';

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createTask(String content, Integer type, String describe,
                                   String associationId, Map<String, List<String>> groupToHostList) {
        //新建
        Task task = new Task();
        task.create(content, type, describe, associationId, groupToHostList);


        //存储
        int id = taskRepository.insertTask(task);
        task.getDetailList().forEach(taskDetail -> {
            taskDetail.setId(id);
        });

        //存储host任务
        taskDetailRepository.batchInsertTaskDetail(task.getDetailList());
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> executeTask(int taskId) {
        Task task = taskRepository.getTaskById(taskId);
        if (null == task) {
            return Result.fail(ResultCode.TASK_NOT_EXIST_ERROR);
        }
        Result checkRes = task.checkExecuteTaskStatus();
        if (checkRes.failed()) {
            return checkRes;
        }

        //获取要执行的分组信息
        List<TaskDetail> detailList = taskDetailRepository.listTaskDetailByTaskId(taskId);
        Result<Map.Entry<String, List<String>>> group2ListRes = getFirstGroup(detailList);
        if (group2ListRes.failed()) {
            return Result.fail(group2ListRes.getMessage());
        }

        //获取模板和分组
        Result<Tuple<GeneralGroupConfig, String>> configAndTemplateIdRes = getConfigByGroupName(task, group2ListRes.getData().getKey());
        if (configAndTemplateIdRes.failed()) {
            return Result.fail(configAndTemplateIdRes.getMessage());
        }

        //执行zeus任务
        Result<Integer> deployRes = deploymentService.execute(configAndTemplateIdRes.getData().v2(),
                taskId, group2ListRes.getData().getKey(), Strings.join(group2ListRes.getData().getValue(), REX),
                task.getType().toString());

        if (deployRes.failed()) {
            return Result.fail(deployRes.getMessage());
        }

        //更新任务状态
        taskRepository.updateTaskStatus(taskId, TaskStatusEnum.RUNNING.getStatus());
        //回写执行id
        taskDetailRepository.updateTaskDetailExecuteIdByTaskIdAndGroupName(taskId, group2ListRes.getData().getKey(),
                deployRes.getData());
        return Result.success();
    }

    @Override
    public Result<Void> actionTask(int taskId, TaskActionEnum action) {
        Task task = taskRepository.getTaskById(taskId);
        if (null == task) {
            return Result.fail(ResultCode.TASK_NOT_EXIST_ERROR);
        }
        Result checkRes = task.checkTaskActionStatus(action);
        if (checkRes.failed()) {
            return checkRes;
        }

        //获取正在执行的任务id
        int executeTaskId = getExecuteTaskId(taskId);


        //执行zeus任务
        Result<Void> actionRes = deploymentService.actionTask(executeTaskId, action.getAction());
        if (actionRes.failed()) {
            return actionRes;
        }

        //更新任务状态
        taskRepository.updateTaskStatus(taskId, action.getStatus());
        return Result.success();
    }

    @Override
    public Result<Void> retryTask(int taskId) {
        Task task = taskRepository.getTaskById(taskId);
        if (null == task) {
            return Result.fail(ResultCode.TASK_NOT_EXIST_ERROR);
        }
        Result checkRes = task.checkRetryActionStatus();

        if (checkRes.failed()) {
            return checkRes;
        }

        taskDetailRepository.deleteByTaskId(taskId);
        //更新任务状态
        taskRepository.updateTaskStatus(taskId, TaskStatusEnum.WAITING.getStatus());
        return Result.success();
    }

    @Override
    public Result<Void> actionHost(int taskId, String host, String groupName, HostActionEnum action) {
        TaskDetail taskDetail = taskDetailRepository.getDetailByHostAndGroupName(taskId, host, groupName);

        if (null == taskDetail) {
            return Result.fail(ResultCode.TASK_HOST_IS_NOT_EXIST);
        }
        Result checkRes = taskDetail.checkHostActionStatus(action);
        if (checkRes.failed()) {
            return checkRes;
        }

        //执行zeus任务
        Result<Void> actionRes = deploymentService.actionHost(taskDetail.getExecuteTaskId(), host, action.getAction());
        if (actionRes.failed()) {
            return actionRes;
        }

        //更新子任务状态
        updateTaskDetail(taskId, taskDetail.getExecuteTaskId(), action.getStatus(), Collections.singletonList(host));

        //更新主任务状态，这里因为zeus执行中如果一个节点失败了，然后重试或其他操作，这时整个任务是暂停的，需要再次重启
        taskRepository.updateTaskStatusAndIsFinish(taskId, TaskStatusEnum.PAUSE.getStatus(), 0);
        return Result.success();
    }


    private int getExecuteTaskId(int taskId) {
        List<TaskDetail> detailList = taskDetailRepository.listTaskDetailByTaskId(taskId);
        int executeTaskId = detailList.stream().filter(detail ->
                null != detail.getExecuteTaskId() &&
                        (detail.getStatus() == TaskStatusEnum.RUNNING.getStatus() ||
                                detail.getStatus() == TaskStatusEnum.WAITING.getStatus())
        ).findFirst().get().getExecuteTaskId();
        return executeTaskId;
    }

    @NotNull
    private Result<Map.Entry<String, List<String>>> getFirstGroup(List<TaskDetail> detailList) {
        Map<String, List<String>> groupToHostList = new LinkedHashMap<>();
        detailList.forEach(taskDetail -> {
            List<String> hosts = groupToHostList.get(taskDetail.getGroupName());
            if (null == hosts) {
                hosts = new ArrayList<>();
                groupToHostList.put(taskDetail.getGroupName(), hosts);
            }
            hosts.add(taskDetail.getHost());
        });
        if (0 == groupToHostList.size()) {
            Result.fail("没有可以执行的分组");
        }
        return Result.success(groupToHostList.entrySet().toArray(new Map.Entry[]{})[0]);
    }

    @Override
    public Result<Task> getTaskById(int taskId) {
        return Result.success(taskRepository.getTaskById(taskId));
    }

    @Override
    public Result<GeneralGroupConfig> getConfig(Task task, String groupName) {
        Result<Tuple<GeneralGroupConfig, String>> configRes = getConfigByGroupName(task, groupName);

        if (configRes.failed()) {
            return Result.fail(configRes.getMessage());
        }

        return Result.success(configRes.getData().v1());
    }

    @Override
    public Result<List<Task>> getUnFinishTaskList() {
        List<Task> taskList = taskRepository.getUnFinishTaskList();
        taskList.forEach(task -> {
            List<TaskDetail> detailList = taskDetailRepository.listTaskDetailByTaskId(task.getId());
            task.setDetailList(detailList);
        });
        return Result.success(taskList);
    }

    @Override
    public Result<Void> updateTaskDetail(int taskId, int executeId, int status, List<String> hosts) {
        //taskDetailRepository.update
        return null;
    }

    @Override
    public Result<Void> updateTaskStatusAndIsFinish(int taskId, int isFinish, int status) {
        taskRepository.updateTaskStatusAndIsFinish(taskId, status, isFinish);
        return Result.success();
    }

    private Result<Tuple<GeneralGroupConfig, String>> getConfigByGroupName(Task task, String name) {

        switch (OperationEnum.valueOfType(task.getType())) {
            case INSTALL:
            case EXPAND:
            case SHRINK:
            case CONFIG_CHANGE:
            case RESTART:
                GeneralBaseOperationComponent baseOperationComponent = ConvertUtil.obj2ObjByJSON(task.getContent(), GeneralBaseOperationComponent.class);
                for (GeneralGroupConfig config : baseOperationComponent.getGroupConfigList()) {
                    if (config.getGroupName().equals(name)) {
                        return Result.success(new Tuple<>(config, baseOperationComponent.getTemplateId()));
                    }
                }
            case UN_KNOW:
            default:
                return Result.fail("分组名未匹配到相应配置");

        }
    }
}

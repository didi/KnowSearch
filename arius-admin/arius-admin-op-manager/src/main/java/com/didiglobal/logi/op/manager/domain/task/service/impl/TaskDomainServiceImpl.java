package com.didiglobal.logi.op.manager.domain.task.service.impl;

import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.entity.value.TaskDetail;
import com.didiglobal.logi.op.manager.domain.task.repository.TaskDetailRepository;
import com.didiglobal.logi.op.manager.domain.task.repository.TaskRepository;
import com.didiglobal.logi.op.manager.domain.task.service.TaskDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralInstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.deployment.DeploymentService;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import org.apache.logging.log4j.util.Strings;
import org.elasticsearch.common.collect.Tuple;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public Result<GeneralGroupConfig> getConfig(int taskId, String groupName) {
        Task task = taskRepository.getTaskById(taskId);
        if (null == task) {
            return Result.fail(ResultCode.TASK_NOT_EXIST_ERROR);
        }

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
    public synchronized Result<Void> updateTaskStatus(int taskId, int isFinish, int status) {
        return null;
    }

    private Result<Tuple<GeneralGroupConfig, String>> getConfigByGroupName(Task task, String name) {

        switch (OperationEnum.valueOfType(task.getType())) {
            case INSTALL:
                GeneralInstallComponent installComponent = ConvertUtil.obj2ObjByJSON(task.getContent(), GeneralInstallComponent.class);
                for (GeneralGroupConfig config : installComponent.getGroupConfigList()) {
                    if (config.getGroupName().equals(name)) {
                        return Result.success(new Tuple<>(config, installComponent.getTemplateId()));
                    }
                }
            case UN_KNOW:
            default:
                return Result.fail("分组名未匹配到相应配置");

        }
    }
}

package com.didiglobal.logi.op.manager.domain.task.service;

import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostActionEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskActionEnum;

import java.util.List;
import java.util.Map;

/**
 * @author didi
 * @date 2022-07-13 10:50 上午
 */
public interface TaskDomainService {

    /**
     * 创建任务
     * @param content
     * @param type
     * @param describe
     * @param associationId
     * @param groupToIpList
     * @return
     */
    Result<Void> createTask(String content, Integer type, String describe, String associationId,
                            Map<String, List<String>> groupToIpList);

    /**
     * 执行任务
     * @param task
     * @return
     */
    Result<Void> executeDeployTask(Task task);


    /**
     * 执行功能任务
     * @param task
     * @return
     */
    Result<Void> executeFunctionTask(Task task);

    /**
     * 对任务执行相应的操作，暂停，取消，杀死，继续
     * @param taskId
     * @param action
     * @return
     */
    Result<Void> actionTask(int taskId, TaskActionEnum action);

    /**
     * 任务重试
     * @param taskId
     * @return
     */
    Result<Void> retryTask(int taskId);

    /**
     * 对节点执行相应的操作，重试，忽略，kill
     * @param taskId
     * @param host
     * @param groupName
     * @param action
     * @return
     */
    Result<Void> actionHost(int taskId, String host, String groupName, HostActionEnum action);

    /**
     * 通过id获取任务
     * @param taskId
     * @return
     */
    Result<Task> getTaskById(int taskId);

    /**
     * 获取分组配置
     * @param task
     * @param groupName
     * @return
     */
    Result<GeneralGroupConfig> getConfig(Task task, String groupName);

    /**
     * 获取未完成任务列表
     * @return
     */
    Result<List<Task>> getUnFinishTaskList();

    /**
     * 更新task任务
     * @param taskId
     * @param executeId
     * @param status
     * @param hosts
     * @return
     */
    Result<Void> updateTaskDetail(int taskId, int executeId, int status, List<String> hosts);

    /**
     * 更新任务状态以及是否完成
     * @param taskId
     * @param isFinish
     * @param status
     * @return
     */
    Result<Void> updateTaskStatusAndIsFinish(int taskId, int isFinish, int status);
}

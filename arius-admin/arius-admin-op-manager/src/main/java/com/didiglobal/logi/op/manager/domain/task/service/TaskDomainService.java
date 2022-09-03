package com.didiglobal.logi.op.manager.domain.task.service;

import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.Tuple;
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
     *
     * @param content       任务内容
     * @param type          任务类型
     * @param describe      任务描述
     * @param associationId 关联外部任务id
     * @param groupToIpList 分组倒ip的映射
     * @return 返回创建后的任务id
     */
    Result<Integer> createTask(String content, Integer type, String describe, String associationId,
                               Map<String, List<Tuple<String, Integer>>> groupToIpList);

    /**
     * 执行部署任务，比如重启，扩缩容等
     *
     * @param task 任务
     * @return
     */
    Result<Void> executeDeployTask(Task task);


    /**
     * 执行功能型任务，就是组件安装后，组件自带的一些功能
     *
     * @param task
     * @return
     */
    Result<Void> executeFunctionTask(Task task);

    /**
     * 对任务执行相应的操作，暂停，取消，杀死，继续
     *
     * @param taskId 任务id
     * @param action 对应的action
     * @return
     */
    Result<Void> actionTask(int taskId, TaskActionEnum action);

    /**
     * 任务重试
     *
     * @param taskId 任务id
     * @return
     */
    Result<Void> retryTask(int taskId);

    /**
     * 对节点执行相应的操作，重试，忽略，kill
     *
     * @param taskId    任务id
     * @param host      节点host
     * @param groupName 分组名
     * @param action    节点action
     * @return
     */
    Result<Void> actionHost(int taskId, String host, String groupName, HostActionEnum action);

    /**
     * 通过id获取任务
     *
     * @param taskId 任务id
     * @return 任务task
     */
    Result<Task> getTaskById(int taskId);

    /**
     * 获取分组配置
     *
     * @param task
     * @param groupName
     * @return
     */
    Result<GeneralGroupConfig> getConfig(Task task, String groupName);

    /**
     * 获取未完成任务列表
     *
     * @return 任务列表
     */
    Result<List<Task>> getUnFinishTaskList();

    /**
     * 更新task任务
     *
     * @param taskId    任务id
     * @param executeId 执行id
     * @param status    状态
     * @param hosts     主机列表
     * @return
     */
    Result<Void> updateTaskDetail(int taskId, int executeId, int status, List<String> hosts);

    /**
     * 更新任务状态以及是否完成
     *
     * @param taskId   任务id
     * @param isFinish 是否完成
     * @param status   状态
     * @return
     */
    Result<Void> updateTaskStatusAndIsFinish(int taskId, int isFinish, int status);


    /**
     * 是否有未完成的重名工单
     *
     * @param name        组件安装名
     * @param componentId 组件id
     * @return
     */
    Result<Void> hasRepeatTask(String name, Integer componentId);


    /**
     * 更新任务状态
     *
     * @param taskId 任务id
     * @param status 状态
     * @return 更新条数
     */
    Result<Integer> updateTaskStatus(int taskId, int status);

    /**
     * 获取任务执行完成后的标准输出
     *
     * @param taskId 任务id
     * @param hostname 主机名(可选)
     * @return String
     */
    Result<String> getTaskStdOuts(int taskId, String hostname);

    /**
     * 获取任务执行完成后的错误输出
     *
     * @param taskId 任务id
     * @param hostname 主机名(可选)
     * @return String
     */
    Result<String> getTaskStdErrs(int taskId, String hostname);

}

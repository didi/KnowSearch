package com.didiglobal.logi.op.manager.domain.task.repository;

import com.didiglobal.logi.op.manager.domain.task.entity.Task;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-13 10:37 上午
 */
public interface TaskRepository {
    /**
     * 插入一条任务
     *
     * @param task 任务实体
     * @return 自增任务id
     */
    int insertTask(Task task);

    /**
     * 根据id获取任务
     *
     * @param id 任务id
     * @return 任务实体
     */
    Task getTaskById(int id);

    /**
     * 更新任务状态
     *
     * @param id     任务id
     * @param status 状态
     * @return 条数
     */
    int updateTaskStatus(int id, int status);

    /**
     * 更新任务状态以及是否完成状态
     *
     * @param id       任务id
     * @param status   状态
     * @param isFinish 是否完成
     */
    void updateTaskStatusAndIsFinish(int id, int status, int isFinish);

    /**
     * 获取未完成的任务列表
     *
     * @return 未完成task列表
     */
    List<Task> getUnFinishTaskList();

    /**
     * 获取没有到达终态的任务
     *
     * @return 未终止状态的任务列表
     */
    List<Task> getUnFinalStatusTaskList();
}

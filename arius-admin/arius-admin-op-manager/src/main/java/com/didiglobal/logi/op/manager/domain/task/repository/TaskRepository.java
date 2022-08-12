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
     * @param task
     * @return
     */
    int insertTask(Task task);

    /**
     * 根据id获取任务
     * @param id
     * @return
     */
    Task getTaskById(int id);

    /**
     * 更新任务状态
     * @param id
     * @param status
     */
    void updateTaskStatus(int id, int status);

//    /**
//     * 更新任务状态以及是否完成状态
//     * @param id
//     * @param status
//     * @param isFinish
//     */
//    void updateTaskStatusAndIsFinish(int id, int status, int isFinish);

    /**
     * 获取未完成的任务列表
     * @return
     */
    List<Task> getUnFinishTaskList();
}

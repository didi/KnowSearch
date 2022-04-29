package com.didichuxing.datachannel.arius.admin.biz.worktask;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.WorkTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.WorkTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.WorkTask;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;

import java.util.List;

/**
 * 任务 Service
 *
 * @author d06679
 * @date 2020/12/21
 */
public interface WorkTaskManager {

    /**
     * 提交一个任务
     * @param workTaskDTO 任务数据
     * @return Result
     * @throws AdminOperateException 异常
     */
    Result<WorkTask> addTask(WorkTaskDTO workTaskDTO);

    /**
     * 判断一个任务是否存在
     * @param key 关键值
     * @param type 任务类型
     * @return
     */
    boolean existUnClosedTask(Integer key, Integer type);

    /**
     * 插入一条任务
     * @param task task
     * @return int
     */
    void insert(WorkTask task);

    /**
     * 通过id更新任务
     * @param task task
     * @return int
     */
    void updateTask(WorkTask task);

    /**
     * 通过id获取任务
     *
     * @param id 任务id
     * @return TaskPO
     */
    Result<WorkTask> getById(Integer id);

    /**
     * 获取所有的任务
     *
     * @return List<TaskPO>
     */
    Result<List<WorkTask>> list();

    /**
     * 处理任务任务
     *
     * @param processDTO 任务
     * @return Result
     */
    Result<Void> processTask(WorkTaskProcessDTO processDTO);

    /**
     * 通过businessKey获取最新的任务
     *
     * @param businessKey 业务id
     * @param taskType 任务类型
     */
    Result<WorkTask> getLatestTask(String businessKey, Integer taskType);

    /**
     * 通过businessKey获取待处理任务
     *
     * @param businessKey 业务id
     * @param taskType 任务类型
     */
    WorkTask getPengingTask(String businessKey, Integer taskType);

    /**
     * 通过taskType获取待处理任务
     *
     * @param taskType 任务类型
     */
    List<WorkTask> getPengingTaskByType(Integer taskType);

    /**
     * 根据类型获取失败任务
     * @param taskType
     * @return
     */
    List<WorkTask> getSuccessTaskByType(Integer taskType);
}

package com.didichuxing.datachannel.arius.admin.biz.worktask;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.AriusOpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.AriusOpTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.AriusOpTask;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import java.util.List;

/**
 * 任务 Service
 *
 * @author d06679
 * @date 2020/12/21
 */
public interface AriusOpTaskManager {

    /**
     * 提交一个任务
     * @param ariusOpTaskDTO 任务数据
     * @return Result
     * @throws AdminOperateException 异常
     */
    Result<AriusOpTask> addTask(AriusOpTaskDTO ariusOpTaskDTO);

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
    void insert(AriusOpTask task);

    /**
     * 通过id更新任务
     * @param task task
     * @return int
     */
    void updateTask(AriusOpTask task);

    /**
     * 通过id获取任务
     *
     * @param id 任务id
     * @return TaskPO
     */
    Result<AriusOpTask> getById(Integer id);

    /**
     * 获取所有的任务
     *
     * @return List<TaskPO>
     */
    Result<List<AriusOpTask>> list();

    /**
     * 处理任务任务
     *
     * @param processDTO 任务
     * @return Result
     */
    Result<Void> processTask(AriusOpTaskProcessDTO processDTO);
    
    /**获取最新任务
     * 通过businessKey获取最新的任务
     *
     * @param businessKey 业务id
     * @param taskType 任务类型
     * @return {@link Result}<{@link AriusOpTask}>
     */
    Result<AriusOpTask> getLatestTask(String businessKey, Integer taskType);


    
    /**
     * 通过taskType获取待处理任务
     *
     * @param taskType 任务类型
     * @return {@link List}<{@link AriusOpTask}>
     */
    List<AriusOpTask> getPendingTaskByType(Integer taskType);

    /**
     * 根据类型获取失败任务
     * @param taskType
     * @return
     */
    List<AriusOpTask> getSuccessTaskByType(Integer taskType);
}
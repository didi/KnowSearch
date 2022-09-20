package com.didiglobal.logi.op.manager.domain.task.repository;

import com.didiglobal.logi.op.manager.domain.task.entity.value.TaskDetail;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-13 10:39 上午
 */
public interface TaskDetailRepository {

    /**
     * 批量写入任务详情
     *
     * @param taskDetailList
     */
    void batchInsertTaskDetail(List<TaskDetail> taskDetailList);

    /**
     * 通过taskId获取
     *
     * @param taskId 任务id
     * @return List<TaskDetail> 任务详情列表
     */
    List<TaskDetail> listTaskDetailByTaskId(int taskId);

    /**
     * 通过任务id，host以及groupName获取信息
     *
     * @param taskId    任务id
     * @param host      host名
     * @param groupName 分组名
     * @return 任务详情实体
     */
    TaskDetail getDetailByHostAndGroupName(int taskId, String host, String groupName);

    /**
     * 更新执行的id
     *
     * @param taskId        任务id
     * @param groupName     分组名
     * @param executeTaskId 执行id
     * @return 更新条数
     */
    int updateTaskDetailExecuteIdByTaskIdAndGroupName(int taskId, String groupName, int executeTaskId);

    /**
     * 通过taskId删除
     *
     * @param taskId 任务id
     * @return 删除条数
     */
    int deleteByTaskId(int taskId);

    /**
     * 更新任务状态
     *
     * @param taskId    任务id
     * @param executeId 执行id
     * @param status    状态
     * @param hosts     host列表
     * @return 更新条数
     */
    int updateStatusByExecuteTaskId(int taskId, int executeId, int status, List<String> hosts);
}

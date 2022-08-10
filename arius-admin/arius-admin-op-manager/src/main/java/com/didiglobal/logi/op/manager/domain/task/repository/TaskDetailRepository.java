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
     * @param taskDetailList
     */
    void batchInsertTaskDetail(List<TaskDetail> taskDetailList);

    /**
     * 通过taskId获取
     * @param taskId
     * @return
     */
    List<TaskDetail> listTaskDetailByTaskId(int taskId);

    /**
     * 通过任务id，host以及groupName获取信息
     * @param taskId
     * @param host
     * @param groupName
     * @return
     */
    TaskDetail getDetailByHostAndGroupName(int taskId, String host, String groupName);

    /**
     * 更新执行的id
     * @param taskId
     * @param groupName
     * @param executeTaskId
     */
    void updateTaskDetailExecuteIdByTaskIdAndGroupName(int taskId, String groupName, int executeTaskId);
}

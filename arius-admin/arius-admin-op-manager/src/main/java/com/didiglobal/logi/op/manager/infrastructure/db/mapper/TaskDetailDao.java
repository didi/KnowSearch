package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.TaskDetailPO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-13 4:35 下午
 */
@Repository
public interface TaskDetailDao {

    /**
     * 批量插入任务详情
     * @param list
     */
    void batchInsert(List<TaskDetailPO> list);

    /**
     * 根据task_id获取
     * @param taskId
     * @return
     */
    List<TaskDetailPO> listByTaskId(int taskId);

    /**
     * 更新执行任务id
     * @param taskId
     * @param groupName
     * @param executeId
     */
    void updateExecuteId(int taskId, String groupName, int executeId);


    /**
     * 根据taskId
     * @param taskId
     * @param host
     * @param groupName
     * @return
     */
    TaskDetailPO getByHostAndGroupName(int taskId, String host, String groupName);
}

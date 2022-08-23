package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.TaskDetailPO;
import org.apache.ibatis.annotations.Param;
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
    void updateExecuteId(@Param("taskId" )int taskId, @Param("groupName") String groupName, @Param("executeTaskId") int executeId);


    /**
     * 根据taskId
     * @param taskId
     * @param host
     * @param groupName
     * @return
     */
    TaskDetailPO getByHostAndGroupName(@Param("taskId") int taskId, @Param("host") String host, @Param("groupName") String groupName);

    /**
     * 根据task_id删除detail信息
     * @param taskId
     * @return
     */
    void deleteByTaskId(int taskId);

    /**
     * 根据host和taskId以及executeId更新任务状态
     * @param taskId
     * @param executeId
     * @param status
     * @param hosts
     */
    void updateStatusByExecuteId(@Param("taskId") int taskId, @Param("executeId") int executeId,
                                 @Param("status") int status,@Param("hosts") List<String> hosts);
}

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
     *
     * @param list detail列表
     */
    void batchInsert(List<TaskDetailPO> list);

    /**
     * 根据task_id获取任务详情
     *
     * @param taskId 任务id
     * @return List<TaskDetailPO>
     */
    List<TaskDetailPO> listByTaskId(int taskId);

    /**
     * 更新执行任务id
     *
     * @param taskId    任务id
     * @param groupName 分组名
     * @param executeId 执行id
     * @return 更新条数
     */
    int updateExecuteId(@Param("taskId") int taskId, @Param("groupName") String groupName, @Param("executeTaskId") int executeId);


    /**
     * 根据taskId
     *
     * @param taskId    任务id
     * @param host      host
     * @param groupName 分组名
     * @return
     */
    TaskDetailPO getByHostAndGroupName(@Param("taskId") int taskId, @Param("host") String host, @Param("groupName") String groupName);

    /**
     * 根据task_id删除detail信息
     *
     * @param taskId 任务id
     * @return 删除条数
     */
    int deleteByTaskId(int taskId);

    /**
     * 根据host和taskId以及executeId更新任务状态
     *
     * @param taskId    任务id
     * @param executeId 执行id
     * @param status    状态
     * @param hosts     主机列表
     * @return 更新条数
     */
    int updateStatusByExecuteId(@Param("taskId") int taskId, @Param("executeId") int executeId,
                                @Param("status") int status, @Param("hosts") List<String> hosts);
}

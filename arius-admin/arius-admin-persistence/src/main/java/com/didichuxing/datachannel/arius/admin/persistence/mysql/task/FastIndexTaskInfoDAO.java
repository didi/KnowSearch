package com.didichuxing.datachannel.arius.admin.persistence.mysql.task;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.FastIndexTaskInfo;

@Repository
public interface FastIndexTaskInfoDAO {

    int insert(FastIndexTaskInfo record);

    int insertSelective(FastIndexTaskInfo record);

    FastIndexTaskInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FastIndexTaskInfo record);

    int updateByPrimaryKey(FastIndexTaskInfo record);

    /**
     * 批量新增索引数据迁移子任务
     *
     * @param recordList 记录
     * @return int
     */
    int insertBatch(List<FastIndexTaskInfo> recordList);

    /**
     * 刷新任务
     * 负责更新内核任务ID、任务状态、任务统计信息、任务开始与结束时间
     *
     * @param fastIndexTaskInfo 记录
     * @return int
     */
    int refreshTask(FastIndexTaskInfo fastIndexTaskInfo);

    /**
     * 批更新状态
     * @param taskStatus 任务状态
     * @param ids        id
     * @return int
     */
    int updateStatusBatch(@Param("taskStatus") Integer taskStatus, @Param("ids") List<Integer> ids);

    /**
     * 根据任务ID查询子任务列表
     * @param taskId 任务id
     * @return {@link List}<{@link FastIndexTaskInfo}>
     */
    List<FastIndexTaskInfo> listByTaskId(@Param("taskId") Integer taskId);

    /**
     * 根据任务ID与状态查询子任务列表
     * @param taskId 任务id
     * @return {@link List}<{@link FastIndexTaskInfo}>
     */
    List<FastIndexTaskInfo> listByTaskIdAndStatus(@Param("taskId") Integer taskId, @Param("taskStatusList") List<Integer> taskStatusList);

    List<Integer> listTemplateIdByTaskId(Integer taskId);
}
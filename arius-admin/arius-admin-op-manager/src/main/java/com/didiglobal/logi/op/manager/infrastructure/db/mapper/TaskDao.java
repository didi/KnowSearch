package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.TaskPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-13 1:46 下午
 */
@Repository
public interface TaskDao {
    /**
     * 新建任务
     *
     * @param po 任务po
     * @return 自增id
     */
    int insert(TaskPO po);

    /**
     * 获取任务id
     *
     * @param id 任务id
     * @return 任务po
     */
    TaskPO getById(int id);

    /**
     * 更新任务状态
     *
     * @param id     任务id
     * @param status 状态
     * @return 自增id
     */
    int updateStatus(@Param("id") int id, @Param("status") int status);


    /**
     * 根据状态和isFinish
     *
     * @param id       任务id
     * @param status   状态
     * @param isFinish 是否完成
     * @return 更新条数
     */
    int updateStatusAndIsFinish(@Param("id") int id, @Param("status") int status, @Param("isFinish") int isFinish);

    /**
     * 获取未完成的任务列表
     *
     * @return 列表
     */
    List<TaskPO> getUnFinishTaskList();

    /**
     * 获取未到达终态的任务列表
     *
     * @return 列表
     */
    List<TaskPO> getUnFinalStatusTaskList();
}

package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.infrastructure.db.PackagePO;
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
     * @param po
     * @return
     */
    int insert(TaskPO po);

    /**
     * 获取任务id
     * @param id
     * @return
     */
    TaskPO getById(int id);

    /**
     * 更新任务状态
     * @param id
     * @param status
     * @return
     */
    int updateStatus(@Param("id") int id, @Param("status") int status);

    /**
     * 获取未完成的任务列表
     * @return
     */
    List<TaskPO>  getUnFinishTaskList();
}

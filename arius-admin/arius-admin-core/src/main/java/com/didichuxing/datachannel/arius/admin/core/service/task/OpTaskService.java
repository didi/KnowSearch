package com.didichuxing.datachannel.arius.admin.core.service.task;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.OpTaskVO;

import java.util.List;

/**
 * op任务service
 *
 * @author shizeying
 * @date 2022/08/12
 */
public interface OpTaskService {
    /**
     * 更新
     *
     * @param task 任务
     * @return boolean
     */
    boolean update(OpTask task);
    
    /**
     * 获取通过id
     *
     * @param id id
     * @return {@link OpTask}
     */
    OpTask getById(Integer id);
    
    /**
     * 获取所有集合
     *
     * @return {@link List}<{@link OpTask}>
     */
    List<OpTask> listAll();
    
    /**
     * 获取成功任务通过类型
     *
     * @param taskType 任务类型
     * @return {@link List}<{@link OpTask}>
     */
    List<OpTask> getSuccessTaskByType(Integer taskType);
    
    /**
     * 获取pending任务通过类型
     *
     * @param taskType 任务类型
     * @return {@link List}<{@link OpTask}>
     */
    List<OpTask> getPendingTaskByType(Integer taskType);
    
    /**
     * 获取最新任务
     *
     * @param businessKey 业务key
     * @param taskType    任务类型
     * @return {@link OpTask}
     */
    OpTask getLatestTask(String businessKey, Integer taskType);
    
    /**
     * 新增
     *
     * @param task 任务
     * @return boolean
     */
    boolean insert(OpTask task);

    /**
     * 任务中心分页查询
     * @param queryDTO
     * @return
     */
    Tuple<Long, List<OpTaskVO>> pagingGetTasksByCondition(OpTaskQueryDTO queryDTO);
}
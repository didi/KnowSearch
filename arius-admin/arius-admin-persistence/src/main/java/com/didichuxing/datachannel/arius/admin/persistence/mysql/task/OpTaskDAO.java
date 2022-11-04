/*
 * Copyright (c) 2015, WINIT and/or its affiliates. All rights reserved. Use, Copy is subject to authorized license.
 */
package com.didichuxing.datachannel.arius.admin.persistence.mysql.task;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.task.OpTaskPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * OpTask Dao
 * 
 * @author fengqiongfeng
 * @date 2020-12-21
 */
@Repository
public interface OpTaskDAO {
    /**
     * 新增
     *
     * @param param 参数
     * @return int
     */
    int insert(OpTaskPO param);

    /**
     * 获取通过id
     *
     * @param id id
     * @return {@link OpTaskPO}
     */
    OpTaskPO getById(@Param("id") Integer id);

    /**
     * 获取所有集合
     *
     * @return {@link List}<{@link OpTaskPO}>
     */
    List<OpTaskPO> listAll();

    /**
     * 通过入参条件动态获取集合
     *
     * @param param 入参
     * @return {@link List}<{@link OpTaskPO}>
     */
    List<OpTaskPO> listByCondition(OpTaskPO param);

    /**
     * 更新
     *
     * @param param 入参
     * @return int
     */
    int update(OpTaskPO param);

    /**
     * 获取最新任务
     *
     * @param businessKey 业务关键
     * @param taskType 任务类型
     *
     * @return {@link OpTaskPO}
     */
    OpTaskPO getLatestTask(@Param("businessKey") String businessKey, @Param("taskType") Integer taskType);

    /**
     * 获取pending任务
     *
     * @param businessKey 业务key
     * @param taskType 任务类型
     * @return {@link OpTaskPO}
     */
    OpTaskPO getPendingTask(@Param("businessKey") String businessKey, @Param("taskType") Integer taskType);

    /**
     * 通过任务类型获取pending任务
     *
     * @param taskType 任务类型
     * @return {@link List}<{@link OpTaskPO}>
     */
    List<OpTaskPO> getPendingTaskByType(@Param("taskType") Integer taskType);

    /**
     *  通过任务类型获取成功任务
     *
     * @param taskType 任务类型
     * @return {@link List}<{@link OpTaskPO}>
     */
    List<OpTaskPO> getSuccessTaskByType(@Param("taskType") Integer taskType);

    /**
     * 任务中心分页查询数据
     * @param queryDTO
     * @param from
     * @param size
     * @param sortTerm
     * @param sortType
     * @return
     */
    List<OpTaskPO> pagingByCondition(@Param("param")OpTaskQueryDTO queryDTO, @Param("from")Long from,
                                     @Param("size")Long size, @Param("sortTerm")String sortTerm, @Param("sortType")String sortType);

    /**
     * 任务中心分页查询条数
     * @param queryDTO
     * @return
     */
    Long countByCondition(OpTaskQueryDTO queryDTO);
}
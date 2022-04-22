/*
 * Copyright (c) 2015, WINIT and/or its affiliates. All rights reserved. Use, Copy is subject to authorized license.
 */
package com.didichuxing.datachannel.arius.admin.persistence.mysql.task;

import com.didichuxing.datachannel.arius.admin.common.bean.po.task.AriusOpTaskPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WorkTask Dao
 * 
 * @author fengqiongfeng
 * @date 2020-12-21
 */
@Repository
public interface AriusOpTaskDAO {
   /**
     * 新增
     *
     * @param param 参数
     * @return int
     */
    int insert(AriusOpTaskPO param);
    /**
     * 获取通过id
     *
     * @param id id
     * @return {@link AriusOpTaskPO}
     */
    AriusOpTaskPO getById(@Param("id") Integer id);
    /**
     * 获取所有集合
     *
     * @return {@link List}<{@link AriusOpTaskPO}>
     */
    List<AriusOpTaskPO> listAll();
    /**
     * 通过入参条件动态获取集合
     *
     * @param param 入参
     * @return {@link List}<{@link AriusOpTaskPO}>
     */
    List<AriusOpTaskPO> listByCondition(AriusOpTaskPO param);
  /**
     * 更新
     *
     * @param param 入参
     * @return int
     */
    int update(AriusOpTaskPO param);
   /**
     * 获取最新任务
     *
     * @param businessKey 业务关键
     * @param taskType 任务类型
     *
     * @return {@link AriusOpTaskPO}
     */
    AriusOpTaskPO getLatestTask(@Param("businessKey") String businessKey,
                                @Param("taskType") Integer taskType);
    /**
     * 获取pending任务
     *
     * @param businessKey 业务key
     * @param taskType 任务类型
     * @return {@link AriusOpTaskPO}
     */
    AriusOpTaskPO getPendingTask(@Param("businessKey") String businessKey,
                                 @Param("taskType") Integer taskType);
   /**
     * 通过任务类型获取pending任务
     *
     * @param taskType 任务类型
     * @return {@link List}<{@link AriusOpTaskPO}>
     */
    List<AriusOpTaskPO> getPendingTaskByType(@Param("taskType") Integer taskType);
    /**
     *  通过任务类型获取成功任务
     *
     * @param taskType 任务类型
     * @return {@link List}<{@link AriusOpTaskPO}>
     */
    List<AriusOpTaskPO> getSuccessTaskByType(@Param("taskType") Integer taskType);
}
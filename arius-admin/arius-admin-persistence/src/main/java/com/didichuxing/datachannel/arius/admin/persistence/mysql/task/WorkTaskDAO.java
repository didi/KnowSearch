/*
 * Copyright (c) 2015, WINIT and/or its affiliates. All rights reserved. Use, Copy is subject to authorized license.
 */
package com.didichuxing.datachannel.arius.admin.persistence.mysql.task;

import com.didichuxing.datachannel.arius.admin.common.bean.po.task.AriusWorkTaskPO;
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
public interface WorkTaskDAO {

    int insert(AriusWorkTaskPO param);

    AriusWorkTaskPO getById(@Param("id") Integer id);

    List<AriusWorkTaskPO> listAll();

    List<AriusWorkTaskPO> listByCondition(AriusWorkTaskPO param);

    int update(AriusWorkTaskPO param);

    AriusWorkTaskPO getLatestTask(@Param("businessKey") String businessKey,
                                  @Param("taskType") Integer taskType);

    AriusWorkTaskPO getPengingTask(@Param("businessKey") String businessKey,
                                   @Param("taskType") Integer taskType);

    List<AriusWorkTaskPO> getPengingTaskByType(@Param("taskType") Integer taskType);

    List<AriusWorkTaskPO> getSuccessTaskByType(@Param("taskType") Integer taskType);
}
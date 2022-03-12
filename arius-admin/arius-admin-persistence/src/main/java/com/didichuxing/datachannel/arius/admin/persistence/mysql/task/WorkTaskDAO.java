/*
 * Copyright (c) 2015, WINIT and/or its affiliates. All rights reserved. Use, Copy is subject to authorized license.
 */
package com.didichuxing.datachannel.arius.admin.persistence.mysql.task;

import com.didichuxing.datachannel.arius.admin.common.bean.po.task.WorkTaskPO;
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

    int insert(WorkTaskPO param);

    WorkTaskPO getById(@Param("id") Integer id);

    List<WorkTaskPO> listAll();

    List<WorkTaskPO> listByCondition(WorkTaskPO param);

    int update(WorkTaskPO param);

    WorkTaskPO getLatestTask(@Param("businessKey") Integer businessKey,
                             @Param("taskType") Integer taskType);

    WorkTaskPO getPengingTask(@Param("businessKey") Integer businessKey,
                              @Param("taskType") Integer taskType);

}

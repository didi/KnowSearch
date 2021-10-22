package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.dao.mysql;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po.CapacityPlanRegionTaskPO;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Repository
public interface CapacityPlanRegionTaskDAO {

    List<CapacityPlanRegionTaskPO> getByRegionId(Long regionId);

    CapacityPlanRegionTaskPO getDecreasingTaskByRegionId(Long regionId);

    int insert(CapacityPlanRegionTaskPO taskPO);

    List<CapacityPlanRegionTaskPO> getByStatus(int status);

    CapacityPlanRegionTaskPO getById(Long taskId);

    int updateStatus(@Param("taskId") Long taskId, @Param("status") int status);

    CapacityPlanRegionTaskPO getLastCheckTask(Long regionId);

    int deleteByRegionId(Long regionId);

    CapacityPlanRegionTaskPO getLastDecreaseTask(@Param("regionId") Long regionId, @Param("start") Date start,
                                                 @Param("end") Date end);
}

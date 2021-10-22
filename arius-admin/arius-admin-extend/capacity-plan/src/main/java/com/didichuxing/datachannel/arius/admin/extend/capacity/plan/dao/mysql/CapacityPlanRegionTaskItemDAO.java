package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.dao.mysql;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po.CapacityPlanRegionTaskItemPO;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Repository
public interface CapacityPlanRegionTaskItemDAO {

    List<CapacityPlanRegionTaskItemPO> getByTaskId(Long taskId);

    int insertBatch(List<CapacityPlanRegionTaskItemPO> itemPOS);
}

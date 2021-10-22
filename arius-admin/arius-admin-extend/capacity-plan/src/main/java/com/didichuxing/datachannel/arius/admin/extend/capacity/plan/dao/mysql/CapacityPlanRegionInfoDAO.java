package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.dao.mysql;

import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po.CapacityPlanRegionInfoPO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * region容量信息DAO
 * @author d06679
 * @date 2019-06-24
 */
@Repository
public interface CapacityPlanRegionInfoDAO {

    CapacityPlanRegionInfoPO getByRegionId(Long regionId);

    List<CapacityPlanRegionInfoPO> listByRegionIds(List<Long> regionIds);

    int insert(CapacityPlanRegionInfoPO param);

    int updateByRegionId(CapacityPlanRegionInfoPO param);

    int deleteByRegionId(Long regionId);
}

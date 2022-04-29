package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

/**
 * @author d06679
 * @date 2019-09-04
 */
public interface CapacityPlanStatisticsService {

    /**
     * 统计一个容量规划集群的规划信息
     * @param planClusterId planClusterId
     * @return true/false
     */
    Result<Void> statisticsPlanClusterById(Long planClusterId);

}

package com.didichuxing.datachannel.arius.admin.task.capaciryplan;

import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanStatisticsService;

/**
 * 预先创建索引任务
 * @author d06679
 * @date 2019/4/8
 */
@Component
public class CapacityAreaStatisTask extends BaseConcurrentCapacityPlanAreaTask {

    @Autowired
    private CapacityPlanStatisticsService capacityPlanStatisticsService;

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    @Override
    public String getTaskName() {
        return "容量统计";
    }

    /**
     * 任务的线程个数
     * @return 任务的线程个数
     */
    @Override
    public int poolSize() {
        return 5;
    }

    /**
     * 并发度
     *
     * @return
     */
    @Override
    public int current() {
        return TaskConcurrentConstants.CAPACITY_AREA_STATICS_TASK_CONCURRENT;
    }

    /**
     * 处理一个集群
     *
     * @param areaId 集群名字
     * @return true/false
     */
    @Override
    protected Result<Void> executeByArea(Long areaId) {
        return capacityPlanStatisticsService.statisticsPlanClusterById(areaId);
    }

}

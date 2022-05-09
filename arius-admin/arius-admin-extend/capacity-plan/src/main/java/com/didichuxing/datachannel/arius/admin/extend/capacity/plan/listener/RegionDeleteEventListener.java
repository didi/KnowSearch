package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionDeleteEvent;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionTaskService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/7
 * @Comment: region删除事件监听器
 */
@Component
public class RegionDeleteEventListener implements ApplicationListener<RegionDeleteEvent> {

    private static final ILog             LOGGER = LogFactory.getLog(RegionDeleteEventListener.class);

    @Autowired
    private CapacityPlanRegionService     capacityPlanRegionService;

    @Autowired
    private CapacityPlanRegionTaskService capacityPlanRegionTaskService;

    @Autowired
    private ClusterContextManager         clusterContextManager;

    @Override
    public void onApplicationEvent(RegionDeleteEvent regionDeleteEvent) {
        ClusterRegion deletedRegion = regionDeleteEvent.getClusterRegion();
        if (null == deletedRegion) {
            return;
        }

        clusterContextManager.flushClusterContextByClusterRegion(deletedRegion);

        // 删除容量信息记录
        Result<Void> delResult = capacityPlanRegionService.deleteRegionCapacityInfo(deletedRegion.getId(),
            regionDeleteEvent.getOperator());

        if (delResult.success()) {
            // 删除容量规划任务信息
            int delNum = capacityPlanRegionTaskService.deleteTasksByRegionId(deletedRegion.getId());
            LOGGER.info(
                "class=RegionDeleteEventListener||method=onApplicationEvent||msg=delete region capacity info and tasks for region"
                        + "||regionId={}||result={}||delTaskNum={}",
                deletedRegion.getId(), delResult, delNum);
        } else {
            LOGGER.info(
                "class=RegionDeleteEventListener||method=onApplicationEvent||msg=delete region capacity info for region||regionId={}||result={}",
                deletedRegion.getId(), delResult);
        }

    }
}

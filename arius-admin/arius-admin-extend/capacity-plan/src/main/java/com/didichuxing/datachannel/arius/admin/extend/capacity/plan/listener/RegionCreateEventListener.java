package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionCreateEvent;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanRegionDTO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/7
 * @Comment: region创建事件监听器
 */
@Component
public class RegionCreateEventListener implements ApplicationListener<RegionCreateEvent> {

    private static final ILog         LOGGER = LogFactory.getLog(RegionCreateEventListener.class);

    @Autowired
    private CapacityPlanRegionService capacityPlanRegionService;

    @Autowired
    private ClusterContextManager     clusterContextManager;

    @Override
    public void onApplicationEvent(RegionCreateEvent regionCreateEvent) {
        ClusterRegion region = regionCreateEvent.getClusterRegion();
        if (null == region) {
            return;
        }
        clusterContextManager.flushClusterContextByClusterRegion(region);

        // 创建容量信息记录
        CapacityPlanRegionDTO capacityPlanRegionDTO = new CapacityPlanRegionDTO();
        capacityPlanRegionDTO.setRegionId(region.getId());
        capacityPlanRegionDTO.setConfigJson("");
        capacityPlanRegionDTO.setFreeQuota(0.0);
        if (regionCreateEvent.getShare() == null) {
            // 初始不加入容量规划
            capacityPlanRegionDTO.setShare(AdminConstant.YES);
        } else {
            capacityPlanRegionDTO.setShare(regionCreateEvent.getShare());
        }

        Result<Void> createResult = capacityPlanRegionService.createRegionCapacityInfo(capacityPlanRegionDTO,
            regionCreateEvent.getOperator());

        LOGGER.info(
            "class=RegionCreateEventListener||method=onApplicationEvent||msg=create region capacity info for region||regionId={}||result={}",
            region.getId(), createResult);

    }
}

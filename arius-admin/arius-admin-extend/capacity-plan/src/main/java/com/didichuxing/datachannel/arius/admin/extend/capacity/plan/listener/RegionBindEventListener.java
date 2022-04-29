package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.listener;

import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionBindEvent;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.dto.CapacityPlanAreaDTO;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanAreaService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

import java.util.List;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/7
 * @Comment: region绑定事件监听器
 */
@Component
public class RegionBindEventListener implements ApplicationListener<RegionBindEvent> {

    private static final ILog       LOGGER = LogFactory.getLog(RegionBindEventListener.class);

    @Autowired
    private CapacityPlanAreaService capacityPlanAreaService;

    @Autowired
    private ClusterContextManager   clusterContextManager;

    @Override
    public void onApplicationEvent(RegionBindEvent regionBindEvent) {
        //更新单集群上下文
        ClusterRegion region = regionBindEvent.getClusterRegion();
        if (null == region) {
            return;
        }

        clusterContextManager.flushClusterContextByClusterRegion(region);

        // 构建area信息
        List<Long> logicClusterIds = ListUtils.string2LongList(region.getLogicClusterIds());
        if (CollectionUtils.isEmpty(logicClusterIds)) {
            LOGGER.info("class=RegionBindEventListener||method=onApplicationEvent||msg=the logicClusterOf {} is empty", region.getId());
            return;
        }

        for (Long logicClusterId : logicClusterIds) {
            CapacityPlanAreaDTO capacityPlanAreaDTO = new CapacityPlanAreaDTO();
            capacityPlanAreaDTO.setResourceId(logicClusterId);
            capacityPlanAreaDTO.setClusterName(region.getPhyClusterName());
            capacityPlanAreaDTO.setConfigJson("");

            // 如果area不存在则创建
            Result<Long> createResult = capacityPlanAreaService.createPlanAreaInNotExist(capacityPlanAreaDTO,
                    regionBindEvent.getOperator());

            LOGGER.info(
                    "class=RegionBindEventListener||method=onApplicationEvent||msg=create area for bound region||regionId={}||result={}",
                    region.getId(), createResult);
        }
    }
}

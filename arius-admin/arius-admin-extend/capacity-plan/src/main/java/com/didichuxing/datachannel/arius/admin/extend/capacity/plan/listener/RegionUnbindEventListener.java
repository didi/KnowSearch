package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.listener;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionUnbindEvent;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanAreaService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/7
 * @Comment: region绑定事件监听器
 */
@Component
public class RegionUnbindEventListener implements ApplicationListener<RegionUnbindEvent> {

    private static final ILog         LOGGER = LogFactory.getLog(RegionUnbindEventListener.class);

    @Autowired
    private CapacityPlanAreaService   capacityPlanAreaService;

    @Autowired
    private CapacityPlanRegionService capacityPlanRegionService;

    @Autowired
    private ClusterContextManager clusterContextManager;

    @Override
    public void onApplicationEvent(RegionUnbindEvent regionUnbindEvent) {
        //更新集群校验模型
        clusterContextManager.flushClusterContext();
    }
}

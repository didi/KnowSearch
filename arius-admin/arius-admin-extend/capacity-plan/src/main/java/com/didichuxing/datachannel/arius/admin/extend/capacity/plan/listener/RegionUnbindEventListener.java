package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.event.region.RegionUnbindEvent;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/7
 * @Comment: region绑定事件监听器
 */
@Component
public class RegionUnbindEventListener implements ApplicationListener<RegionUnbindEvent> {

    @Autowired
    private ClusterContextManager clusterContextManager;

    @Override
    public void onApplicationEvent(RegionUnbindEvent regionUnbindEvent) {
        ClusterRegion region = regionUnbindEvent.getClusterRegion();
        if (null == region) {
            return;
        }

        clusterContextManager.flushClusterContextByClusterRegion(region);
    }
}

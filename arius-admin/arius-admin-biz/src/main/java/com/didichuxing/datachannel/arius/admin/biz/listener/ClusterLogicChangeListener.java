package com.didichuxing.datachannel.arius.admin.biz.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterLogicEvent;

/**
 * Created by linyunan on 2021-06-03
 */
@Component
public class ClusterLogicChangeListener implements ApplicationListener<ClusterLogicEvent> {

    @Autowired
    private ClusterContextManager clusterContextManager;

    @Override
    public void onApplicationEvent(ClusterLogicEvent event) {
        clusterContextManager.flushClusterContext();
    }
}

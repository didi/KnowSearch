package com.didichuxing.datachannel.arius.admin.biz.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterLogicEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.EventException;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 * Created by linyunan on 2021-06-03
 */
@Component
public class ClusterLogicChangeListener extends ApplicationRetryListener<ClusterLogicEvent> {

    private static final ILog     LOGGER = LogFactory.getLog(ClusterLogicChangeListener.class);

    @Autowired
    private ClusterLogicManager   clusterLogicManager;

    @Override
    public void onApplicationRetryEvent(ClusterLogicEvent event) throws EventException {
        if(!clusterLogicManager.updateClusterLogicHealth(event.getClusterLogicId())){
            LOGGER.error(
                    "class=ClusterPhyChangeListener||method=onApplicationEvent||projectId={}||clusterPhyName={}||ErrorMsg={}",
                    event.getProjectId(), event.getClusterLogicId());
            throw new EventException("e.getMessage(), e");
        }
    }

}
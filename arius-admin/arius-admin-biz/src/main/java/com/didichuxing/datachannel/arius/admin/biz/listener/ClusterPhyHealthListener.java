package com.didichuxing.datachannel.arius.admin.biz.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterPhyHealthEvent;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

@Component
public class ClusterPhyHealthListener implements ApplicationListener<ClusterPhyHealthEvent> {

    private static final ILog LOGGER = LogFactory.getLog(ClusterPhyHealthListener.class);

    @Autowired
    private ESOpClient        esOpClient;

    @Autowired
    private ClusterPhyManager clusterPhyManager;

    @Override
    public void onApplicationEvent(ClusterPhyHealthEvent event) {
        if (event.getClusterPhyName() == null) {
            return;
        }
        try {
            // 刷新es-client中的map连接信息
            esOpClient.connect(event.getClusterPhyName());
            // 修改物理集群对应的状态
            clusterPhyManager.updateClusterHealth(event.getClusterPhyName(), AriusUser.SYSTEM.getDesc());
        } catch (Exception e) {
            LOGGER.error("class=ClusterPhyHealthListener||method=onApplicationEvent||cluster={},errMsg={}",
                event.getClusterPhyName(), e.getMessage());
        }
    }
}

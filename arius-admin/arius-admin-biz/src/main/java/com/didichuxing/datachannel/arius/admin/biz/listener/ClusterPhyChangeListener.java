package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterPhyEvent;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author linyunan
 * @date 2021-06-03
 */
@Component
public class ClusterPhyChangeListener implements ApplicationListener<ClusterPhyEvent> {

    private static final ILog     LOGGER = LogFactory.getLog(ClusterPhyChangeListener.class);

    @Autowired
    private ClusterContextManager clusterContextManager;

    @Autowired
    private ClusterPhyManager     clusterPhyManager;

    @Override
    public void onApplicationEvent(ClusterPhyEvent event) {
        try {
            //保证数据已经刷到数据库，如果立即执行，会存在获取不到数据库中数据的状态
            Thread.sleep(5000);
            clusterContextManager.flushClusterPhyContext(event.getClusterPhyName());

            clusterPhyManager.updateClusterHealth(event.getClusterPhyName(), AriusUser.SYSTEM.getDesc());
        } catch (Exception e) {
            LOGGER.error(
                "class=ClusterPhyChangeListener||method=onApplicationEvent||operator={}||clusterPhyName={}||ErrorMsg={}",
                event.getOperator(), event.getClusterPhyName(), e.getMessage());
        }
    }
}
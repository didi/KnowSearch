package com.didichuxing.datachannel.arius.admin.task.resource;

import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;

/**
 * Created by linyunan on 2021-10-15
 */
@Component
public class ClusterPhyInfoTask extends BaseConcurrentClusterTask {

    @Override
    public String getTaskName() {
        return "采集物理集群资源信息";
    }

    @Override
    public int poolSize() {
        return 5;
    }

    @Override
    public int current() {
        return 10;
    }

    @Override
    protected boolean executeByCluster(String cluster) {
        return  clusterPhyManager.updateClusterInfo(cluster, AriusUser.SYSTEM.getDesc());
    }
}
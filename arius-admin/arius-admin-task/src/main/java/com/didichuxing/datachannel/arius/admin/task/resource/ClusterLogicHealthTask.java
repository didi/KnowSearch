package com.didichuxing.datachannel.arius.admin.task.resource;

import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterLogicTask;

/**
 * Created by linyunan on 2021-10-15
 */
@Component
public class ClusterLogicHealthTask extends BaseConcurrentClusterLogicTask {
    @Override
    public String getTaskName() {
        return "采集逻辑集群健康信息";
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
    protected boolean executeByClusterLogic(Long clusterLogicId) {
        return clusterLogicManager.updateClusterLogicHealth(clusterLogicId);
    }
}
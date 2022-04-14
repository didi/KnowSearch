package com.didichuxing.datachannel.arius.admin.core.service.cluster.monitorTask;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterMonitorTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;

/**
 * Created by linyunan on 3/21/22
 */
public interface ClusterMonitorTaskService {

    List<ClusterMonitorTask> getTaskByHost(String monitorHost, int size);

    /**
     * 获取单台机器监控采集的集群名称列表, 当分布式部署分组采集，可分摊采集压力
     * @param monitorHost            采集机器名称
     * @return                       采集集群列表
     */
    List<ClusterPhy> getSingleMachineMonitorCluster(String monitorHost);
}

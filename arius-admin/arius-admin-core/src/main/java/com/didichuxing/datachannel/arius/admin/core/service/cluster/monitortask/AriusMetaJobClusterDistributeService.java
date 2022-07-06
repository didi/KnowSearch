package com.didichuxing.datachannel.arius.admin.core.service.cluster.monitortask;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.AriusMetaJobClusterDistribute;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;

/**
 * Created by linyunan on 3/21/22
 * modify by ohushenglin_v 2022/5/9
 * @author ohushenglin_v
 */
public interface AriusMetaJobClusterDistributeService {

    /**
     * 根据host获取监控采集的集群名称列表
     * @param monitorHost monitorHost
     * @param size  size
     * @return 监控的集群信息
     */
    List<AriusMetaJobClusterDistribute> getTaskByHost(String monitorHost, int size);

    /**
     * 获取单台机器监控采集的集群名称列表, 当分布式部署分组采集，可分摊采集压力
     * @param monitorHost            采集机器名称
     * @return                       采集集群列表
     */
    List<ClusterPhy> getSingleMachineMonitorCluster(String monitorHost);
}

package com.didichuxing.datachannel.arius.admin.task.resource;

import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 从es集群同步节点信息
 * @author d06679
 * @date 2019/3/21
 */
@Component
public class CollectClusterNodeSettingFromEsTask extends BaseConcurrentClusterTask {

    @Autowired
    private ESRoleClusterHostService esRoleClusterHostService;

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    @Override
    public String getTaskName() {
        return "采集集群节点信息";
    }

    /**
     * 并发度
     *
     * @return
     */
    @Override
    public int current() {
        return TaskConcurrentConstants.COLLECT_CLUSTER_NODE_SETTING_FROM_ES_TASK_CONCURRENT;
    }

    /**
     * 同步处理指定集群
     *
     * 1、获取es集群中的所有节点
     * 2、获取数据库中的全部节点
     * 3、对比
     * 4、刷库
     *
     * @param cluster 集群名字
     */
    @Override
    public boolean executeByCluster(String cluster) {
        return esRoleClusterHostService.collectClusterNodeSettings(cluster);
    }

}

package com.didichuxing.datachannel.arius.admin.core.service.cluster.monitorTask.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterMonitorTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.monitorTask.ClusterMonitorTaskService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor.ClusterMonitorTaskDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * Created by linyunan on 3/21/22
 */
@Service
public class ClusterMonitorTaskServiceImpl implements ClusterMonitorTaskService {
    private static final ILog LOGGER = LogFactory.getLog(ClusterMonitorTaskServiceImpl.class);

    /**
     * maxPoolSize，当前monitorjob能支持的最大集群采集个数，
     * 超过maxPoolSize的集群不会被采集，保证maxPoolSize个集群采集的稳定性
     */
    @Value("${dashboard.thread.maxsize:30}")
    private int  maxPoolSize;

    @Autowired
    private ClusterPhyService     clusterPhyService;

    @Autowired
    private ClusterMonitorTaskDAO clusterMonitorTaskDAO;

    @Override
    public List<ClusterPhy> getSingleMachineMonitorCluster(String monitorHost) {
        List<ClusterPhy> monitorCluster = Lists.newArrayList();
        List<ClusterPhy> clusterPhyList = clusterPhyService.listAllClusters();
        if (CollectionUtils.isEmpty(clusterPhyList)) {
            LOGGER.warn("class=ClusterMonitorTaskServiceImpl||method=getSingleMachineMonitorCluster||" +
                    "msg=clusterPhyList is empty");
            return monitorCluster;
        }

        List<ClusterMonitorTask> clusterMonitorTasks = getTaskByHost(monitorHost, maxPoolSize);
        if(CollectionUtils.isEmpty(clusterMonitorTasks)){
            LOGGER.warn("class=ClusterMonitorTaskServiceImpl||method=getSingleMachineMonitorCluster||" +
                    "msg=clusterMonitorTaskPOS is empty");
        }else {
            Map<String, ClusterMonitorTask> taskMap = clusterMonitorTasks.stream()
                    .collect(Collectors.toMap(ClusterMonitorTask::getCluster, c -> c));

            for(ClusterPhy clusterPhy : clusterPhyList){
                if(null != taskMap.get(clusterPhy.getCluster())){
                    monitorCluster.add(clusterPhy);
                }
            }
        }

        LOGGER.info("class=ClusterMonitorJobHandler||method=handlePhysicalClusterStats||monitorCluster={}",
                JSON.toJSONString(monitorCluster));
        return monitorCluster;
    }

    @Override
    public List<ClusterMonitorTask> getTaskByHost(String monitorHost, int size) {
        return ConvertUtil.list2List(clusterMonitorTaskDAO.getTaskByHost(monitorHost, size), ClusterMonitorTask.class);
    }
}

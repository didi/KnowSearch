package com.didichuxing.datachannel.arius.admin.task.dashboard;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpHostUtil;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.monitortask.AriusMetaJobClusterDistributeService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.task.component.TaskResultBuilder;
import com.didichuxing.datachannel.arius.admin.task.dashboard.collector.BaseDashboardCollector;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.consensual.ConsensualEnum;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * Created by linyunan on 3/11/22
 * @author linyunan
 */
@Task(name = "DashboardSingleClusterCollectorBroadcastTask", description = "采集DashBoard单个集群数据信息", cron = "0 0/5 * * * ? *", autoRegister = true, consensual = ConsensualEnum.BROADCAST)
@Component
public class DashboardSingleClusterCollectorBroadcastTask implements Job {
    private static final ILog                         LOGGER                       = LogFactory
        .getLog(DashboardSingleClusterCollectorBroadcastTask.class);

    /**
     * 单组采集的集群数量，每个组会并发采集，分组为了降低 Gateway QPS
     */
    private static final int                          SINGLE_GROUP_CLUSTER_NUM     = 5;

    @Autowired
    private AriusMetaJobClusterDistributeService      ariusMetaJobClusterDistributeService;
    @Autowired
    private ClusterPhyService                         clusterPhyService;
    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    private static final FutureUtil<Void>             BATCH_COLLECTOR_FUTURE_UTIL  = FutureUtil
        .init("batchCollectorFutureUtil", 30, 30, 1000);

    private String                                    hostName                     = HttpHostUtil.HOST_NAME;

    private final Map<String, BaseDashboardCollector> BASE_DASHBOARD_COLLECTOR_MAP = SpringTool
        .getBeansOfType(BaseDashboardCollector.class);

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info(
            "class=DashboardSingleClusterCollectorBroadcastTask||method=execute||msg=DashboardSingleClusterCollectorBroadcastTask start.");
        // 获取单台admin实例能采集的集群数
        List<ClusterPhy> monitorCluster = ariusMetaJobClusterDistributeService.getSingleMachineMonitorCluster(hostName);
        if (CollectionUtils.isEmpty(monitorCluster)) {
            return TaskResult.buildSuccess();
        }

        long currentTimeMillis = System.currentTimeMillis();
        long currentTime = CommonUtils.monitorTimestamp2min(currentTimeMillis);
        TaskResultBuilder taskResultBuilder = new TaskResultBuilder();

        // 分组
        List<List<ClusterPhy>> partitionClusterList = Lists.partition(monitorCluster, SINGLE_GROUP_CLUSTER_NUM);
        for (List<ClusterPhy> partitionCluster : partitionClusterList) {
            // 各组执行, 单组并发执行
            for (ClusterPhy clusterPhy : partitionCluster) {
                for (Map.Entry<String, BaseDashboardCollector> entry : BASE_DASHBOARD_COLLECTOR_MAP.entrySet()) {
                    BATCH_COLLECTOR_FUTURE_UTIL.runnableTask(() -> {
                        // 并发执行多个不同集群的采集器
                        BaseDashboardCollector collector = entry.getValue();
                        try {
                            collector.collectSingleCluster(clusterPhy.getCluster(), currentTime);
                        } catch (Exception e) {
                            Thread.currentThread().interrupt();
                            String errLog = "class=DashboardSingleClusterCollectorBroadcastTask||collectorName="
                                            + collector.getName() + "||method=execute||errMsg=" + e.getMessage();
                            LOGGER.error(errLog, e);
                            taskResultBuilder.append(errLog);
                        }
                    });
                }
            }
            // 阻塞等待多个采集器执行结束
            BATCH_COLLECTOR_FUTURE_UTIL.waitExecute();
        }

        LOGGER.info(
            "class=DashboardSingleClusterCollectorBroadcastTask||method=execute||msg=DashboardSingleClusterCollectorBroadcastTask finish, cost:{}ms",
            System.currentTimeMillis() - currentTimeMillis);
        return taskResultBuilder.build();
    }
}
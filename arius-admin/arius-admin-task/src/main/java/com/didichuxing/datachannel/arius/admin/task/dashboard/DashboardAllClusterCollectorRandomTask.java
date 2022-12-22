package com.didichuxing.datachannel.arius.admin.task.dashboard;

import java.util.List;
import java.util.Map;
import com.didichuxing.datachannel.arius.admin.task.component.TaskResultBuilder;
import com.didichuxing.datachannel.arius.admin.task.dashboard.collector.BaseDashboardCollector;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 * Created by linyunan on 3/11/22
 */
@Task(name = "DashboardAllClusterCollectorRandomTask", description = "采集DashBoard平台全集群汇总数据信息", cron = "0 0/5 * * * ? *", autoRegister = true)
@Component
public class DashboardAllClusterCollectorRandomTask implements Job {
    private static final ILog                         LOGGER                       = LogFactory
        .getLog(DashboardAllClusterCollectorRandomTask.class);

    @Autowired
    private ClusterPhyService                         clusterPhyService;

    private final Map<String, BaseDashboardCollector> BASE_DASHBOARD_COLLECTOR_MAP = SpringTool
        .getBeansOfType(BaseDashboardCollector.class);

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info(
            "class=DashboardAllClusterCollectorRandomTask||method=execute||msg=DashboardAllClusterCollectorRandomTask start.");
        long currentTimeMillis = System.currentTimeMillis();
        long currentTime = CommonUtils.monitorTimestamp2min(currentTimeMillis);
        TaskResultBuilder taskResultBuilder = new TaskResultBuilder();
        List<String> clusterNameList = clusterPhyService.listClusterNames();
        if (CollectionUtils.isEmpty(clusterNameList)) {
            LOGGER.warn("class=DashboardAllClusterCollectorRandomTask||method=execute||msg=clusterNameList is empty");
            return TaskResult.buildSuccess();
        }

        for (Map.Entry<String, BaseDashboardCollector> entry : BASE_DASHBOARD_COLLECTOR_MAP.entrySet()) {
            BaseDashboardCollector collector = entry.getValue();
            if (null == collector) {
                continue;
            }
            try {
                collector.collectAllCluster(clusterNameList, currentTime);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                String errLog = "class=DashboardAllClusterCollectorRandomTask||collectorName=" + collector.getName()
                                + "||method=execute||errMsg=" + e.getMessage();
                LOGGER.error(errLog, e);
                taskResultBuilder.append(errLog);
            }
        }

        LOGGER.info(
            "class=DashboardAllClusterCollectorRandomTask||method=execute||msg=DashboardAllClusterCollectorRandomTask finish, cost:{}ms",
            System.currentTimeMillis() - currentTimeMillis);
        return taskResultBuilder.build();
    }

}

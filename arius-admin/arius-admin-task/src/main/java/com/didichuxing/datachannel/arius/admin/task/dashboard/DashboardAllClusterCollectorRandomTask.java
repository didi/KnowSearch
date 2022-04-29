package com.didichuxing.datachannel.arius.admin.task.dashboard;

import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.task.component.TaskResultBuilder;
import com.didichuxing.datachannel.arius.admin.task.dashboard.collector.BaseDashboardCollector;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by linyunan on 3/11/22
 */
@Task(name = "DashboardAllClusterCollectorRandomTask", description = "采集DashBoard平台全集群汇总数据信息", cron = "0 0/5 * * * ? *", autoRegister = true)
@Component
public class DashboardAllClusterCollectorRandomTask implements Job {
    private static final ILog        LOGGER     = LogFactory.getLog(DashboardAllClusterCollectorRandomTask.class);

    @Autowired
    private ClusterPhyService                                clusterPhyService;

    private final Map<String, BaseDashboardCollector> BASE_DASHBOARD_COLLECTOR_MAP = SpringTool
        .getBeansOfType(BaseDashboardCollector.class);

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=DashboardAllClusterCollectorRandomTask||method=execute||msg=DashboardAllClusterCollectorRandomTask start.");
        long currentTimeMillis = System.currentTimeMillis();
        long currentTime       = CommonUtils.monitorTimestamp2min(currentTimeMillis);
        TaskResultBuilder taskResultBuilder = new TaskResultBuilder();
        List<String> clusterNameList = clusterPhyService.listAllClusterNameList();
        if (CollectionUtils.isEmpty(clusterNameList)) {
            LOGGER.warn("class=DashboardAllClusterCollectorRandomTask||method=execute||msg=clusterNameList is empty");
            return TaskResult.SUCCESS;
        }

        for (Map.Entry<String, BaseDashboardCollector> entry : BASE_DASHBOARD_COLLECTOR_MAP.entrySet()) {
            BaseDashboardCollector collector = entry.getValue();
            if (null == collector) { continue;}
            try {
                collector.collectAllCluster(clusterNameList, currentTime);
                // 采集缓冲, 每个collector停顿2s, 避免集群多个采集器爆炸式访问网关, 空闲出cpu
                Thread.sleep(2000);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                String errLog = "class=DashboardAllClusterCollectorRandomTask||collectorName=" + collector.getName()
                        + "||method=execute||errMsg=" + e.getMessage();
                LOGGER.error(errLog, e);
                taskResultBuilder.append(errLog);
            }
        }

        LOGGER.info("class=DashboardAllClusterCollectorRandomTask||method=execute||msg=DashboardAllClusterCollectorRandomTask finish, cost:{}ms",
                System.currentTimeMillis() - currentTimeMillis);
        return taskResultBuilder.build();
    }

}

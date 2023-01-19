package com.didichuxing.datachannel.arius.admin.task.resource;

import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

@Task(name = "ClusterNodeSettingCollectorRandomTask", description = "同步节点配置任务", cron = "0 0/3 * * * ?", autoRegister = true)
public class ClusterNodeSettingCollectorRandomTask extends BaseConcurrentClusterTask implements Job {

    private static final ILog      LOGGER = LogFactory.getLog(ClusterNodeSettingCollectorRandomTask.class);

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info(
            "class=ClusterNodeSettingCollectorRandomTask||method=execute||msg=ClusterNodeSettingCollectorRandomTask start.");
        if (execute()) {
            return TaskResult.buildSuccess();
        }
        return TaskResult.buildFail();
    }

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    @Override
    public String getTaskName() {
        return "ClusterNodeSettingCollectorRandomTask";
    }

    /**
     * 任务的线程个数
     * @return 任务的线程个数
     */
    @Override
    public int poolSize() {
        return 3;
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
    public boolean executeByCluster(String cluster) throws AdminTaskException {
        return clusterRoleHostService.collectClusterNodeSettings(cluster);
    }
}

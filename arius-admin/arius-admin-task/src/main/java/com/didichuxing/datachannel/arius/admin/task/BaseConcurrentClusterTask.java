package com.didichuxing.datachannel.arius.admin.task;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.notify.info.rd.ScheduleTaskFailNotifyInfo;
import com.didichuxing.datachannel.arius.admin.core.notify.service.NotifyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * base任务 集群级别并发处理 记录任务完成时间  检查任务前置依赖
 * @author d06679
 * @date 2019/3/21
 */
public abstract class BaseConcurrentClusterTask extends BaseConcurrentTask {

    private static final ILog     LOGGER         = LogFactory.getLog(BaseConcurrentClusterTask.class);

    protected static final String TASK_RETRY_URL = "";

    @Autowired
    protected ESClusterPhyService esClusterPhyService;

    @Autowired
    protected ClusterPhyManager clusterPhyManager;

    @Autowired
    protected NotifyService       notifyService;

    /**
     * 任务全集
     *
     * @return
     */
    @Override
    protected List getAllItems() {
        return esClusterPhyService.listAllClusters();
    }

    /**
     * 处理一个批次任务
     *
     * @param taskBatch
     */
    @Override
    protected boolean executeByBatch(TaskBatch taskBatch) throws AdminOperateException {
        List items = taskBatch.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return true;
        }

        boolean succ = true;

        // 只要有一个集群失败就认为batch失败
        for (Object item : items) {
            ESClusterPhy cluster = (ESClusterPhy) item;
            try {
                LOGGER.info("executeByCluster begin||cluster={}||task={}", cluster.getCluster(), getTaskName());
                if (executeByCluster(cluster.getCluster())) {
                    LOGGER.info("executeByCluster succ||cluster={}||task={}", cluster.getCluster(), getTaskName());
                } else {
                    succ = false;
                    LOGGER.warn("executeByCluster fail||cluster={}||task={}", cluster.getCluster(), getTaskName());
                    notifyService.send(
                            NotifyTaskTypeEnum.SCHEDULE_TASK_FAILED,
                            new ScheduleTaskFailNotifyInfo(getTaskName(), "集群_" + cluster.getCluster(), TASK_RETRY_URL),
                            Arrays.asList()
                    );
                }
            } catch (Exception e) {
                succ = false;
                LOGGER.warn("executeByCluster error||cluster={}||task={}||errMsg={}", cluster.getCluster(),
                    getTaskName(), e.getMessage(), e);

                notifyService.send(
                        NotifyTaskTypeEnum.SCHEDULE_TASK_FAILED,
                        new ScheduleTaskFailNotifyInfo(getTaskName(), "集群_" + cluster.getCluster(), TASK_RETRY_URL, e.getMessage()),
                        Arrays.asList()
                );
            }
        }

        return succ;
    }

    /**
     * 处理一个集群
     * @param cluster 集群名字
     * @return
     */
    protected abstract boolean executeByCluster(String cluster) throws AdminOperateException;
}

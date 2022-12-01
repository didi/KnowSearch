package com.didichuxing.datachannel.arius.admin.task;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 * base任务 集群级别并发处理 记录任务完成时间  检查任务前置依赖
 * @author d06679
 * @date 2019/3/21
 */
public abstract class BaseConcurrentClusterTask extends BaseConcurrentTask<ClusterPhy> {

    private static final ILog     LOGGER         = LogFactory.getLog(BaseConcurrentClusterTask.class);

    protected static final String TASK_RETRY_URL = "";

    @Autowired
    protected ClusterPhyService   clusterPhyService;

    @Autowired
    protected ClusterPhyManager   clusterPhyManager;

    /**
     * 任务全集
     *
     * @return
     */
    @Override
    protected List<ClusterPhy> getAllItems() {
        return clusterPhyService.listAllClusters();
    }

    /**
     * 处理一个批次任务
     *
     * @param taskBatch
     */
    @Override
    protected boolean executeByBatch(TaskBatch<ClusterPhy> taskBatch) throws AdminOperateException {
        List<ClusterPhy> items = taskBatch.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return true;
        }

        boolean succ = true;

        // 只要有一个集群失败就认为batch失败
        for (ClusterPhy item : items) {
            ClusterPhy cluster = item;
            try {
                LOGGER.info("executeByCluster begin||cluster={}||task={}", cluster.getCluster(), getTaskName());
                if (executeByCluster(cluster.getCluster())) {
                    LOGGER.info("executeByCluster succ||cluster={}||task={}", cluster.getCluster(), getTaskName());
                } else {
                    succ = false;
                    LOGGER.warn("executeByCluster fail||cluster={}||task={}", cluster.getCluster(), getTaskName());
                }
            } catch (Exception e) {
                succ = false;
                LOGGER.warn("executeByCluster error||cluster={}||task={}||errMsg={}", cluster.getCluster(),
                    getTaskName(), e.getMessage(), e);
            }
        }

        return succ;
    }

    /**
     * 处理一个集群
     * @param cluster 集群名字
     * @throws AdminOperateException
     * @return
     */
    protected abstract boolean executeByCluster(String cluster) throws AdminOperateException;
}

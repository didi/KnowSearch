package com.didichuxing.datachannel.arius.admin.task;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

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
        List<String> succeedClusterList = Lists.newArrayList();
        List<String> failedClusterList = Lists.newArrayList();

        LOGGER.info(
                "class=BaseConcurrentClusterTask||method=executeByBatch||taskBatch executeByCluster begin||task={}||clusterListSize={}||clusters={}",
                getTaskName(), items.size(), items.stream().map(ClusterPhy::getCluster).collect(Collectors.joining(",")));

        // 只要有一个集群失败就认为batch失败
        for (ClusterPhy item : items) {
            ClusterPhy cluster = item;
            try {
                if (executeByCluster(cluster.getCluster())) {
                    succeedClusterList.add(cluster.getCluster());
                } else {
                    succ = false;
                    failedClusterList.add(cluster.getCluster());
                }
            } catch (Exception e) {
                succ = false;
                LOGGER.warn("executeByCluster error||cluster={}||task={}||errMsg={}", cluster.getCluster(),
                        getTaskName(), e.getMessage(), e);
            }
        }

        if (succ) {
            LOGGER.info(
                    "class=BaseConcurrentClusterTask||method=executeByBatch||taskBatch executeByCluster succ||task={}||clusterListSize={}||clusters={}",
                    getTaskName(), succeedClusterList.size(), String.join(",", succeedClusterList));
        } else {
            LOGGER.info(
                    "class=BaseConcurrentClusterTask||method=executeByBatch||taskBatch executeByCluster fail||task={}||clusterListSize={}||succeedClusterListSize={}||failedClusterListSize={}||succeedClusters={}||failedClusters={}",
                    getTaskName(), items.size(), succeedClusterList.size(), failedClusterList.size(), StringUtils.join(succeedClusterList, ","), StringUtils.join(failedClusterList, ","));
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

package com.didichuxing.datachannel.arius.admin.task;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 * base任务 集群级别并发处理 记录任务完成时间  检查任务前置依赖
 * @author d06679
 * @date 2019/3/21
 */
public abstract class BaseConcurrentClusterLogicTask extends BaseConcurrentTask<ClusterLogic> {

    private static final ILog       LOGGER = LogFactory.getLog(BaseConcurrentClusterLogicTask.class);

    @Autowired
    protected ClusterLogicManager   clusterLogicManager;

    @Autowired
    protected ClusterLogicService   clusterLogicService;

    @Autowired
    protected ClusterPhyService     clusterPhyService;

    @Autowired
    protected ClusterContextManager clusterContextManager;

    /**
     * 任务全集
     *
     * @return
     */
    @Override
    protected List<ClusterLogic> getAllItems() {
        return clusterLogicService.listAllClusterLogics();
    }

    /**
     * 处理一个批次任务
     *
     * @param taskBatch
     */
    @Override
    protected boolean executeByBatch(TaskBatch<ClusterLogic> taskBatch) throws AdminOperateException {
        List<ClusterLogic> items = taskBatch.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return true;
        }

        boolean succ = true;
        List<String> succeedClusterList = Lists.newArrayList();
        List<String> failedClusterList = Lists.newArrayList();

        LOGGER.info(
                "class=BaseConcurrentClusterLogicTask||method=executeByBatch||taskBatch executeByClusterLogic begin||task={}||clusterListSize={}||clusters={}",
                getTaskName(), items.size(), items.stream().map(ClusterLogic::getName).collect(Collectors.joining(",")));

        // 只要有一个集群失败就认为batch失败
        for (ClusterLogic item : items) {
            try {
                if (executeByClusterLogic(item.getId())) {
                    succeedClusterList.add(item.getName());
                } else {
                    succ = false;
                    failedClusterList.add(item.getName());
                }
            } catch (Exception e) {
                succ = false;
                LOGGER.warn("executeByClusterLogic error||cluster={}||task={}||errMsg={}", item.getName(),
                    getTaskName(), e.getMessage(), e);
            }
        }
        if (succ) {
            LOGGER.info(
                    "class=BaseConcurrentClusterLogicTask||method=executeByBatch||taskBatch executeByClusterLogic succ||task={}||clusterListSize={}||clusters={}",
                    getTaskName(), succeedClusterList.size(), String.join(",", succeedClusterList));
        } else {
            LOGGER.info(
                    "class=BaseConcurrentClusterLogicTask||method=executeByBatch||taskBatch executeByClusterLogic fail||task={}||clusterListSize={}||succeedClusterListSize={}||failedClusterListSize={}||succeedClusters={}||failedClusters={}",
                    getTaskName(), items.size(), succeedClusterList.size(), failedClusterList.size(), StringUtils.join(succeedClusterList, ","), StringUtils.join(failedClusterList, ","));
        }
        return succ;
    }

    /**
     * 处理一个集群
     * @param clusterLogicId 逻辑集群Id
     * @throws AdminOperateException
     * @return 执行结果
     */
    protected abstract boolean executeByClusterLogic(Long clusterLogicId) throws AdminOperateException;
}

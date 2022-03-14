package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.capacityplan.IndexPlanManager;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author cjm
 */
@Component
public class IndexRolloverTask extends BaseConcurrentClusterTask {

    @Autowired
    private IndexPlanManager indexPlanManager;

    @Override
    protected boolean executeByCluster(String clusterPhyName) throws AdminOperateException {
        // cluster 物理集群
        return indexPlanManager.indexRollover(clusterPhyName);
    }

    @Override
    public String getTaskName() {
        return "IndexRolloverTask";
    }

    @Override
    public int poolSize() {
        // 任务线程个数
        return 10;
    }

    @Override
    public int current() {
        // 并发度
        return TaskConcurrentConstants.INDEX_ROLLOVER_TASK_CONCURRENT;
    }
}

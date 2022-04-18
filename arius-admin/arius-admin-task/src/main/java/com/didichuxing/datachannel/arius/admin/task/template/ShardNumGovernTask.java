package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.capacityplan.IndexPlanManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author didi
 * shord调整
 */
@Component
public class ShardNumGovernTask extends BaseConcurrentClusterTask {

    @Autowired
    private IndexPlanManager indexPlanManager;

    @Override
    protected boolean executeByCluster(String cluster) throws AdminOperateException {
        Result<Void> result = indexPlanManager.adjustShardCountByPhyClusterName(cluster);
        return !result.failed();
    }

    @Override
    public String getTaskName() {
        return "ShardNumGovernTask";
    }

    @Override
    public int poolSize() {
        return 10;
    }

    @Override
    public int current() {
        return TaskConcurrentConstants.INDEX_ROLLOVER_TASK_CONCURRENT;
    }
}

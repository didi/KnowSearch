package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.TemplateColdManager;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * 关闭集群rebalance任务
 * @author d06679
 * @date 2019/3/21
 */
@Component
public class ColdDataMoveTask extends BaseConcurrentClusterTask {

    private static final ILog   LOGGER = LogFactory.getLog(ColdDataMoveTask.class);

    @Autowired
    private TemplateColdManager templateColdManager;

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    @Override
    public String getTaskName() {
        return "冷数据搬迁任务";
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
        return TaskConcurrentConstants.COLD_DATA_MOVE_TASK_CONCURRENT;
    }

    /**
     * 同步处理指定集群
     *
     * @param cluster 集群名字
     */
    @Override
    public boolean executeByCluster(String cluster) {
        Result<Boolean> result = templateColdManager.move2ColdNode(cluster);
        if (result.failed()) {
            LOGGER.warn("class=ColdDataMoveTask||method=executeByCluster||cluster={}||failMsg={}", cluster, result.getMessage());
            return false;
        }
        return true;
    }
}

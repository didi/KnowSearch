package com.didichuxing.datachannel.arius.admin.task.op;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.security.SecurityService;
import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;

/**
 * 关闭集群rebalance任务
 * @author d06679
 * @date 2019/3/21
 */
@Component
public class SecurityMetaCheckTask extends BaseConcurrentClusterTask {

    @Autowired
    private SecurityService securityService;

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    @Override
    public String getTaskName() {
        return "权限数据校验";
    }

    /**
     * 并发度
     *
     * @return
     */
    @Override
    public int current() {
        return TaskConcurrentConstants.SECURITY_META_CHECK_TASK_CONCURRENT;
    }

    /**
     * 同步处理指定集群
     *
     * @param cluster 集群名字
     */
    @Override
    public boolean executeByCluster(String cluster) throws ESOperateException {
        return securityService.checkMeta(cluster);
    }
}

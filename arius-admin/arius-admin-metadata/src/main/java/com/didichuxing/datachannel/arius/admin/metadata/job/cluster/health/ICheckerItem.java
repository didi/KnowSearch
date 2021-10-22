package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health;


import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;

public interface ICheckerItem {

    HealthCheckType getType();

    /**
     * 执行检查
     */
    void exec(ClusterHealthCheckJobConfig healthCheckJobConfig);
}

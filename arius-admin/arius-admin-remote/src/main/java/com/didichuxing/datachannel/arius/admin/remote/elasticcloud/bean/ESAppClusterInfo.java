package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESAppClusterInfo {
    /**
     * id
     */
    private Integer            id;

    /**
     *  es集群实例
     */
    private ESClusterInstances instances;

    /**
     * kube服务名称
     */
    private String             kubeSvcName;

    /**
     * 最后运行时间
     */
    private String             lastRunningTime;

    /**
     * 名称
     */
    private String             name;

    /**
     * 产品线NS，比如op.didi.com
     */
    private String             namespace;

    /**
     * 区域
     */
    private String             region;

    /**
     * 场景
     */
    private String             scene;

    /**
     * 状态
     */
    private String             status;

    /**
     * 任务类型
     */
    private String             taskType;
}

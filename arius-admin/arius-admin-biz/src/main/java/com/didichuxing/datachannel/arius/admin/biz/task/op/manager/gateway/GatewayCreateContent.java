package com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway;

import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.GeneraInstallComponentContent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 该类用于创建集群
 *
 * @author shizeying
 * @date 2022/10/20
 * @since 0.3.2
 */

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class GatewayCreateContent extends GeneraInstallComponentContent {
    /**
     * 备忘录
     */
    private String memo;
    /**
     * 数据中心
     */
    private String dataCenter;
    /**
     * 代理地址
     */
    private String proxyAddress;
    /**
     * 群集类型
     */
    private String clusterType;
    /**
     * 原因
     */
    private String reason;
}
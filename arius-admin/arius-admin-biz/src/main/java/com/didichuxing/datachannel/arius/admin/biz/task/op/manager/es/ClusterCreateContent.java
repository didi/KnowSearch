package com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es;

import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.GeneraInstallComponentContent;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class ClusterCreateContent extends GeneraInstallComponentContent {
    private String memo;
    private String dataCenter;
    private String proxyAddress;
    private String clusterType;
}
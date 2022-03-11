package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.ESClusterRoleHost;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 物理机的集群扩缩容操作
 */
@Data
@NoArgsConstructor
public class ClusterOpIndecreaseHostContent extends ClusterOpHostContent {
    /**
     * 物理集群id
     */
    private Long                    phyClusterId;

    /**
     * 2:扩容 3：缩容
     */
    private int                     operationType;

    /**
     * 单机实例数
     */
    private Integer                 pidCount;

    /**
     * 集群角色 对应的原始的主机列表
     */
    private List<ESClusterRoleHost> originRoleClusterHosts;
}

package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.ESClusterRoleDocker;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 弹性云的集群扩缩容操作
 */
@Data
@NoArgsConstructor
public class ClusterOpIndecreaseDockerContent extends ClusterOpBaseContent {
    /**
     * 物理集群id
     */
    private Long                      phyClusterId;

    /**
     * 2:扩容 3：缩容
     */
    private int                       operationType;

    /**
     * 集群变动之后的角色列表
     */
    private List<ESClusterRoleDocker> roleClusters;

    /**
     * 集群原始角色列表
     */
    private List<ESClusterRoleDocker> originRoleClusters;
}

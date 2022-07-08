package com.didichuxing.datachannel.arius.admin.biz.task.content;

import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleDocker;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 弹性云的集群扩缩容操作
 *
 * @author ohushenglin_v
 * @date 2022-05-24
 */
@Data
@NoArgsConstructor
public class ClusterIndecreaseDockerContent extends ClusterBaseContent {
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

package com.didichuxing.datachannel.arius.admin.biz.worktask.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ohushenglin_v
 * @date 2022-05-24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterRestartContent extends ClusterHostContent {
    /**
     * 物理集群id
     */
    private Long   phyClusterId;

    /**
     * 角色顺序，如：airepo-masternode,airepo-clientnode,airepo-datanode
     */
    private String roleOrder;
}

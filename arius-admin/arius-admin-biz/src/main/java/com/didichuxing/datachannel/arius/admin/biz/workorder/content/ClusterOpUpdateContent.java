package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.ESClusterRoleHost;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ClusterOpUpdateContent extends ClusterOpHostContent {
    /**
     * 物理集群id
     */
    private Long   phyClusterId;

    /**
     * 物理集群名称
     */
    private String phyClusterName;

    /**
     * 集群版本
     */
    private String esVersion;

    /**
     * 角色顺序，如：airepo-masternode,airepo-clientnode,airepo-datanode
     */
    private String roleOrder;
}

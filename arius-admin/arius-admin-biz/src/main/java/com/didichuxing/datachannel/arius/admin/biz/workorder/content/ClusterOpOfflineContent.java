package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClusterOpOfflineContent extends BaseContent {
    /**
     * 物理集群id
     */
    private Long   phyClusterId;
    /**
     * 物理集群名称
     */
    private String phyClusterName;
}

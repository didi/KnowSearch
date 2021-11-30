package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClusterOpBaseContent extends BaseContent {
    /**
     * type 3:docker 4:host
     */
    private int    type;

    /**
     * 物理集群名称
     */
    private String phyClusterName;
}

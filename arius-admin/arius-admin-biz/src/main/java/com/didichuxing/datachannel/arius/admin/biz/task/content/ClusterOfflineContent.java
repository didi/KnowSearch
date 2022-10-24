package com.didichuxing.datachannel.arius.admin.biz.task.content;

import com.didichuxing.datachannel.arius.admin.biz.workorder.content.BaseContent;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ohushenglin_v
 * @date 2022-05-24
 */
@Data
@NoArgsConstructor
public class ClusterOfflineContent extends BaseContent {
    /**
     * 物理集群id
     */
    private Long   phyClusterId;
    /**
     * 物理集群名称
     */
    private String phyClusterName;
}

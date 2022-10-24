package com.didichuxing.datachannel.arius.admin.biz.task.content;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ohushenglin_v
 * @date 2022-05-24
 */
@Data
@NoArgsConstructor
public class ClusterBaseContent {
    /**
     * type 3:docker 4:host
     */
    private int    type;

    /**
     * 物理集群名称
     */
    private String phyClusterName;
}

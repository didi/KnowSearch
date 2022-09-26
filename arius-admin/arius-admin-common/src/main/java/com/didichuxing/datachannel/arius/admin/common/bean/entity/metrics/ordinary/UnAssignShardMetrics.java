package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnAssignShardMetrics implements Serializable {
    /**
     * 归属索引
     */
    private String index;

    /**
     * shard标识
     */
    private long   shard;

    /**
     * 主/备
     */
    private String prirep;

    /**
     * 状态
     */
    private String state;

    /**
     * 文档
     */
    private long docs;

    /**
     * 容量
     */
    private String store;

    /**
     * 所属节点Ip
     */
    private String ip;

    /**
     * 所属节点
     */
    private String node;

}

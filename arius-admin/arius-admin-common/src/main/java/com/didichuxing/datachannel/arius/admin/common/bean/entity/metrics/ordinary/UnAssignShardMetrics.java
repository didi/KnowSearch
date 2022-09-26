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

}

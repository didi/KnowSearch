package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShardMetrics implements Serializable {
    /**
     * shard标识
     */
    private long   shard;

    /**
     * 归属索引
     */
    private String index;

    /**
     * 主/备
     */
    private String prirep;

    /**
     * 所属节点Ip
     */
    private String ip;

    /**
     * 所属节点Ip
     */
    private String node;

    /**
     * 容量
     */
    private String store;
}

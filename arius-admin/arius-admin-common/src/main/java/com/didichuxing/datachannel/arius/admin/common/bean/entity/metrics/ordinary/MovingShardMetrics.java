package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-29
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovingShardMetrics implements Serializable {
    /**
     * 归属索引
     */
    private String i;

    /**
     * shard标识
     */
    private long   s;

    /**
     * 耗时
     */
    private String t;

    /**
     * 源节点Ip
     */
    private String shost;

    /**
     * 目标节点ip
     */
    private String thost;

    /**
     * 状态 init、index、start、translog、finalize、done
     */
    private String st;
}

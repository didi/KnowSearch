package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-01-13 11:45 上午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESClusterTaskStats extends BaseESPO {
    /**
     * 集群名称，all则表示全集群信息
     */
    private String cluster;

    /**
     * 统计的时间戳，单位：毫秒
     */
    private long timestamp;

    /**
     * 是否是物理集群 1：是；0：不是
     */
    private long physicCluster;

    /**
     * 统计信息
     */
    private ESClusterTaskStatsResponse metrics;

    /**
     * 数据中心
     */
    private String dataCenter;

    @Override
    public String getKey() {
        return String.format("%s@%s@%d", metrics.getTaskId(), cluster, timestamp);
    }

    @Override
    public String getRoutingValue() {
        return cluster;
    }
}

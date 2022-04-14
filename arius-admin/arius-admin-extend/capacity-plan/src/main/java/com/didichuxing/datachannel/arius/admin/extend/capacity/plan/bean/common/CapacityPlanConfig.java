package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-25
 */
@Data
public class CapacityPlanConfig {

    /**
     * 每个region最少的rack数量
     */
    private Integer countRackPerRegion         = 6;

    /**
     * 资源最高利用率
     */
    private Double  regionWatermarkHigh        = 0.80;

    /**
     * 资源最低利用率
     * 在满足countRackPerRegion的前提下，利用率低于这个值会缩容
     */
    private Double  regionWatermarkLow         = 0.65;

    /**
     * 资源规划的时间跨度，单位：天
     */
    private Integer planRegionResourceDays     = 7;

    /**
     * 检查资源的时间跨度，单位：分钟
     */
    private Integer checkRegionResourceMinutes = 15;

    /**
     * 资源超卖比例, [0.0, 1.0]
     * 1.0：100%超卖（Region的资源利用率完全取决于实际的资源消耗，与用户申请的quota无关）
     * 0.0：不超卖（Region的资源利用率完全取决于Quota资源消耗，与实际的资源消耗无关）
     */
    private Double  overSoldRate               = 1.0;

    /**
     * 文档大小的基准 单位：KB
     */
    private Double  docSizeBaseline            = 1.0;

    /**
     * 每个CPU的tps能力 单位：条/s
     */
    private Double  tpsPerCpu                  = 2000.0;

    /**
     * 每个CPU的查询能力  单位：ms
     */
    private Double  queryTimePerCpu            = 1000.0;

    /**
     * 每个shard消耗的磁盘
     */
    private Double  costDiskPerShardG          = 50.0;

    /**
     * 每个shard消耗的cpu
     */
    private Double  costCpuPerShard            = 1.0;

    /**
     * 节点free的阈值
     */
    private Double  nodeDiskFreeThreshold      = 0.95;

    /**
     * 节点free的阈值
     */
    private Long    docCountPerShard           = 70000000L;

    /**
     * 独占region的大索引的阈值，热数据的阈值
     */
    private Double  bigIndexHotDiskThreshold   = 7.2 * 1024;

}

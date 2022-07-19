package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 3/11/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterMetrics {
    /**
     * 统计的时间戳，单位：毫秒
     */
    private Long    timestamp;

    /**
     * 集群名称
     */
    private String  cluster;

    /**
     * 集群总shard数
     */
    private Long    shardNum;

    /**
     * 单集群写入耗时
     */
    private Double  indexingLatency;

    /**
     * 单集群查询耗时
     */
    private Double  searchLatency;

    /**
     * 单集群写入请求数
     */
    private Long    indexReqNum;

    /**
     * 单集群网关成功率
     */
    private Double  gatewaySucPer;
    /**
     * 单集群失败率
     */
    private Double  gatewayFailedPer;

    /**
     * 单集群Pending task数
     */
    private Long    pendingTaskNum;

    /**
     * 集群http连接数
     */
    private Long    httpNum;

    /**
     * 写入文档数突增量（上个时间间隔的写文档数的两倍）
     */
    private Long    docUprushNum;

    /**
     * 查询请求数突增量 （上个时间间隔请求数的两倍）
     */
    private Long    reqUprushNum;

    /**
     * 消耗时间 （开始采集到结束采集的时间）
     */
    private Long    clusterElapsedTime;

    /**
     * 消耗时间是否大于5分钟（开始采集到结束采集的时间）
     */
    private Boolean clusterElapsedTimeGte5Min;
}
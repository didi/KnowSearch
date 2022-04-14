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
public class IndexMetrics {
    private Long    timestamp;
    /**
     * 集群名称
     */
    private String  cluster;
    /**
     * 索引名称
     */
    private String  index;
    /**
     * 是否为RED索引
     */
    private Boolean red;
    /**
     * 是否为单副本索引
     */
    private Boolean singReplicate;
    /**
     * 是否存在未分配shard
     */
    private Boolean unassignedShard;
    /**
     * 是否存在大shard
     */
    private Boolean bigShard;
    /**
     * 是否存在小shard
     */
    private Boolean smallShard;
    /**
     * mapping字段个数
     */
    private Long    mappingNum;
    /**
     * segment个数
     */
    private Long    segmentNum;
    /**
     * 占用segment内存大小
     */
    private Double  segmentMemSize;

    /**
     * 写入文档数突增量（上个时间间隔的两倍）
     */
    private Long    docUprushNum;

    /**
     * 查询请求数突增量 （上个时间间隔的两倍）
     */
    private Long    reqUprushNum;
}
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
public class ClusterThreadPoolQueueMetrics {
    /**
     * 集群名称
     */
    private String cluster;
    private Long   timestamp;
    private Long   management;
    private Long   refresh;
    private Long   flush;
    private Long   merge;
    private Long   search;
    private Long   write;
}
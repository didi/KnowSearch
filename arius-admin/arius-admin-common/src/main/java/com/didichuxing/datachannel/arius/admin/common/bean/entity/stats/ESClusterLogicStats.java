package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESClusterLogicStats {
    private Long    clusterId;
    private Integer status;
    private Long    pendingTask;
    private Long    unassignedShards;
    private Integer clusterLevel;
    private Long    timestamp;
}

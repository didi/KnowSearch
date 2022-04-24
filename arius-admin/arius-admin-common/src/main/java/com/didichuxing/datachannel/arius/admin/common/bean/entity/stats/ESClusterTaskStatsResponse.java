package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-01-13 11:55 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESClusterTaskStatsResponse {
    private String action;
    private String taskId;
    private String parentTaskId;
    private String type;
    private long   startTime;
    private long   runningTime;
    private String runningTimeString;
    private String ip;
    private String node;
    private String description;
}
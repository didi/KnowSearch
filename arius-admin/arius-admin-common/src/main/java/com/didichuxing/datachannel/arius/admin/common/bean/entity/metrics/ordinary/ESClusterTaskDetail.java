package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author didi
 * @date 2022-01-14 1:42 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESClusterTaskDetail implements Serializable {

    private String taskId;

    private String node;

    private String action;

    private long startTime;

    private long runningTime;

    private String runningTimeString;

    private String description;
}

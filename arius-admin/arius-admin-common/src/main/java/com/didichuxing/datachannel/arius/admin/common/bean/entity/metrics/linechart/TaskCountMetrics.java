package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author didi
 * @date 2022-01-13 6:01 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCountMetrics implements Serializable {
    private long timeStamp;
    private long taskCount;
}

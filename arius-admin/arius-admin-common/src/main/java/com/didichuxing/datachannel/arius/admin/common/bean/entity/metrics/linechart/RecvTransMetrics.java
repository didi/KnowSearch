package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by linyunan on 2021-08-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecvTransMetrics implements Serializable {
    private long   timeStamp;
    private double recvTransSize;
}

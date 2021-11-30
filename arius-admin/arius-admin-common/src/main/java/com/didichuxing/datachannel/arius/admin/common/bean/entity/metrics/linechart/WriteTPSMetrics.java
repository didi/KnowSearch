package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-08-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WriteTPSMetrics implements Serializable {
    private long   timeStamp;
    private double writeTps;
}

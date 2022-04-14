package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiskInfoMetrics implements Serializable {
    private long   timeStamp;
    private double totalStoreSize;
    private double storeSize;
    private double freeStoreSize;
}

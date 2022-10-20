package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.percentiles;

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
public class BasePercentileMetrics implements Serializable, Comparable<BasePercentileMetrics> {

    private double aggType;
    private double st99;
    private double st95;
    private double st75;
    private double st55;
    private long   timeStamp;

    @Override
    public int compareTo(BasePercentileMetrics o) {
        if (null == o) {
            return 0;
        }

        return (int) (this.getTimeStamp() - o.getTimeStamp());
    }
}

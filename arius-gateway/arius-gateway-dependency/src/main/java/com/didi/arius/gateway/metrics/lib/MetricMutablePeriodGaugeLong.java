/**
 * Kuaidadi.com Inc.
 * Copyright (c) 2012-2015 All Rights Reserved.
 */
package com.didi.arius.gateway.metrics.lib;

import com.didi.arius.gateway.metrics.MetricsRecordBuilder;

/**
 * 
 * @author liujianhui
 * @version:2015年11月11日 上午11:04:06
 */
public class MetricMutablePeriodGaugeLong extends MetricMutablePeriodGauge<Long> {
    private volatile long value;

    /**
     * Construct a mutable long gauge metric
     * @param name  of the gauge
     * @param description of the gauge
     * @param initValue the initial value of the gauge
     */
    public MetricMutablePeriodGaugeLong(String name, String description, long initValue) {
        super(name, description);
        this.value = initValue;
    }

    public synchronized void incr() {
        ++value;
        setChanged();
    }

    /**
     * Increment by delta
     * @param delta of the increment
     */
    public synchronized void incr(long delta) {
        value += delta;
        setChanged();
    }

    public synchronized void decr() {
        --value;
        setChanged();
    }

    /**
     * decrement by delta
     * @param delta of the decrement
     */
    public synchronized void decr(long delta) {
        value -= delta;
        setChanged();
    }

    /**
     * Set the value of the metric
     * @param value to set
     */
    public void set(long value) {
        this.value = value;
        setChanged();
    }

    public void doSnapshot(MetricsRecordBuilder builder, boolean all) {
        builder.addGauge(name, description, value);
    }

    /**
     * set the value of the metric with 0
     * @see MetricMutablePeriodGauge#reset()
     */
    public synchronized void reset() {
        this.value = 0;
    }

}

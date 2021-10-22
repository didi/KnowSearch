package com.didi.arius.gateway.metrics.lib;

import com.didi.arius.gateway.metrics.MetricsRecordBuilder;

public class MetricMutablePeriodGaugeInt extends MetricMutablePeriodGauge<Integer> {
    private volatile int value;

    /**
     * Construct a mutable int gauge metric
     * @param name  of the gauge
     * @param description of the gauge
     * @param initValue the initial value of the gauge
     */
    public MetricMutablePeriodGaugeInt(String name, String description, int initValue) {
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
    public synchronized void incr(int delta) {
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
    public synchronized void decr(int delta) {
        value -= delta;
        setChanged();
    }

    /**
     * Set the value of the metric
     * @param value to set
     */
    public void set(int value) {
        this.value = value;
        setChanged();
    }

    public void doSnapshot(MetricsRecordBuilder builder, boolean all) {
        builder.addGauge(name, description, value);
    }

    @Override
    public synchronized void reset() {
        this.value = 0;
    }
}

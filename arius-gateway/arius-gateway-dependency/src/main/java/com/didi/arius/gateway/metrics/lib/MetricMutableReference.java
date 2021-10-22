package com.didi.arius.gateway.metrics.lib;

import com.didi.arius.gateway.metrics.MetricsRecordBuilder;

import java.util.concurrent.atomic.AtomicReference;

public class MetricMutableReference<T> extends MetricMutable {

    private final AtomicReference<T> reference;

    public MetricMutableReference(String name, String description, T value) {
        super(name, description);
        this.reference = new AtomicReference<T>(value);
    }

    @Override
    public void snapshot(MetricsRecordBuilder builder, boolean all) {
        if (all || changed()) {
            builder.add(new MetricReference<T>(name, description, reference.get()));
            clearChanged();
        }
    }

    public void set(T value) {
        reference.set(value);
    }

    public T get() {
        return reference.get();
    }
}

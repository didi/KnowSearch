package com.didi.cloud.fastdump.common.event.es.metrics;

import com.didi.cloud.fastdump.common.event.BaseEvent;

/**
 * Created by linyunan on 2022/11/22
 */
public abstract class BaseMetricsEvent extends BaseEvent {
    public BaseMetricsEvent(Object source) {
        super(source);
    }
}

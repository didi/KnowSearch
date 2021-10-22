package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class Recovery {
    @JSONField(name = "current_as_source")
    private long currentAsSource;

    @JSONField(name = "current_as_target")
    private long currentAsTarget;

    @JSONField(name = "throttle_time_in_millis")
    private long throttleTimeInMillis;

    public long getCurrentAsSource() {
        return currentAsSource;
    }

    public void setCurrentAsSource(long currentAsSource) {
        this.currentAsSource = currentAsSource;
    }

    public long getCurrentAsTarget() {
        return currentAsTarget;
    }

    public void setCurrentAsTarget(long currentAsTarget) {
        this.currentAsTarget = currentAsTarget;
    }

    public long getThrottleTimeInMillis() {
        return throttleTimeInMillis;
    }

    public void setThrottleTimeInMillis(long throttleTimeInMillis) {
        this.throttleTimeInMillis = throttleTimeInMillis;
    }
}

package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class Store {
    @JSONField(name = "size_in_bytes")
    private long sizeInBytes;

    @JSONField(name = "throttle_time_in_millis")
    private long throttleTimeInMillis;

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public long getThrottleTimeInMillis() {
        return throttleTimeInMillis;
    }

    public void setThrottleTimeInMillis(long throttleTimeInMillis) {
        this.throttleTimeInMillis = throttleTimeInMillis;
    }
}

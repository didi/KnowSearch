package com.didi.arius.gateway.elasticsearch.client.response.model.fs;

import com.alibaba.fastjson.annotation.JSONField;

public class FSTotal {
    @JSONField(name = "total_in_bytes")
    private long totalInBytes;

    @JSONField(name = "free_in_bytes")
    private long freeInBytes;

    @JSONField(name = "available_in_bytes")
    private long availableInBytes;

    public FSTotal() {
        // pass
    }

    public long getTotalInBytes() {
        return totalInBytes;
    }

    public void setTotalInBytes(long totalInBytes) {
        this.totalInBytes = totalInBytes;
    }

    public long getFreeInBytes() {
        return freeInBytes;
    }

    public void setFreeInBytes(long freeInBytes) {
        this.freeInBytes = freeInBytes;
    }

    public long getAvailableInBytes() {
        return availableInBytes;
    }

    public void setAvailableInBytes(long availableInBytes) {
        this.availableInBytes = availableInBytes;
    }
}

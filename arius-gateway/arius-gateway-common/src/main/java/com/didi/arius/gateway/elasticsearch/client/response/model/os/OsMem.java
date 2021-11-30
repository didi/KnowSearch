package com.didi.arius.gateway.elasticsearch.client.response.model.os;

import com.alibaba.fastjson.annotation.JSONField;

public class OsMem {

    @JSONField(name = "total_in_bytes")
    private long totalInBytes;
    @JSONField(name = "free_in_bytes")
    private long freeInBytes;
    @JSONField(name = "used_in_bytes")
    private long usedInBytes;
    @JSONField(name = "free_percent")
    private long freePercent;
    @JSONField(name = "used_percent")
    private long usedPercent;

    public OsMem() {
        // pass
    }

    public long getTotalInBytes() {
        return totalInBytes;
    }

    public long getFreeInBytes() {
        return freeInBytes;
    }

    public void setTotalInBytes(long totalInBytes) {
        this.totalInBytes = totalInBytes;
    }

    public void setFreeInBytes(long freeInBytes) {
        this.freeInBytes = freeInBytes;
    }

    public long getUsedInBytes() {
        return usedInBytes;
    }

    public void setUsedInBytes(long usedInBytes) {
        this.usedInBytes = usedInBytes;
    }

    public long getFreePercent() {
        return freePercent;
    }

    public void setFreePercent(long freePercent) {
        this.freePercent = freePercent;
    }

    public long getUsedPercent() {
        return usedPercent;
    }

    public void setUsedPercent(long usedPercent) {
        this.usedPercent = usedPercent;
    }
}

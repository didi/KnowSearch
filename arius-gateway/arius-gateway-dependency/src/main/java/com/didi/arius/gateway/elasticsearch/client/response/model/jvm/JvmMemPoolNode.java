package com.didi.arius.gateway.elasticsearch.client.response.model.jvm;

import com.alibaba.fastjson.annotation.JSONField;

public class JvmMemPoolNode {
    @JSONField(name = "used_in_bytes")
    private long usedInBytes;

    @JSONField(name = "max_in_bytes")
    private long maxInBytes;

    @JSONField(name = "peak_used_in_bytes")
    private long peakUsedInBytes;

    @JSONField(name = "peak_max_in_bytes")
    private long peakMaxInBytes;

    public long getUsedInBytes() {
        return usedInBytes;
    }

    public void setUsedInBytes(long usedInBytes) {
        this.usedInBytes = usedInBytes;
    }

    public long getMaxInBytes() {
        return maxInBytes;
    }

    public void setMaxInBytes(long maxInBytes) {
        this.maxInBytes = maxInBytes;
    }

    public long getPeakUsedInBytes() {
        return peakUsedInBytes;
    }

    public void setPeakUsedInBytes(long peakUsedInBytes) {
        this.peakUsedInBytes = peakUsedInBytes;
    }

    public long getPeakMaxInBytes() {
        return peakMaxInBytes;
    }

    public void setPeakMaxInBytes(long peakMaxInBytes) {
        this.peakMaxInBytes = peakMaxInBytes;
    }
}

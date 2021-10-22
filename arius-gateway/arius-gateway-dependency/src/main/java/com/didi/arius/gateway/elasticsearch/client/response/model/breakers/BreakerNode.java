package com.didi.arius.gateway.elasticsearch.client.response.model.breakers;

import com.alibaba.fastjson.annotation.JSONField;

public class BreakerNode {
    @JSONField(name = "limit_size_in_bytes")
    private long limitSizeInBytes;

    @JSONField(name = "limit_size")
    private String limitSize;

    @JSONField(name = "estimated_size_in_bytes")
    private long estimatedSizeInBytes;

    @JSONField(name = "estimated_size")
    private String estimatedSize;

    @JSONField(name = "overhead")
    private long overhead;

    @JSONField(name = "tripped")
    private long tripped;

    public long getLimitSizeInBytes() {
        return limitSizeInBytes;
    }

    public void setLimitSizeInBytes(long limitSizeInBytes) {
        this.limitSizeInBytes = limitSizeInBytes;
    }

    public String getLimitSize() {
        return limitSize;
    }

    public void setLimitSize(String limitSize) {
        this.limitSize = limitSize;
    }

    public long getEstimatedSizeInBytes() {
        return estimatedSizeInBytes;
    }

    public void setEstimatedSizeInBytes(long estimatedSizeInBytes) {
        this.estimatedSizeInBytes = estimatedSizeInBytes;
    }

    public String getEstimatedSize() {
        return estimatedSize;
    }

    public void setEstimatedSize(String estimatedSize) {
        this.estimatedSize = estimatedSize;
    }

    public long getOverhead() {
        return overhead;
    }

    public void setOverhead(long overhead) {
        this.overhead = overhead;
    }

    public long getTripped() {
        return tripped;
    }

    public void setTripped(long tripped) {
        this.tripped = tripped;
    }
}

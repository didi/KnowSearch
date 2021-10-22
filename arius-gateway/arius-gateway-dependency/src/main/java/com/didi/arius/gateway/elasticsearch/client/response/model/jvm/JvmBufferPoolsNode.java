package com.didi.arius.gateway.elasticsearch.client.response.model.jvm;

import com.alibaba.fastjson.annotation.JSONField;

public class JvmBufferPoolsNode {
    @JSONField(name = "count")
    private long count;

    @JSONField(name = "used_in_bytes")
    private long usedInBytes;

    @JSONField(name = "total_capacity_in_bytes")
    private long totalCapacityInBytes;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getUsedInBytes() {
        return usedInBytes;
    }

    public void setUsedInBytes(long usedInBytes) {
        this.usedInBytes = usedInBytes;
    }

    public long getTotalCapacityInBytes() {
        return totalCapacityInBytes;
    }

    public void setTotalCapacityInBytes(long totalCapacityInBytes) {
        this.totalCapacityInBytes = totalCapacityInBytes;
    }
}

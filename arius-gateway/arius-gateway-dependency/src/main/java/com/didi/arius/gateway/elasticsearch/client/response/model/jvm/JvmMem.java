package com.didi.arius.gateway.elasticsearch.client.response.model.jvm;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;

public class JvmMem {

    @JSONField(name = "heap_used_in_bytes")
    private long heapUsedInBytes;

    @JSONField(name = "heap_used_percent")
    private long heapUsedPercent;

    @JSONField(name = "heap_committed_in_bytes")
    private long heapCommittedInBytes;

    @JSONField(name = "heap_max_in_bytes")
    private long heapMaxInBytes;

    @JSONField(name = "non_heap_used_in_bytes")
    private long nonHeapUsedInBytes;

    @JSONField(name = "non_heap_committed_in_bytes")
    private long nonHeapCommittedInBytes;

    private Map<String, JvmMemPoolNode> pools;

    public long getHeapUsedInBytes() {
        return heapUsedInBytes;
    }

    public void setHeapUsedInBytes(long heapUsedInBytes) {
        this.heapUsedInBytes = heapUsedInBytes;
    }

    public long getHeapUsedPercent() {
        return heapUsedPercent;
    }

    public void setHeapUsedPercent(long heapUsedPercent) {
        this.heapUsedPercent = heapUsedPercent;
    }

    public long getHeapCommittedInBytes() {
        return heapCommittedInBytes;
    }

    public void setHeapCommittedInBytes(long heapCommittedInBytes) {
        this.heapCommittedInBytes = heapCommittedInBytes;
    }

    public long getHeapMaxInBytes() {
        return heapMaxInBytes;
    }

    public void setHeapMaxInBytes(long heapMaxInBytes) {
        this.heapMaxInBytes = heapMaxInBytes;
    }

    public long getNonHeapUsedInBytes() {
        return nonHeapUsedInBytes;
    }

    public void setNonHeapUsedInBytes(long nonHeapUsedInBytes) {
        this.nonHeapUsedInBytes = nonHeapUsedInBytes;
    }

    public long getNonHeapCommittedInBytes() {
        return nonHeapCommittedInBytes;
    }

    public void setNonHeapCommittedInBytes(long nonHeapCommittedInBytes) {
        this.nonHeapCommittedInBytes = nonHeapCommittedInBytes;
    }

    public Map<String, JvmMemPoolNode> getPools() {
        return pools;
    }

    public void setPools(Map<String, JvmMemPoolNode> pools) {
        this.pools = pools;
    }
}

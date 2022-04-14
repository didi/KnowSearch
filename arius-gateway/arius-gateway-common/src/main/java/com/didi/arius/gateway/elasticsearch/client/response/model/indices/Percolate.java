package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class Percolate {
    @JSONField(name = "total")
    private long total;

    @JSONField(name = "time_in_millis")
    private long timeInMillis;

    @JSONField(name = "current")
    private long current;

    @JSONField(name = "memory_size_in_bytes")
    private long memorySizeInBytes;

    @JSONField(name = "memory_size")
    private String memorySize;

    @JSONField(name = "queries")
    private long queries;

    public Percolate() {
        // pass
    }

    public long getTotal() {
        return total;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public long getCurrent() {
        return current;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getMemorySizeInBytes() {
        return memorySizeInBytes;
    }

    public void setMemorySizeInBytes(long memorySizeInBytes) {
        this.memorySizeInBytes = memorySizeInBytes;
    }

    public String getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(String memorySize) {
        this.memorySize = memorySize;
    }

    public long getQueries() {
        return queries;
    }

    public void setQueries(long queries) {
        this.queries = queries;
    }
}

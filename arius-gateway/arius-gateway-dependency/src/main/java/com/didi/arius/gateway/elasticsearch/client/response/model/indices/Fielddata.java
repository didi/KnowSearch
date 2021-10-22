package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class Fielddata {
    @JSONField(name = "memory_size_in_bytes")
    private long memorySizeInBytes;

    @JSONField(name = "evictions")
    private long evictions;

    public long getMemorySizeInBytes() {
        return memorySizeInBytes;
    }

    public void setMemorySizeInBytes(long memorySizeInBytes) {
        this.memorySizeInBytes = memorySizeInBytes;
    }

    public long getEvictions() {
        return evictions;
    }

    public void setEvictions(long evictions) {
        this.evictions = evictions;
    }
}

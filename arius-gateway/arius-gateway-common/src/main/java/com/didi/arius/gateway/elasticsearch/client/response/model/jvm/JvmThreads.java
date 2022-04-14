package com.didi.arius.gateway.elasticsearch.client.response.model.jvm;

import com.alibaba.fastjson.annotation.JSONField;

public class JvmThreads {
    @JSONField(name = "count")
    private long count;

    @JSONField(name = "peak_count")
    private long peakCount;

    public JvmThreads() {
        // pass
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getPeakCount() {
        return peakCount;
    }

    public void setPeakCount(long peakCount) {
        this.peakCount = peakCount;
    }
}

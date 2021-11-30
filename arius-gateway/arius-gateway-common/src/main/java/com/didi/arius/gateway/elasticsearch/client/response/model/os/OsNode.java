package com.didi.arius.gateway.elasticsearch.client.response.model.os;

import com.alibaba.fastjson.annotation.JSONField;

public class OsNode {
    @JSONField(name = "timestamp")
    private long timestamp;
    @JSONField(name = "cpu_percent")
    private long cpuPercent;
    @JSONField(name = "load_average")
    private double loadAverage;
    @JSONField(name = "mem")
    private OsMem mem;
    @JSONField(name = "swap")
    private OsSwap swap;

    public OsNode() {
        // pass
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getCpuPercent() {
        return cpuPercent;
    }

    public void setCpuPercent(long cpuPercent) {
        this.cpuPercent = cpuPercent;
    }

    public double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
    }

    public OsMem getMem() {
        return mem;
    }

    public void setMem(OsMem mem) {
        this.mem = mem;
    }

    public OsSwap getSwap() {
        return swap;
    }

    public void setSwap(OsSwap swap) {
        this.swap = swap;
    }
}

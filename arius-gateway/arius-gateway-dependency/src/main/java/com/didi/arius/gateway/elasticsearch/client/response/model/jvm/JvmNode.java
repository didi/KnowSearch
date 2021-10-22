package com.didi.arius.gateway.elasticsearch.client.response.model.jvm;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;

public class JvmNode {
    @JSONField(name = "timestamp")
    private long timestamp;
    @JSONField(name = "uptime_in_millis")
    private long uptimeInMillis;

    @JSONField(name = "mem")
    private JvmMem mem;
    @JSONField(name = "threads")
    private JvmThreads threads;
    @JSONField(name = "gc")
    private Map<String, Map<String, JvmGCNode>> gc;
    @JSONField(name = "buffer_pools")
    private Map<String, JvmBufferPoolsNode> bufferPools;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getUptimeInMillis() {
        return uptimeInMillis;
    }

    public void setUptimeInMillis(long uptimeInMillis) {
        this.uptimeInMillis = uptimeInMillis;
    }

    public JvmMem getMem() {
        return mem;
    }

    public void setMem(JvmMem mem) {
        this.mem = mem;
    }

    public JvmThreads getThreads() {
        return threads;
    }

    public void setThreads(JvmThreads threads) {
        this.threads = threads;
    }

    public Map<String, Map<String, JvmGCNode>> getGc() {
        return gc;
    }

    public void setGc(Map<String, Map<String, JvmGCNode>> gc) {
        this.gc = gc;
    }

    public Map<String, JvmBufferPoolsNode> getBufferPools() {
        return bufferPools;
    }

    public void setBufferPools(Map<String, JvmBufferPoolsNode> bufferPools) {
        this.bufferPools = bufferPools;
    }
}

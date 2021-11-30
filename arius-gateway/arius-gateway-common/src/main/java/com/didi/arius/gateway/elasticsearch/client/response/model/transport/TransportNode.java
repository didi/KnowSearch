package com.didi.arius.gateway.elasticsearch.client.response.model.transport;

import com.alibaba.fastjson.annotation.JSONField;

public class TransportNode {
    @JSONField(name = "server_open")
    private long serverOpen;

    @JSONField(name = "rx_count")
    private long rxCount;

    @JSONField(name = "rx_size_in_bytes")
    private long rxSizeInBytes;

    @JSONField(name = "tx_count")
    private long txCount;

    @JSONField(name = "tx_size_in_bytes")
    private long txSizeInBytes;

    public TransportNode() {
        // pass
    }

    public long getServerOpen() {
        return serverOpen;
    }

    public void setServerOpen(long serverOpen) {
        this.serverOpen = serverOpen;
    }

    public long getRxCount() {
        return rxCount;
    }

    public void setRxCount(long rxCount) {
        this.rxCount = rxCount;
    }

    public long getRxSizeInBytes() {
        return rxSizeInBytes;
    }

    public void setRxSizeInBytes(long rxSizeInBytes) {
        this.rxSizeInBytes = rxSizeInBytes;
    }

    public long getTxCount() {
        return txCount;
    }

    public void setTxCount(long txCount) {
        this.txCount = txCount;
    }

    public long getTxSizeInBytes() {
        return txSizeInBytes;
    }

    public void setTxSizeInBytes(long txSizeInBytes) {
        this.txSizeInBytes = txSizeInBytes;
    }
}

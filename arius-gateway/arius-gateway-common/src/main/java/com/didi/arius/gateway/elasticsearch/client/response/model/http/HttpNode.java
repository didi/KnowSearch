package com.didi.arius.gateway.elasticsearch.client.response.model.http;

import com.alibaba.fastjson.annotation.JSONField;

public class HttpNode {
    @JSONField(name = "current_open")
    private long currentOpen;

    @JSONField(name = "total_opened")
    private long totalOpened;

    public HttpNode() {
        // pass
    }

    public long getCurrentOpen() {
        return currentOpen;
    }

    public void setCurrentOpen(long currentOpen) {
        this.currentOpen = currentOpen;
    }

    public long getTotalOpened() {
        return totalOpened;
    }

    public void setTotalOpened(long totalOpened) {
        this.totalOpened = totalOpened;
    }
}

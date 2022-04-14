package com.didi.arius.gateway.elasticsearch.client.response.model.fs;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class FSNode {
    @JSONField(name = "timestamp")
    private long timestamp;

    @JSONField(name = "total")
    private FSTotal total;

    @JSONField(name = "data")
    private List<FSDataNode> data;

    public FSNode() {
        // pass
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public FSTotal getTotal() {
        return total;
    }

    public void setTotal(FSTotal total) {
        this.total = total;
    }

    public List<FSDataNode> getData() {
        return data;
    }

    public void setData(List<FSDataNode> data) {
        this.data = data;
    }
}

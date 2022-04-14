package com.didi.arius.gateway.elasticsearch.client.response.model.fs;

import com.alibaba.fastjson.annotation.JSONField;

public class FSDataNode {
    @JSONField(name = "path")
    private String path;

    @JSONField(name = "mount")
    private String mount;

    @JSONField(name = "type")
    private String type;

    @JSONField(name = "total_in_bytes")
    private long totalInBytes;

    @JSONField(name = "free_in_bytes")
    private long freeInBytes;

    @JSONField(name = "available_in_bytes")
    private long availableInBytes;

    public FSDataNode() {
        // pass
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMount() {
        return mount;
    }

    public void setMount(String mount) {
        this.mount = mount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTotalInBytes() {
        return totalInBytes;
    }

    public void setTotalInBytes(long totalInBytes) {
        this.totalInBytes = totalInBytes;
    }

    public void setFreeInBytes(long freeInBytes) {
        this.freeInBytes = freeInBytes;
    }

    public long getAvailableInBytes() {
        return availableInBytes;
    }

    public void setAvailableInBytes(long availableInBytes) {
        this.availableInBytes = availableInBytes;
    }

    public long getFreeInBytes() {
        return freeInBytes;
    }
}

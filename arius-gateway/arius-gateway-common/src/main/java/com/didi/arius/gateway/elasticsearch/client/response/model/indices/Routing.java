package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class Routing {
    @JSONField(name = "state")
    private String state;

    @JSONField(name = "primary")
    private boolean primary;

    @JSONField(name = "node")
    private String node;

    @JSONField(name = "relocating_node")
    private String relocatingNode;

    public Routing() {
        // pass
    }

    public String getState() {
        return state;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isPrimary() {
        return primary;
    }

    public String getNode() {
        return node;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getRelocatingNode() {
        return relocatingNode;
    }

    public void setRelocatingNode(String relocatingNode) {
        this.relocatingNode = relocatingNode;
    }
}

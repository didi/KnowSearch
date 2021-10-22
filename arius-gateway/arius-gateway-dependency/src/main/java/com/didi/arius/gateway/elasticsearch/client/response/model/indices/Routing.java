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


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getRelocatingNode() {
        return relocatingNode;
    }

    public void setRelocatingNode(String relocatingNode) {
        this.relocatingNode = relocatingNode;
    }
}

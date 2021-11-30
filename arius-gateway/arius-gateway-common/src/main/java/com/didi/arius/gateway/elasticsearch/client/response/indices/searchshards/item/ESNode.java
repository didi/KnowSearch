package com.didi.arius.gateway.elasticsearch.client.response.indices.searchshards.item;


import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.response.model.node.NodeAttributes;

public class ESNode {
    @JSONField(name = "name")
    private String name;

    @JSONField(name = "transport_address")
    private String transportAddress;

    @JSONField(name = "attributes")
    private NodeAttributes attributes;

    public ESNode() {
        // pass
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransportAddress() {
        return transportAddress;
    }

    public void setTransportAddress(String transportAddress) {
        this.transportAddress = transportAddress;
    }

    public NodeAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(NodeAttributes attributes) {
        this.attributes = attributes;
    }
}

package com.didi.arius.gateway.elasticsearch.client.response.model.node;

import com.alibaba.fastjson.annotation.JSONField;

public class NodeAttributes {
    @JSONField(name = "rack")
    private String rack;
    @JSONField(name = "set")
    private String set;
    @JSONField(name = "max_local_storage_nodes")
    private long maxLocalStorageNodes;
    @JSONField(name = "master")
    private boolean master;

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public long getMaxLocalStorageNodes() {
        return maxLocalStorageNodes;
    }

    public void setMaxLocalStorageNodes(long maxLocalStorageNodes) {
        this.maxLocalStorageNodes = maxLocalStorageNodes;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }
}

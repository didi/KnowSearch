package com.didi.arius.gateway.elasticsearch.client.response.model.node;

import com.alibaba.fastjson.annotation.JSONField;

public class NodeAttributes {
    @JSONField(name = "set")
    private String set;
    @JSONField(name = "max_local_storage_nodes")
    private long maxLocalStorageNodes;
    @JSONField(name = "master")
    private boolean master;

    public NodeAttributes() {
        // pass
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

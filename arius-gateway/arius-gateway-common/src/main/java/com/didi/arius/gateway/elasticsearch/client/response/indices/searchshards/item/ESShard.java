package com.didi.arius.gateway.elasticsearch.client.response.indices.searchshards.item;

import com.alibaba.fastjson.annotation.JSONField;

public class ESShard {
    @JSONField(name = "state")
    private String state;

    @JSONField(name = "primary")
    private boolean primary;

    @JSONField(name = "node")
    private String node;

    @JSONField(name = "relocating_node")
    private String relocatingNode;

    @JSONField(name = "shard")
    private long shard;

    @JSONField(name = "index")
    private String index;

    @JSONField(name = "version")
    private String version;

    @JSONField(name = "allocation_id")
    private AllocatinID allocationId;

    public ESShard() {
        // pass
    }

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

    public long getShard() {
        return shard;
    }

    public void setShard(long shard) {
        this.shard = shard;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public AllocatinID getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(AllocatinID allocationId) {
        this.allocationId = allocationId;
    }


    public static class AllocatinID {
        @JSONField(name = "id")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}

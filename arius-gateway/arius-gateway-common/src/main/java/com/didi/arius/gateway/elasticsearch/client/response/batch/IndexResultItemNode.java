package com.didi.arius.gateway.elasticsearch.client.response.batch;

public class IndexResultItemNode {
    private IndexResultNode index;

    public IndexResultItemNode() {
        // pass
    }

    public IndexResultNode getIndex() {
        return index;
    }

    public void setIndex(IndexResultNode index) {
        this.index = index;
    }

    /**
     * Is this a failed execution of an operation.
     */
    public boolean isFailed() {
        return index.getError() != null;
    }
}

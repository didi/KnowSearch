package com.didi.arius.gateway.elasticsearch.client.response.model.jvm;

import com.alibaba.fastjson.annotation.JSONField;

public class JvmGCNode {
    @JSONField(name = "collection_count")
    private long collectionCount;

    @JSONField(name = "collection_time_in_millis")
    private long collectionTimeInMillis;

    public long getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(long collectionCount) {
        this.collectionCount = collectionCount;
    }

    public long getCollectionTimeInMillis() {
        return collectionTimeInMillis;
    }

    public void setCollectionTimeInMillis(long collectionTimeInMillis) {
        this.collectionTimeInMillis = collectionTimeInMillis;
    }
}

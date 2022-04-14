package com.didi.arius.gateway.elasticsearch.client.request.index.stats;

public enum IndicesStatsLevel {
    CLUSTER("cluster"),
    INDICES("indices"),
    SHARDS("shards");


    private String str;

    private IndicesStatsLevel(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }
}

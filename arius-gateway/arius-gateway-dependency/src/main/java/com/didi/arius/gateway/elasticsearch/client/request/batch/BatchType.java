package com.didi.arius.gateway.elasticsearch.client.request.batch;

public enum BatchType {

    CREATE("create"),
    INDEX("index"),
    UPDATE("update"),
    DELETE("delete");

    private String str;

    BatchType(String s) {
        this.str = s;
    }

    public String getStr() {
        return str;
    }
}

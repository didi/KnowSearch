package com.didi.arius.gateway.elasticsearch.client.model.type;

public enum ESVersion {
    ES233("es-version2.3.3"),
    ES651("es-version6.5.1");

    private String str;

    private ESVersion(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }
}

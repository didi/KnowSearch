package com.didi.arius.gateway.elasticsearch.client.response.model.breakers;

import com.alibaba.fastjson.annotation.JSONField;

public class Breakers {
    @JSONField(name = "request")
    private BreakerNode request;

    @JSONField(name = "fielddata")
    private BreakerNode fielddata;

    @JSONField(name = "parent")
    private BreakerNode parent;

    public BreakerNode getRequest() {
        return request;
    }

    public void setRequest(BreakerNode request) {
        this.request = request;
    }

    public BreakerNode getFielddata() {
        return fielddata;
    }

    public void setFielddata(BreakerNode fielddata) {
        this.fielddata = fielddata;
    }

    public BreakerNode getParent() {
        return parent;
    }

    public void setParent(BreakerNode parent) {
        this.parent = parent;
    }
}

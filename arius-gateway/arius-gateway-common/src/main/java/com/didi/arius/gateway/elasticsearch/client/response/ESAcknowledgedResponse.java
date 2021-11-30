package com.didi.arius.gateway.elasticsearch.client.response;

import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;

public class ESAcknowledgedResponse extends ESActionResponse {
    private Boolean acknowledged;

    public ESAcknowledgedResponse() {
        // pass
    }

    public Boolean getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
}

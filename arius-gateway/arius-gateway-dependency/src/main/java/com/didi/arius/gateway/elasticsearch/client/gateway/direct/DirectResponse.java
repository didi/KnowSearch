package com.didi.arius.gateway.elasticsearch.client.gateway.direct;

import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;

public class DirectResponse extends ESActionResponse {
    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    private String responseContent;

    public DirectResponse() {

    }

    public DirectResponse(String responseContent) {
        this.responseContent = responseContent;
    }

    @Override
    public String toString() {
        return responseContent;
    }
}

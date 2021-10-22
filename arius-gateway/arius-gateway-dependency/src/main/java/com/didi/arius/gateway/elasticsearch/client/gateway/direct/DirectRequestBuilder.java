package com.didi.arius.gateway.elasticsearch.client.gateway.direct;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class DirectRequestBuilder extends ActionRequestBuilder<DirectRequest, DirectResponse, DirectRequestBuilder> {
    protected DirectRequestBuilder(ElasticsearchClient client, Action<DirectRequest, DirectResponse, DirectRequestBuilder> action, DirectRequest request) {
        super(client, action, request);
    }

    public DirectRequestBuilder(ElasticsearchClient client, DirectAction directAction) {
        super(client, directAction, new DirectRequest());
    }
}

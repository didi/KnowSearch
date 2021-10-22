package com.didi.arius.gateway.elasticsearch.client.gateway.direct;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class DirectAction extends Action<DirectRequest, DirectResponse, DirectRequestBuilder> {
    public static final DirectAction INSTANCE = new DirectAction();
    public static final String NAME = "rest:direct/action";

    private DirectAction() {
        super(NAME);
    }

    @Override
    public DirectResponse newResponse() {
        return new DirectResponse();
    }

    @Override
    public DirectRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new DirectRequestBuilder(client, this);
    }
}

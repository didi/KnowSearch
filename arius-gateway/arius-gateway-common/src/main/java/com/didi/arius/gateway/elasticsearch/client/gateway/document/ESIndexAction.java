package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ESIndexAction extends Action<ESIndexRequest, ESIndexResponse, ESIndexRequestBuilder> {
    public static final ESIndexAction INSTANCE = new ESIndexAction();
    public static final String NAME = "indices:data/write/index";

    private ESIndexAction() {
        super(NAME);
    }

    @Override
    public ESIndexResponse newResponse() {
        return new ESIndexResponse();
    }

    @Override
    public ESIndexRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESIndexRequestBuilder(client, this);
    }
}

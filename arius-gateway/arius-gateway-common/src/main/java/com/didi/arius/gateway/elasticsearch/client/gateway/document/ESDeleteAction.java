package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ESDeleteAction extends Action<ESDeleteRequest, ESDeleteResponse, ESDeleteRequestBuilder> {
    public static final ESDeleteAction INSTANCE = new ESDeleteAction();
    public static final String NAME = "indices:data/write/delete";

    private ESDeleteAction() {
        super(NAME);
    }

    @Override
    public ESDeleteResponse newResponse() {
        return new ESDeleteResponse();
    }

    @Override
    public ESDeleteRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESDeleteRequestBuilder(client, this);
    }
}

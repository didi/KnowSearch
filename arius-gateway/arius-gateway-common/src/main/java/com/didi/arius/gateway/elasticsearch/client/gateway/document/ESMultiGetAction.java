package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ESMultiGetAction extends Action<ESMultiGetRequest, ESMultiGetResponse, ESMultiGetBuilder> {
    public static final ESMultiGetAction INSTANCE = new ESMultiGetAction();
    public static final String NAME = "indices:data/internal/mget";

    private ESMultiGetAction() {
        super(NAME);
    }

    @Override
    public ESMultiGetResponse newResponse() {
        return new ESMultiGetResponse();
    }

    @Override
    public ESMultiGetBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESMultiGetBuilder(client, this);
    }
}

package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class ESMultiGetBuilder extends ActionRequestBuilder<ESMultiGetRequest, ESMultiGetResponse, ESMultiGetBuilder> {

    public ESMultiGetBuilder(ElasticsearchClient client, ESMultiGetAction action) {
        super(client, action, new ESMultiGetRequest());
    }
}

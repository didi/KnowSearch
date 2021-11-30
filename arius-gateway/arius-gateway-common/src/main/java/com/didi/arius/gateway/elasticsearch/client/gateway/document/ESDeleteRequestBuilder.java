package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class ESDeleteRequestBuilder extends ActionRequestBuilder<ESDeleteRequest, ESDeleteResponse, ESDeleteRequestBuilder> {
    public ESDeleteRequestBuilder(ElasticsearchClient client, ESDeleteAction action) {
        super(client, action, new ESDeleteRequest());
    }
}
package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class ESIndexRequestBuilder  extends ActionRequestBuilder<ESIndexRequest, ESIndexResponse, ESIndexRequestBuilder> {
    public ESIndexRequestBuilder(ElasticsearchClient client, ESIndexAction action) {
        super(client, action, new ESIndexRequest());
    }
}

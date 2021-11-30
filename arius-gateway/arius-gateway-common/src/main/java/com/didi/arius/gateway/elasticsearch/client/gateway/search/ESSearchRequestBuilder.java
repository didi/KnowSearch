package com.didi.arius.gateway.elasticsearch.client.gateway.search;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class ESSearchRequestBuilder extends ActionRequestBuilder<ESSearchRequest, ESSearchResponse, ESSearchRequestBuilder> {
    protected ESSearchRequestBuilder(ElasticsearchClient client, Action<ESSearchRequest, ESSearchResponse, ESSearchRequestBuilder> action, ESSearchRequest request) {
        super(client, action, request);
    }

    public ESSearchRequestBuilder(ElasticsearchClient client, ESSearchAction action) {
        super(client, action, new ESSearchRequest());
    }
}

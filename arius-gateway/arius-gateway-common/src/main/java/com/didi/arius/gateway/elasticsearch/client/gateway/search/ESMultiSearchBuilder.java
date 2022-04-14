package com.didi.arius.gateway.elasticsearch.client.gateway.search;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class ESMultiSearchBuilder extends ActionRequestBuilder<ESMultiSearchRequest, ESMultiSearchResponse, ESMultiSearchBuilder> {
    protected ESMultiSearchBuilder(ElasticsearchClient client, Action<ESMultiSearchRequest, ESMultiSearchResponse, ESMultiSearchBuilder> action, ESMultiSearchRequest request) {
        super(client, action, request);
    }

    public ESMultiSearchBuilder(ElasticsearchClient client, ESMultiSearchAction action) {
        super(client, action, new ESMultiSearchRequest());
    }
}

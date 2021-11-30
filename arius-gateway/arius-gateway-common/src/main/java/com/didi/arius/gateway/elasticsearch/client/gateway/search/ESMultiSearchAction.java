package com.didi.arius.gateway.elasticsearch.client.gateway.search;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ESMultiSearchAction extends Action<ESMultiSearchRequest, ESMultiSearchResponse, ESMultiSearchBuilder> {
    public static final ESMultiSearchAction INSTANCE = new ESMultiSearchAction();
    public static final String NAME = "indices:data/internal/msearch";

    private ESMultiSearchAction() {
        super(NAME);
    }

    @Override
    public ESMultiSearchResponse newResponse() {
        return new ESMultiSearchResponse();
    }

    @Override
    public ESMultiSearchBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESMultiSearchBuilder(client, this);
    }
}

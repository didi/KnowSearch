package com.didi.arius.gateway.elasticsearch.client.gateway.search;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ESSearchAction extends Action<ESSearchRequest, ESSearchResponse, ESSearchRequestBuilder> {

    public static final ESSearchAction INSTANCE = new ESSearchAction();
    public static final String NAME = "indices:data/read/search";

    private ESSearchAction() {
        super(NAME);
    }

    @Override
    public ESSearchResponse newResponse() {
        return new ESSearchResponse();
    }

    @Override
    public ESSearchRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESSearchRequestBuilder(client, this);
    }
}

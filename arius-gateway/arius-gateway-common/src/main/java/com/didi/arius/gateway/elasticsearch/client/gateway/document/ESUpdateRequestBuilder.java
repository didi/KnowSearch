package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class ESUpdateRequestBuilder extends ActionRequestBuilder<ESUpdateRequest, ESUpdateResponse, ESUpdateRequestBuilder> {
    public ESUpdateRequestBuilder(ElasticsearchClient client, ESUpdateAction action) {
        super(client, action, new ESUpdateRequest());
    }
}

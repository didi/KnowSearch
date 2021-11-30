package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ESUpdateAction extends Action<ESUpdateRequest, ESUpdateResponse, ESUpdateRequestBuilder> {
    public static final ESUpdateAction INSTANCE = new ESUpdateAction();
    public static final String NAME = "indices:data/write/update";

    private ESUpdateAction() {
        super(NAME);
    }

    @Override
    public ESUpdateResponse newResponse() {
        return new ESUpdateResponse();
    }

    @Override
    public ESUpdateRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESUpdateRequestBuilder(client, this);
    }
}

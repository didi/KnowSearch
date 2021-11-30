package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.ElasticsearchClient;

public abstract class ESBroadcastTimeoutRequestBuilder<Request extends ESBroadcastTimeoutRequest<Request>, Response extends ActionResponse, RequestBuilder extends ESBroadcastTimeoutRequestBuilder<Request, Response, RequestBuilder>>
        extends ActionRequestBuilder<Request, Response, RequestBuilder> {

    protected ESBroadcastTimeoutRequestBuilder(ElasticsearchClient client, Action<Request, Response, RequestBuilder> action, Request request) {
        super(client, action, request);
    }

    @SuppressWarnings("unchecked")
    public final RequestBuilder setIndices(String... indices) {
        request.indices(indices);
        return (RequestBuilder) this;
    }

    @SuppressWarnings("unchecked")
    public final RequestBuilder setIndicesOptions(IndicesOptions indicesOptions) {
        request.indicesOptions(indicesOptions);
        return (RequestBuilder) this;
    }
}


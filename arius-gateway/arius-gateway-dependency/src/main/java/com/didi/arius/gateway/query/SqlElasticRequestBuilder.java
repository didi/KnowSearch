package com.didi.arius.gateway.query;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;

public interface SqlElasticRequestBuilder {
    ActionRequest request();
    String explain();
    ActionResponse get();
    ActionRequestBuilder getBuilder();

}

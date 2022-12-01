package com.didi.cloud.fastdump.rest.rest;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

public interface RestHandler {
    void dispatchRequest(RestRequest request, RestChannel channel);
}

/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ccr.rest;

import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

import java.io.IOException;

import static org.elasticsearch.xpack.core.ccr.action.PutFollowAction.INSTANCE;
import static org.elasticsearch.xpack.core.ccr.action.PutFollowAction.Request;

public class RestPutFollowAction extends BaseRestHandler {

    public RestPutFollowAction(RestController controller) {
        controller.registerHandler(RestRequest.Method.PUT, "/{index}/_ccr/follow", this);
    }

    @Override
    public String getName() {
        return "ccr_put_follow_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        Request request = createRequest(restRequest);
        return channel -> client.execute(INSTANCE, request, new RestToXContentListener<>(channel));
    }

    private static Request createRequest(RestRequest restRequest) throws IOException {
        try (XContentParser parser = restRequest.contentOrSourceParamParser()) {
            ActiveShardCount waitForActiveShards = ActiveShardCount.parseString(restRequest.param("wait_for_active_shards"));
            return Request.fromXContent(parser, restRequest.param("index"), waitForActiveShards);
        }
    }
}

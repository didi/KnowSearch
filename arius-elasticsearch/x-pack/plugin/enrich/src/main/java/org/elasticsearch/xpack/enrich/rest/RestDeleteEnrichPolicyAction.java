/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.enrich.rest;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;
import org.elasticsearch.xpack.core.enrich.action.DeleteEnrichPolicyAction;

import java.io.IOException;

public class RestDeleteEnrichPolicyAction extends BaseRestHandler {

    public RestDeleteEnrichPolicyAction(final RestController controller) {
        controller.registerHandler(RestRequest.Method.DELETE, "/_enrich/policy/{name}", this);
    }

    @Override
    public String getName() {
        return "delete_enrich_policy";
    }

    @Override
    protected RestChannelConsumer prepareRequest(final RestRequest restRequest, final NodeClient client) throws IOException {
        final DeleteEnrichPolicyAction.Request request = new DeleteEnrichPolicyAction.Request(restRequest.param("name"));
        return channel -> client.execute(DeleteEnrichPolicyAction.INSTANCE, request, new RestToXContentListener<>(channel));
    }
}

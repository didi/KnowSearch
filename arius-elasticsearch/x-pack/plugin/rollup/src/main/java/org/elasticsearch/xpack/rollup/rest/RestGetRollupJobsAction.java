/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.rollup.rest;

import org.apache.logging.log4j.LogManager;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;
import org.elasticsearch.xpack.core.rollup.action.GetRollupJobsAction;

import static org.elasticsearch.rest.RestRequest.Method.GET;

public class RestGetRollupJobsAction extends BaseRestHandler {

    private static final DeprecationLogger deprecationLogger = new DeprecationLogger(LogManager.getLogger(RestGetRollupJobsAction.class));

    public static final ParseField ID = new ParseField("id");

    public RestGetRollupJobsAction(RestController controller) {
        // TODO: remove deprecated endpoint in 8.0.0
        controller.registerWithDeprecatedHandler(
                GET, "/_rollup/job/{id}", this,
                GET, "/_xpack/rollup/job/{id}/", deprecationLogger);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) {
        String id = restRequest.param(ID.getPreferredName());
        GetRollupJobsAction.Request request = new GetRollupJobsAction.Request(id);

        return channel -> client.execute(GetRollupJobsAction.INSTANCE, request, new RestToXContentListener<>(channel));
    }

    @Override
    public String getName() {
        return "get_rollup_job";
    }

}

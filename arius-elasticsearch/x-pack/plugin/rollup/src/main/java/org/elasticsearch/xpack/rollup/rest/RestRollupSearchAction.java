/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.rollup.rest;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;
import org.elasticsearch.rest.action.search.RestSearchAction;
import org.elasticsearch.xpack.core.rollup.action.RollupSearchAction;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RestRollupSearchAction extends BaseRestHandler {

    private static final Set<String> RESPONSE_PARAMS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            RestSearchAction.TYPED_KEYS_PARAM,
            RestSearchAction.TOTAL_HITS_AS_INT_PARAM)));

    public RestRollupSearchAction(RestController controller) {
        controller.registerHandler(RestRequest.Method.GET, "_rollup_search", this);
        controller.registerHandler(RestRequest.Method.POST, "_rollup_search", this);
        controller.registerHandler(RestRequest.Method.GET, "{index}/_rollup_search", this);
        controller.registerHandler(RestRequest.Method.POST,  "{index}/_rollup_search", this);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        restRequest.withContentOrSourceParamParserOrNull(parser ->
                RestSearchAction.parseSearchRequest(searchRequest, restRequest, parser, size -> searchRequest.source().size(size)));
        RestSearchAction.checkRestTotalHits(restRequest, searchRequest);
        return channel -> client.execute(RollupSearchAction.INSTANCE, searchRequest, new RestToXContentListener<>(channel));
    }

    @Override
    public String getName() {
        return "rollup_search_action";
    }

    @Override
    protected Set<String> responseParams() {
        return RESPONSE_PARAMS;
    }
}

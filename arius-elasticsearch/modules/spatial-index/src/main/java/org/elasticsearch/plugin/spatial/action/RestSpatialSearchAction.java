/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.plugin.spatial.action;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.spatial.router.Router;
import org.elasticsearch.plugin.spatial.router.RouterResult;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestStatusToXContentListener;
import org.elasticsearch.rest.action.search.RestSearchAction;

import java.io.IOException;
import java.util.function.IntConsumer;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

public class RestSpatialSearchAction extends BaseRestHandler {
    private Router router;

    public RestSpatialSearchAction(Settings settings, RestController controller, Router router) {
        controller.registerHandler(GET, "/{index}/_spatial_search", this);
        controller.registerHandler(POST, "/{index}/_spatial_search", this);
        controller.registerHandler(GET, "/{index}/{type}/_spatial_search", this);
        controller.registerHandler(POST, "/{index}/{type}/_spatial_search", this);

        this.router = router;
    }

    @Override
    public String getName() {
        return "spatial_search_action";
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        /*
         * We have to pull out the call to `source().size(size)` because
         * _update_by_query and _delete_by_query uses this same parsing
         * path but sets a different variable when it sees the `size`
         * url parameter.
         *
         * Note that we can't use `searchRequest.source()::size` because
         * `searchRequest.source()` is null right now. We don't have to
         * guard against it being null in the IntConsumer because it can't
         * be null later. If that is confusing to you then you are in good
         * company.
         */
        SearchRequest searchRequest = new SearchRequest();
        IntConsumer setSize = size -> searchRequest.source().size(size);
        request.withContentOrSourceParamParserOrNull(parser -> RestSearchAction.parseSearchRequest(searchRequest, request, parser, setSize));

        RouterResult rr = router.doQueryRequest(searchRequest);
        if (logger.isTraceEnabled()) {
            String uri = request.uri();
            String traceId = request.param("traceid");
            String requestId = request.param("requestId");
            logger.trace("_spatial_request_in||requestId={}||traceId={}||uri={}||preference_shards={}||dsl={}", requestId, traceId, uri, rr.getShardIdsStr(), searchRequest.source());
        }

        searchRequest.preference("_shards:" + rr.getShardIdsStr());

        return channel -> client.search(searchRequest, new RestStatusToXContentListener<>(channel));
    }
}

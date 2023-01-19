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

import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.spatial.router.Router;
import org.elasticsearch.plugin.spatial.router.RouterResult;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;
import org.elasticsearch.rest.action.search.RestMultiSearchAction;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

public class RestSpatialMultiSearchAction extends BaseRestHandler {
    private final boolean allowExplicitIndex;
    private Router router;

    public RestSpatialMultiSearchAction(Settings settings, RestController controller, Router router) {
        controller.registerHandler(GET, "/_spatial_msearch", this);
        controller.registerHandler(POST, "/_spatial_msearch", this);
        controller.registerHandler(GET, "/{index}/_spatial_msearch", this);
        controller.registerHandler(POST, "/{index}/_spatial_msearch", this);
        controller.registerHandler(GET, "/{index}/{type}/_spatial_msearch", this);
        controller.registerHandler(POST, "/{index}/{type}/_spatial_msearch", this);

        this.allowExplicitIndex = MULTI_ALLOW_EXPLICIT_INDEX.get(settings);
        this.router = router;
    }

    @Override
    public String getName() {
        return "spatial_msearch_action";
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        MultiSearchRequest multiSearchRequest = RestMultiSearchAction.parseRequest(request, allowExplicitIndex);

        try {
            for (SearchRequest searchRequest : multiSearchRequest.requests()) {
                RouterResult rr = router.doQueryRequest(searchRequest);
                searchRequest.preference("_shards:" + rr.getShardIdsStr());
            }
        } catch (Throwable t) {
            throw new IOException("spatial multi search error", t);
        }

        return channel -> client.multiSearch(multiSearchRequest, new RestToXContentListener<>(channel));
    }

    @Override
    public boolean supportsContentStream() {
        return true;
    }
}

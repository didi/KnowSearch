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

package org.elasticsearch.rest.action.admin.indices;


import org.apache.logging.log4j.LogManager;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.HEAD;

/**
 * The REST handler for get index and head index APIs.
 */
public class RestGetIndicesAction extends BaseRestHandler {

    private static final DeprecationLogger deprecationLogger = new DeprecationLogger(LogManager.getLogger(RestGetIndicesAction.class));
    public static final String TYPES_DEPRECATION_MESSAGE = "[types removal] Using `include_type_name` in get indices requests"
            + " is deprecated. The parameter will be removed in the next major version.";

    private static final Set<String> allowedResponseParameters = Collections
            .unmodifiableSet(Stream.concat(Collections.singleton(INCLUDE_TYPE_NAME_PARAMETER).stream(), Settings.FORMAT_PARAMS.stream())
                    .collect(Collectors.toSet()));

    public RestGetIndicesAction(final RestController controller) {
        controller.registerHandler(GET, "/{index}", this);
        controller.registerHandler(HEAD, "/{index}", this);
    }

    @Override
    public String getName() {
        return "get_indices_action";
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        String[] indices = Strings.splitStringByCommaToArray(request.param("index"));
        // starting with 7.0 we don't include types by default in the response to GET requests
        if (request.hasParam(INCLUDE_TYPE_NAME_PARAMETER) && request.method().equals(GET)) {
            deprecationLogger.deprecatedAndMaybeLog("get_indices_with_types", TYPES_DEPRECATION_MESSAGE);
        }
        final GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(indices);
        getIndexRequest.indicesOptions(IndicesOptions.fromRequest(request, getIndexRequest.indicesOptions()));
        getIndexRequest.local(request.paramAsBoolean("local", getIndexRequest.local()));
        getIndexRequest.masterNodeTimeout(request.paramAsTime("master_timeout", getIndexRequest.masterNodeTimeout()));
        getIndexRequest.humanReadable(request.paramAsBoolean("human", false));
        getIndexRequest.includeDefaults(request.paramAsBoolean("include_defaults", false));
        return channel -> client.admin().indices().getIndex(getIndexRequest, new RestToXContentListener<>(channel));
    }

    /**
     * Parameters used for controlling the response and thus might not be consumed during
     * preparation of the request execution in {@link BaseRestHandler#prepareRequest(RestRequest, NodeClient)}.
     */
    @Override
    protected Set<String> responseParams() {
        return allowedResponseParameters;
    }
}

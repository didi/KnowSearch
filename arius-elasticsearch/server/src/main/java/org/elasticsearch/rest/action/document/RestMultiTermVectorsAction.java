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

package org.elasticsearch.rest.action.document;

import org.apache.logging.log4j.LogManager;
import org.elasticsearch.action.termvectors.MultiTermVectorsRequest;
import org.elasticsearch.action.termvectors.TermVectorsRequest;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

public class RestMultiTermVectorsAction extends BaseRestHandler {
    private static final DeprecationLogger deprecationLogger = new DeprecationLogger(
        LogManager.getLogger(RestTermVectorsAction.class));
    static final String TYPES_DEPRECATION_MESSAGE = "[types removal] " +
        "Specifying types in multi term vector requests is deprecated.";

    public RestMultiTermVectorsAction(RestController controller) {
        controller.registerHandler(GET, "/_mtermvectors", this);
        controller.registerHandler(POST, "/_mtermvectors", this);
        controller.registerHandler(GET, "/{index}/_mtermvectors", this);
        controller.registerHandler(POST, "/{index}/_mtermvectors", this);

        // Deprecated typed endpoints.
        controller.registerHandler(GET, "/{index}/{type}/_mtermvectors", this);
        controller.registerHandler(POST, "/{index}/{type}/_mtermvectors", this);
    }

    @Override
    public String getName() {
        return "document_multi_term_vectors_action";
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        MultiTermVectorsRequest multiTermVectorsRequest = new MultiTermVectorsRequest();
        TermVectorsRequest template = new TermVectorsRequest()
            .index(request.param("index"));

        if (request.hasParam("type")) {
            deprecationLogger.deprecatedAndMaybeLog("mtermvectors_with_types", TYPES_DEPRECATION_MESSAGE);
            template.type(request.param("type"));
        } else {
            template.type(MapperService.SINGLE_MAPPING_NAME);
        }

        RestTermVectorsAction.readURIParameters(template, request);
        multiTermVectorsRequest.ids(Strings.commaDelimitedListToStringArray(request.param("ids")));
        request.withContentOrSourceParamParserOrNull(p -> multiTermVectorsRequest.add(template, p));

        return channel -> client.multiTermVectors(multiTermVectorsRequest, new RestToXContentListener<>(channel));
    }

}

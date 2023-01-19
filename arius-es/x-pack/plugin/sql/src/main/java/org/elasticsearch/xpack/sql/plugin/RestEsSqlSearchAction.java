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

package org.elasticsearch.xpack.sql.plugin;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestStatusToXContentListener;
import org.elasticsearch.xpack.sql.action.SqlTranslateAction;
import org.elasticsearch.xpack.sql.action.SqlTranslateRequest;
import org.elasticsearch.xpack.sql.action.SqlTranslateResponse;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

public class RestEsSqlSearchAction extends BaseRestHandler {
    public RestEsSqlSearchAction(RestController controller) {
        controller.registerHandler(GET, "/_es_sql", this);
        controller.registerHandler(POST, "/_es_sql", this);
    }

    @Override
    public String getName() {
        return "es_sql_action";
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        SqlTranslateResponse response = convert2Dsl(request, client);

        // 构建searchResuest
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(response.source());
        searchRequest.indices(response.index());
        return channel -> client.search(searchRequest, new RestStatusToXContentListener<>(channel));
    }

    private SqlTranslateResponse convert2Dsl(RestRequest request, NodeClient client) throws IOException {
        SqlTranslateRequest sqlRequest;
        try (XContentParser parser = request.contentOrSourceParamParser()) {
            sqlRequest = SqlTranslateRequest.fromXContent(parser);
        }

        PlainActionFuture<SqlTranslateResponse> actionFuture = PlainActionFuture.newFuture();
        client.executeLocally(SqlTranslateAction.INSTANCE, sqlRequest, actionFuture);
        return actionFuture.actionGet();
    }
}

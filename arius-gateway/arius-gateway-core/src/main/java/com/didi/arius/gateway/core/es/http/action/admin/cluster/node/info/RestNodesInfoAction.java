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

package com.didi.arius.gateway.core.es.http.action.admin.cluster.node.info;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.RestActionListenerImpl;
import com.didi.arius.gateway.core.es.http.StatAction;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Component;


/**
 *
 */
@Component("restNodesInfoAction")
public class RestNodesInfoAction extends StatAction {

	@Override
	public String name() {
		return "restNodesInfo";
	}

	private BytesRestResponse bytesRestResponse = null;
	
	private long cacheTime = 0;
	
	private static final long CACHE_MILLIS = 60000;


    @Override
	protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client)
			throws Exception {
        // cache
        if (request.rawPath().equals("/_nodes")) {
            if (queryContext.isFromKibana()) {
                client = esRestClientService.getAdminClient(actionName);
            }

        	long now = System.currentTimeMillis();
        	if (now - cacheTime > CACHE_MILLIS) {
                directRequest(client, queryContext, new RestActionListenerImpl<DirectResponse>(queryContext) {
                    @Override
                    public void onResponse(DirectResponse response) {
                        bytesRestResponse = new BytesRestResponse(RestStatus.OK, XContentType.JSON.restContentType(), response.getResponseContent());
                        super.onResponse(response);
                        cacheTime = now;
                    }
                });
        	} else {
                sendDirectResponse(queryContext, bytesRestResponse);
        	}
        } else if (request.rawPath().equals("/_nodesclean")) {
            sendDirectResponse(queryContext, bytesRestResponse);

            bytesRestResponse = null;
        	cacheTime = 0;
        } else {
        	directRequest(client, queryContext);
        }
    }
}

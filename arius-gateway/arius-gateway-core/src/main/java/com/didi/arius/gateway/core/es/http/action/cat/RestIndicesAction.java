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

package com.didi.arius.gateway.core.es.http.action.cat;

import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.es.http.StatAction;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Component;


@Component("restIndicesAction")
public class RestIndicesAction extends StatAction {

	public static final String NAME = "restIndices";
	
	@Override
	public String name() {
		return NAME;
	}
	
    @Override
    protected void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client)
            throws Exception {
        final String[] indices = Strings.splitStringByCommaToArray(request.param("index"));
        boolean allIndices = false;
        if (indices.length == 0) {
        	allIndices = true;
        }
        
        for (String index : indices) {
        	if (index.trim().equals("*")) {
        		allIndices = true;
        		break;
        	}
        }
        
        if (allIndices) {
            sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, "not support!"));
            return ;
        } else if (request.hasParam("h") && request.param("h").equals("i")){
            // 用户控制台直接获取索引列表
            StringBuilder builder = new StringBuilder();
            for (String index : indices) {
                builder.append(index);
                builder.append("\n");
            }

            sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, builder.toString()));
        } else {
            directRequest(client, queryContext);
        }


    }
}

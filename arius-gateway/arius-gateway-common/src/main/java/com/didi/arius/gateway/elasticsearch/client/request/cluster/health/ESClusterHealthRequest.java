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

package com.didi.arius.gateway.elasticsearch.client.request.cluster.health;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import org.elasticsearch.action.ActionRequestValidationException;

public class ESClusterHealthRequest extends ESActionRequest<ESClusterHealthRequest> {
    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    @Override
    public RestRequest toRequest() throws Exception {
        String endpoint = buildEndPoint();
        return new RestRequest("GET", endpoint, null);
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        String respStr = response.getResponseContent();

        return JSON.parseObject(respStr, ESClusterHealthResponse.class);
    }

    private String buildEndPoint() {
        return "_cluster/health";
    }
}

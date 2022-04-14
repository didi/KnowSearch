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

package com.didi.arius.gateway.elasticsearch.client.request.index.getalias;

import com.didi.arius.gateway.elasticsearch.client.request.broadcast.ESBroadcastRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import org.apache.commons.lang3.StringUtils;

public class ESIndicesGetAliasRequest extends ESBroadcastRequest<ESIndicesGetAliasRequest> {
    private String[] indices;

    public ESIndicesGetAliasRequest() {
        // pass
    }

    public ESIndicesGetAliasRequest setIndices(String... indices) {
        this.indices = indices;

        return this;
    }


    @Override
    public RestRequest toRequest() throws Exception {
        String indicesStr = null;
        if(indices!=null) {
            indicesStr = StringUtils.join(indices, ",");
        }
        if(indicesStr!=null && indicesStr.length()==0) {
            indicesStr = null;
        }

        String endPoint;
        if(indicesStr==null) {
            endPoint = "/_alias";
        } else {
            endPoint = indicesStr.trim()+"/_alias";
        }

        return new RestRequest("GET", endPoint, null);
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        return ESIndicesGetAliasResponse.getResponse(response.getResponseContent());
    }
}

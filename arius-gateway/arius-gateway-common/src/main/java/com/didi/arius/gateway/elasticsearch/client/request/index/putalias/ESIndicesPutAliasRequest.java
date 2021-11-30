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

package com.didi.arius.gateway.elasticsearch.client.request.index.putalias;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.putalias.ESIndicesPutAliasResponse;
import org.elasticsearch.action.ActionRequestValidationException;

import java.util.ArrayList;
import java.util.List;

public class ESIndicesPutAliasRequest extends ESActionRequest<ESIndicesPutAliasRequest> {
    private List<PutAliasNode> putAliasNodeList = new ArrayList<>();


    public ESIndicesPutAliasRequest addPutAliasNode(PutAliasNode node) {
        putAliasNodeList.add(node);
        return this;
    }

    public ESIndicesPutAliasRequest addPutAliasNodes(List<PutAliasNode> nodes) {
        putAliasNodeList.addAll(nodes);
        return this;
    }


    @Override
    public RestRequest toRequest() throws Exception {
        RestRequest rr = new RestRequest("POST", "/_aliases", null);
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for(PutAliasNode node : putAliasNodeList) {
            array.add(node.toJson());
        }

        obj.put("actions", array);
        rr.setBody(obj.toJSONString());

        return rr;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        String respStr = response.getResponseContent();
        return JSON.parseObject(respStr, ESIndicesPutAliasResponse.class);
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }
}

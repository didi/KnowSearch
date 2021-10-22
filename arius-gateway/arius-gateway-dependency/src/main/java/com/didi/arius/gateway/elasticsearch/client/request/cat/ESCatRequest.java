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

package com.didi.arius.gateway.elasticsearch.client.request.cat;

import com.didi.arius.gateway.elasticsearch.client.response.cat.ESCatResponse;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import org.elasticsearch.action.ActionRequestValidationException;

import java.util.HashMap;
import java.util.Map;

public class ESCatRequest extends ESActionRequest<ESCatRequest> {
    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    private String uri;
    private Map<String, String> param = new HashMap<String, String>();
    private Class clazz;

    public ESCatRequest setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public ESCatRequest addParam(String key, String value) {
        param.put(key, value);
        return this;
    }

    public ESCatRequest removeParam(String key) {
        param.remove(key);
        return this;
    }

    public ESCatRequest setClazz(Class clazz) {
        this.clazz = clazz;
        return this;
    }


    @Override
    public RestRequest toRequest() throws Exception {
        String endpoint = buildEndPoint();
        RestRequest req = new RestRequest("GET", endpoint, null);

        for(String key : param.keySet()) {
            req.addParam(key, param.get(key));
        }

        return req;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        String respStr = response.getResponseContent();
        return new ESCatResponse(respStr, clazz);
    }

    private String buildEndPoint() {
        if(uri.startsWith("/")) {
            uri = uri.substring(1);
        }

        return "_cat/" + uri;
    }
}

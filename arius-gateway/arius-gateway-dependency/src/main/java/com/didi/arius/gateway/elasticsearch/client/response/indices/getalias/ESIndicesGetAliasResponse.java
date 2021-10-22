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

package com.didi.arius.gateway.elasticsearch.client.response.indices.getalias;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;

import java.util.HashMap;
import java.util.Map;

public class ESIndicesGetAliasResponse extends ESActionResponse {
    private Map<String, AliasIndexNode> m;

    public Map<String, AliasIndexNode> getM() {
        return m;
    }

    public void setM(Map<String, AliasIndexNode> m) {
        this.m = m;
    }

    @Override
    public String toString() {
        return toJson().toJSONString();
    }

    public JSONObject toJson()  {
        return (JSONObject) JSONObject.toJSON(this);
    }


    public static ESIndicesGetAliasResponse getResponse(String str) {
        Map<String, AliasIndexNode> m = new HashMap<>();

        JSONObject obj = JSON.parseObject(str);
        for(String key : obj.keySet()) {
            m.put(key, JSON.parseObject(obj.get(key).toString(), AliasIndexNode.class));
        }

        ESIndicesGetAliasResponse response = new ESIndicesGetAliasResponse();
        response.setM(m);
        return response;
    }
}

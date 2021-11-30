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

package com.didi.arius.gateway.elasticsearch.client.response.indices.stats;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.response.Shards;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class ESIndicesStatsResponse extends ESActionResponse {

    @JSONField(name = "_shards")
    private Shards shards;

    @JSONField(name = "_all")
    private IndexNodes all;

    @JSONField(name = "indices")
    private Map<String, IndexNodes> indices = new HashMap<>();

    public ESIndicesStatsResponse() {
        // pass
    }

    public Shards getShards() {
        return shards;
    }

    public void setShards(Shards shards) {
        this.shards = shards;
    }

    public IndexNodes getAll() {
        return all;
    }

    public void setAll(IndexNodes all) {
        this.all = all;
    }

    public JSONObject getIndices() {
        if (indices == null || indices.size() == 0) {
            return null;
        }

        JSONObject ret = new JSONObject();
        for (Map.Entry<String,IndexNodes> entry : indices.entrySet()) {
            ret.put(entry.getKey(), JSON.toJSON(entry.getValue()));
        }
        return ret;
    }

    public void setIndices(JSONObject root) {
        if (root == null) {
            return;
        }

        for (String index : root.keySet()) {
            String str = root.getString(index);
            indices.put(index, JSON.parseObject(str, IndexNodes.class));
        }
    }


    @JSONField(serialize = false)
    public Map<String, IndexNodes> getIndicesMap() {
        return indices;
    }

    @JSONField(serialize = false)
    public void setIndicesMap(Map<String, IndexNodes> indicesMap) {
        this.indices = indicesMap;
    }


    @Override
    public String toString() {
        return toJson().toJSONString();
    }

    public JSONObject toJson() {
        return (JSONObject) JSON.toJSON(this);
    }
}

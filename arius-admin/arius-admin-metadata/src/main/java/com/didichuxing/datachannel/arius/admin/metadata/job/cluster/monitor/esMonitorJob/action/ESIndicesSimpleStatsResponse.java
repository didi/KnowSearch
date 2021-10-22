package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.action;

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.elasticsearch.client.gateway.search.response.Shards;
import com.didichuxing.datachannel.arius.elasticsearch.client.model.ESActionResponse;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class ESIndicesSimpleStatsResponse extends ESActionResponse {

    @JSONField(name = "_shards")
    private Shards shards;

    @JSONField(name = "_all")
    private SimpleIndexNodes all;

    @JSONField(name = "indices")
    private Map<String, SimpleIndexNodes> indices = new HashMap<>();


    public Shards getShards() {
        return shards;
    }

    public void setShards(Shards shards) {
        this.shards = shards;
    }

    public SimpleIndexNodes getAll() {
        return all;
    }

    public void setAll(SimpleIndexNodes all) {
        this.all = all;
    }

    public JSONObject getIndices() {
        if (indices == null || indices.size() == 0) {
            return null;
        }

        JSONObject ret = new JSONObject();
        for (String index : indices.keySet()) {
            ret.put(index, JSONObject.toJSON(indices.get(index)));
        }
        return ret;
    }

    public void setIndices(JSONObject root) {
        if (root == null) {
            return;
        }

        for (String index : root.keySet()) {
            String str = root.getString(index);
            indices.put(index, JSON.parseObject(str, SimpleIndexNodes.class));
        }
    }


    @JSONField(serialize = false)
    public Map<String, SimpleIndexNodes> getIndicesMap() {
        return indices;
    }

    @JSONField(serialize = false)
    public void setIndicesMap(Map<String, SimpleIndexNodes> indicesMap) {
        this.indices = indicesMap;
    }


    @Override
    public String toString() {
        return toJson().toJSONString();
    }

    public JSONObject toJson() {
        return (JSONObject) JSONObject.toJSON(this);
    }
}

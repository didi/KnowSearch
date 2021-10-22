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

package com.didi.arius.gateway.elasticsearch.client.response.indices.searchshards;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.searchshards.item.ESNode;
import com.didi.arius.gateway.elasticsearch.client.response.indices.searchshards.item.ESShard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class ESIndicesSearchShardsResponse extends ESActionResponse {
    @JSONField(name = "nodes")
    private Map<String, ESNode> nodes;

    @JSONField(name = "shards")
    private List<List<ESShard>> shards;


    public Map<String, ESNode> getNodes() {
        return nodes;
    }

    public void setNodes(Map<String, ESNode> nodes) {
        this.nodes = nodes;
    }

    public List<List<ESShard>> getShards() {
        return shards;
    }

    public void setShards(List<List<ESShard>> shards) {
        this.shards = shards;
    }



    @JSONField(serialize=false)
    public Map<String, List<List<ESShard>>> getIndexMap() {
        Map<String, List<List<ESShard>>> ret = new HashMap<>();

        for(List<ESShard> les : shards) {
            if(les==null || les.size()==0 | les.get(0)==null) {
                continue;
            }

            String index = les.get(0).getIndex();
            if(!ret.containsKey(index)) {
                ret.put(index, new ArrayList<>());
            }

            ret.get(index).add(les);
        }

        return ret;
    }



    @Override
    public String toString() {
        return toJson().toJSONString();
    }

    public JSONObject toJson()  {
        return (JSONObject) JSONObject.toJSON(this);
    }
}

package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.index;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.didiglobal.knowframework.elasticsearch.client.gateway.search.response.Shards;
import com.didiglobal.knowframework.elasticsearch.client.model.ESActionResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.stats.IndexNodes;

import java.util.HashMap;
import java.util.Map;

public class ESIndexStatsResponse extends ESActionResponse {

    @JSONField(name = "_shards")
    private Shards                  shards;

    @JSONField(name = "_all")
    private IndexNodes              all;

    @JSONField(name = "indices")
    private Map<String, IndexNodes> indices = new HashMap<>();

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
        for (Map.Entry<String, IndexNodes> entry : indices.entrySet()) {
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

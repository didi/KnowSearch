package com.didichuxing.arius.admin.extend.fastindex.bean.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class IndexShardsVersion {
    private static final String INDEX_SHARDS_VERSION = "shardVersions";
    private List<ShardVersion> shardVersions;

    public IndexShardsVersion(List<ShardVersion> shardVersions) {
        this.shardVersions = shardVersions;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(shardVersions);

        jsonObject.put(INDEX_SHARDS_VERSION, jsonArray);
        return jsonObject;
    }
}
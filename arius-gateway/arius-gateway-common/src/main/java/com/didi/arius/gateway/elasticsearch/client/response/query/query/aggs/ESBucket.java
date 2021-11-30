package com.didi.arius.gateway.elasticsearch.client.response.query.query.aggs;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ESBucket {
    private Map<String, Object> unusedMap = new HashMap<>();
    private Map<String, ESAggr> aggrMap = new HashMap<>();


    public ESBucket() {}
    public ESBucket(JSONObject root) {
        if(root==null) {
            return;
        }

        for(Map.Entry<String,Object> entry : root.entrySet()) {
            String key = entry.getKey();
            Object obj = entry.getValue();

            if(obj instanceof JSONObject) {
                aggrMap.put(key, new ESAggr((JSONObject) obj));
            } else {
                unusedMap.put(key, obj);
            }
        }

    }

    public Map<String, Object> getUnusedMap() {
        return unusedMap;
    }

    public void setUnusedMap(Map<String, Object> unusedMap) {
        this.unusedMap = unusedMap;
    }

    public Map<String, ESAggr> getAggrMap() {
        return aggrMap;
    }

    public void setAggrMap(Map<String, ESAggr> aggrMap) {
        this.aggrMap = aggrMap;
    }

    @Override
    public String toString() {
        return toJson().toJSONString();
    }

    public JSONObject toJson() {
        JSONObject root = new JSONObject();


        for(Map.Entry<String,Object> entry : unusedMap.entrySet()) {
            root.put(entry.getKey(), entry.getValue());
        }


        for(Map.Entry<String, ESAggr> entry : aggrMap.entrySet()) {
            root.put(entry.getKey(), entry.getValue().toJson());
        }

        return root;
    }
}

package com.didi.arius.gateway.elasticsearch.client.response.query.query.aggs;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ESAggrMap {
    private Map<String, ESAggr> esAggrMap = new HashMap<>();

    public ESAggrMap(JSONObject root) {
       if(root==null) {
           return;
       }

        for(Map.Entry<String,Object> entry : root.entrySet()) {
            String key = entry.getKey();
           esAggrMap.put(key, new ESAggr((JSONObject) root.get(key)));
        }
    }


    public Map<String, ESAggr> getEsAggrMap() {
        return esAggrMap;
    }

    public void setEsAggrMap(Map<String, ESAggr> esAggrMap) {
        this.esAggrMap = esAggrMap;
    }

    @Override
    public String toString() {
        return toJson().toJSONString();
    }
    public JSONObject toJson() {
        JSONObject root = new JSONObject();

        for(Map.Entry<String, ESAggr> entry : esAggrMap.entrySet()) {
            root.put(entry.getKey(), entry.getValue().toJson());
        }

        return root;
    }

}

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

        for(String key : root.keySet()) {

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

        for(String key : esAggrMap.keySet()) {
            root.put(key, esAggrMap.get(key).toJson());
        }

        return root;
    }

}

package com.didi.arius.gateway.elasticsearch.client.response.query.query.hits;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESHits {
    private Map<String, Object> unusedMap = new HashMap<>();

    private static final String HITS_STR = "hits";
    private List<ESHit> hits;


    public ESHits() { }
    public ESHits(JSONObject root, Class clazz) {
        if (root == null) {
            return;
        }

        for (Map.Entry<String,Object> entry : root.entrySet()) {
            String key = entry.getKey();
            if (HITS_STR.equalsIgnoreCase(key)) {
                JSONArray array = (JSONArray) root.get(HITS_STR);
                hits= new ArrayList<>();

                for(Object obj : array) {
                    hits.add(new ESHit((JSONObject) obj, clazz));
                }

            } else {
                unusedMap.put(key, root.get(key));
            }
        }
    }


    public Map<String, Object> getUnusedMap() {
        return unusedMap;
    }

    public void setUnusedMap(Map<String, Object> unusedMap) {
        this.unusedMap = unusedMap;
    }

    public List<ESHit> getHits() {
        return hits;
    }

    public void setHits(List<ESHit> hits) {
        this.hits = hits;
    }

    public boolean isEmpty() {
        boolean res = false;
        if (hits == null || hits.isEmpty()) {
            res = true;
        }
        return res;
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


        if(hits!=null) {
            JSONArray array = new JSONArray();
            for(ESHit hit : hits) {
                array.add(hit.toJson());
            }
            root.put(HITS_STR, array);
        }

        return root;
    }
}


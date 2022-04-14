package com.didi.arius.gateway.elasticsearch.client.response.query.query.aggs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESAggr {
    private Map<String, Object> unusedMap = new HashMap<>();


    private static final String BUCKETS_STR = "buckets";
    private List<ESBucket> bucketList = null;

    public ESAggr(JSONObject root) {
        if(root==null) {
           return;
        }


        for(Map.Entry<String,Object> entry : root.entrySet()) {
            String key = entry.getKey();
            if(BUCKETS_STR.equalsIgnoreCase(key)) {
                bucketList = new ArrayList<>();

                JSONArray array = root.getJSONArray(key);
                for(Object obj : array) {
                    JSONObject jsonObject = (JSONObject) obj;

                    bucketList.add(new ESBucket(jsonObject));
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

    public List<ESBucket> getBucketList() {
        return bucketList;
    }

    public void setBucketList(List<ESBucket> bucketList) {
        this.bucketList = bucketList;
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


        if(bucketList!=null) {
            JSONArray array = new JSONArray();
            for(ESBucket bucket : bucketList) {
                array.add(bucket.toJson());
            }

            root.put(BUCKETS_STR, array);
        }

        return root;
    }


}

package com.didi.arius.gateway.elasticsearch.client.response.query.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import com.didi.arius.gateway.elasticsearch.client.response.query.query.hits.ESHit;
import com.didi.arius.gateway.elasticsearch.client.response.query.query.hits.ESHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ESQueryResponse extends ESActionResponse {
    private static final String HITS_STR = "hits";
    private static final String AGGREGATIONS_STR = "aggregations";

    private Map<String, Object> unusedMap = new HashMap<>();

    private ESHits hits;
    private ESAggrMap aggs;


    public ESQueryResponse() {}
    public ESQueryResponse(JSONObject root, Class clazz) {
        if(root==null) {
            return;
        }

        for(Map.Entry<String,Object> entry : root.entrySet()) {
            String key = entry.getKey();
            if(HITS_STR.equalsIgnoreCase(key)) {
                hits = new ESHits((JSONObject) root.get(key), clazz);

            } else if(AGGREGATIONS_STR.equalsIgnoreCase(key)) {
                aggs = new ESAggrMap((JSONObject) root.get(key));

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


    public ESHits getHits() {
        return hits;
    }

    public void setHits(ESHits hits) {
        this.hits = hits;
    }

    public ESAggrMap getAggs() {
        return aggs;
    }

    public void setAggs(ESAggrMap aggs) {
        this.aggs = aggs;
    }


    public boolean isEmptyHits() {
        boolean res = false;
        if (this.getHits() == null || this.getHits().isEmpty()) {
            res = true;
        }

        return res;
    }

    public Object getFirstHit() {
        if (isEmptyHits()) {
            return null;
        }
        return hits.getHits().get(0).getSource();
    }

    public List<Object> getSourceList() {
        if(isEmptyHits()) {
            return Collections.emptyList();
        }

        List<Object> ret = new ArrayList<>();
        for(ESHit hit : hits.getHits()) {
            ret.add(hit.getSource());
        }
        return ret;
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
            root.put(HITS_STR, hits.toJson());
        }

        if(aggs!=null) {
            root.put(AGGREGATIONS_STR, aggs.toJson());
        }

        return root;
    }


    public static ESQueryResponse parserResponse(String str, Class clazz) {
        JSONObject root = JSON.parseObject(str);
        return new ESQueryResponse(root, clazz);
    }

}

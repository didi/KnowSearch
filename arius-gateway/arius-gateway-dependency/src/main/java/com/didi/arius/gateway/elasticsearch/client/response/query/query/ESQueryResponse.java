package com.didi.arius.gateway.elasticsearch.client.response.query.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import com.didi.arius.gateway.elasticsearch.client.response.query.query.hits.ESHit;
import com.didi.arius.gateway.elasticsearch.client.response.query.query.hits.ESHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESQueryResponse extends ESActionResponse {
    private static final Logger LOGGER = LoggerFactory.getLogger(ESQueryResponse.class);
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

        for(String key : root.keySet()) {
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
        if (this.getHits() == null || this.getHits().isEmpty()) {
            return true;
        }

        return false;
    }

    public Object getFirstHit() {
        if (isEmptyHits()) {
            return null;
        }
        return hits.getHits().get(0).getSource();
    }

    public List<Object> getSourceList() {
        if(isEmptyHits()) {
            return null;
        }

        List ret = new ArrayList<>();
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

        for(String key : unusedMap.keySet()) {
            root.put(key, unusedMap.get(key));
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

    public static void main(String[] args) {


    }
}

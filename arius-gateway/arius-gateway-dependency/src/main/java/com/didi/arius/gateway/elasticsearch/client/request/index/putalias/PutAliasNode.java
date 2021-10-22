package com.didi.arius.gateway.elasticsearch.client.request.index.putalias;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PutAliasNode {
    private static final String INDEX_STR = "index";
    private static final String ALIAS_STR = "alias";

    private PutAliasType type;
    private String index;
    private String alias;
    private Map<String, Object> other = new HashMap<>();

    public PutAliasType getType() {
        return type;
    }

    public void setType(PutAliasType type) {
        this.type = type;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void putOtherConfig(String key, JSONObject obj) {
        other.put(key, obj);
    }

    public JSONObject toJson() throws Exception {
        if(type==null || index==null || alias==null) {
            throw new Exception("type, index, alias is null");
        }

        JSONObject obj = new JSONObject();
        obj.put(INDEX_STR, index);
        obj.put(ALIAS_STR, alias);
        for(String key : other.keySet()) {
            obj.put(key, other.get(key));
        }

        JSONObject ret = new JSONObject();
        ret.put(type.getStr(), obj);
        return ret;
    }

}

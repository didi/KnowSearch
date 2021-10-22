package com.didi.arius.gateway.elasticsearch.client.response.indices.getalias;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;

public class AliasIndexNode {
    @JSONField(name = "aliases")
    private Map<String, JSONObject> aliases;

    public Map<String, JSONObject> getAliases() {
        return aliases;
    }

    public void setAliases(Map<String, JSONObject> aliases) {
        this.aliases = aliases;
    }
}

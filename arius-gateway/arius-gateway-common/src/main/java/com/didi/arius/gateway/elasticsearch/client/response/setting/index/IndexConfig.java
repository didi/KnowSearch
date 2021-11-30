package com.didi.arius.gateway.elasticsearch.client.response.setting.index;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.response.setting.common.MappingConfig;
import com.didi.arius.gateway.elasticsearch.client.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public class IndexConfig {
    private static final String ALIASES_STR = "aliases";
    private static final String MAPPINGS_STR = "mappings";
    private static final String SETTINGS_STR = "settings";

    private JSONObject aliases;
    private MappingConfig mappings = null;
    private JSONObject settings;
    private Map<String, Object> notUsedMap = new HashMap<>();


    public IndexConfig() {}

    public IndexConfig(JSONObject root) throws Exception {
        if(root==null) {
            throw new Exception("root is null");
        }

        for(Map.Entry<String,Object> entry : root.entrySet()) {
            String key = entry.getKey();
            if(key.equalsIgnoreCase(SETTINGS_STR)) {
                settings = root.getJSONObject(key);

            } else if(key.equalsIgnoreCase(ALIASES_STR)) {
                aliases = root.getJSONObject(key);

            } else if(key.equalsIgnoreCase(MAPPINGS_STR)) {
                mappings = new MappingConfig(root.getJSONObject(key));

            } else {
                notUsedMap.put(key, root.get(key));
            }
        }
    }


    public JSONObject getAliases() {
        return aliases;
    }

    public void setAliases(JSONObject aliases) {
        this.aliases = aliases;
    }

    public MappingConfig getMappings() {
        return mappings;
    }

    public void setMappings(MappingConfig mappings) {
        this.mappings = mappings;
    }

    public Map<String, String> getSettings() {
        return JsonUtils.flat(settings);
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = JsonUtils.reFlat(settings);
    }

    public Object getOther(String key) {
        return notUsedMap.get(key);
    }

    public void setOther(String key, Object value) {
        this.notUsedMap.put(key, value);
    }


    public JSONObject toJson() {
        JSONObject root = new JSONObject();

        if(settings!=null && settings.size()>0) {
            root.put(SETTINGS_STR, settings);
        }

        if(aliases!=null && aliases.size()>0) {
            root.put(ALIASES_STR, aliases);
        }

        if(mappings!=null && !mappings.isEmpty()) {
            root.put(MAPPINGS_STR, mappings.toJson());
        }

        for(Map.Entry<String,Object> entry : notUsedMap.entrySet()) {
            root.put(entry.getKey(), entry.getValue());
        }

        return root;
    }
}

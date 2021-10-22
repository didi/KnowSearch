package com.didi.arius.gateway.elasticsearch.client.response.setting.index;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.response.setting.common.MappingConfig;

import java.util.HashMap;
import java.util.Map;

public class MultiIndexsConfig {
    private Map<String, IndexConfig> indexConfigMap = new HashMap<>();

    public MultiIndexsConfig(JSONObject root) throws Exception {
        if(root==null) {
            throw new Exception("root is null");
        }

        for(String indexName: root.keySet()) {
            indexConfigMap.put(indexName, new IndexConfig(root.getJSONObject(indexName)));
        }
    }

    public JSONObject toJson() {
        JSONObject root = new JSONObject();

        for(String indexName: indexConfigMap.keySet()) {
            root.put(indexName, indexConfigMap.get(indexName).toJson());
        }

        return root;
    }

    public Map<String, IndexConfig> getIndexConfigMap() {
        return indexConfigMap;
    }

    public IndexConfig getIndexConfig(String index) {
        return indexConfigMap.get(index);
    }

    @JSONField(serialize=false)
    public MappingConfig getNewestMappings() {
        String newestName= "";
        for(String indexName : indexConfigMap.keySet()) {
            if(newestName.compareTo(indexName)<0) {
                newestName = indexName;
            }
        }

        return indexConfigMap.get(newestName).getMappings();
    }

    @JSONField(serialize=false)
    public boolean isEmpty() {
        return indexConfigMap.isEmpty();
    }
}

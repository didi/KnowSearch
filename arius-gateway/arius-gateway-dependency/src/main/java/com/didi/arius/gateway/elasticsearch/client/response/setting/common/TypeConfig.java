package com.didi.arius.gateway.elasticsearch.client.response.setting.common;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.type.ESVersion;

import java.util.HashMap;
import java.util.Map;

public class TypeConfig {
    private Map<String, Object> notUsedMap = new HashMap<>();

    private TypeProperties properties = null;


    public TypeConfig() {}
    public TypeConfig(JSONObject root) throws Exception {
        if (root == null) {
            throw new Exception("root is null");
        }

        for (String key : root.keySet()) {
            if (key.equalsIgnoreCase(TypeProperties.PROPERTIES_STR)) {
                properties = new TypeProperties(root.getJSONObject(key));
            } else {
                notUsedMap.put(key, root.get(key));
            }
        }
    }

    public void addProperties(Map<String, Object> m) {
        notUsedMap.putAll(m);
    }

    public void addField(String field, TypeDefine define) {
        if(properties==null) {
            properties = new TypeProperties();
        }

        properties.addField(field, define);
    }

    public void deleteField(String fieldName) {
        if (properties != null) {
            properties.deleteField(fieldName);
        }
    }

    public boolean isFieldExists(String fieldName) {
        if (properties != null) {
            Map<String, TypeDefine> fieldNameMap = properties.getTypeDefine();
            for (String field : fieldNameMap.keySet()) {
                if (field.equals(fieldName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public JSONObject toJson() {
        JSONObject root = new JSONObject();

        for (String key : notUsedMap.keySet()) {
            root.put(key, notUsedMap.get(key));
        }

        if (properties != null) {
            root.put(TypeProperties.PROPERTIES_STR, properties.toJson());
        }

        return root;
    }

    public JSONObject toJson(ESVersion version) {
        JSONObject root = new JSONObject();

        for (String key : notUsedMap.keySet()) {
            root.put(key, notUsedMap.get(key));
        }

        if (properties != null) {
            root.put(TypeProperties.PROPERTIES_STR, properties.toJson(version));
        }

        return root;
    }


    public Map<String, TypeDefine> getTypeDefine() {
        if (properties != null) {
            return properties.getTypeDefine();
        } else {
            return new HashMap<>();
        }
    }

    public Map<String, Object> getNotUsedMap() {
        return notUsedMap;
    }

    public void setNotUsedMap(Map<String, Object> notUsedMap) {
        this.notUsedMap = notUsedMap;
    }

    public TypeProperties getProperties() {
        return properties;
    }

    public void setProperties(TypeProperties properties) {
        this.properties = properties;
    }
}

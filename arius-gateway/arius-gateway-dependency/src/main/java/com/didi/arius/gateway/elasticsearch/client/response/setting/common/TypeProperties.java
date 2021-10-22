package com.didi.arius.gateway.elasticsearch.client.response.setting.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.type.ESVersion;

import java.util.HashMap;
import java.util.Map;

public class TypeProperties {
    public static final String PROPERTIES_STR = "properties";

    private Map<String, TypeDefine> jsonMap = new HashMap<>();

    private Map<String, TypeDefine> propertyTypeMap = new HashMap<>();
    private Map<String, TypeProperties> propertyMap = new HashMap<>();

    public TypeProperties() {}
    public TypeProperties(JSONObject root) {
        for(String key : root.keySet()) {
            JSONObject obj = root.getJSONObject(key);

            if(obj.containsKey(PROPERTIES_STR)) {
                propertyMap.put(key, new TypeProperties(obj.getJSONObject(PROPERTIES_STR)));

                // 处理nest本省有类型的情况
                JSONObject o = JSON.parseObject(obj.toJSONString());
                o.remove(PROPERTIES_STR);
                if(o.size()>0) {
                    propertyTypeMap.put(key, new TypeDefine(o));
                }

            } else {
                jsonMap.put(key, new TypeDefine(obj));
            }
        }
    }

    public JSONObject toJson() {
        JSONObject root = new JSONObject();

        for(String key : jsonMap.keySet()) {
            root.put(key, jsonMap.get(key).toJson());
        }

        for(String key : propertyMap.keySet()) {
            JSONObject obj = new JSONObject();
            obj.put(PROPERTIES_STR, propertyMap.get(key).toJson());
            if(propertyTypeMap.containsKey(key)) {
                obj.putAll(propertyTypeMap.get(key).toJson());
            }

            root.put(key, obj);
        }

        return root;
    }

    public JSONObject toJson(ESVersion version) {
        JSONObject root = new JSONObject();

        for (String key : jsonMap.keySet()) {
            root.put(key, jsonMap.get(key).toJson(version));
        }

        for (String key : propertyMap.keySet()) {
            JSONObject obj = new JSONObject();
            obj.put(PROPERTIES_STR, propertyMap.get(key).toJson(version));
            if (propertyTypeMap.containsKey(key)) {
                obj.putAll(propertyTypeMap.get(key).toJson(version));
            }

            root.put(key, obj);
        }

        return root;
    }


    public Map<String, TypeDefine>  getTypeDefine() {
        Map<String, TypeDefine> ret = new HashMap<>();

        for(String key : jsonMap.keySet()) {
            ret.put(key, jsonMap.get(key));
        }

        for(String key : propertyMap.keySet()) {
            // 处理nest的情况
            ret.put(key, new TypeDefine(new JSONObject()));

            Map<String, TypeDefine> tmp = propertyMap.get(key).getTypeDefine();
            for(String k : tmp.keySet()) {
                ret.put(key.trim()+"."+k.trim(), tmp.get(k));
            }
        }

        return ret;
    }


    public void addField(String field, TypeDefine define) {
        propertyMap.remove(field);
        jsonMap.put(field, define);
    }

    public void deleteField(String fieldName) {
        propertyMap.remove(fieldName);
        jsonMap.remove(fieldName);
    }

    public Map<String, TypeDefine> getJsonMap() {
        return jsonMap;
    }

    public void setJsonMap(Map<String, TypeDefine> jsonMap) {
        this.jsonMap = jsonMap;
    }

    public Map<String, TypeProperties> getPropertyMap() {
        return propertyMap;
    }

    public void setPropertyMap(Map<String, TypeProperties> propertyMap) {
        this.propertyMap = propertyMap;
    }
}

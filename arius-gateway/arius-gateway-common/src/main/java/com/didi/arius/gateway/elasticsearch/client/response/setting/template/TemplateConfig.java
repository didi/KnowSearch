package com.didi.arius.gateway.elasticsearch.client.response.setting.template;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.type.ESVersion;
import com.didi.arius.gateway.elasticsearch.client.response.setting.common.MappingConfig;
import com.didi.arius.gateway.elasticsearch.client.response.setting.common.TypeDefine;
import com.didi.arius.gateway.elasticsearch.client.utils.JsonUtils;
import com.google.common.collect.Sets;

import java.util.*;

public class TemplateConfig {
    private static final String ORDER_STR = "order";
    private static final String SETTINGS_STR = "settings";
    private static final String ALIASES_STR = "aliases";
    private static final String MAPPINGS_STR = "mappings";
    private static final String TEMPLATE_STR = "template";
    private static final String INDEX_PATTERNS_STR = "index_patterns";

    private Set<String> template = new HashSet<>();
    private Long order;
    private JSONObject setttings;
    private JSONObject aliases;
    private MappingConfig mappings = null;
    private Map<String, Object> notUsedMap = new HashMap<>();

    public TemplateConfig() {}
    public TemplateConfig(JSONObject root) throws Exception {
        if(root==null) {
            throw new Exception("root is null");
        }

        for(Map.Entry<String,Object> entry : root.entrySet()) {
            String key = entry.getKey();
            if(key.equalsIgnoreCase(TEMPLATE_STR)) {
                template.add((String) root.get(key));

            } else if(key.equalsIgnoreCase(INDEX_PATTERNS_STR)) {
                dealIndexPattern(root, key);

            } else if(key.equalsIgnoreCase(ORDER_STR)) {
                order = root.getLong(key);

            } else if(key.equalsIgnoreCase(SETTINGS_STR)) {
                setttings = root.getJSONObject(key);

            } else if(key.equalsIgnoreCase(ALIASES_STR)) {
                aliases = root.getJSONObject(key);

            } else if(key.equalsIgnoreCase(MAPPINGS_STR)) {
                mappings = new MappingConfig(root.getJSONObject(key));

            } else {
                notUsedMap.put(key, root.get(key));
            }
        }

        if(mappings==null) {
            throw new Exception("not have mapping, config:" + root.toJSONString());
        }
    }

    private void dealIndexPattern(JSONObject root, String key) {
        Object obj = root.get(key);
        if(obj instanceof JSONArray) {
            for(Object o : (JSONArray)obj) {
                template.add(o.toString());
            }
        } else {
            template.add(obj.toString());
        }
    }

    public String getTemplate() {
        for(String k : template) {
            return k;
        }

        return null;
    }

    public Set<String> getTemplates() {
        return template;
    }


    public void setTemplate(String template) {
        this.template.clear();
        this.template.add(template);
    }

    public void addTemplate(String template) {
        this.template.add(template);
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public Map<String, String> getSetttings() {
        return JsonUtils.flat(setttings);
    }

    public void setSetttings(Map<String, String> setttings) {
        this.setttings = JsonUtils.reFlat(setttings);
    }

    public JSONObject getAliases() {
        return aliases;
    }

    public void setAliases(JSONObject aliases) {
        this.aliases = aliases;
    }

    public void setMappings(MappingConfig mappings) {
        this.mappings = mappings;
    }

    public MappingConfig getMappings() {
        return mappings;
    }

    public void addOther(String key, Object value) {
        notUsedMap.put(key, value);
    }

    public void getOther(String key) {
        notUsedMap.get(key);
    }

    public JSONObject toJson(ESVersion version) {
        if(version==null) {
            version=ESVersion.ES233;
        }

        JSONObject root = new JSONObject();

        if(template!=null && !template.isEmpty()) {
            root.put(TEMPLATE_STR, genTemplateByVersion(template, version));
        }

        if(order!=null) {
            root.put(ORDER_STR, order);
        }

        if(setttings!=null) {
            root.put(SETTINGS_STR, setttings);
        }

        if(aliases!=null) {
            root.put(ALIASES_STR, aliases);
        }

        if(mappings!=null && !mappings.isEmpty()) {
            root.put(MAPPINGS_STR, mappings.toJson(version));
        }

        for(Map.Entry<String,Object> entry : notUsedMap.entrySet()) {
            root.put(entry.getKey(), entry.getValue());
        }

        return root;
    }

    private Object genTemplateByVersion(Set<String> template, ESVersion version) {
        if (version == ESVersion.ES233) {
            return getTemplate();
        }

        JSONArray array = new JSONArray();
        for (String key : template) {
            array.add(key);
        }
        return array;
    }

    public Map<String/*typeName*/, Map<String/*field*/, TypeDefine>> getTypeDefines() {
        return mappings.getTypeDefines();
    }

    public Map<String, List<TypeDefine>> getTypes() {
        Map<String, Map<String, TypeDefine>> tm = getTypeDefines();

        Map<String, List<TypeDefine>> ret = new HashMap<>();
        for(String key : tm.keySet()) {
            for(String field : tm.get(key).keySet()) {
                if(!ret.containsKey(field)) {
                    ret.put(field, new ArrayList<>());
                }

                ret.get(field).add(tm.get(key).get(field));
            }
        }

        return ret;
    }

    /**
     * 获取mapping中所有字段名 fieldName,不包含type这层，由于不同type的类型必须一致
     * @return
     */
    public Set<String> getFieldNames() {
        Set<String> fieldNameSet = Sets.newLinkedHashSet();
        Map<String/*typeName*/, Map<String/*field*/, TypeDefine>> typeFieldMap = mappings.getTypeDefines();
        for (Map.Entry<String, Map<String, TypeDefine>> entry : typeFieldMap.entrySet()) {
            for (Map.Entry<String, TypeDefine> typeDefineEntry : entry.getValue().entrySet()) {
                fieldNameSet.add(typeDefineEntry.getKey());
            }
        }

        return fieldNameSet;
    }

}

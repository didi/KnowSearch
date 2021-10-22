package com.didi.arius.gateway.elasticsearch.client.response.setting.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.type.ESVersion;
import com.didi.arius.gateway.elasticsearch.client.response.setting.common.MappingConfig;
import com.didi.arius.gateway.elasticsearch.client.response.setting.common.TypeDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MultiTemplatesConfig {
    private final Logger LOGGER = LoggerFactory.getLogger(MultiTemplatesConfig.class);

    private Map<String, TemplateConfig> templateConfigMap = new HashMap<>();

    public MultiTemplatesConfig(JSONObject root) throws Exception {
        if(root==null) {
            throw new Exception("root is null");
        }

        for(String key : root.keySet()) {
            templateConfigMap.put(key, new TemplateConfig(root.getJSONObject(key)));
        }
    }

    public JSONObject toJson(ESVersion version) {
        JSONObject root = new JSONObject();

        for(String key : templateConfigMap.keySet()) {
            root.put(key, templateConfigMap.get(key).toJson(version));
        }

        return root;
    }

    public Map<String, TemplateConfig> getTemplateConfigMap() {
        return templateConfigMap;
    }

    public TemplateConfig getSingleConfig() {
        for(String key : templateConfigMap.keySet()) {
            return templateConfigMap.get(key);
        }

        return null;
    }

    public void addTemplateConfig(String name, TemplateConfig config) {
        templateConfigMap.put(name, config);
    }

    // 检测模版和base是否一直
    private static final String BASE_STR = "base";
    public static final String TESTMAPPINGS = "testMappings";
    public Map<String, Set<String>> checkBaseConfig(String cluster, MappingConfig testMappings) throws Exception {
        TemplateConfig baseConfig = templateConfigMap.get(BASE_STR);
        if(baseConfig == null) {
            LOGGER.error("not have baesConfig, clusterName:" + cluster);
            return new HashMap<>();
        }

        MappingConfig baseMapping = baseConfig.getMappings();

        Map<String, Set<String>> ret = new HashMap<>();
        for(String template : templateConfigMap.keySet()) {
            if(template.equalsIgnoreCase(BASE_STR)) {
                continue;
            }

            MappingConfig mappings = templateConfigMap.get(template).getMappings();

            Set<String> diffField = baseMapping.diffTypeDefine(mappings);
            if(diffField!=null && diffField.size()>0) {
                ret.put(template, diffField);
            }
        }

        if(testMappings!=null) {
            Set<String> diffField = baseMapping.diffTypeDefine(testMappings);
            if (diffField != null && diffField.size() > 0) {
                ret.put(TESTMAPPINGS, diffField);
            }
        }

        return ret;
    }

    // 检测每一个模版的mapping中的字段的定义是否一致
    public Map<String, Set<String>> checkMapping() {
        Map<String, Set<String>> ret = new HashMap<>();

        for(String template : templateConfigMap.keySet()) {
            Set<String> fields = templateConfigMap.get(template).getMappings().checkMapping();
            if(fields!=null && fields.size()>0) {
                ret.put(template, fields);
            }
        }

        return ret;
    }


    // 检测每一个模版的mapping中的字段的定义是否一致
    public Map<String, Set<String>> xiaoyi() {
        Map<String, Set<String>> ret = new HashMap<>();

        for(String template : templateConfigMap.keySet()) {
            Map<String, List<TypeDefine>> m = templateConfigMap.get(template).getTypes();
            Set<String> f = new HashSet<>();
            for(String name : m.keySet()) {
                if(name.startsWith("json")) {
                    Set<String> info = new HashSet<>();
                    for(TypeDefine typeDefine : m.get(name)) {
                        info.add(JSON.toJSONString(typeDefine));
                    }

                    f.add(name + "_" + JSON.toJSON(info));
                }
            }

            if(f.size()>0) {
                ret.put(template, f);
            }
        }

        return ret;
    }
}

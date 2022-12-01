package com.didi.arius.gateway.elasticsearch.client.response.setting.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.type.ESVersion;
import com.didi.arius.gateway.elasticsearch.client.response.setting.common.MappingConfig;
import com.didi.arius.gateway.elasticsearch.client.response.setting.common.TypeDefine;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

import java.util.*;

public class MultiTemplatesConfig {
    private final ILog LOGGER = LogFactory.getLog(MultiTemplatesConfig.class);

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

        for(Map.Entry<String, TemplateConfig> entry : templateConfigMap.entrySet()) {
            root.put(entry.getKey(), entry.getValue().toJson(version));
        }

        return root;
    }

    public Map<String, TemplateConfig> getTemplateConfigMap() {
        return templateConfigMap;
    }

    public TemplateConfig getSingleConfig() {
        for(Map.Entry<String, TemplateConfig> entry : templateConfigMap.entrySet()) {
            return entry.getValue();
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
            LOGGER.error("not have baesConfig, clusterName:{}", cluster);
            return new HashMap<>();
        }

        MappingConfig baseMapping = baseConfig.getMappings();

        Map<String, Set<String>> ret = new HashMap<>();
        for(Map.Entry<String,TemplateConfig> entry : templateConfigMap.entrySet()) {
            String template = entry.getKey();
            if(template.equalsIgnoreCase(BASE_STR)) {
                continue;
            }

            MappingConfig mappings = templateConfigMap.get(template).getMappings();

            Set<String> diffField = baseMapping.diffTypeDefine(mappings);
            if(diffField!=null && !diffField.isEmpty()) {
                ret.put(template, diffField);
            }
        }

        if(testMappings!=null) {
            Set<String> diffField = baseMapping.diffTypeDefine(testMappings);
            if (diffField != null && !diffField.isEmpty()) {
                ret.put(TESTMAPPINGS, diffField);
            }
        }

        return ret;
    }

    // 检测每一个模版的mapping中的字段的定义是否一致
    public Map<String, Set<String>> checkMapping() {
        Map<String, Set<String>> ret = new HashMap<>();

        for(Map.Entry<String, TemplateConfig> entry : templateConfigMap.entrySet()) {
            String template = entry.getKey();
            Set<String> fields = templateConfigMap.get(template).getMappings().checkMapping();
            if(fields!=null && !fields.isEmpty()) {
                ret.put(template, fields);
            }
        }

        return ret;
    }


    // 检测每一个模版的mapping中的字段的定义是否一致
    public Map<String, Set<String>> xiaoyi() {
        Map<String, Set<String>> ret = new HashMap<>();

        for(Map.Entry<String, TemplateConfig> entry : templateConfigMap.entrySet()) {
            String template = entry.getKey();
            Map<String, List<TypeDefine>> m = templateConfigMap.get(template).getTypes();
            Set<String> f = new HashSet<>();
            for(Map.Entry<String, List<TypeDefine>> entry1 : m.entrySet()) {
                String name = entry1.getKey();
                if(name.startsWith("json")) {
                    Set<String> info = new HashSet<>();
                    for(TypeDefine typeDefine : m.get(name)) {
                        info.add(JSON.toJSONString(typeDefine));
                    }

                    f.add(name + "_" + JSON.toJSON(info));
                }
            }

            if(!f.isEmpty()) {
                ret.put(template, f);
            }
        }

        return ret;
    }
}

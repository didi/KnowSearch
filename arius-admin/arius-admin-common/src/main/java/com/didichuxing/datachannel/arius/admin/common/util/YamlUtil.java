package com.didichuxing.datachannel.arius.admin.common.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 读取 .yml 配置文件
 *
 * @author cjm
 */
public class YamlUtil {

    private static final Map<String/* ymlName */, Map<String, String>/* properties */> YML_NAME_TO_PROPERTIES = new HashMap<>();

    /**
     * 根据文件名获取yml的文件内容
     *
     * @param ymlPath .yml 配置文件路径
     * @param keys     第一个参数对应第一个key，第二个参数对应第二个key 比如spring.name下的所有 就是两个参数、
     *                 getYmlByFileName(bootstrap_file,"spring", "name");
     * @return Map<String, String>
     */
    public static Map<String, String> getYmlByFileName(String ymlPath, String... keys) throws IOException {
        if (ymlPath == null) {
            return null;
        }
        if (YML_NAME_TO_PROPERTIES.containsKey(ymlPath)) {
            return YML_NAME_TO_PROPERTIES.get(ymlPath);
        }
        Map<String, String> properties = new HashMap<>();
        InputStream in = null;
        try {
            File file = ResourceUtils.getFile(ymlPath);
            in = new BufferedInputStream(new FileInputStream(file));
            Yaml props = new Yaml();
            Object obj = props.loadAs(in, Map.class);
            Map<String, Object> param = (Map<String, Object>) obj;

            for (Map.Entry<String, Object> entry : param.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                if (keys.length != 0 && !keys[0].equals(key)) {
                    continue;
                }
                if (val instanceof Map) {
                    forEachYaml(properties, key, (Map<String, Object>) val, 1, keys);
                } else {
                    properties.put(key, val != null ? val.toString() : null);
                }
            }
            YML_NAME_TO_PROPERTIES.put(ymlPath, properties);
            return properties;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 根据 key 获取值
     */
    public static String getValue(String ymlPath, String key) throws IOException {
        if (YML_NAME_TO_PROPERTIES.containsKey(ymlPath)) {
            return YML_NAME_TO_PROPERTIES.get(ymlPath).get(key);
        }
        Map<String, String> properties = getYmlByFileName(ymlPath);

        if (properties == null) {
            return null;
        }

        return properties.get(key);
    }

    /**
     * 遍历 yml 文件，获取 map 集合
     */
    private static void forEachYaml(Map<String, String> properties, String key_str, Map<String, Object> obj, int i,
                                    String... keys) {
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            if (keys.length > i && !keys[i].equals(key)) {
                continue;
            }
            String str_new = "";
            if (StringUtils.isNotEmpty(key_str)) {
                str_new = key_str + "." + key;
            } else {
                str_new = key;
            }
            if (val instanceof Map) {
                forEachYaml(properties, str_new, (Map<String, Object>) val, ++i, keys);
                i--;
            } else {
                properties.put(str_new, val != null ? val.toString() : null);
            }
        }
    }
}
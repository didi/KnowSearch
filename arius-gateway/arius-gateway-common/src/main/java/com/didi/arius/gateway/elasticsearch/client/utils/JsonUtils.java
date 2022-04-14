package com.didi.arius.gateway.elasticsearch.client.utils;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

public class JsonUtils {

    public static Map<String, String> flat(JSONObject obj) {
        Map<String, String> ret = new HashMap<>();

        if(obj==null) {
            return ret;
        }

        for(Map.Entry<String,Object> entry : obj.entrySet()) {
            Object o = entry.getValue();
            String key = entry.getKey();

            if (o instanceof JSONObject) {
                Map<String, String> m = flat((JSONObject) o);
                for (Map.Entry<String,String> entry1 : m.entrySet()) {
                    ret.put(key.replace(".", "#") + "." + entry1.getKey(), entry1.getValue());
                }
            } else {
                ret.put(key.replace(".", "#"), o.toString());
            }
        }

        return ret;
    }

    public static JSONObject reFlat(Map<String, String> m) {
        JSONObject ret = new JSONObject();
        for(Map.Entry<String,String> entry : m.entrySet()) {
            String key = entry.getKey();
            // 增加一个value
            String[] subKeys = key.split("\\.");
            for(int i=0; i<subKeys.length; i++) {
                subKeys[i] = subKeys[i].replace("#", ".");
            }

            JSONObject obj = ret;
            int i;
            for(i=0; i<subKeys.length-1; i++) {
                String subKey = subKeys[i];

                // 逐层增加
                if(!obj.containsKey(subKey)) {
                    obj.put(subKey, new JSONObject());
                }

                obj = obj.getJSONObject(subKey);
            }

            String value = m.get(key);
            if(value!=null && value.startsWith("[") && value.endsWith("]")) {
                // 看是否可以转化成jsonArray
                //这里应该进行try catch否则会导致解析json中的正则失败
                JSONArray array = null;
                try {
                    array = JSONArray.parseArray(value);
                } catch (JSONException e) {
                    //pass
                }
                obj.put(subKeys[i], array);
                if(array==null) {
                    obj.put(subKeys[i], value);
                }

            } else {
                obj.put(subKeys[i], value);
            }
        }

        return ret;
    }


    /**
     * 将一维的kv对转化为嵌套的kv对
     * 例如:
     *  a.b.c:"test"
     *
     *  转化为:
     *
     *    {
     *      "a": {
     *          "b": {
     *              "c": "test"
     *              }
     *          }
     *    }
     *
     * @param map
     * @return
     */
    public static Map<String, Object> formatMap(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String,Object> entry : map.entrySet()) {
            String longKey = entry.getKey();
            if (longKey.contains(".")) {
                String firstKey = StringUtils.substringBefore(longKey, ".");
                Map<String, Object> innerMap;
                if (result.containsKey(firstKey)) {
                    innerMap = (Map<String, Object>) result.get(firstKey);
                } else {
                    innerMap = new HashMap<>();
                }
                String lastKey = StringUtils.substringAfter(longKey, ".");
                innerMap.put(lastKey, map.get(longKey));
                result.put(firstKey, formatMap(innerMap));
            } else {
                result.put(longKey, map.get(longKey));
            }
        }
        return result;
    }

}

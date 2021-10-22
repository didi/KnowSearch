package com.didi.arius.gateway.elasticsearch.client.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils {

    public static Map<String, String> flat(JSONObject obj) {
        Map<String, String> ret = new HashMap<>();

        if(obj==null) {
            return ret;
        }

        for(String key : obj.keySet()) {
            Object o = obj.get(key);

            if (o instanceof JSONObject) {
                Map<String, String> m = flat((JSONObject) o);
                for (String k : m.keySet()) {
                    ret.put(key.replaceAll("\\.", "#") + "." + k, m.get(k));
                }
            } else {
                ret.put(key.replaceAll("\\.", "#"), o.toString());
            }
        }

        return ret;
    }

    public static JSONObject reFlat(Map<String, String> m) {
        JSONObject ret = new JSONObject();
        for(String key : m.keySet()) {
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
                // 看是否可以转化成jsonarray

                JSONArray array = JSONArray.parseArray(value);
                if(array==null) {
                    obj.put(subKeys[i], value);
                } else {
                    obj.put(subKeys[i], array);
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
        for (String longKey : map.keySet()) {
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



    public static void main(String[] args) {
       String str = "{\n" +
               "  \"persistent\": {\n" +
               "    \"cluster\": {\n" +
               "      \"routing\": {\n" +
               "        \"rebalance\": {\n" +
               "          \"enable\": \"all\"\n" +
               "        },\n" +
               "        \"allocation\": {\n" +
               "          \"cluster_concurrent_rebalance\": \"8\",\n" +
               "          \"node_concurrent_recoveries\": \"10\",\n" +
               "          \"disk\": {\n" +
               "            \"include_relocations\": \"false\",\n" +
               "            \"threshold_enabled\": \"true\",\n" +
               "            \"watermark\": {\n" +
               "              \"low\": \"90%\",\n" +
               "              \"high\": \"92%\"\n" +
               "            }\n" +
               "          },\n" +
               "          \"exclude\": {\n" +
               "            \"_ip\": \"10.89.150.62,10.89.150.53,10.89.150.40,10.89.150.63,10.89.150.43,10.89.150.44,10.89.150.46,10.89.150.60,10.89.150.61,10.89.150.47,10.89.150.48,10.89.150.49,10.89.83.34,10.89.150.55\"\n" +
               "          },\n" +
               "          \"balance\": {\n" +
               "            \"index\": \"0.95f\",\n" +
               "            \"shard\": \"0.05f\"\n" +
               "          },\n" +
               "          \"enable\": \"all\"\n" +
               "        }\n" +
               "      },\n" +
               "      \"service\": {\n" +
               "        \"slow_task_logging_threshold\": \"1s\"\n" +
               "      }\n" +
               "    },\n" +
               "    \"indices\": {\n" +
               "      \"recovery\": {\n" +
               "        \"concurrent_streams\": \"3\",\n" +
               "        \"max_size_per_sec\": \"200MB\",\n" +
               "        \"concurrent_small_file_streams\": \"100\",\n" +
               "        \"max_bytes_per_sec\": \"200MB\"\n" +
               "      },\n" +
               "      \"store\": {\n" +
               "        \"throttle\": {\n" +
               "          \"max_bytes_per_sec\": \"100mb\"\n" +
               "        }\n" +
               "      },\n" +
               "      \"breaker\": {\n" +
               "        \"fielddata\": {\n" +
               "          \"limit\": \"10%\",\n" +
               "          \"overhead\": \"1.1\"\n" +
               "        },\n" +
               "        \"request\": {\n" +
               "          \"limit\": \"15%\",\n" +
               "          \"overhead\": \"1.1\"\n" +
               "        },\n" +
               "        \"total\": {\n" +
               "          \"limit\": \"20%\"\n" +
               "        }\n" +
               "      },\n" +
               "      \"ttl\": {\n" +
               "        \"interval\": \"1m\"\n" +
               "      }\n" +
               "    },\n" +
               "    \"search\": {\n" +
               "      \"default_search_timeout\": \"2s\"\n" +
               "    },\n" +
               "    \"discovery\": {\n" +
               "      \"zen\": {\n" +
               "        \"publish_timeout\": \"45s\"\n" +
               "      }\n" +
               "    },\n" +
               "    \"logger\": {\n" +
               "      \"action\": \"INFO\",\n" +
               "      \"index\": {\n" +
               "        \"indexing\": {\n" +
               "          \"slowlog\": \"WARN\"\n" +
               "        },\n" +
               "        \"search\": {\n" +
               "          \"slowlog\": \"DEBUG\"\n" +
               "        },\n" +
               "        \"fetch\": {\n" +
               "          \"slowlog\": \"DEBUG\"\n" +
               "        }\n" +
               "      },\n" +
               "      \"transport\": {\n" +
               "        \"tracer\": \"INFO\"\n" +
               "      },\n" +
               "      \"action.search\": \"INFO\"\n" +
               "    },\n" +
               "    \"transport\": {\n" +
               "      \"tracer\": {\n" +
               "        \"include\": \"\",\n" +
               "        \"exclude\": \"\",\n" +
               "        \"exclude.0\": \"\",\n" +
               "        \"include.0\": \"indices:data/read/search\",\n" +
               "        \"exclude.1\": \"\",\n" +
               "        \"exclude.2\": \"\",\n" +
               "        \"exclude.3\": \"\"\n" +
               "      }\n" +
               "    },\n" +
               "    \"threadpool\": {\n" +
               "      \"bulk\": {\n" +
               "        \"queue_size\": \"1000\",\n" +
               "        \"size\": \"16\"\n" +
               "      },\n" +
               "      \"search\": {\n" +
               "        \"queue_size\": \"5000\",\n" +
               "        \"size\": \"16\"\n" +
               "      }\n" +
               "    }\n" +
               "  },\n" +
               "  \"transient\": {\n" +
               "    \"cluster\": {\n" +
               "      \"routing\": {\n" +
               "        \"rebalance\": {\n" +
               "          \"enable\": \"all\"\n" +
               "        },\n" +
               "        \"allocation\": {\n" +
               "          \"disk\": {\n" +
               "            \"watermark\": {\n" +
               "              \"low\": \"89%\",\n" +
               "              \"high\": \"89%\"\n" +
               "            }\n" +
               "          }\n" +
               "        }\n" +
               "      }\n" +
               "    }\n" +
               "  }\n" +
               "}" ;

       JSONObject oldObj = JSON.parseObject(str);
       Map m = JsonUtils.flat(oldObj);
       System.out.println(m);

       JSONObject newObj = JsonUtils.reFlat(m);

        System.out.println(oldObj.toJSONString());
        System.out.println(newObj.toJSONString());

        if(oldObj.equals(newObj)) {
            System.out.println("OK");
        } else {
            System.out.println("not queal");
        }
    }
}

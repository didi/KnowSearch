package com.didichuxing.datachannel.arius.admin.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CompareUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareUtil.class);

    private CompareUtil() {}

    /**
     * src为用户传入的参数，dst为系统返回，src和dst类型不需要相同，dst中可能包含src中没有的属性
     * 如果设置ignore对象的子对象属性，可以用'.'分隔，ignore属性只针对src
     *
     * @param src
     * @param dst
     * @param ignoreFields
     * @return
     */
    public static boolean objectEquals(Object src, Object dst, String... ignoreFields) throws AdminOperateException {
        if (src == null) {
            LOGGER.warn("class=CompareUtils||method=objectEquals||msg=src and dst is null");
            return dst == null;
        } else if (dst == null) {
            return false;
        }
        Class<?> c = src.getClass();
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName();
            if ("id".equals(name)) {
                continue;
            }
            if (search(ignoreFields, name) >= 0) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object srcValue = field.get(src);
                Object dstValue = field.get(dst);
                if (srcValue == null) {
                    return dstValue == null;
                }
                if (srcValue.getClass() != dstValue.getClass()) {
                    LOGGER.error("class=CompareUtils||method=objectEquals||msg=type not match, field name: {}, src type: {}, dst type: {}", name, srcValue.getClass(), dstValue.getClass());
                    return false;
                }
                if (isJsonTypeOrObject(srcValue)) {
                    if (!srcValue.equals(dstValue)) {
                        LOGGER.error("class=CompareUtils||method=objectEquals||errMsg=field not equals, name: {}, src: {}, dst: {}", name, srcValue, dstValue);
                        return false;
                    }
                } else {
                    String[] nextFields = multiSlice(ignoreFields, '.');
                    if (List.class.isAssignableFrom(srcValue.getClass())) {
                        if (!listEqualsIgnoreOrder((List) srcValue, (List) dstValue, nextFields)) {
                            return false;
                        }
                    } else {
                        if (!objectEquals(srcValue, dstValue, nextFields)) {
                            return false;
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("", e);
                throw new AdminOperateException("", ResultType.FAIL);
            }
        }
        return true;
    }

    public static String[] multiSlice(String[] s, char c) {
        List<String> result = new ArrayList<>();
        for (String s1 : s) {
            int index = s1.indexOf(c);
            if (index >= 0) {
                result.add(s1.substring(index + 1));
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * dst为系统返回列表，不可少于src
     *
     * @param src
     * @param dst
     * @param ignoreFields
     * @return
     */
    public static boolean listEqualsIgnoreOrder(List<?> src, List<?> dst, String... ignoreFields) throws AdminOperateException {
        if (src == null) {
            LOGGER.warn("class=CompareUtils||method=listEqualsIgnoreOrder||msg=src and dst is null");
            return dst == null;
        } else if (dst == null) {
            return false;
        }
        if (src.size() != dst.size()) {
            LOGGER.error("class=CompareUtils||method=listEqualsIgnoreOrder||errMsg=size not equal, src: {}, dst: {}", src.size(), dst.size());
            return false;
        }
        for (int i = 0; i < src.size(); i++) {
            boolean found = false;
            for (int j = 0; j < dst.size(); j++) {
                if (objectEquals(src.get(i), dst.get(j), ignoreFields)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                LOGGER.error("class=CompareUtils||method=listEqualsIgnoreOrder||errMsg=no equal element found in dst, index: {}, element: {}", i, JSON.toJSONString(src.get(i)));
                return false;
            }
        }
        return true;
    }

    public static int search(String[] array, String s) {
        if (s == null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < array.length; i++) {
                if (s.equals(array[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 判断json对象属性的类型，包括bool, number, string, date
     *
     * @return true属性为基础类型 false属性为其他对象
     */
    public static boolean isJsonTypeOrObject(Object o) {
        Class<?> c = o.getClass();
        return String.class.isAssignableFrom(c) || Number.class.isAssignableFrom(c) || Boolean.class == c || Date.class == c;
    }


    /**
     * 把类反序列化为json
     */
    public static String serialize(Object object){
        // JSON对象序列化
        String objectJson = JSON.toJSONString(object, SerializerFeature.WRITE_MAP_NULL_FEATURES);
        return objectJson;
    }

    public static boolean compareJson(String templateJsonFile,String responseJsonString) throws IOException {
        //根据文件路径读取模版json
        File file = ResourceUtils.getFile(templateJsonFile);
        if (file == null){
            LOGGER.error("class=CompareUtils||method=compareJson||errMsg=templateJsonFile is not found");
            return false;
        }
        String templateJsonString = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        //将返回体与json转换为Map形式方便比较对比
        JSON templateJson = JSON.parseObject(templateJsonString);
        JSON responseJson = JSON.parseObject(responseJsonString);
        Map<String, Object> templateMap = new LinkedHashMap<>();
        Map<String, Object> responseMap = new LinkedHashMap<>();
        convertJsonToMap(templateJson, "", templateMap);
        convertJsonToMap(responseJson, "", responseMap);
        //调用CompareUtil中用于对比的compareMap函数
        return compareMap(responseMap,templateMap);
    }

    public static void convertJsonToMap(Object json, String root, Map<String, Object> resultMap) {
        if (json instanceof JSONObject) {
            JSONObject jsonObject = ((JSONObject) json);
            Iterator iterator = jsonObject.keySet().iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Object value = jsonObject.get(key);
                String newRoot = "".equals(root) ? key + "" : root + "." + key;
                if (value instanceof JSONObject || value instanceof JSONArray) {
                    convertJsonToMap(value, newRoot, resultMap);
                } else {
                    resultMap.put(newRoot, value);
                }
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (int i = 0; i < jsonArray.size(); i++) {
                Object vaule = jsonArray.get(i);
                String newRoot = "".equals(root) ? "[" + i + "]" : root + ".[" + i + "]";
                if (vaule instanceof JSONObject || vaule instanceof JSONArray) {
                    convertJsonToMap(vaule, newRoot, resultMap);
                } else {
                    resultMap.put(newRoot, vaule);
                }
            }
        }
    }

    public static boolean compareMap(Map<String, Object> responseMap,Map<String, Object> templateMap) {
        for(String templateKey : templateMap.keySet()){
            boolean flag = false;
            for (String responseKey : responseMap.keySet()){
                if (isMatch(responseKey,templateKey)){
                    if (responseMap.get(responseKey) == null && templateMap.get(templateKey) != null){
                        return false;
                    }
                    else if (responseMap.get(responseKey) == "" && templateMap.get(templateKey) != ""){
                        return false;
                    }
                    else{
                        flag = true;
                    }
                }
            }
            if (flag == false){
                return false;
            }
        }
        return true;
    }

    public static boolean isMatch(String s,String p) {
        boolean[][] dp = new boolean[s.length()+1][p.length()+1];
        dp[0][0]=true;
        for(int j = 0;j<p.length();j++) {
            if(dp[0][j] && p.charAt(j) == '*') {
                dp[0][j+1] = true;
            }
        }
        for(int i = 0;i < s.length();i++) {
            for(int j = 0;j < p.length();j++) {
                if(p.charAt(j) == '*') {
                    dp[i+1][j+1] = dp[i][j+1]|| dp[i+1][j];
                }else if(p.charAt(j) == '.' || p.charAt(j) == s.charAt(i)) {
                    dp[i+1][j+1] = dp[i][j];
                }
            }
        }
        return dp[s.length()][p.length()];
    }

}

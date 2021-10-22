package com.didi.arius.gateway.elasticsearch.client.response.setting.common;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.type.ESVersion;
import org.apache.commons.lang3.StringUtils;

/**
 * TypeDefine操作类
 *
 * 主要是完成高低版本兼容
 *
 * Created by d06679 on 2019/3/6.
 */
public class TypeDefineOperator {

    private static final String TYPE_STR                       = "type";

    private static final String NESTED_STR                     = "nested";

    private static final String OBJECT_STR                     = "object";

    private static final String FIELDS_STR                     = "fields";

    private static final String RAW_STR                        = "raw";

    private static final String INDEX_STR                      = "index";

    private static final String DOC_VALUES_STR                 = "doc_values";

    private static final String IGNORE_ABOVE_STR               = "ignore_above";

    private static final String ES_HIGH_TYPE_TEXT_STR          = "text";

    private static final String ES_HIGH_TYPE_KEYWORD_STR       = "keyword";

    private static final String ES_LOW_TYPE_STRING_STR         = "string";

    private static final String ES_LOW_STRING_NOT_ANALYZED_STR = "not_analyzed";

    private static final String ES_LOW_STRING_FIELDDATA_STR    = "fielddata";

    /**
     * 是否需要忽略mapping优化
     * @return
     */
    public static boolean isNotOptimze(JSONObject define) {
        if (define.containsKey(TYPE_STR)) {
            String v = define.getString(TYPE_STR);
            if (NESTED_STR.equalsIgnoreCase(v) || OBJECT_STR.equalsIgnoreCase(v)) {
                return true;
            }
        } else if (define.containsKey(FIELDS_STR)) {
            //                    \"artifact\": {\n"
            //                              \"type\": \"string\",\n"
            //                              \"fields\": {\n"
            //                                \"raw\": {\n"
            //                                  \"ignore_above\": 1024,\n"
            //                                  \"index\": \"not_analyzed\",\n"
            //                                   \"type\": \"string\"\n"
            //                                }\n"
            //                              }\n"
            if (define.get(FIELDS_STR) instanceof JSONObject) {
                JSONObject fieldsObj = define.getJSONObject(FIELDS_STR);
                if (fieldsObj.containsKey(RAW_STR)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 获取类型
     *
     * @return
     */
    public static String getType(JSONObject define) {
        if (define != null && define.containsKey(TYPE_STR)) {
            return define.getString(TYPE_STR);
        }

        return null;
    }

    public static boolean isHighVersionString(JSONObject define) {
        String type = getType(define);
        return !StringUtils.isBlank(type)
               && (type.equals(ES_HIGH_TYPE_TEXT_STR) || type.equals(ES_HIGH_TYPE_KEYWORD_STR));
    }

    public static boolean isLowVersionString(JSONObject define) {
        String type = getType(define);
        return !StringUtils.isBlank(type) && type.equals(ES_LOW_TYPE_STRING_STR);
    }

    public static boolean isIndexOff(JSONObject define) {

        if (isHighVersionString(define)) {
            //高版本的es
            //            if ("false".equalsIgnoreCase(define.getString(INDEX_STR))
            //                    && "true".equalsIgnoreCase(define.getString(DOC_VALUES_STR))) {
            //                return true;
            //            }
            throw new RuntimeException("illegal operator");
        } else {
            if ("no".equalsIgnoreCase(define.getString(INDEX_STR))
                && "true".equalsIgnoreCase(define.getString(DOC_VALUES_STR))) {
                return true;
            }
        }

        return false;
    }

    /**
     * 设置成不检索
     *
     */
    public static void setIndexOff(JSONObject define) {
        if (isHighVersionString(define)) {
            define.put(INDEX_STR, "false");
        } else {
            define.put(INDEX_STR, "no");
        }
    }

    /**
     * 设置成检索
     *
     */
    public static void setIndexOn(JSONObject define) {
        if (isHighVersionString(define)) {
            define.remove(INDEX_STR);
            define.put(TYPE_STR, ES_HIGH_TYPE_KEYWORD_STR);
        } else {
            define.put(TYPE_STR, ES_LOW_STRING_NOT_ANALYZED_STR);
        }
    }

    public static boolean isDocValuesOff(JSONObject define) {
        if (isHighVersionString(define)) {
            if ("false".equalsIgnoreCase(define.getString(INDEX_STR))
                && !"true".equalsIgnoreCase(define.getString(DOC_VALUES_STR))) {
                return true;
            }
            throw new RuntimeException("illegal operator");
        } else {
            if ("no".equalsIgnoreCase(define.getString(INDEX_STR))
                && !"true".equalsIgnoreCase(define.getString(DOC_VALUES_STR))) {
                return true;
            }
        }

        return false;
    }

    /**
     * 设置成不支持排序
     */
    public static void setDocValuesOff(JSONObject define) {
        define.put(DOC_VALUES_STR, false);
    }

    /**
     * 设置成支持排序
     */
    public static void setDocValuesOn(JSONObject define) {
        define.put(DOC_VALUES_STR, true);
    }

    public static boolean isEquals(JSONObject define, Object obj) {
        if (!(obj instanceof TypeDefine)) {
            return false;
        }

        TypeDefine t = (TypeDefine) obj;

        JSONObject j1 = (JSONObject) define.clone();
        JSONObject j2 = (JSONObject) t.getDefine().clone();

        j1.remove(IGNORE_ABOVE_STR);
        j2.remove(IGNORE_ABOVE_STR);

        return j1.equals(j2);
    }

    public static JSONObject toJson(JSONObject define, ESVersion version) {

        if (version == ESVersion.ES651) {

            if (isLowVersionString(define)) {
                // 处理String
                if (define.containsKey(INDEX_STR)) {
                    if (ES_LOW_STRING_NOT_ANALYZED_STR.equals(define.get(INDEX_STR))) {
                        // keyword
                        define.remove(INDEX_STR);
                        define.put(TYPE_STR, ES_HIGH_TYPE_KEYWORD_STR);
                    } else if ("no".equals(define.get(INDEX_STR))) {
                        // keyword and not index
                        define.put(INDEX_STR, "false");
                        define.put(TYPE_STR, ES_HIGH_TYPE_KEYWORD_STR);
                    } else {
                        define.remove(INDEX_STR);
                        define.put(TYPE_STR, ES_HIGH_TYPE_TEXT_STR);
                    }
                } else {
                    define.put(TYPE_STR, ES_HIGH_TYPE_TEXT_STR);
                }
            } else {
                // 处理其他字段
                if (define.containsKey(INDEX_STR) && "no".equals(define.get(INDEX_STR))) {
                    define.put(INDEX_STR, "false");
                }
            }

            // 处理fielddata配置
            define.remove(ES_LOW_STRING_FIELDDATA_STR);

        }

        return define;
    }
}

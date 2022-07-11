package com.didichuxing.datachannel.arius.admin.common.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by linyunan on 2021-10-28
 */
public class DSLSearchUtils {

    private static final String QUERY_BOOL_MUST_TERM       = "\"term\":";
    private static final String QUERY_BOOL_MUST_TERMS      = "\"terms\":";
    private static final String QUERY_BOOL_MUST_TERM_VALUE = "\"value\":";
    private static final String QUERY_BOOL_MUST_PREFIX     = "\"prefix\":";
    private static final String QUERY_BOOL_MUST_RANGE      = "\"range\":";
    private static final String QUERY_BOOL_MUST_WILDCARD   = "\"wildcard\":";
    private static final String PLACE_HOLDER               = "\"%s\":";

    private static final String QUERY_BOOL_MUST_RANGE_GTE  = "\"gte\":";
    private static final String QUERY_BOOL_MUST_RANGE_LTE  = "\"lte\":";

    /**
     * 工具类不支持使用构造方法
     */
    private DSLSearchUtils() { throw new IllegalStateException("Utility class"); }

    /**
     * 构建范围查询子条件
     * @param gteValue 大于
     * @param lteValue 小于
     * @param termKey 字段
     * @return dsl
     */
    public static String getTermCellForRangeSearch(Object gteValue, Object lteValue, String termKey) {
        if((gteValue == null && lteValue == null) || AriusObjUtils.isBlack(termKey)) {
            return null;
        }

        StringBuilder rangeSb = new StringBuilder();
        rangeSb.append("{").append(QUERY_BOOL_MUST_RANGE).append("{").append(String.format(PLACE_HOLDER, termKey))
                .append("{");
        if(gteValue != null) {
            rangeSb.append(QUERY_BOOL_MUST_RANGE_GTE).append(gteValue);
        }
        if(lteValue != null) {
            if(gteValue != null) {
                rangeSb.append(",");
            }
            rangeSb.append(QUERY_BOOL_MUST_RANGE_LTE).append(lteValue);
        }
        rangeSb.append("}").append("}").append("}");
        return rangeSb.toString();
    }

    /**
     * 构建精确查询子条件
     *
     * 返回样例：
     * {
     *           "term": {
     *             "health": {
     *               "value": "green"
     *             }
     *           }
     *         }
     * @param term     值（String）
     * @param termKey  键
     * @return
     */
    public static String getTermCellForExactSearch(String term, String termKey) {
        if (AriusObjUtils.isBlack(term) || AriusObjUtils.isBlack(termKey)) {
            return null;
        }

        StringBuilder termSb = new StringBuilder();
        termSb.append("{").append(QUERY_BOOL_MUST_TERM).append("{").append(String.format(PLACE_HOLDER, termKey+".keyword"))
            .append("{").append(QUERY_BOOL_MUST_TERM_VALUE).append("\"").append(term).append("\"").append("}")
            .append("}").append("}");
        return termSb.toString();
    }

    /**
     * 构建精确查询子条件
     * 返回样例：
     *
     * {
     *   "term": {
     *       "projectId": {
     *            "value": 123
     *           }
     *     }
     *  }
     * @param term     值（Integer）
     * @param termKey  键
     * @return
     */
    public static String getTermCellForExactSearch(Integer term, String termKey) {
        if (term == null || AriusObjUtils.isBlack(termKey)) {
            return null;
        }

        StringBuilder termSb = new StringBuilder();
        termSb.append("{").append(QUERY_BOOL_MUST_TERM).append("{").append(String.format(PLACE_HOLDER, termKey))
                .append("{").append(QUERY_BOOL_MUST_TERM_VALUE).append("\"").append(term).append("\"").append("}")
                .append("}").append("}");
        return termSb.toString();
    }

    /**
     * 构建精确查询子条件
     * 返回样例：
     * {
     *   "term": {
     *       "projectId": {
     *            "value": true
     *           }
     *     }
     *  }
     * @param term       值（Boolean）
     * @param termKey    键
     * @return
     */
    public static String getTermCellForExactSearch(Boolean term, String termKey) {
        if (term == null || AriusObjUtils.isBlack(termKey)) {
            return null;
        }

        StringBuilder termSb = new StringBuilder();
        termSb.append("{").append(QUERY_BOOL_MUST_TERM).append("{").append(String.format(PLACE_HOLDER, termKey))
                .append("{").append(QUERY_BOOL_MUST_TERM_VALUE).append(term).append("}")
                .append("}").append("}");

        return termSb.toString();
    }

    /**
     * 批量构建精确查询子条件
     * 返回样例：
     * {
     *   "terms": {
     *     "cluster": [
     *       "logi-elasticsearch-7.6.0",
     *       "dc-cluster",
     *       "dc-es02"
     *     ]
     *   }
     * }
     * @param termList  值
     * @param termsKey  键
     * @return
     */
    public static String getTermCellsForExactSearch(List<String> termList, String termsKey) {
        if (CollectionUtils.isEmpty(termList) || AriusObjUtils.isBlack(termsKey)) {
            return null;
        }

        StringBuilder termSb = new StringBuilder();
        termSb.append("{").append(QUERY_BOOL_MUST_TERMS).append("{").append(String.format(PLACE_HOLDER, termsKey))
            .append("[");
        for (int i = 0; i < termList.size(); i++) {
            termSb.append("\"").append(termList.get(i)).append("\"");
            if (i != termList.size() - 1) {
                termSb.append(",");
            }
        }
        termSb.append("]").append("}").append("}");

        return termSb.toString();
    }

    /**
     * 构建最右匹配查询子条件
     * 返回样例：
     *  {
     *    "wildcard": {
     *      "index": {
     *        "value": "test*"
     *      }
     *    }
     *  }
     *
     * @param term    值
     * @param termKey 键
     * @return
     */
    public static String getTermCellForPrefixSearch(String term, String termKey) {
        if (AriusObjUtils.isBlack(term) || AriusObjUtils.isBlack(termKey)) {
            return null;
        }

        StringBuilder termSb = new StringBuilder();
        if (StringUtils.isNotBlank(term)) {
            termSb.append("{").append(QUERY_BOOL_MUST_PREFIX).append("{").append(String.format(PLACE_HOLDER, termKey))
                .append("{").append(QUERY_BOOL_MUST_TERM_VALUE).append("\"").append(term).append("\"")
                .append("}").append("}").append("}");
        }

        return termSb.toString();
    }

    /**
     * 构建term的模糊查询条件
     * {
     *   "wildcard": {
     *     "index": {
     *       "value": "*my_index*"
     *     }
     *   }
     * }
     * @param term 值
     * @param termKey 键
     * @return
     */
    public static String getTermCellForWildcardSearch(String term, String termKey) {
        if (AriusObjUtils.isBlack(term) || AriusObjUtils.isBlack(termKey)) {
            return null;
        }
        StringBuilder termSb = new StringBuilder();
        if (StringUtils.isNotBlank(term)) {
            termSb.append("{").append(QUERY_BOOL_MUST_WILDCARD).append("{").append(String.format(PLACE_HOLDER, termKey))
                    .append("{").append(QUERY_BOOL_MUST_TERM_VALUE).append("\"").append("*").append(term).append("*")
                    .append("\"").append("}").append("}").append("}");
        }
        return termSb.toString();
    }

    /**
     *
     * @param aggType  聚合算子类型：avg, count, sum, min, max 等
     * @param field    字段名称
     * @return         返回的聚合项
     * eg:
     * {
     *     "avg":{
     *         "field":"totalCost"
     *     }
     * }
     */
    public static JSONObject buildAggItem(String aggType, String field) {
        JSONObject aggTypeJson = new JSONObject();
        JSONObject fieldJson = new JSONObject();
        fieldJson.put("field", field);
        aggTypeJson.put(aggType, fieldJson);
        return aggTypeJson;
    }
}
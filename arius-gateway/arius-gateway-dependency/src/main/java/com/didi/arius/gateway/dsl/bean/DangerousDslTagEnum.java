package com.didi.arius.gateway.dsl.bean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2019/1/5 下午9:25
 * @Modified By
 *
 * 危害DSL标签
 *
 * aggs 大基数查询                                           只从查询语句上看无法知道是大基数字段，并且多少数据量级为大基数
 * aggs 嵌套层数过深                                       这个目前大于3层就是嵌套过深
 * aggs中带了significant_terms
 * aggs中带了cardinality，且不在第一层嵌套
 * DSL中带了script
 * query中带了Wildcard，且前缀*号
 * query中带了Regexp
 * 查询语句超过5k
 *
 */
public enum DangerousDslTagEnum {

    // aggs 嵌套层数过深
    AGGS_DEEP_NEST("aggs deep nest", "aggs嵌套层数过深"),
    // aggs中带了significant_terms
    AGGS_SIGNIFICANT_TERMS("aggs significant_terms", "aggs中带了significant_terms"),
    // aggs中带了cardinality，且不在第一层嵌套
    AGGS_CARDINALITY("aggs cardinality", "aggs中带了cardinality"),
    // query中带了script
    WITH_SCRIPT("script", "query中带了script"),
    // query中带了Wildcard，且前缀*号
    WITH_WILDCARD_PRE("wildcard pre*", "query中带了wildcard，且前缀*号"),
    // query中带了Regexp
    WITH_REGEXP("regexp", "query中带了regexp"),
    // 查询语句超过5k
    DSL_LENGTH_TOO_LARGE("dsl length more 5k", "查询语句超过5k");

    private final String tag;

    private final String desc;

    public String getTag() {
        return tag;
    }

    public String getDesc() {
        return desc;
    }

    DangerousDslTagEnum(String tag, String desc) {
        this.tag = tag;
        this.desc = desc;
    }

    private final static Map<String, DangerousDslTagEnum> TAG_MAP;

    static {
        Map<String, DangerousDslTagEnum> tagMap = new HashMap<>();
        for (DangerousDslTagEnum threadPoolType : DangerousDslTagEnum.values()) {
            tagMap.put(threadPoolType.getTag(), threadPoolType);
        }
        TAG_MAP = Collections.unmodifiableMap(tagMap);
    }

    /**
     * 根据标签得到枚举对象
     *
     * @param tag
     * @return
     */
    public static DangerousDslTagEnum fromTag(String tag) {
        DangerousDslTagEnum dslTagEnum = TAG_MAP.get(tag);
        if (dslTagEnum == null) {
            throw new IllegalArgumentException("no dslTagEnum for " + tag);
        }
        return dslTagEnum;
    }

    /**
     * 获取TagMap
     *
     * @return
     */
    public static Map<String, DangerousDslTagEnum> getTagMap() {
        return TAG_MAP;
    }

}

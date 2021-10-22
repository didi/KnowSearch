package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/10/24 下午5:30
 * @Modified By
 *
 * 慢查原因枚举
 */
public enum SlowDslReasonType {

    /**
     * 用户查询引起
     */
    USER_DSL("user dsl"),
    /**
     * ES查询引起
     */
    ES("es");

    private final String type;

    public String getType() {
        return type;
    }

    SlowDslReasonType(String type) {
        this.type = type;
    }

    private final static Map<String, SlowDslReasonType> TYPE_MAP;

    static {
        Map<String, SlowDslReasonType> typeMap = new HashMap<>();
        for (SlowDslReasonType slowDslReasonType : SlowDslReasonType.values()) {
            typeMap.put(slowDslReasonType.getType(), slowDslReasonType);
        }
        TYPE_MAP = Collections.unmodifiableMap(typeMap);
    }

    public static SlowDslReasonType fromType(String type) {
        SlowDslReasonType slowDslReasonType = TYPE_MAP.get(type);
        if (slowDslReasonType == null) {
            throw new IllegalArgumentException("no SlowDslReasonType for " + type);
        }
        return slowDslReasonType;
    }

    @Override
    public String toString() {
        return getType();
    }

}

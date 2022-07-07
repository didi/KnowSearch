package com.didichuxing.datachannel.arius.admin.common.mapping;

/**
 * 搜索类型枚举
 *
 * @author d06679
 * @date 2017/7/14
 */
public enum IndexEnum {
                       /**关闭分词，关闭倒排索引*/
                       FORBID(1, "forbid"),

                       /**关闭分词，开启倒排索引*/
                       EXACT(2, "exact"),

                       /**开启分词，开启倒排索引*/
                       FUZZY(3, "fuzzy"),

                       UNKNOWN(-1, "unknown");

    IndexEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;

    private final String  desc;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static IndexEnum valueFrom(Integer code) {
        if (code == null) {
            return IndexEnum.UNKNOWN;
        }
        for (IndexEnum state : IndexEnum.values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }

        return IndexEnum.UNKNOWN;
    }

}

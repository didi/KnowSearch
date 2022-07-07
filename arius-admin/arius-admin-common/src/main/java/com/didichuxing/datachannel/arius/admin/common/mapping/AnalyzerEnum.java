package com.didichuxing.datachannel.arius.admin.common.mapping;

/**
 * 搜索类型枚举
 *
 * @author d06679
 * @date 2017/7/14
 */
public enum AnalyzerEnum {
                          /**标准分词器*/
                          DEFAULT(1, "标准分词器"),

                          IK(2, "ik分词器"),

                          UNKNOWN(-1, "unknown");

    AnalyzerEnum(Integer code, String desc) {
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

    public static AnalyzerEnum valueFrom(Integer code) {
        if (code == null) {
            return AnalyzerEnum.UNKNOWN;
        }
        for (AnalyzerEnum state : AnalyzerEnum.values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }

        return AnalyzerEnum.UNKNOWN;
    }

}

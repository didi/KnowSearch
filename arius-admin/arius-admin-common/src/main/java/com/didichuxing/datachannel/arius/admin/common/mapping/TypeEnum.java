package com.didichuxing.datachannel.arius.admin.common.mapping;

/**
 * 字段类型枚举
 *
 * @author d06679
 * @date 2017/7/14
 */
public enum TypeEnum {
                      // 数字类型
                      INT("integer",
                          "integer"), LONG("long",
                                           "long"), DOUBLE("double",
                                                           "double"), SHORT("short",
                                                                            "short"), BYTE("byte",
                                                                                           "byte"), FLOAT("float",
                                                                                                          "float"), HALF_FLOAT("half_float",
                                                                                                                               "half_float"), SCALED_FLOAT("scaled_float",
                                                                                                                                                           "scaled_float"), NUMERIC("numeric",
                                                                                                                                                                                    "numeric"),

                      // 布尔类型
                      BOOLEAN("boolean", "boolean"),

                      KEYWORD("keyword", "keyword"),

                      STRING("string", "string"),

                      // 日期类型
                      DATE("date", "date"), DATE_NANOS("date_nanos", "date_nanos"),

                      ARRAY("array", "array"),

                      // Object类型
                      OBJECT("object", "object"), FLATTENED("flattened", "flattened"), NESTED("nested", "nested"),

                      BINARY("binary", "binary"),

                      // 空间数据类型
                      GEO_POINT("geo-point", "geo-point"), GEO_SHAPE("geo-shape", "geo-shape"), SHARPE("sharpe",
                                                                                                       "sharpe"),

                      // 结构化数据类型
                      IP("ip", "ip"), RANGE("range", "range"),

                      // Text检索类型
                      TEXT("text", "text"),

                      UNKNOWN("unknown", "unknown");

    TypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private String code;

    private String desc;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TypeEnum valueFrom(String code) {
        if (code == null) {
            return TypeEnum.UNKNOWN;
        }
        for (TypeEnum state : TypeEnum.values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }

        return TypeEnum.UNKNOWN;
    }

}

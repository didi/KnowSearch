package com.didichuxing.datachannel.arius.admin.client.constant.app;

/**
 * 用户状态枚举
 *
 *
 * @author d06679
 * @date 2017/7/14
 */
public enum AppSearchTypeEnum {
                               /**集群模式*/
                               CLUSTER(0, "集群模式"),

                               TEMPLATE(1, "索引模式"),

                               PRIMITIVE(2, "原生模式"),

                               UNKNOWN(-1, "未知");

    AppSearchTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int    code;

    private String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static AppSearchTypeEnum valueOf(Integer code) {
        if (code == null) {
            return AppSearchTypeEnum.UNKNOWN;
        }
        for (AppSearchTypeEnum state : AppSearchTypeEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return AppSearchTypeEnum.UNKNOWN;
    }

}

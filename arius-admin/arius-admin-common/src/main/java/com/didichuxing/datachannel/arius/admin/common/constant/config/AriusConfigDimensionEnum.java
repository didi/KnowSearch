package com.didichuxing.datachannel.arius.admin.common.constant.config;

/**
 * 操作记录模块枚举
 *
 * Created by d06679 on 2017/7/14.
 */
public enum AriusConfigDimensionEnum {
                                      /**集群*/
                                      CLUSTER(1, "集群"),

                                      TEMPLATE(2, "模板"),

                                      RESOURCE(3, "资源"),

                                      UNKNOWN(-1, "未知");

    AriusConfigDimensionEnum(int code, String desc) {
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

    public static AriusConfigDimensionEnum valueOf(Integer code) {
        if (code == null) {
            return AriusConfigDimensionEnum.UNKNOWN;
        }
        for (AriusConfigDimensionEnum state : AriusConfigDimensionEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return AriusConfigDimensionEnum.UNKNOWN;
    }

}

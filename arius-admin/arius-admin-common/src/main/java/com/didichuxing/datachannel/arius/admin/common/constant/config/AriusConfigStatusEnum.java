package com.didichuxing.datachannel.arius.admin.common.constant.config;

/**
 * 操作记录模块枚举
 *
 * Created by d06679 on 2017/7/14.
 */
public enum AriusConfigStatusEnum {
                                   /**正常*/
                                   NORMAL(1, "正常"),

                                   DISABLE(2, "禁用"),

                                   DELETED(-1, "删除");

    AriusConfigStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final int    code;

    private final String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static AriusConfigStatusEnum valueOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (AriusConfigStatusEnum state : AriusConfigStatusEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return null;
    }

}

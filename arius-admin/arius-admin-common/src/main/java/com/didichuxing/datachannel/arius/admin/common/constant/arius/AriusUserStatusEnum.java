package com.didichuxing.datachannel.arius.admin.common.constant.arius;

/**
 * 用户状态枚举
 *
 * Created by d06679 on 2017/7/14.
 */
@Deprecated
public enum AriusUserStatusEnum {

                                 NORMAL(1, "normal"),

                                 DISABLE(0, "disable");

    AriusUserStatusEnum(int code, String desc) {
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

}
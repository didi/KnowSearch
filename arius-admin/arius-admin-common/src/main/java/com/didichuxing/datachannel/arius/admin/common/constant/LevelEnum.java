package com.didichuxing.datachannel.arius.admin.common.constant;

public enum LevelEnum {
    /**
     * 服务等级未知
     */
    UNKNOWN(-1, "未知等级类型"),
    /**
     * 服务等级为1，核心
     */
    CORE(1, "核心"),
    /**
     * 服务等级为2，表示为重要
     */
    IMPORTANT(2, "重要"),
    /**
     * 服务等级为3，表示为一般
     */
    ORDINARY(3, "一般");

    LevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private Integer code;
    private String desc;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static LevelEnum valueOfCode(Integer code) {
        if (code == null) {
            return LevelEnum.UNKNOWN;
        }

        for (LevelEnum param : LevelEnum.values()) {
            if (code.equals(param.getCode())) {
                return param;
            }
        }

        return LevelEnum.UNKNOWN;
    }
}

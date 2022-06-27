package com.didichuxing.datachannel.arius.admin.common.constant.project;

/**
 * 用户状态枚举
 *
 *
 * @author d06679
 * @date 2017/7/14
 */
public enum ProjectSearchTypeEnum {
    
    /**
     * 索引模式
     */
    TEMPLATE(1, "索引模式"),
    
    /**
     * 原生模式
     */
    PRIMITIVE(2, "原生模式"),

                               UNKNOWN(-1, "未知");

    ProjectSearchTypeEnum(int code, String desc) {
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

    public static ProjectSearchTypeEnum valueOf(Integer code) {
        if (code == null) {
            return ProjectSearchTypeEnum.UNKNOWN;
        }
        for (ProjectSearchTypeEnum state : ProjectSearchTypeEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return ProjectSearchTypeEnum.UNKNOWN;
    }

}